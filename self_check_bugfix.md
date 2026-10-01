# 自检 BUG 修复分支 · fix/self-check-bugfix

> 创建于 v1.0.18（main af27072e）。本分支用于**自检 & BUG 修复**的专项工作区：
> 任何需要验证的修复先在此分支验证通过，再合并回 main 发布新版本。

## 一、自检清单（每次发布前逐项核对）

| # | 自检项 | 状态 |
|---|--------|------|
| 1 | 角标不遮挡站点内容、动态效果正常（流光/呼吸/摇摆） | ☑ 已核验（v1.0.18） |
| 2 | 更新弹窗可正常下载并安装新版本（权限/授权引导/多镜像） | ☑ 已核验（v1.0.18） |
| 3 | 工具箱/软件版块取消展开收纳，直接平铺 | ☑ 已核验（v1.0.18） |
| 4 | 设置版块主题与软件主题同步 | ☑ 已核验（v1.0.18） |
| 5 | 软件主题=国庆可爱风格（默认），旧主题已移除 | ☑ 已核验（v1.0.18） |
| 6 | 版本四要素对齐（version.code / name / apkUrl / 真实APK） | ☑ 已核验（v1.0.18） |
| 7 | 新版本提示文案「退出软件重进即完成更新」 | ☑ 已核验 |
| 8 | 云端实时同步（5秒轮询） | ☑ 已核验 |

## 二、最新版 APK 下载直链（自动更新用）

**官方直链（code）：**
```
https://raw.githubusercontent.com/shuting52/2026-9-21landezhaole/main/dist/apk/landezhao-v1.0.18-1790835592.apk
```

**加速镜像（国内更快）：**
```
https://testingcf.jsdelivr.net/gh/shuting52/2026-9-21landezhaole@main/dist/apk/landezhao-v1.0.18-1790835592.apk
```

**备用镜像：**
```
https://gcore.jsdelivr.net/gh/shuting52/2026-9-21landezhaole@main/dist/apk/landezhao-v1.0.18-1790835592.apk
https://ghfast.top/https://raw.githubusercontent.com/shuting52/2026-9-21landezhaole/main/dist/apk/landezhao-v1.0.18-1790835592.apk
```

## 三、BUG 修复记录区（在此分支提交修复，验证后合并 main）

- [ ] 待修复项记录区：发现的 BUG 在此追加，修复后勾选并注明版本
