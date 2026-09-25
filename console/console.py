#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
================================================================================
懒得找了 · 云端总控台 CLI 程序 (Console CLI)
================================================================================
功能支持：
1. 首页：增删分类、子分类、站点、角标，实时推送触发本体更新弹窗
2. 软件：本地上传 APK 文件到云端仓库，自动关联并推送更新弹窗
3. Skill：Skill技能库与 Prompt 提示词管理，本地上传图片/视频，推送更新弹窗
4. 设置：完整管理 App 名称、Slogan、关于文本、官方联系方式等
5. 开屏动画：自定义输入 HTML/CSS/JS 代码或图片/视频控制开屏，实时推送
6. 欢迎弹窗：添加新功能特性列表、配图，实时推送更新弹窗
7. 更新弹窗：编辑更新标题、日志、按钮、强制更新，自定义 CSS/HTML 样式代码
8. APK 仓库与制作：本地打包产物上传、自动检测仓库已有 APK、一键触发更新
================================================================================
"""

import sys
import os
import json
import base64
import time
import argparse
from urllib import request, error

# 安全修复：不再内置 Token（旧 Token 已泄露）。
# 请通过环境变量 GITHUB_TOKEN 传入你自己的 Token：
#   export GITHUB_TOKEN=你的token   （Windows: set GITHUB_TOKEN=你的token）
DEFAULT_TOKEN = os.environ.get("GITHUB_TOKEN", "")
DEFAULT_OWNER = "shuting52"
DEFAULT_REPO  = "2026-9-21landezhaole"
CONFIG_PATH   = "admin-data.json"
API_BASE      = "https://api.github.com"


class GitHubClient:
    # 只读镜像列表（国内网络下 api.github.com / cdn.jsdelivr.net 可能不可达或被 DNS 污染，自动顺延）
    # 仅用于「读取文件内容」兜底；写入/删除仍必须走官方 API。
    MIRRORS = [
        lambda o, r, p: f"https://testingcf.jsdelivr.net/gh/{o}/{r}@main/{p}",
        lambda o, r, p: f"https://cdn.jsdelivr.net/gh/{o}/{r}@main/{p}",
        lambda o, r, p: f"https://gcore.jsdelivr.net/gh/{o}/{r}@main/{p}",
        lambda o, r, p: f"https://ghfast.top/https://raw.githubusercontent.com/{o}/{r}/main/{p}",
    ]

    def __init__(self, token=DEFAULT_TOKEN, owner=DEFAULT_OWNER, repo=DEFAULT_REPO):
        self.token = token
        self.owner = owner
        self.repo = repo

    def _headers(self, is_json=True):
        h = {
            "Accept": "application/vnd.github+json",
            "User-Agent": "Lzdz-Admin-Console/2.0"
        }
        if self.token:
            h["Authorization"] = f"token {self.token}"
        if is_json:
            h["Content-Type"] = "application/json"
        return h

    def get_contents(self, path):
        url = f"{API_BASE}/repos/{self.owner}/{self.repo}/contents/{path}"
        req = request.Request(url, headers=self._headers(False), method="GET")
        try:
            with request.urlopen(req, timeout=15) as resp:
                return json.loads(resp.read().decode("utf-8"))
        except error.HTTPError as e:
            if e.code == 404:
                return None
            raise
        except Exception as e:
            # API 不可达（网络受限/被墙/限流）：尝试只读镜像，返回与 API 同构的假响应
            api_err = str(e)
            mirror_errors = []
            for maker in self.MIRRORS:
                try:
                    murl = maker(self.owner, self.repo, path)
                    with request.urlopen(murl, timeout=15) as resp:
                        text = resp.read().decode("utf-8")
                    if text.lstrip().startswith("<"):
                        mirror_errors.append(f"{murl}: 返回网页(疑似被拦截)")
                        continue
                    return {"sha": "", "content": base64.b64encode(text.encode("utf-8")).decode("utf-8")}
                except Exception as e2:
                    mirror_errors.append(f"{murl}: {e2}")
            raise RuntimeError(f"API 与全部镜像均失败。API: {api_err}; 镜像: {'; '.join(mirror_errors[:4])}")

    def put_file(self, path, content_bytes, commit_message, sha=None):
        url = f"{API_BASE}/repos/{self.owner}/{self.repo}/contents/{path}"
        b64_content = base64.b64encode(content_bytes).decode("utf-8")
        payload = {
            "message": commit_message,
            "content": b64_content
        }
        if sha:
            payload["sha"] = sha
        body = json.dumps(payload).encode("utf-8")
        req = request.Request(url, data=body, headers=self._headers(True), method="PUT")
        with request.urlopen(req, timeout=30) as resp:
            return json.loads(resp.read().decode("utf-8"))

    def raw_url(self, path):
        return f"https://raw.githubusercontent.com/{self.owner}/{self.repo}/main/{path}"

    def delete_file(self, path, sha):
        url = f"{API_BASE}/repos/{self.owner}/{self.repo}/contents/{path}"
        payload = {
            "message": f"console: 清理旧版本文件 {path.split('/')[-1]}",
            "sha": sha
        }
        body = json.dumps(payload).encode("utf-8")
        req = request.Request(url, data=body, headers=self._headers(True), method="DELETE")
        with request.urlopen(req, timeout=30) as resp:
            return json.loads(resp.read().decode("utf-8"))


class AdminConsole:
    def __init__(self, client):
        self.client = client
        self.data = None
        self.sha = None

    def load(self):
        print(f"\n[+] 正在连接 GitHub 仓库 {self.client.owner}/{self.client.repo}...")
        try:
            res = self.client.get_contents(CONFIG_PATH)
            self.sha = res["sha"]
            raw_text = base64.b64decode(res["content"]).decode("utf-8")
            self.data = json.loads(raw_text)
            cats = len(self.data.get("home", {}).get("categories", []))
            v_name = self.data.get("version", {}).get("name", "1.0")
            v_code = self.data.get("version", {}).get("code", 1)
            print(f"[✓] 成功连接！当前版本: v{v_name} (versionCode: {v_code}) · 包含 {cats} 个首页分类")
            return True
        except Exception as e:
            print(f"[!] 连接或加载失败: {e}")
            return False

    def publish(self, bump_version=False, action_desc=""):
        if not self.data:
            print("[!] 请先加载配置！")
            return False
        try:
            if bump_version:
                old_code = int(self.data.get("version", {}).get("code", 1))
                new_code = old_code + 1
                self.data.setdefault("version", {})["code"] = new_code
                old_name = self.data.get("version", {}).get("name", "1.0")
                parts = old_name.split(".")
                if len(parts) > 1:
                    parts[-1] = str(int(parts[-1]) + 1)
                    new_name = ".".join(parts)
                else:
                    new_name = old_name + ".1"
                self.data["version"]["name"] = new_name
                note = action_desc if action_desc else "功能与体验全面升级"
                if "changelog" not in self.data["version"]:
                    self.data["version"]["changelog"] = []

                def meaningful(line):
                    bad = ["【控制台推送】", "管理后台全局版本发布", "APK 制作入库", "静默同步", "更新设置版块", "控制台", "后台"]
                    if not line:
                        return False
                    if any(b in line for b in bad):
                        return False
                    if line.strip().startswith("【v") or line.strip().startswith("【V"):
                        return False
                    return True

                self.data["version"]["changelog"] = [note] + [x for x in self.data["version"].get("changelog", []) if meaningful(x)][:11]
                self.data.setdefault("updateDialog", {
                    "title": "发现新版本",
                    "changelog": [],
                    "confirmText": "立即更新",
                    "cancelText": "稍后再说"
                })
                self.data["updateDialog"]["changelog"] = [note] + [
                    x for x in self.data["updateDialog"].get("changelog", []) if meaningful(x)
                ][:9]
                commit_msg = f"console: [新版本 v{new_name} code:{new_code}] {action_desc or '更新发布'}"
                # 新版本发布后：APK 仓库只保留当前版本与最新版本两个安装包
                try:
                    self._cleanup_old_apks()
                except Exception as e:
                    print(f"[!] 清理旧版本 APK 失败（可稍后在 APK 仓库手动处理）: {e}")
            else:
                commit_msg = f"console: 应用并实时同步 {time.strftime('%Y-%m-%d %H:%M:%S')}"

            content_bytes = json.dumps(self.data, indent=2, ensure_ascii=False).encode("utf-8")
            res = self.client.put_file(CONFIG_PATH, content_bytes, commit_msg, self.sha)
            self.sha = res["content"]["sha"]
            if bump_version:
                print(f"\n[🚀 成功] 新版本已发布！v{self.data['version']['name']} (code: {self.data['version']['code']})")
                print(">>> 本体软件将在下次刷新/轮询或启动时即刻弹出更新弹窗！")
            else:
                print("\n[✅ 成功] 已应用并实时同步至云端仓库！本体软件零延迟生效！")
            return True
        except Exception as e:
            print(f"[!] 发布失败: {e}")
            return False

    def upload_local_file(self, local_path, remote_folder):
        if not os.path.exists(local_path):
            print(f"[!] 本地文件不存在: {local_path}")
            return None
        filename = os.path.basename(local_path)
        safe_name = "".join(c if c.isalnum() or c in "._-" else "_" for c in filename)
        remote_path = f"{remote_folder}/{int(time.time())}_{safe_name}"
        file_size = os.path.getsize(local_path)
        print(f"[+] 正在上传 {filename} ({round(file_size/1048576, 2)} MB) 至 {remote_path}...")
        with open(local_path, "rb") as f:
            data = f.read()
        self.client.put_file(remote_path, data, f"console: upload {filename}")
        url = self.client.raw_url(remote_path)
        print(f"[✓] 上传成功！直链: {url}")
        return url

    # ================= 1. 首页分类与站点 =================
    def menu_home(self):
        print("\n" + "="*50)
        print("【1. 首页分类与站点管理】")
        cats = self.data.setdefault("home", {}).setdefault("categories", [])
        for i, c in enumerate(cats):
            cards = c.get("cards", [])
            print(f"  [{i+1}] {c.get('name')} (id: {c.get('id')}, {len(cards)} 个站点)")
        print("\n  [A] 增加分类  [D] 删除分类  [S] 增加站点  [B] 编辑角标  [P] 推送并触发更新弹窗  [0] 返回")
        ch = input("\n请选择操作: ").strip().upper()
        if ch == "A":
            cid = input("输入分类英文ID (如 tools): ").strip()
            cname = input("输入分类中文名称: ").strip()
            cicon = input("输入分类图标 (默认 grid): ").strip() or "grid"
            cats.append({"id": cid, "name": cname, "iconKey": cicon, "subcategories": [], "cards": []})
            print(f"[✓] 已添加分类「{cname}」")
            self._ask_publish("新增分类 " + cname)
        elif ch == "D":
            idx = int(input("输入要删除的分类序号: ").strip()) - 1
            if 0 <= idx < len(cats):
                del_name = cats[idx].get("name")
                cats.pop(idx)
                print(f"[✓] 已删除分类「{del_name}」")
                self._ask_publish("删除分类 " + del_name)
        elif ch == "S":
            idx = int(input("输入目标分类序号: ").strip()) - 1
            if 0 <= idx < len(cats):
                cat = cats[idx]
                title = input("站点名称: ").strip()
                url = input("站点链接 (https://...): ").strip()
                badge = input("角标文字 (可留空): ").strip()
                btype = input("角标色彩 (ROSE/NEW/GOLD/BLUE, 默认NEW): ").strip() or "NEW"
                cat.setdefault("cards", []).append({
                    "id": f"site_{int(time.time())}",
                    "title": title,
                    "url": url,
                    "badge": badge or None,
                    "badgeType": btype,
                    "categoryId": cat.get("id"),
                    "subcatId": "all"
                })
                print(f"[✓] 站点「{title}」已加入分类「{cat.get('name')}」")
                self._ask_publish(f"新增站点 {title}")
        elif ch == "P":
            self.publish(bump_version=True, action_desc="首页分类与站点更新")

    # ================= 2. 软件库管理 =================
    def menu_software(self):
        print("\n" + "="*50)
        print("【2. 软件库管理 (支持本地上传 APK)】")
        sws = self.data.setdefault("software", [])
        for i, s in enumerate(sws):
            has_apk = "✅" if s.get("apkUrl") else "❌"
            print(f"  [{i+1}] {s.get('title')} ({s.get('badge') or '无角标'}) - APK: {has_apk}")
        print("\n  [A] 新增软件 (本地上传APK)  [D] 删除软件  [P] 推送并触发更新弹窗  [0] 返回")
        ch = input("\n请选择操作: ").strip().upper()
        if ch == "A":
            title = input("软件名称: ").strip()
            desc = input("软件描述: ").strip()
            author = input("作者: ").strip()
            badge = input("角标 (如 站长推荐): ").strip()
            apk_path = input("本地 APK 文件路径 (回车跳过): ").strip()
            apk_url = ""
            if apk_path and os.path.exists(apk_path):
                apk_url = self.upload_local_file(apk_path, "dist/apk") or ""
            sws.append({
                "id": f"sw_{int(time.time())}",
                "type": "software",
                "title": title,
                "desc": desc,
                "author": author,
                "badge": badge,
                "apkUrl": apk_url,
                "url": ""
            })
            print(f"[✓] 软件「{title}」已添加！")
            self._ask_publish(f"软件库上新「{title}」")
        elif ch == "D":
            idx = int(input("输入要删除的序号: ").strip()) - 1
            if 0 <= idx < len(sws):
                sws.pop(idx)
                print("[✓] 软件已删除")
                self._ask_publish("删除软件")
        elif ch == "P":
            self.publish(bump_version=True, action_desc="软件库更新")

    # ================= 3. Skill / Prompt =================
    def menu_skill(self):
        print("\n" + "="*50)
        print("【3. Skill 技能库 / Prompt 提示词 (支持上传图/视频)】")
        skills = self.data.setdefault("skills", [])
        for i, sk in enumerate(skills):
            print(f"  [{i+1}] {sk.get('title')} (Prompt: {len(sk.get('prompt',''))} 字符)")
        print("\n  [A] 新增 Skill/Prompt  [E] 编辑 Prompt  [D] 删除  [P] 推送并触发更新弹窗  [0] 返回")
        ch = input("\n请选择操作: ").strip().upper()
        if ch == "A":
            title = input("Skill 标题: ").strip()
            desc = input("简介: ").strip()
            print("请输入 Prompt 提示词正文 (输入完成后单独一行输入 EOF 结束):")
            lines = []
            while True:
                line = input()
                if line.strip() == "EOF":
                    break
                lines.append(line)
            prompt = "\n".join(lines)
            img_path = input("本地预览图路径 (回车跳过): ").strip()
            img_url = self.upload_local_file(img_path, "dist/uploads") if img_path else ""
            vid_path = input("本地演示视频路径 (回车跳过): ").strip()
            vid_url = self.upload_local_file(vid_path, "dist/uploads") if vid_path else ""
            skills.append({
                "id": f"sk_{int(time.time())}",
                "type": "skill",
                "title": title,
                "desc": desc,
                "prompt": prompt,
                "previewUrl": img_url or "",
                "mediaUrl": vid_url or ""
            })
            print(f"[✓] Skill「{title}」已保存！")
            self._ask_publish(f"新增 Skill「{title}」")
        elif ch == "E":
            idx = int(input("输入要编辑的 Skill 序号: ").strip()) - 1
            if 0 <= idx < len(skills):
                print(f"当前 Prompt:\n{skills[idx].get('prompt','')}")
                print("\n请输入新 Prompt (输入 EOF 结束):")
                lines = []
                while True:
                    line = input()
                    if line.strip() == "EOF":
                        break
                    lines.append(line)
                skills[idx]["prompt"] = "\n".join(lines)
                print("[✓] Prompt 已更新！")
                self._ask_publish(f"更新 Prompt: {skills[idx].get('title')}")
        elif ch == "P":
            self.publish(bump_version=True, action_desc="Skill技能与提示词库更新")

    # ================= 4. 设置 =================
    def menu_settings(self):
        print("\n" + "="*50)
        print("【4. 设置版块管理】")
        s = self.data.setdefault("settings", {})
        print(f"  App 名称: {s.get('appName', '懒得找了')}")
        print(f"  Slogan:   {s.get('slogan', '')}")
        print(f"  关于说明: {s.get('aboutText', '')[:60]}...")
        print("\n  [E] 修改设置内容  [P] 推送并触发更新弹窗  [0] 返回")
        ch = input("\n请选择操作: ").strip().upper()
        if ch == "E":
            name = input(f"App 名称 [{s.get('appName','')}]: ").strip()
            if name: s["appName"] = name
            slogan = input(f"Slogan [{s.get('slogan','')}]: ").strip()
            if slogan: s["slogan"] = slogan
            about = input(f"关于说明 [{s.get('aboutText','')}]: ").strip()
            if about: s["aboutText"] = about
            print("[✓] 设置已更新！")
            self._ask_publish("设置与品牌信息更新")
        elif ch == "P":
            self.publish(bump_version=True, action_desc="设置版块更新")

    # ================= 5. 开屏动画 =================
    def menu_splash(self):
        print("\n" + "="*50)
        print("【5. 开屏动画管理 (支持输入代码控制开屏)】")
        sp = self.data.setdefault("splash", {})
        print(f"  当前模式: {sp.get('type', 'default')}")
        print(f"  展示时长: {sp.get('durationSeconds', 3)} 秒")
        print(f"  自定义代码长度: {len(sp.get('customHtml', ''))} 字符")
        print("\n  [1] 切换为默认 3D 方块动画")
        print("  [2] 输入自定义 HTML/CSS/JS 代码 (全屏 WebView)")
        print("  [3] 上传图片/视频开屏媒体")
        print("  [P] 推送并触发更新弹窗")
        ch = input("\n请选择操作: ").strip().upper()
        if ch == "1":
            sp["type"] = "default"
            print("[✓] 已恢复默认开屏动画")
            self._ask_publish("恢复默认开屏动画")
        elif ch == "2":
            sp["type"] = "html"
            print("请输入自定义 HTML/JS 开屏代码 (输入 EOF 结束):")
            lines = []
            while True:
                line = input()
                if line.strip() == "EOF":
                    break
                lines.append(line)
            sp["customHtml"] = "\n".join(lines)
            print("[✓] 自定义开屏代码已保存！")
            self._ask_publish("自定义开屏动画代码发布")
        elif ch == "3":
            sp["type"] = "media"
            p = input("输入本地图片/视频文件路径: ").strip()
            if os.path.exists(p):
                sp["mediaUrl"] = self.upload_local_file(p, "dist/uploads")
                print("[✓] 开屏媒体已上传并保存！")
                self._ask_publish("更新开屏媒体")
        elif ch == "P":
            self.publish(bump_version=True, action_desc="开屏动画配置更新")

    # ================= 6. 欢迎弹窗 =================
    def menu_welcome(self):
        print("\n" + "="*50)
        print("【6. 欢迎界面弹窗管理 (增加新功能特性)】")
        w = self.data.setdefault("welcome", {})
        status = "✅ 开启" if w.get("enabled") else "🚫 停用"
        print(f"  弹窗状态: {status}")
        print(f"  标题:     {w.get('title', '欢迎体验全新版本')}")
        print(f"  内容说明: \n{w.get('content', '')}")
        print("\n  [T] 开启/停用切换  [E] 编辑新功能列表  [P] 推送并触发更新弹窗  [0] 返回")
        ch = input("\n请选择操作: ").strip().upper()
        if ch == "T":
            w["enabled"] = not w.get("enabled", False)
            print(f"[✓] 欢迎弹窗已设为: {'开启' if w['enabled'] else '停用'}")
            self._ask_publish("切换欢迎弹窗状态")
        elif ch == "E":
            w["enabled"] = True
            w["title"] = input("弹窗标题: ").strip() or "欢迎使用新功能"
            print("请输入新功能说明列表 (输入 EOF 结束):")
            lines = []
            while True:
                line = input()
                if line.strip() == "EOF":
                    break
                lines.append(line)
            w["content"] = "\n".join(lines)
            print("[✓] 欢迎弹窗新功能已保存！")
            self._ask_publish("发布欢迎弹窗新特性")
        elif ch == "P":
            self.publish(bump_version=True, action_desc="欢迎弹窗配置更新")

    # ================= 7. 更新弹窗 =================
    def menu_update(self):
        print("\n" + "="*50)
        print("【7. 更新弹窗管理 (标题/日志/按钮/自定义样式代码)】")
        v = self.data.setdefault("version", {})
        u = self.data.setdefault("updateDialog", {})
        print(f"  当前发布版本: v{v.get('name', '1.0')} (code: {v.get('code', 1)})")
        print(f"  更新弹窗标题: {u.get('title', '发现新版本')}")
        print(f"  更新日志项:   {len(u.get('changelog', []))} 条")
        print("\n  [E] 编辑更新标题与日志内容")
        print("  [C] 自定义 CSS 弹窗样式代码")
        print("  [H] 自定义 HTML/JS 弹窗接管代码")
        print("  [P] 递增版本代码并发布 (立即触发本体弹窗)")
        ch = input("\n请选择操作: ").strip().upper()
        if ch == "E":
            u["title"] = input(f"更新弹窗标题 [{u.get('title','')}]: ").strip() or u.get("title")
            print("请输入更新日志列表 (输入 EOF 结束):")
            lines = []
            while True:
                line = input()
                if line.strip() == "EOF":
                    break
                lines.append(line)
            if lines:
                u["changelog"] = lines
                v["changelog"] = lines
            print("[✓] 更新弹窗内容已保存！")
            self._ask_publish("更新弹窗内容配置修改")
        elif ch == "C":
            print("请输入自定义 CSS 样式代码 (输入 EOF 结束):")
            lines = []
            while True:
                line = input()
                if line.strip() == "EOF":
                    break
                lines.append(line)
            u["customCss"] = "\n".join(lines)
            print("[✓] 自定义 CSS 已保存！")
            self._ask_publish("自定义弹窗 CSS 样式更新")
        elif ch == "H":
            print("请输入自定义 HTML/JS 弹窗代码 (输入 EOF 结束):")
            lines = []
            while True:
                line = input()
                if line.strip() == "EOF":
                    break
                lines.append(line)
            u["customHtml"] = "\n".join(lines)
            print("[✓] 自定义 HTML/JS 已保存！")
            self._ask_publish("自定义弹窗代码接管更新")
        elif ch == "P":
            self.publish(bump_version=True, action_desc="版本发布更新")

    # ================= 8. APK 仓库与制作 =================
    def menu_apk_repo(self):
        print("\n" + "="*50)
        print("【8. APK 仓库管理与制作 (自动检测仓库/一键触发更新)】")
        print("[+] 正在扫描 GitHub 仓库 dist/apk/ 目录...")
        apks = []
        try:
            items = self.client.get_contents("dist/apk")
            if isinstance(items, list):
                apks = [x for x in items if x.get("name","").endswith(".apk")]
        except Exception:
            pass

        print(f"[✓] 检测到仓库已有 {len(apks)} 个安装包:")
        for i, a in enumerate(apks):
            size_mb = round(a.get("size", 0) / 1048576, 2)
            print(f"  [{i+1}] {a.get('name')} ({size_mb} MB) -> {a.get('download_url')}")

        print("\n  [U] 本地上传新 APK 到仓库 dist/apk/")
        print("  [S] 选择仓库已有 APK 设为最新并触发本体更新")
        print("  [G] 查看本地打包制作 Gradle 指南")
        print("  [0] 返回")
        ch = input("\n请选择操作: ").strip().upper()
        if ch == "U":
            p = input("输入本地打包生成的 APK 路径: ").strip()
            if os.path.exists(p):
                url = self.upload_local_file(p, "dist/apk")
                if url:
                    yn = input("是否立即将该 APK 设为最新版本并触发更新弹窗？(Y/n): ").strip().lower()
                    if yn != "n":
                        self.data.setdefault("version", {})["apkUrl"] = url
                        self.publish(bump_version=True, action_desc=f"上传新安装包 {os.path.basename(p)}")
        elif ch == "S":
            idx = int(input("输入要设为最新版本的 APK 序号: ").strip()) - 1
            if 0 <= idx < len(apks):
                apk = apks[idx]
                self.data.setdefault("version", {})["apkUrl"] = apk.get("download_url")
                self.publish(bump_version=True, action_desc=f"设为最新发布包 {apk.get('name')}")
        elif ch == "G":
            print("\n【Android 打包制作指南】")
            print("1. 生成 Debug 测试包:")
            print("   gradle assembleDebug")
            print("   产物: app/build/outputs/apk/debug/app-debug.apk")
            print("2. 生成 Release 正式包:")
            print("   gradle assembleRelease")

    def _cleanup_old_apks(self):
        """APK 仓库只保留当前版本与最新版本两个 APK（删除更旧的）"""
        items = self.client.get_contents("dist/apk")
        if not isinstance(items, list):
            return
        apks = [x for x in items if x.get("name", "").endswith(".apk")]
        apks.sort(key=lambda x: x.get("name", ""), reverse=True)
        current_name = (self.data.get("version", {}).get("apkUrl", "") or "").split("/")[-1]
        keep = set()
        if current_name:
            keep.add(current_name)
        for a in apks:
            if len(keep) >= 2:
                break
            keep.add(a["name"])
        for a in apks:
            if a["name"] not in keep:
                try:
                    self.client.delete_file(f"dist/apk/{a['name']}", a["sha"])
                    print(f"[✓] 已清理旧版本 APK: {a['name']}")
                except Exception as e:
                    print(f"[!] 清理 {a['name']} 失败: {e}")

    def _ask_publish(self, action_desc):
        print("\n请选择同步方式:")
        print("  [1] 🚀 触发更新弹窗 (递增版本代码，本体软件下次刷新即刻弹窗提醒)")
        print("  [2] ✅ 应用 (保存到云端仓库，本体软件零延迟实时同步生效，不弹窗提醒)")
        print("  [0] 暂不推送")
        c = input("选择: ").strip()
        if c == "1":
            self.publish(bump_version=True, action_desc=action_desc)
        elif c == "2":
            self.publish(bump_version=False)

    def interactive_loop(self):
        if not self.load():
            return
        while True:
            v_name = self.data.get("version", {}).get("name", "1.0")
            v_code = self.data.get("version", {}).get("code", 1)
            print("\n" + "="*50)
            print(f"   懒得找了 · 控制台程序 (当前版本: v{v_name} | code: {v_code})")
            print("="*50)
            print("  [1] 首页：增删分类、子分类、站点、角标 (实时推送更新弹窗)")
            print("  [2] 软件：本地上传 APK 文件到仓库 (实时推送更新弹窗)")
            print("  [3] Skill：技能库/Prompt (上传图片/视频，编辑Prompt)")
            print("  [4] 设置：管理本版块所有内容 (App名/Slogan/联系方式)")
            print("  [5] 开屏动画：自定义输入代码/上传媒体控制开屏")
            print("  [6] 欢迎弹窗：增加新功能特性列表 (实时推送更新弹窗)")
            print("  [7] 更新弹窗：编辑标题/内容/按钮/自定义CSS/HTML样式")
            print("  [8] APK 仓库：制作上传与自动检测仓库已有安装包")
            print("  [9] 🚀 一键发布新版本并触发本体更新")
            print("  [0] 退出控制台")
            print("="*50)
            ch = input("请输入选项 [0-9]: ").strip()
            if ch == "1":
                self.menu_home()
            elif ch == "2":
                self.menu_software()
            elif ch == "3":
                self.menu_skill()
            elif ch == "4":
                self.menu_settings()
            elif ch == "5":
                self.menu_splash()
            elif ch == "6":
                self.menu_welcome()
            elif ch == "7":
                self.menu_update()
            elif ch == "8":
                self.menu_apk_repo()
            elif ch == "9":
                self.publish(bump_version=True, action_desc="控制台一键新版本推送")
            elif ch == "0":
                print("已退出控制台。")
                break


def main():
    parser = argparse.ArgumentParser(description="懒得找了 · 云端总控台 CLI")
    parser.add_argument("--token", default=DEFAULT_TOKEN, help="GitHub Personal Access Token")
    parser.add_argument("--owner", default=DEFAULT_OWNER, help="GitHub 仓库所有者")
    parser.add_argument("--repo",  default=DEFAULT_REPO,  help="GitHub 仓库名称")
    parser.add_argument("--bump-version", metavar="NOTE", help="命令行直接递增版本代码并发布更新弹窗")
    parser.add_argument("--upload-apk", metavar="FILE", help="命令行直接上传 APK 并发布更新")
    parser.add_argument("--check-apks", action="store_true", help="检测仓库已有的 APK 文件列表")
    parser.add_argument("--status", action="store_true", help="检测云端连接与当前版本状态")
    args = parser.parse_args()

    client = GitHubClient(token=args.token, owner=args.owner, repo=args.repo)
    console = AdminConsole(client)

    if args.status:
        console.load()
        return

    if args.check_apks:
        items = client.get_contents("dist/apk")
        apks = [x for x in items if x.get("name","").endswith(".apk")] if isinstance(items, list) else []
        print(f"仓库共有 {len(apks)} 个 APK 文件:")
        for a in apks:
            print(f"- {a['name']} ({round(a['size']/1048576,2)}MB) : {a['download_url']}")
        return

    if args.upload_apk:
        if not console.load():
            sys.exit(1)
        url = console.upload_local_file(args.upload_apk, "dist/apk")
        if url:
            console.data.setdefault("version", {})["apkUrl"] = url
            console.publish(bump_version=True, action_desc=f"上传安装包 {os.path.basename(args.upload_apk)}")
        return

    if args.bump_version:
        if not console.load():
            sys.exit(1)
        console.publish(bump_version=True, action_desc=args.bump_version)
        return

    # 无参数进入交互式控制台
    console.interactive_loop()


if __name__ == "__main__":
    main()

