package com.yuntai;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 懒得找了 · 本体维护控制台（原生版 v2.0.0）
 * ─────────────────────────────────────────────
 * 全新打造的原生 Android 控制台，不再使用任何 WebView / 网页壳。
 * 界面与交互完全参照桌面端「本体维护控制台」：
 *   · 顶部：Token 输入 + 连接 + 状态
 *   · 中部：7 个功能页签（首页/软件/Skill/设置/更新/其他/APK）
 *   · 底部：固定操作栏（⚡ 应用 / 🚀 发布 / 🔍 诊断）
 * 网络层内置 8 通道多镜像兜底读取 + 网页劫持检测，彻底告别
 * 「云端数据解析失败 / Maximum call stack size exceeded」。
 */
public class MainActivity extends Activity {

    private static final String OWNER = "shuting52";
    private static final String REPO = "2026-9-21landezhaole";
    private static final String CONFIG = "admin-data.json";
    private static final String API_BASE = "https://api.github.com";

    // 只读镜像链（顺序即优先级；API 失败后依次尝试）
    private static final String[][] MIRRORS = {
        {"jsdelivr-testingcf", "https://testingcf.jsdelivr.net/gh/%s/%s@main/%s"},
        {"jsdelivr-cdn",       "https://cdn.jsdelivr.net/gh/%s/%s@main/%s"},
        {"jsdelivr-fastly",    "https://fastly.jsdelivr.net/gh/%s/%s@main/%s"},
        {"jsdelivr-gcore",     "https://gcore.jsdelivr.net/gh/%s/%s@main/%s"},
        {"ghfast-top",         "https://ghfast.top/https://raw.githubusercontent.com/%s/%s/main/%s"},
        {"ghproxy-net",        "https://ghproxy.net/https://raw.githubusercontent.com/%s/%s/main/%s"},
        {"gitmirror",          "https://raw.gitmirror.com/%s/%s/main/%s"},
        {"raw",                "https://raw.githubusercontent.com/%s/%s/main/%s"},
    };

    // ---- 控件类型 ----
    private static final int T_EDIT = 0, T_TEXT = 1, T_CHECK = 2, T_SPIN = 3;

    // ---- 备份/导入 请求码 ----
    private static final int PICK_IMPORT_JSON = 200;
    private static final int CREATE_BACKUP_JSON = 201;

    // ---- 表单字段描述 ----
    static class FieldSpec {
        String label; int type; String[] opts; String path; boolean numeric;
        FieldSpec(String label, int type, String[] opts, String path, boolean numeric) {
            this.label = label; this.type = type; this.opts = opts;
            this.path = path; this.numeric = numeric;
        }
    }
    static class Field {
        FieldSpec spec; View view; EditText et; CheckBox cb; Spinner sp;
        Field(FieldSpec s) { spec = s; }
    }

    // ---- 状态 ----
    private String token = "";
    private JSONObject admin;
    private String sha = "";
    private String source = "";
    private final List<JSONObject> catCache = new ArrayList<>();
    private final List<JSONObject> siteCache = new ArrayList<>();
    private final List<JSONObject> softCache = new ArrayList<>();
    // ---- v2.0.4 增强：站点搜索视图（过滤后显示列表） ----
    private final List<JSONObject> siteView = new ArrayList<>();
    private EditText siteFilterEt;
    private final List<JSONObject> skillCache = new ArrayList<>();
    private final List<JSONObject> apkCache = new ArrayList<>();
    private int selCat = -1, selSite = -1, selSoft = -1, selSkill = -1, selApk = -1;
    private int selSubcat = -1;

    // ---- UI ----
    private EditText tokenEt;
    private TextView statusBar;
    private LinearLayout pageArea;
    private final Map<String, View> pages = new LinkedHashMap<>();
    private final Map<String, List<Field>> pageFields = new LinkedHashMap<>();
    private final Map<String, Integer> pageFieldTypes = new LinkedHashMap<>();
    private String currentTab = "home";
    private LinearLayout tabRow;

    // ============================================================
    // 生命周期
    // ============================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(buildRoot());
        // 修复闪退：先构建首页（catLv/siteLv 等控件必须先存在），
        // 否则连接成功回调 renderHomeLists() 会因控件为 null 而 NPE 闪退
        buildPage("home");
        doConnect(false);
    }

    // ============================================================
    // UI 构建
    // ============================================================
    private int dp(int v) { return (int) (getResources().getDisplayMetrics().density * v); }

    private LinearLayout buildRoot() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF0F1220);

        // ---- 顶部：Token + 连接 + 状态 ----
        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setPadding(dp(10), dp(8), dp(10), dp(8));
        top.setBackgroundColor(0xFF151A2B);

        tokenEt = new EditText(this);
        tokenEt.setHint("GitHub Token（留空可只读浏览）");
        tokenEt.setTextColor(Color.WHITE);
        tokenEt.setHintTextColor(0xFF64748B);
        tokenEt.setSingleLine(true);
        tokenEt.setTextSize(13);
        tokenEt.setBackgroundColor(0xFF1E2438);
        tokenEt.setPadding(dp(8), dp(4), dp(8), dp(4));
        tokenEt.setMinHeight(dp(38));
        top.addView(tokenEt, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        Button conn = new Button(this);
        conn.setText("连接");
        conn.setTextSize(13);
        conn.setTextColor(0xFF0F172A);
        conn.setBackgroundColor(0xFF38BDF8);
        conn.setAllCaps(false);
        LinearLayout.LayoutParams lpc = new LinearLayout.LayoutParams(dp(72), dp(40));
        lpc.leftMargin = dp(8);
        conn.setLayoutParams(lpc);
        conn.setOnClickListener(v -> doConnect(false));
        top.addView(conn);

        Button bBackup = new Button(this);
        bBackup.setText("⬇");
        bBackup.setTextSize(13);
        bBackup.setTextColor(0xFF0F172A);
        bBackup.setBackgroundColor(0xFFA5B4FC);
        bBackup.setAllCaps(false);
        LinearLayout.LayoutParams lpb = new LinearLayout.LayoutParams(dp(44), dp(40));
        lpb.leftMargin = dp(6);
        bBackup.setLayoutParams(lpb);
        bBackup.setOnClickListener(v -> backupJson());
        top.addView(bBackup);

        Button bImport = new Button(this);
        bImport.setText("⬆");
        bImport.setTextSize(13);
        bImport.setTextColor(0xFF0F172A);
        bImport.setBackgroundColor(0xFF6EE7B7);
        bImport.setAllCaps(false);
        LinearLayout.LayoutParams lpi = new LinearLayout.LayoutParams(dp(44), dp(40));
        lpi.leftMargin = dp(6);
        bImport.setLayoutParams(lpi);
        bImport.setOnClickListener(v -> pickFile(PICK_IMPORT_JSON));
        top.addView(bImport);
        root.addView(top);

        statusBar = new TextView(this);
        statusBar.setText("● 未连接（读取已内置多镜像兜底）");
        statusBar.setTextSize(11);
        statusBar.setTextColor(0xFF64748B);
        statusBar.setPadding(dp(12), dp(4), dp(12), dp(4));
        root.addView(statusBar);

        // ---- 页签栏 ----
        HorizontalScrollView tabScroll = new HorizontalScrollView(this);
        tabRow = new LinearLayout(this);
        tabRow.setOrientation(LinearLayout.HORIZONTAL);
        tabRow.setPadding(dp(4), 0, dp(4), 0);
        tabRow.setBackgroundColor(0xFF151A2B);
        String[] tabs = {"首页", "软件", "Skill", "设置", "更新", "其他", "APK"};
        String[] keys = {"home", "soft", "skill", "settings", "update", "other", "apk"};
        for (int i = 0; i < tabs.length; i++) {
            final String k = keys[i];
            TextView tv = new TextView(this);
            tv.setText(tabs[i]);
            tv.setTextSize(14);
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(dp(16), dp(10), dp(16), dp(10));
            tv.setTextColor(0xFF94A3B8);
            tv.setTag(k);
            tv.setOnClickListener(v -> switchTab(k));
            tabRow.addView(tv);
        }
        tabScroll.addView(tabRow);
        root.addView(tabScroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // ---- 内容区 ----
        pageArea = new LinearLayout(this);
        pageArea.setOrientation(LinearLayout.VERTICAL);
        root.addView(pageArea, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        // ---- 底部固定操作栏（桌面端同款）----
        LinearLayout bottom = new LinearLayout(this);
        bottom.setOrientation(LinearLayout.HORIZONTAL);
        bottom.setPadding(dp(8), dp(7), dp(8), dp(10));
        bottom.setBackgroundColor(0xFF151A2B);

        Button bApply = mkBottom("⚡ 应用", 0xFF22C55E, v -> doApply());
        Button bPublish = mkBottom("🚀 发布", 0xFFF59E0B, v -> doPublish());
        Button bDiag = mkBottom("🔍 诊断", 0xFF64748B, v -> doDiagnose());
        bottom.addView(bApply, lastWeight());
        bottom.addView(bPublish, lastWeight());
        bottom.addView(bDiag, lastWeight());
        root.addView(bottom);
        return root;
    }

    private Button mkBottom(String text, int bg, View.OnClickListener l) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(14);
        b.setTextColor(Color.WHITE);
        b.setAllCaps(false);
        b.setBackgroundColor(bg);
        b.setOnClickListener(l);
        return b;
    }
    private LinearLayout.LayoutParams lastWeight() {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(44), 1f);
        p.leftMargin = dp(4); p.rightMargin = dp(4);
        return p;
    }

    // ============================================================
    // 页签切换与页面构建
    // ============================================================
    private void switchTab(String key) {
        buildPage(key);
    }

    private void highlightTabs() {
        if (tabRow == null) return;
        for (int i = 0; i < tabRow.getChildCount(); i++) {
            View c = tabRow.getChildAt(i);
            if (c instanceof TextView) {
                TextView tv = (TextView) c;
                boolean act = tv.getTag() != null && tv.getTag().equals(currentTab);
                tv.setTextColor(act ? 0xFF38BDF8 : 0xFF94A3B8);
                tv.setTypeface(null, act ? Typeface.BOLD : Typeface.NORMAL);
            }
        }
    }

    private void buildPage(String key) {
        try {
            currentTab = key;
            if (pages.containsKey(key)) {
                pageArea.removeAllViews();
                pageArea.addView(pages.get(key));
                if (admin != null) renderCachedPage(key);
                highlightTabs();
                return;
            }
            LinearLayout page = new LinearLayout(this);
            page.setOrientation(LinearLayout.VERTICAL);
            page.setPadding(dp(12), dp(10), dp(12), dp(12));
            page.setBackgroundColor(0xFF0F1220);

            switch (key) {
                case "home": buildHome(page); break;
                case "soft": buildSoft(page); break;
                case "skill": buildSkill(page); break;
                case "settings": buildSettings(page); break;
                case "update": buildUpdate(page); break;
                case "other": buildOther(page); break;
                case "apk": buildApk(page); break;
            }
            pages.put(key, page);
            highlightTabs();

            pageArea.removeAllViews();
            pageArea.addView(page);
            if (admin != null) renderCachedPage(key);
        } catch (Exception e) {
            // 防闪退：单个页面构建失败只提示，不让整个 App 崩溃
            log("buildPage(" + key + ") 失败: " + e);
            toast("页面加载异常(" + key + ")：" + e.getMessage());
        }
    }

    /** 页面缓存渲染：切回页面时把最新云端数据填回表单 */
    private void renderCachedPage(String key) {
        if ("settings".equals(key)) renderFields(pageFields.get("settings"), admin, "settingsPage");
        else if ("update".equals(key)) renderFields(pageFields.get("update"), admin, "updatePage");
        else if ("other".equals(key)) renderFields(pageFields.get("other"), admin, "otherPage");
        else if ("home".equals(key)) renderHomeLists();
        else if ("soft".equals(key)) renderSoftList();
        else if ("skill".equals(key)) renderSkillList();
        else if ("apk".equals(key)) { if (apkCache.isEmpty()) loadApkRepo(true); renderApkList(); }
    }

    // ============================================================
    // 首页：分类 + 站点
    // ============================================================
    private ListView catLv, siteLv;
    private ArrayAdapter<String> catAd, siteAd;

    private void buildHome(LinearLayout page) {
        TextView t1 = secTitle("首页分类与站点管理（复用云端 admin-data.json）");
        page.addView(t1);

        catLv = new ListView(this);
        catAd = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<String>());
        catLv.setAdapter(catAd);
        catLv.setBackgroundColor(0xFF1E2438);
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(150));
        lp1.topMargin = dp(6);
        catLv.setLayoutParams(lp1);
        catLv.setOnItemClickListener((p, v, pos, id) -> { selCat = pos; renderSites(); });
        page.addView(catLv);

        LinearLayout catBtns = row();
        addBtn(catBtns, "＋ 新增分类", v -> dlgCat(-1));
        addBtn(catBtns, "✎ 编辑分类", v -> dlgCat(selCat));
        addBtn(catBtns, "－ 删除分类", v -> delCat());
        page.addView(catBtns);

        LinearLayout catBtns2 = row();
        addBtn(catBtns2, "＋ 子分类", v -> dlgSubcat(-1));
        addBtn(catBtns2, "✎ 子分类", v -> dlgSubcat(selSubcat));
        addBtn(catBtns2, "⬆ 上移分类", v -> moveCat(-1));
        addBtn(catBtns2, "⬇ 下移分类", v -> moveCat(1));
        page.addView(catBtns2);

        TextView t2 = secTitle("—— 该分类下的站点 ——");
        page.addView(t2);

        // v2.0.4 增强：站点搜索过滤（1000+ 站点快速定位）
        siteFilterEt = new EditText(this);
        siteFilterEt.setHint("🔍 搜索站点标题 / 域名（1000+ 站点秒定位）");
        siteFilterEt.setTextColor(Color.WHITE);
        siteFilterEt.setHintTextColor(0xFF64748B);
        siteFilterEt.setSingleLine(true);
        siteFilterEt.setTextSize(13);
        siteFilterEt.setBackgroundColor(0xFF1E2438);
        siteFilterEt.setPadding(dp(8), dp(4), dp(8), dp(4));
        siteFilterEt.setMinHeight(dp(36));
        siteFilterEt.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void afterTextChanged(android.text.Editable s) { renderSites(); }
        });
        page.addView(siteFilterEt);

        siteLv = new ListView(this);
        siteAd = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<String>());
        siteLv.setAdapter(siteAd);
        siteLv.setBackgroundColor(0xFF1E2438);
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(170));
        lp2.topMargin = dp(6);
        siteLv.setLayoutParams(lp2);
        page.addView(siteLv);

        LinearLayout siteBtns = row();
        addBtn(siteBtns, "＋ 新增站点", v -> dlgSite(-1));
        addBtn(siteBtns, "✎ 编辑站点", v -> dlgSite(selSite));
        addBtn(siteBtns, "－ 删除站点", v -> delSite());
        page.addView(siteBtns);

        LinearLayout siteBtns2 = row();
        addBtn(siteBtns2, "⬆ 上移", v -> moveSite(-1));
        addBtn(siteBtns2, "⬇ 下移", v -> moveSite(1));
        addBtn(siteBtns2, "🌟 一键收录", v -> dlgCollect());
        addBtn(siteBtns2, "🔁 全库去重", v -> dedupAllSites());
        page.addView(siteBtns2);

        // v2.0.4 增强：站点 JSON 批量导入（配合 1000+ 站点库）
        LinearLayout siteBtns3 = row();
        addBtn(siteBtns3, "📥 剪贴板导入JSON", v -> dlgImportJson());
        page.addView(siteBtns3);
    }

    private void renderHomeLists() {
        if (admin == null) return;
        catCache.clear();
        JSONArray cats = admin.optJSONObject("home") != null
                ? admin.optJSONObject("home").optJSONArray("categories") : null;
        if (cats != null) {
            for (int i = 0; i < cats.length(); i++) {
                JSONObject c = cats.optJSONObject(i);
                if (c != null) catCache.add(c);
            }
        }
        List<String> names = new ArrayList<>();
        for (JSONObject c : catCache) {
            names.add((c.optString("name", "")) + "  [" + c.optString("id", "") + "] · "
                    + (c.optJSONArray("cards") == null ? 0 : c.optJSONArray("cards").length()) + "站");
        }
        catAd.clear(); catAd.addAll(names); catAd.notifyDataSetChanged();
        if (selCat >= catCache.size()) selCat = -1;
        renderSites();
    }

    private void renderSites() {
        // v2.0.4：支持搜索过滤（siteView 只存过滤后的站点对象）
        siteCache.clear();
        if (selCat >= 0 && selCat < catCache.size()) {
            JSONObject cat = catCache.get(selCat);
            JSONArray cards = cat.optJSONArray("cards");
            if (cards != null) {
                for (int i = 0; i < cards.length(); i++) {
                    JSONObject s = cards.optJSONObject(i);
                    if (s != null) siteCache.add(s);
                }
            }
        }
        String kw = (siteFilterEt == null ? "" : siteFilterEt.getText().toString().trim()).toLowerCase(Locale.ROOT);
        siteView.clear();
        for (JSONObject s : siteCache) {
            if (kw.isEmpty()) { siteView.add(s); continue; }
            String title = s.optString("title", "").toLowerCase(Locale.ROOT);
            String url = s.optString("url", "").toLowerCase(Locale.ROOT);
            if (title.contains(kw) || url.contains(kw)) siteView.add(s);
        }
        List<String> names = new ArrayList<>();
        for (JSONObject s : siteView) {
            names.add((s.optString("title", "")) + (s.optString("badge", "").isEmpty() ? "" : " ⭐" + s.optString("badge")));
        }
        siteAd.clear(); siteAd.addAll(names); siteAd.notifyDataSetChanged();
        selSite = -1;
    }

    private void dlgCat(final int idx) {
        final JSONObject c = (idx >= 0 && idx < catCache.size()) ? catCache.get(idx) : new JSONObject();
        LinearLayout fm = new LinearLayout(this);
        fm.setOrientation(LinearLayout.VERTICAL);
        EditText idE = le(fm, "分类 ID（英文，如 ai_tools）", c.optString("id", ""));
        EditText nmE = le(fm, "分类名称", c.optString("name", ""));
        EditText icE = le(fm, "图标 key（sparkle/grid/star…）", c.optString("iconKey", "grid"));
        EditText dsE = le(fm, "描述", c.optString("desc", ""));
        new AlertDialog.Builder(this)
            .setTitle(idx < 0 ? "新增分类" : "编辑分类")
            .setView(fm)
            .setPositiveButton("保存", (d, w) -> {
                try {
                    if (idx < 0) {
                        JSONObject nc = new JSONObject();
                        nc.put("id", idE.getText().toString().trim());
                        nc.put("name", nmE.getText().toString().trim());
                        nc.put("iconKey", icE.getText().toString().trim().isEmpty() ? "grid" : icE.getText().toString().trim());
                        nc.put("desc", dsE.getText().toString().trim());
                        nc.put("subcategories", arr("all", "全部"));
                        nc.put("cards", new JSONArray());
                        ensureCatArray().put(nc);
                        selCat = catCache.size();
                    } else {
                        c.put("id", idE.getText().toString().trim());
                        c.put("name", nmE.getText().toString().trim());
                        c.put("iconKey", icE.getText().toString().trim().isEmpty() ? "grid" : icE.getText().toString().trim());
                        c.put("desc", dsE.getText().toString().trim());
                    }
                    collectCatFromCache();
                    renderHomeLists();
                    toast("分类已修改，点底部「⚡ 应用」同步");
                } catch (Exception e) { toast("保存失败: " + e.getMessage()); }
            })
            .setNegativeButton("取消", null)
            .show();
    }

    private void delCat() {
        if (selCat < 0 || selCat >= catCache.size()) { toast("请先在列表选择一个分类"); return; }
        confirm("删除分类「" + catCache.get(selCat).optString("name") + "」及其全部站点？", () -> {
            catCache.remove(selCat);
            collectCatFromCache();
            selCat = -1;
            renderHomeLists();
            toast("分类已删除（点「⚡ 应用」生效）");
        });
    }

    private void dlgSite(final int idx) {
        if (selCat < 0 || selCat >= catCache.size()) { toast("请先选择上方分类"); return; }
        final JSONObject s = (idx >= 0 && idx < siteView.size()) ? siteView.get(idx) : new JSONObject();
        LinearLayout fm = new LinearLayout(this);
        fm.setOrientation(LinearLayout.VERTICAL);
        EditText tiE = le(fm, "站点名称", s.optString("title", ""));
        EditText uE = le(fm, "站点链接（https://…）", s.optString("url", ""));
        EditText bE = le(fm, "角标文字（可留空）", s.optString("badge", ""));
        EditText btE = le(fm, "角标色 NEW/ROSE/GOLD/BLUE", s.optString("badgeType", "NEW"));
        new AlertDialog.Builder(this)
            .setTitle(idx < 0 ? "新增站点" : "编辑站点")
            .setView(fm)
            .setPositiveButton("保存", (d, w) -> {
                try {
                    String title = tiE.getText().toString().trim();
                    String url = uE.getText().toString().trim();
                    String badge = bE.getText().toString().trim();
                    if (idx < 0) {
                        JSONObject ns = new JSONObject();
                        ns.put("id", "site_" + System.currentTimeMillis());
                        ns.put("title", title);
                        ns.put("url", url);
                        ns.put("badge", badge.isEmpty() ? JSONObject.NULL : badge);
                        ns.put("badgeType", btE.getText().toString().trim().isEmpty() ? "NEW" : btE.getText().toString().trim());
                        ns.put("categoryId", catCache.get(selCat).optString("id", ""));
                        ns.put("subcatId", "all");
                        JSONArray cards = catCache.get(selCat).optJSONArray("cards");
                        if (cards == null) { cards = new JSONArray(); catCache.get(selCat).put("cards", cards); }
                        cards.put(ns);
                    } else {
                        s.put("title", title);
                        s.put("url", url);
                        s.put("badge", badge.isEmpty() ? JSONObject.NULL : badge);
                        s.put("badgeType", btE.getText().toString().trim().isEmpty() ? "NEW" : btE.getText().toString().trim());
                    }
                    collectCatFromCache();
                    renderHomeLists();
                    toast("站点已修改，点「⚡ 应用」同步");
                } catch (Exception e) { toast("保存失败: " + e.getMessage()); }
            })
            .setNegativeButton("取消", null)
            .show();
    }

    private void delSite() {
        if (selCat < 0 || selCat >= catCache.size() || selSite < 0 || selSite >= siteView.size()) {
            toast("请先在列表选择站点"); return;
        }
        confirm("删除站点「" + siteView.get(selSite).optString("title") + "」？", () -> {
            JSONObject victim = siteView.get(selSite);
            siteView.remove(selSite);
            siteCache.remove(victim);
            // 同步从 cards 数组中真实移除（保持与云端一致）
            JSONObject cat = catCache.get(selCat);
            JSONArray cards = cat.optJSONArray("cards");
            if (cards != null) {
                JSONArray keep = new JSONArray();
                for (int i = 0; i < cards.length(); i++) {
                    JSONObject c = cards.optJSONObject(i);
                    if (c != victim) keep.put(c);
                }
                cat.put("cards", keep);
            }
            collectCatFromCache();
            renderHomeLists();
            toast("站点已删除（点「⚡ 应用」生效）");
        });
    }

    /** 把缓存中的分类/站点写回 admin.home.categories */
    private void collectCatFromCache() {
        try {
            JSONArray cats = new JSONArray();
            for (JSONObject c : catCache) cats.put(c);
            JSONObject home = admin.optJSONObject("home");
            if (home == null) { home = new JSONObject(); admin.put("home", home); }
            home.put("categories", cats);
        } catch (Exception ignored) { }
    }

    // ============================================================
    // 首页进阶：子分类 / 上下移 / 一键收录 / 全库去重（旧版控制台交互迁移）
    // ============================================================

    private void dlgSubcat(final int idx) {
        if (selCat < 0 || selCat >= catCache.size()) { toast("请先选择上方分类"); return; }
        final JSONObject cat = catCache.get(selCat);
        JSONArray subs = cat.optJSONArray("subcategories");
        final List<JSONObject> list = new ArrayList<>();
        if (subs != null) {
            for (int i = 0; i < subs.length(); i++) {
                JSONObject s = subs.optJSONObject(i);
                if (s != null) list.add(s);
            }
        }
        final JSONObject s = (idx >= 0 && idx < list.size()) ? list.get(idx) : new JSONObject();
        LinearLayout fm = new LinearLayout(this);
        fm.setOrientation(LinearLayout.VERTICAL);
        EditText idE = le(fm, "子分类 ID（英文，如 all）", s.optString("id", idx < 0 ? "" : ""));
        EditText nmE = le(fm, "子分类名称", s.optString("name", ""));
        new AlertDialog.Builder(this)
            .setTitle(idx < 0 ? "＋ 新增子分类" : "✎ 编辑子分类")
            .setView(fm)
            .setPositiveButton("保存", (d, w) -> {
                try {
                    if (idx < 0) {
                        JSONObject ns = new JSONObject();
                        ns.put("id", idE.getText().toString().trim().isEmpty() ? "sub_" + System.currentTimeMillis() : idE.getText().toString().trim());
                        ns.put("name", nmE.getText().toString().trim());
                        JSONArray arr = cat.optJSONArray("subcategories");
                        if (arr == null) { arr = new JSONArray(); cat.put("subcategories", arr); }
                        arr.put(ns);
                    } else {
                        s.put("id", idE.getText().toString().trim());
                        s.put("name", nmE.getText().toString().trim());
                    }
                    collectCatFromCache();
                    renderHomeLists();
                    toast("子分类已保存，点「⚡ 应用」同步");
                } catch (Exception e) { toast("保存失败: " + e.getMessage()); }
            })
            .setNegativeButton("取消", null)
            .show();
    }

    private void moveCat(int dir) {
        if (selCat < 0 || selCat >= catCache.size()) { toast("请先选择分类"); return; }
        int to = selCat + dir;
        if (to < 0 || to >= catCache.size()) { toast("已在最" + (dir < 0 ? "上" : "下") + "边"); return; }
        JSONObject tmp = catCache.get(selCat);
        catCache.set(selCat, catCache.get(to));
        catCache.set(to, tmp);
        selCat = to;
        collectCatFromCache();
        renderHomeLists();
        toast("分类已移动，点「⚡ 应用」同步");
    }

    private void moveSite(int dir) {
        if (selCat < 0 || selSite < 0 || selSite >= siteView.size()) { toast("请先选择站点"); return; }
        int to = selSite + dir;
        if (to < 0 || to >= siteView.size()) { toast("已在最" + (dir < 0 ? "上" : "下") + "边"); return; }
        JSONObject tmp = siteView.get(selSite);
        siteView.set(selSite, siteView.get(to));
        siteView.set(to, tmp);
        selSite = to;
        // 同步到 cards 数组顺序
        JSONObject cat = catCache.get(selCat);
        JSONArray cards = cat.optJSONArray("cards");
        if (cards != null) {
            JSONArray re = new JSONArray();
            for (JSONObject s : siteView) re.put(s);
            cat.put("cards", re);
        }
        collectCatFromCache();
        renderHomeLists();
        toast("站点已移动，点「⚡ 应用」同步");
    }

    /** 一键收录：多行 URL → 自动分类 + 自动图标 + 去重（旧版 🌟 一键收录并同步本体） */
    private void dlgCollect() {
        if (admin == null) { toast("请先连接云端"); return; }
        LinearLayout fm = new LinearLayout(this);
        fm.setOrientation(LinearLayout.VERTICAL);
        final EditText urlsE = new EditText(this);
        urlsE.setHint("每行一个 URL，支持批量（如 https://chatgpt.com）");
        urlsE.setTextColor(Color.WHITE);
        urlsE.setHintTextColor(0xFF64748B);
        urlsE.setBackgroundColor(0xFF1E2438);
        urlsE.setGravity(Gravity.TOP);
        urlsE.setMinHeight(dp(140));
        urlsE.setTextSize(13);
        urlsE.setPadding(dp(8), dp(6), dp(8), dp(6));
        fm.addView(urlsE, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(150)));
        new AlertDialog.Builder(this)
            .setTitle("🌟 一键收录站点")
            .setMessage("自动识别分类（按关键词匹配）· 自动获取图标 · 自动去重")
            .setView(fm)
            .setPositiveButton("收录", (d, w) -> collectSites(urlsE.getText().toString()))
            .setNegativeButton("取消", null)
            .show();
    }

    // ============================================================
    // v2.0.4 增强：剪贴板 JSON 批量导入（支持 1000+ 站点库一键导入）
    // ============================================================
    private void dlgImportJson() {
        if (admin == null) { toast("请先连接云端"); return; }
        final android.content.ClipboardManager cm = (android.content.ClipboardManager)
                getSystemService(Context.CLIPBOARD_SERVICE);
        String clip = "";
        if (cm != null && cm.hasPrimaryClip() && cm.getPrimaryClip() != null
                && cm.getPrimaryClip().getItemCount() > 0) {
            clip = cm.getPrimaryClip().getItemAt(0).coerceToText(this).toString();
        }
        LinearLayout fm = new LinearLayout(this);
        fm.setOrientation(LinearLayout.VERTICAL);
        final EditText jsonE = new EditText(this);
        jsonE.setHint("粘贴站点 JSON 数组，如 [{title,url,desc,badge,subcatId},...]");
        jsonE.setTextColor(Color.WHITE);
        jsonE.setHintTextColor(0xFF64748B);
        jsonE.setBackgroundColor(0xFF1E2438);
        jsonE.setGravity(Gravity.TOP);
        jsonE.setMinHeight(dp(160));
        jsonE.setTextSize(12);
        jsonE.setPadding(dp(8), dp(6), dp(8), dp(6));
        if (!clip.isEmpty() && clip.contains("\"url\"")) jsonE.setText(clip);
        fm.addView(jsonE, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(180)));
        TextView info = new TextView(this);
        info.setText("导入到当前选中分类「" + (selCat >= 0 && selCat < catCache.size()
                ? catCache.get(selCat).optString("name", "") : "（请先选分类）") + "」，自动按 URL 去重");
        info.setTextSize(12);
        info.setTextColor(0xFF94A3B8);
        info.setPadding(0, dp(6), 0, 0);
        fm.addView(info);
        new AlertDialog.Builder(this)
            .setTitle("📥 批量导入站点 JSON")
            .setView(fm)
            .setPositiveButton("导入", (d, w) -> importSitesJson(jsonE.getText().toString()))
            .setNegativeButton("取消", null)
            .show();
    }

    /** 解析剪贴板 JSON 数组并合并到当前分类（URL 去重 + 只增不删） */
    private void importSitesJson(String raw) {
        raw = raw == null ? "" : raw.trim();
        if (raw.isEmpty()) { toast("内容为空"); return; }
        if (selCat < 0 || selCat >= catCache.size()) { toast("请先在首页选择目标分类"); return; }
        try {
            JSONArray arr = new JSONArray(raw);
            JSONObject cat = catCache.get(selCat);
            JSONArray cards = cat.optJSONArray("cards");
            if (cards == null) { cards = new JSONArray(); cat.put("cards", cards); }
            // 收集现有 URL（去重用）
            Set<String> exist = new HashSet<>();
            for (int i = 0; i < cards.length(); i++) {
                JSONObject c = cards.optJSONObject(i);
                if (c != null) exist.add(normUrl(c.optString("url", "")));
            }
            int added = 0, dup = 0, bad = 0;
            String catId = cat.optString("id", "");
            String subId = "all";
            for (int i = 0; i < arr.length(); i++) {
                try {
                    JSONObject o = arr.getJSONObject(i);
                    String title = o.optString("title", "").trim();
                    String url = o.optString("url", "").trim();
                    if (title.isEmpty() || url.isEmpty()) { bad++; continue; }
                    if (!url.startsWith("http")) url = "https://" + url;
                    String nk = normUrl(url);
                    if (exist.contains(nk)) { dup++; continue; }
                    JSONObject ns = new JSONObject();
                    ns.put("id", "site_imp_" + System.currentTimeMillis() + "_" + added);
                    ns.put("title", title);
                    ns.put("url", url);
                    ns.put("icon", o.optString("icon", ""));
                    ns.put("fallbackText", o.optString("fallbackText", title.length() > 2 ? title.substring(0, 2) : title));
                    ns.put("badge", o.optString("badge", ""));
                    ns.put("badgeType", o.optString("badgeType", "NEW"));
                    ns.put("desc", o.optString("desc", ""));
                    ns.put("categoryId", catId);
                    ns.put("subcatId", o.optString("subcatId", subId));
                    ns.put("highlights", o.optString("highlights", ""));
                    cards.put(ns);
                    exist.add(nk);
                    added++;
                } catch (Exception e) { bad++; }
            }
            collectCatFromCache();
            renderHomeLists();
            toast("导入完成：新增 " + added + "，跳过重复 " + dup + "，无效 " + bad + "（点「⚡ 应用」同步）");
        } catch (Exception e) {
            toast("JSON 解析失败：" + e.getMessage());
        }
    }


    private void collectSites(String raw) {
        String[] lines = raw.split("\n");
        int added = 0, dup = 0;
        try {
            for (String ln : lines) {
                String u = ln.trim();
                if (u.isEmpty()) continue;
                if (!u.startsWith("http")) u = "https://" + u;
                if (dupCheck(u)) { dup++; continue; }
                JSONObject ns = new JSONObject();
                String domain = domainOf(u);
                ns.put("id", "site_" + System.currentTimeMillis() + "_" + added);
                ns.put("title", domain);
                ns.put("url", u);
                ns.put("icon", "https://" + domain + "/favicon.ico");
                ns.put("fallbackText", domain.length() > 3 ? domain.substring(0, 3).toUpperCase() : domain.toUpperCase());
                ns.put("badge", "NEW");
                ns.put("badgeType", "NEW");
                ns.put("desc", "");
                ns.put("categoryId", "");
                ns.put("subcatId", "all");
                ns.put("highlights", "");
                // 自动分类：按 URL/域名关键词匹配现有分类
                JSONObject target = autoPickCategory(domain + " " + u);
                if (target == null) {
                    JSONArray cats = admin.optJSONObject("home").optJSONArray("categories");
                    target = (cats != null && cats.length() > 0) ? cats.optJSONObject(0) : null;
                }
                if (target != null) {
                    ns.put("categoryId", target.optString("id", ""));
                    JSONArray cards = target.optJSONArray("cards");
                    if (cards == null) { cards = new JSONArray(); target.put("cards", cards); }
                    cards.put(ns);
                } else {
                    toast("仓库暂无分类，请先新增分类");
                    return;
                }
                added++;
            }
            collectCatFromCache();
            renderHomeLists();
            toast("收录完成：新增 " + added + " 个，跳过重复 " + dup + " 个（点「⚡ 应用」同步）");
        } catch (Exception e) {
            toast("收录失败: " + e.getMessage());
        }
    }

    /** 按关键词自动匹配分类（收录时使用） */
    private JSONObject autoPickCategory(String keyword) {
        try {
            String k = keyword.toLowerCase(Locale.ROOT);
            JSONArray cats = admin.optJSONObject("home").optJSONArray("categories");
            if (cats == null) return null;
            JSONObject best = null;
            for (int i = 0; i < cats.length(); i++) {
                JSONObject c = cats.optJSONObject(i);
                if (c == null) continue;
                String name = (c.optString("name", "") + " " + c.optString("desc", "")).toLowerCase(Locale.ROOT);
                String[] keys = name.split("[\\s,，、;；|/]");
                for (String key : keys) {
                    if (key.length() >= 2 && k.contains(key)) { best = c; break; }
                }
                if (best != null) break;
            }
            return best;
        } catch (Exception e) { return null; }
    }

    private static String domainOf(String url) {
        try {
            java.net.URI uri = new java.net.URI(url);
            String host = uri.getHost();
            if (host == null) return "site";
            return host.replaceFirst("^(www\\.)", "");
        } catch (Exception e) { return "site"; }
    }

    /** 全库去重：扫描所有分类的站点，按 URL 去重（旧版 🔁 一键去重） */
    private void dedupAllSites() {
        if (admin == null) { toast("请先连接云端"); return; }
        try {
            JSONArray cats = admin.optJSONObject("home").optJSONArray("categories");
            if (cats == null) { toast("无分类"); return; }
            Set<String> seen = new HashSet<>();
            int removed = 0;
            for (int i = 0; i < cats.length(); i++) {
                JSONObject c = cats.optJSONObject(i);
                if (c == null) continue;
                JSONArray cards = c.optJSONArray("cards");
                if (cards == null) continue;
                JSONArray keep = new JSONArray();
                for (int j = 0; j < cards.length(); j++) {
                    JSONObject card = cards.optJSONObject(j);
                    if (card == null) continue;
                    String url = card.optString("url", "").trim();
                    if (url.isEmpty()) { keep.put(card); continue; }
                    if (seen.add(normUrl(url))) { keep.put(card); } else { removed++; }
                }
                c.put("cards", keep);
            }
            collectCatFromCache();
            renderHomeLists();
            toast("去重完成：移除重复站点 " + removed + " 个（点「⚡ 应用」同步）");
        } catch (Exception e) {
            toast("去重失败: " + e.getMessage());
        }
    }

    /** URL 规范化（去 https:// 前缀、www、尾斜杠、大小写） */
    private static String normUrl(String url) {
        String s = url.trim().toLowerCase(Locale.ROOT);
        s = s.replaceFirst("^https?://", "");
        s = s.replaceFirst("^www\\.", "");
        while (s.endsWith("/")) s = s.substring(0, s.length() - 1);
        return s;
    }

    private boolean dupCheck(String url) {
        String n = normUrl(url);
        JSONArray cats = admin.optJSONObject("home").optJSONArray("categories");
        if (cats == null) return false;
        for (int i = 0; i < cats.length(); i++) {
            JSONObject c = cats.optJSONObject(i);
            if (c == null) continue;
            JSONArray cards = c.optJSONArray("cards");
            if (cards == null) continue;
            for (int j = 0; j < cards.length(); j++) {
                JSONObject card = cards.optJSONObject(j);
                if (card != null && normUrl(card.optString("url", "")).equals(n)) return true;
            }
        }
        return false;
    }

    private JSONArray ensureCatArray() {
        try {
            JSONObject home = admin.optJSONObject("home");
            if (home == null) { home = new JSONObject(); admin.put("home", home); }
            JSONArray a = home.optJSONArray("categories");
            if (a == null) { a = new JSONArray(); home.put("categories", a); }
            return a;
        } catch (Exception e) { return new JSONArray(); }
    }

    // ============================================================
    // 软件库
    // ============================================================
    private ListView softLv;
    private ArrayAdapter<String> softAd;

    private void buildSoft(LinearLayout page) {
        page.addView(secTitle("软件库（本地上传 APK / 修改元数据）"));
        softLv = new ListView(this);
        softAd = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<String>());
        softLv.setAdapter(softAd);
        softLv.setBackgroundColor(0xFF1E2438);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(200));
        lp.topMargin = dp(6);
        softLv.setLayoutParams(lp);
        softLv.setOnItemClickListener((p, v, pos, id) -> { selSoft = pos; });
        page.addView(softLv);

        LinearLayout btns = row();
        addBtn(btns, "＋ 新增软件", v -> dlgSoft(-1));
        addBtn(btns, "✎ 编辑", v -> dlgSoft(selSoft));
        addBtn(btns, "－ 删除", v -> delSoft());
        addBtn(btns, "📦 上传APK并关联", v -> uploadApkForSoft());
        page.addView(btns);
    }

    private void renderSoftList() {
        if (admin == null) return;
        // 防闪退：软件页签尚未构建时 softAd 为 null，直接跳过（进入页面时会再渲染）
        if (softAd == null) return;
        softCache.clear();
        JSONArray arr = admin.optJSONArray("software");
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++) {
                JSONObject s = arr.optJSONObject(i);
                if (s != null) softCache.add(s);
            }
        }
        List<String> names = new ArrayList<>();
        for (JSONObject s : softCache) {
            names.add((s.optString("title", "")) + (s.optString("apkUrl", "").isEmpty() ? " [无APK]" : " [有APK]"));
        }
        softAd.clear(); softAd.addAll(names); softAd.notifyDataSetChanged();
    }

    private void dlgSoft(final int idx) {
        final JSONObject s = (idx >= 0 && idx < softCache.size()) ? softCache.get(idx) : new JSONObject();
        LinearLayout fm = new LinearLayout(this);
        fm.setOrientation(LinearLayout.VERTICAL);
        EditText tiE = le(fm, "软件名称", s.optString("title", ""));
        EditText dsE = le(fm, "描述", s.optString("desc", ""));
        EditText auE = le(fm, "作者", s.optString("author", ""));
        EditText bE = le(fm, "角标", s.optString("badge", ""));
        EditText tgE = le(fm, "标签", s.optString("tags", ""));
        new AlertDialog.Builder(this)
            .setTitle(idx < 0 ? "新增软件" : "编辑软件")
            .setView(fm)
            .setPositiveButton("保存", (d, w) -> {
                try {
                    if (idx < 0) {
                        JSONObject ns = new JSONObject();
                        ns.put("id", "sw_" + System.currentTimeMillis());
                        ns.put("type", "software");
                        ns.put("title", tiE.getText().toString().trim());
                        ns.put("desc", dsE.getText().toString().trim());
                        ns.put("author", auE.getText().toString().trim());
                        ns.put("badge", bE.getText().toString().trim());
                        ns.put("tags", tgE.getText().toString().trim());
                        ns.put("url", "");
                        ns.put("apkUrl", "");
                        JSONArray arr = admin.optJSONArray("software");
                        if (arr == null) { arr = new JSONArray(); admin.put("software", arr); }
                        arr.put(ns);
                        selSoft = arr.length() - 1;
                    } else {
                        s.put("title", tiE.getText().toString().trim());
                        s.put("desc", dsE.getText().toString().trim());
                        s.put("author", auE.getText().toString().trim());
                        s.put("badge", bE.getText().toString().trim());
                        s.put("tags", tgE.getText().toString().trim());
                    }
                    renderSoftList();
                    toast("软件已修改，点「⚡ 应用」同步");
                } catch (Exception e) { toast("保存失败: " + e.getMessage()); }
            })
            .setNegativeButton("取消", null)
            .show();
    }

    private void delSoft() {
        if (selSoft < 0 || selSoft >= softCache.size()) { toast("请先选择软件"); return; }
        confirm("删除软件「" + softCache.get(selSoft).optString("title") + "」？", () -> {
            softCache.remove(selSoft);
            syncSoftCache();
            renderSoftList();
            toast("已删除（点「⚡ 应用」生效）");
        });
    }

    private void syncSoftCache() {
        try {
            JSONArray arr = new JSONArray();
            for (JSONObject s : softCache) arr.put(s);
            admin.put("software", arr);
        } catch (Exception ignored) { }
    }

    private void uploadApkForSoft() {
        if (selSoft < 0 || selSoft >= softCache.size()) { toast("请先选择软件再上传 APK"); return; }
        pickFile(PICK_APK_FOR_SOFT);
    }

    // ============================================================
    // Skill
    // ============================================================
    private ListView skillLv;
    private ArrayAdapter<String> skillAd;

    private void buildSkill(LinearLayout page) {
        page.addView(secTitle("Skill 技能库 / Prompt"));
        skillLv = new ListView(this);
        skillAd = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<String>());
        skillLv.setAdapter(skillAd);
        skillLv.setBackgroundColor(0xFF1E2438);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(200));
        lp.topMargin = dp(6);
        skillLv.setLayoutParams(lp);
        skillLv.setOnItemClickListener((p, v, pos, id) -> { selSkill = pos; });
        page.addView(skillLv);

        LinearLayout btns = row();
        addBtn(btns, "＋ 新增 Skill", v -> dlgSkill(-1));
        addBtn(btns, "✎ 编辑", v -> dlgSkill(selSkill));
        addBtn(btns, "－ 删除", v -> delSkill());
        page.addView(btns);
    }

    private void renderSkillList() {
        if (admin == null) return;
        // 防闪退：Skill 页签尚未构建时 skillAd 为 null，直接跳过（进入页面时会再渲染）
        if (skillAd == null) return;
        skillCache.clear();
        JSONArray arr = admin.optJSONArray("skills");
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++) {
                JSONObject s = arr.optJSONObject(i);
                if (s != null) skillCache.add(s);
            }
        }
        List<String> names = new ArrayList<>();
        for (JSONObject s : skillCache) names.add(s.optString("title", ""));
        skillAd.clear(); skillAd.addAll(names); skillAd.notifyDataSetChanged();
    }

    private void dlgSkill(final int idx) {
        final JSONObject s = (idx >= 0 && idx < skillCache.size()) ? skillCache.get(idx) : new JSONObject();
        LinearLayout fm = new LinearLayout(this);
        fm.setOrientation(LinearLayout.VERTICAL);
        EditText tiE = le(fm, "标题", s.optString("title", ""));
        EditText dsE = le(fm, "简介", s.optString("desc", ""));
        EditText auE = le(fm, "作者", s.optString("author", ""));
        EditText bE = le(fm, "角标", s.optString("badge", ""));
        EditText tgE = le(fm, "标签", s.optString("tags", ""));
        EditText pmE = le(fm, "Prompt 提示词", s.optString("prompt", ""));
        new AlertDialog.Builder(this)
            .setTitle(idx < 0 ? "新增 Skill" : "编辑 Skill")
            .setView(fm)
            .setPositiveButton("保存", (d, w) -> {
                try {
                    if (idx < 0) {
                        JSONObject ns = new JSONObject();
                        ns.put("id", "sk_" + System.currentTimeMillis());
                        ns.put("type", "skill");
                        ns.put("promptType", "skill");
                        ns.put("title", tiE.getText().toString().trim());
                        ns.put("desc", dsE.getText().toString().trim());
                        ns.put("author", auE.getText().toString().trim());
                        ns.put("badge", bE.getText().toString().trim());
                        ns.put("tags", tgE.getText().toString().trim());
                        ns.put("prompt", pmE.getText().toString());
                        ns.put("url", "");
                        ns.put("previewUrl", "");
                        ns.put("mediaUrl", "");
                        JSONArray arr = admin.optJSONArray("skills");
                        if (arr == null) { arr = new JSONArray(); admin.put("skills", arr); }
                        arr.put(ns);
                        selSkill = arr.length() - 1;
                    } else {
                        s.put("title", tiE.getText().toString().trim());
                        s.put("desc", dsE.getText().toString().trim());
                        s.put("author", auE.getText().toString().trim());
                        s.put("badge", bE.getText().toString().trim());
                        s.put("tags", tgE.getText().toString().trim());
                        s.put("prompt", pmE.getText().toString());
                    }
                    renderSkillList();
                    toast("Skill 已修改，点「⚡ 应用」同步");
                } catch (Exception e) { toast("保存失败: " + e.getMessage()); }
            })
            .setNegativeButton("取消", null)
            .show();
    }

    private void delSkill() {
        if (selSkill < 0 || selSkill >= skillCache.size()) { toast("请先选择 Skill"); return; }
        confirm("删除 Skill「" + skillCache.get(selSkill).optString("title") + "」？", () -> {
            skillCache.remove(selSkill);
            syncSkillCache();
            renderSkillList();
            toast("已删除（点「⚡ 应用」生效）");
        });
    }

    private void syncSkillCache() {
        try {
            JSONArray arr = new JSONArray();
            for (JSONObject s : skillCache) arr.put(s);
            admin.put("skills", arr);
        } catch (Exception ignored) { }
    }

    // ============================================================
    // 设置页（表单）
    // ============================================================
    private void buildSettings(LinearLayout page) {
        page.addView(secTitle("App 基础设置"));
        List<FieldSpec> specs = new ArrayList<>();
        specs.add(new FieldSpec("App 名称", T_EDIT, null, "settings.appName", false));
        specs.add(new FieldSpec("Slogan", T_EDIT, null, "settings.slogan", false));
        specs.add(new FieldSpec("包名", T_EDIT, null, "settings.packageName", false));
        specs.add(new FieldSpec("官网", T_EDIT, null, "settings.officialWebsite", false));
        specs.add(new FieldSpec("反馈邮箱", T_EDIT, null, "settings.feedbackEmail", false));
        specs.add(new FieldSpec("QQ 群链接", T_EDIT, null, "settings.qqGroupUrl", false));
        specs.add(new FieldSpec("QQ 群号", T_EDIT, null, "settings.qqGroupUin", false));
        specs.add(new FieldSpec("微信二维码URL", T_EDIT, null, "settings.contactWechat", false));
        specs.add(new FieldSpec("QQ 二维码URL", T_EDIT, null, "settings.contactQQ", false));
        specs.add(new FieldSpec("支付宝二维码URL", T_EDIT, null, "settings.contactAlipay", false));
        specs.add(new FieldSpec("关于我们", T_TEXT, null, "settings.aboutText", false));
        specs.add(new FieldSpec("品牌 Logo URL", T_EDIT, null, "settings.logoUrl", false));
        specs.add(new FieldSpec("自定义主题 CSS", T_TEXT, null, "settings.customThemeCss", false));
        specs.add(new FieldSpec("自定义主题 HTML", T_TEXT, null, "settings.customThemeHtml", false));
        specs.add(new FieldSpec("安全加固-开启", T_CHECK, null, "settings.security.enabled", false));
        specs.add(new FieldSpec("安全加固-期望签名SHA", T_TEXT, null, "settings.security.expectedSha", false));
        List<Field> fs = buildFields(page, specs);
        pageFields.put("settings", fs);
    }

    // ============================================================
    // 更新弹窗页（表单）
    // ============================================================
    private void buildUpdate(LinearLayout page) {
        page.addView(secTitle("更新弹窗（影响本体 App 更新提示）"));
        List<FieldSpec> specs = new ArrayList<>();
        specs.add(new FieldSpec("版本名 versionName", T_EDIT, null, "version.name", false));
        specs.add(new FieldSpec("版本码 versionCode", T_EDIT, null, "version.code", true));
        specs.add(new FieldSpec("最新 APK 直链", T_EDIT, null, "version.apkUrl", false));
        specs.add(new FieldSpec("强制更新", T_CHECK, null, "version.force", false));
        specs.add(new FieldSpec("弹窗标题", T_EDIT, null, "updateDialog.title", false));
        specs.add(new FieldSpec("确认按钮文字", T_EDIT, null, "updateDialog.confirmText", false));
        specs.add(new FieldSpec("取消按钮文字", T_EDIT, null, "updateDialog.cancelText", false));
        specs.add(new FieldSpec("更新日志（每行一条）", T_TEXT, null, "updateDialog.changelog", false));
        specs.add(new FieldSpec("自定义 CSS", T_TEXT, null, "updateDialog.customCss", false));
        specs.add(new FieldSpec("自定义 HTML/JS", T_TEXT, null, "updateDialog.customHtml", false));
        List<Field> fs = buildFields(page, specs);
        pageFields.put("update", fs);
    }

    // ============================================================
    // 其他（开屏/欢迎/跑马灯）
    // ============================================================
    private void buildOther(LinearLayout page) {
        page.addView(secTitle("开屏动画"));
        List<FieldSpec> specs = new ArrayList<>();
        specs.add(new FieldSpec("模式", T_SPIN, new String[]{"default", "html", "media"}, "splash.type", false));
        specs.add(new FieldSpec("展示时长(秒)", T_EDIT, null, "splash.durationSeconds", true));
        specs.add(new FieldSpec("背景色", T_EDIT, null, "splash.bgColor", false));
        specs.add(new FieldSpec("媒体 URL", T_EDIT, null, "splash.mediaUrl", false));
        specs.add(new FieldSpec("自定义 HTML/JS 代码", T_TEXT, null, "splash.customHtml", false));
        List<Field> fs = buildFields(page, specs);
        fs.addAll(buildFields(page, new ArrayList<FieldSpec>() {{
            add(new FieldSpec("—— 欢迎弹窗 ——", T_EDIT, null, "welcome._section_flag_", false));
        }}));
        // 欢迎弹窗字段
        List<FieldSpec> wspecs = new ArrayList<>();
        wspecs.add(new FieldSpec("启用", T_CHECK, null, "welcome.enabled", false));
        wspecs.add(new FieldSpec("主标题", T_EDIT, null, "welcome.title", false));
        wspecs.add(new FieldSpec("欢迎语", T_EDIT, null, "welcome.welcomeText", false));
        wspecs.add(new FieldSpec("按钮文字", T_EDIT, null, "welcome.buttonText", false));
        wspecs.add(new FieldSpec("内容（每行一条）", T_TEXT, null, "welcome.content", false));
        wspecs.add(new FieldSpec("配图 URL", T_EDIT, null, "welcome.imageUrl", false));
        fs.addAll(buildFields(page, wspecs));
        fs.addAll(buildFields(page, new ArrayList<FieldSpec>() {{
            add(new FieldSpec("—— 跑马灯 ——", T_EDIT, null, "marquee._section_flag_", false));
        }}));
        List<FieldSpec> mspecs = new ArrayList<>();
        mspecs.add(new FieldSpec("启用", T_CHECK, null, "marquee.enabled", false));
        mspecs.add(new FieldSpec("图标", T_EDIT, null, "marquee.icon", false));
        mspecs.add(new FieldSpec("默认公告", T_TEXT, null, "marquee.defaultText", false));
        fs.addAll(buildFields(page, mspecs));
        fs.addAll(buildFields(page, new ArrayList<FieldSpec>() {{
            add(new FieldSpec("—— 跑马灯时间段（24小时轮播）——", T_EDIT, null, "marquee._section_flag_", false));
        }}));
        // 时间段管理按钮：打开对话框逐条编辑 segments
        LinearLayout segRow = row();
        addBtn(segRow, "⏰ 管理时间段", v -> dlgMarqueeSegments());
        page.addView(segRow);
        fs.addAll(buildFields(page, new ArrayList<FieldSpec>() {{
            add(new FieldSpec("—— IP 定位监控 ——", T_EDIT, null, "ipMonitor._section_flag_", false));
        }}));
        List<FieldSpec> ispecs = new ArrayList<>();
        ispecs.add(new FieldSpec("启用", T_CHECK, null, "ipMonitor.enabled", false));
        ispecs.add(new FieldSpec("定位 URL", T_EDIT, null, "ipMonitor.url", false));
        fs.addAll(buildFields(page, ispecs));
        pageFields.put("other", fs);
    }

    // ============================================================
    // 跑马灯时间段管理（旧版交互：24小时北京时间轮播）
    // ============================================================
    private void dlgMarqueeSegments() {
        if (admin == null) { toast("请先连接云端"); return; }
        final JSONObject mq = admin.optJSONObject("marquee");
        if (mq == null) { toast("云端暂无 marquee 配置"); return; }
        JSONArray segs = mq.optJSONArray("segments");
        final List<JSONObject> list = new ArrayList<>();
        if (segs != null) {
            for (int i = 0; i < segs.length(); i++) {
                JSONObject s = segs.optJSONObject(i);
                if (s != null) list.add(s);
            }
        }
        LinearLayout fm = new LinearLayout(this);
        fm.setOrientation(LinearLayout.VERTICAL);
        TextView tip = new TextView(this);
        tip.setText("当前 " + list.size() + " 个时间段。\n每行格式：开始小时-结束小时|文案（如 8-12|早安公告）");
        tip.setTextSize(12);
        tip.setTextColor(0xFF94A3B8);
        fm.addView(tip);
        final EditText segE = new EditText(this);
        segE.setTextColor(Color.WHITE);
        segE.setHintTextColor(0xFF64748B);
        segE.setBackgroundColor(0xFF1E2438);
        segE.setGravity(Gravity.TOP);
        segE.setMinHeight(dp(140));
        segE.setTextSize(13);
        segE.setPadding(dp(8), dp(6), dp(8), dp(6));
        StringBuilder sb = new StringBuilder();
        for (JSONObject s : list) {
            sb.append(s.optInt("start", 0)).append("-").append(s.optInt("end", 23))
              .append("|").append(s.optString("text", "")).append("\n");
        }
        segE.setText(sb.toString());
        fm.addView(segE, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(160)));
        new AlertDialog.Builder(this)
            .setTitle("⏰ 跑马灯时间段管理")
            .setView(fm)
            .setPositiveButton("保存", (d, w) -> {
                try {
                    JSONArray out = new JSONArray();
                    String[] lines = segE.getText().toString().split("\n");
                    for (String ln : lines) {
                        String t = ln.trim();
                        if (t.isEmpty()) continue;
                        int bar = t.indexOf('|');
                        String range = bar >= 0 ? t.substring(0, bar).trim() : t.trim();
                        String text = bar >= 0 ? t.substring(bar + 1).trim() : "";
                        int dash = range.indexOf('-');
                        int start = 0, end = 23;
                        try {
                            if (dash > 0) {
                                start = Integer.parseInt(range.substring(0, dash).trim());
                                end = Integer.parseInt(range.substring(dash + 1).trim());
                            } else {
                                start = Integer.parseInt(range.trim());
                                end = start;
                            }
                        } catch (Exception ignored) { }
                        JSONObject seg = new JSONObject();
                        seg.put("start", Math.max(0, Math.min(start, 23)));
                        seg.put("end", Math.max(0, Math.min(end, 23)));
                        seg.put("text", text);
                        out.put(seg);
                    }
                    mq.put("segments", out);
                    admin.put("marquee", mq);
                    toast("时间段已保存（点「⚡ 应用」同步）");
                } catch (Exception e) { toast("保存失败: " + e.getMessage()); }
            })
            .setNegativeButton("取消", null)
            .show();
    }

    // ============================================================
    // APK 仓库
    // ============================================================
    private ListView apkLv;
    private ArrayAdapter<String> apkAd;
    private static final int PICK_APK_REPO = 100, PICK_APK_FOR_SOFT = 101;

    private void buildApk(LinearLayout page) {
        page.addView(secTitle("dist/apk 仓库 (本体安装包)"));
        apkLv = new ListView(this);
        apkAd = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<String>());
        apkLv.setAdapter(apkAd);
        apkLv.setBackgroundColor(0xFF1E2438);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(240));
        lp.topMargin = dp(6);
        apkLv.setLayoutParams(lp);
        apkLv.setOnItemClickListener((p, v, pos, id) -> { selApk = pos; });
        page.addView(apkLv);

        LinearLayout btns = row();
        addBtn(btns, "🔄 刷新", v -> loadApkRepo(true));
        addBtn(btns, "📦 上传APK", v -> pickFile(PICK_APK_REPO));
        addBtn(btns, "🚀 设为最新并发布", v -> setLatestApk());
        addBtn(btns, "🧹 清理旧包", v -> cleanupApks());
        page.addView(btns);
        if (apkCache.isEmpty()) loadApkRepo(false);
    }

    private void renderApkList() {
        List<String> names = new ArrayList<>();
        for (JSONObject a : apkCache) {
            names.add(a.optString("name", "") + "  (" + fmtSize(a.optLong("size", 0)) + ")");
        }
        apkAd.clear(); apkAd.addAll(names); apkAd.notifyDataSetChanged();
    }

    private static String fmtSize(long b) {
        return String.format(Locale.CHINA, "%.2f MB", b / 1048576.0);
    }

    // ============================================================
    // 统一收集（应用/发布前）—— 桌面端同款逻辑
    // ============================================================
    private void collectAll() {
        if (admin == null) return;
        try {
            collectFields(pageFields.get("settings"), admin);
            collectFields(pageFields.get("update"), admin);
            collectFields(pageFields.get("other"), admin);
            collectCatFromCache();
            syncSoftCache();
            syncSkillCache();
        } catch (Exception e) {
            log("收集表单出错: " + e);
        }
    }

    // ============================================================
    // 表单框架：构建 / 填充 / 收集
    // ============================================================
    private List<Field> buildFields(LinearLayout parent, List<FieldSpec> specs) {
        List<Field> out = new ArrayList<>();
        for (FieldSpec sp : specs) {
            if (sp.path.endsWith("_section_flag_")) {
                TextView sec = new TextView(this);
                sec.setText(sp.label);
                sec.setTextSize(13);
                sec.setTextColor(0xFFA5B4FC);
                sec.setTypeface(null, Typeface.BOLD);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.topMargin = dp(12);
                sec.setLayoutParams(lp);
                parent.addView(sec);
                continue;
            }
            Field f = new Field(sp);
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.VERTICAL);
            TextView lab = new TextView(this);
            lab.setText(sp.label);
            lab.setTextSize(12);
            lab.setTextColor(0xFF94A3B8);
            row.addView(lab);

            switch (sp.type) {
                case T_CHECK: {
                    CheckBox cb = new CheckBox(this);
                    cb.setText("是 / 启用");
                    cb.setTextColor(Color.WHITE);
                    cb.setTextSize(14);
                    f.cb = cb; f.view = cb;
                    row.addView(cb);
                    break;
                }
                case T_SPIN: {
                    Spinner spn = new Spinner(this);
                    ArrayAdapter<String> ad = new ArrayAdapter<>(this,
                            android.R.layout.simple_spinner_item, sp.opts != null ? sp.opts : new String[]{""});
                    ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spn.setAdapter(ad);
                    f.sp = spn; f.view = spn;
                    row.addView(spn);
                    break;
                }
                default: {
                    EditText et = new EditText(this);
                    et.setTextColor(Color.WHITE);
                    et.setHintTextColor(0xFF475569);
                    et.setTextSize(13);
                    et.setBackgroundColor(0xFF1E2438);
                    et.setPadding(dp(8), dp(6), dp(8), dp(6));
                    et.setSingleLine(sp.type == T_EDIT);
                    f.et = et; f.view = et;
                    row.addView(et);
                    break;
                }
            }
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.topMargin = dp(6);
            parent.addView(row, lp);
            out.add(f);
        }
        return out;
    }

    private void renderFields(List<Field> fields, JSONObject admin2, String tag) {
        if (fields == null || admin2 == null) return;
        for (Field f : fields) {
            Object v = getPath(admin2, f.spec.path);
            switch (f.spec.type) {
                case T_CHECK:
                    f.cb.setChecked(v instanceof Boolean && (Boolean) v);
                    break;
                case T_SPIN: {
                    String sv = v == null ? "" : String.valueOf(v);
                    int idx = 0;
                    if (f.spec.opts != null) {
                        for (int i = 0; i < f.spec.opts.length; i++) {
                            if (f.spec.opts[i].equals(sv)) { idx = i; break; }
                        }
                    }
                    f.sp.setSelection(idx);
                    break;
                }
                default:
                    f.et.setText(v == null ? "" : (v instanceof JSONArray ? joinLines((JSONArray) v) : String.valueOf(v)));
            }
        }
    }

    private void collectFields(List<Field> fields, JSONObject admin2) {
        if (fields == null || admin2 == null) return;
        for (Field f : fields) {
            try {
                switch (f.spec.type) {
                    case T_CHECK:
                        setPath(admin2, f.spec.path, f.cb.isChecked());
                        break;
                    case T_SPIN:
                        setPath(admin2, f.spec.path,
                                f.sp.getSelectedItem() == null ? "" : f.sp.getSelectedItem().toString());
                        break;
                    default: {
                        String s = f.et.getText().toString();
                        if (f.spec.path.endsWith(".changelog")) {
                            JSONArray arr = new JSONArray();
                            if (!s.trim().isEmpty()) {
                                String[] lines = s.split("\n");
                                for (String ln : lines) {
                                    String t = ln.trim();
                                    if (!t.isEmpty()) arr.put(t);
                                }
                            }
                            setPath(admin2, f.spec.path, arr);
                        } else if (f.spec.numeric) {
                            try { setPath(admin2, f.spec.path, Integer.parseInt(s.trim())); }
                            catch (Exception e) { setPath(admin2, f.spec.path, JSONObject.NULL); }
                        } else {
                            setPath(admin2, f.spec.path, s);
                        }
                    }
                }
            } catch (Exception ignored) { }
        }
    }

    private String joinLines(JSONArray arr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length(); i++) {
            if (i > 0) sb.append("\n");
            sb.append(arr.optString(i));
        }
        return sb.toString();
    }

    // ============================================================
    // JSON 路径工具（支持 对象键 / 数组下标）
    // ============================================================
    static Object getPath(JSONObject root, String path) {
        try {
            String[] parts = path.split("\\.");
            Object cur = root;
            for (String p : parts) {
                if (cur instanceof JSONObject) cur = ((JSONObject) cur).opt(p);
                else if (cur instanceof JSONArray) cur = ((JSONArray) cur).opt(Integer.parseInt(p));
            }
            return cur;
        } catch (Exception e) { return null; }
    }

    static void setPath(JSONObject root, String path, Object val) {
        try {
            String[] parts = path.split("\\.");
            Object cur = root;
            for (int i = 0; i < parts.length - 1; i++) {
                String p = parts[i];
                Object next;
                if (cur instanceof JSONObject) {
                    next = ((JSONObject) cur).opt(p);
                    if (!(next instanceof JSONObject) && !(next instanceof JSONArray)) {
                        next = new JSONObject();
                        ((JSONObject) cur).put(p, next);
                    }
                } else if (cur instanceof JSONArray) {
                    next = ((JSONArray) cur).opt(Integer.parseInt(p));
                    if (next == null) { next = new JSONObject(); ((JSONArray) cur).put(Integer.parseInt(p), next); }
                } else {
                    return;
                }
                cur = next;
            }
            String last = parts[parts.length - 1];
            if (cur instanceof JSONObject) {
                ((JSONObject) cur).put(last, val == null ? JSONObject.NULL : val);
            } else if (cur instanceof JSONArray) {
                int idx = Integer.parseInt(last);
                if (idx < ((JSONArray) cur).length()) ((JSONArray) cur).put(idx, val == null ? JSONObject.NULL : val);
                else ((JSONArray) cur).put(idx, val == null ? JSONObject.NULL : val);
            }
        } catch (Exception ignored) { }
    }

    private void ensureDefaults() {
        if (admin == null) return;
        try {
            String[][] dp = {
                {"version", "{}"}, {"home", "{}"}, {"settings", "{}"}, {"splash", "{}"},
                {"welcome", "{}"}, {"updateDialog", "{}"}, {"marquee", "{}"}, {"console", "{}"}
            };
            for (String[] kv : dp) {
                if (!admin.has(kv[0])) admin.put(kv[0], new JSONObject());
            }
            if (!admin.has("software") || !(admin.opt("software") instanceof JSONArray)) admin.put("software", new JSONArray());
            if (!admin.has("skills") || !(admin.opt("skills") instanceof JSONArray)) admin.put("skills", new JSONArray());
            if (!admin.optJSONObject("home").has("categories")) admin.optJSONObject("home").put("categories", new JSONArray());
        } catch (Exception ignored) { }
    }

    private static JSONArray arr(Object... items) {
        JSONArray a = new JSONArray();
        for (Object o : items) a.put(o);
        return a;
    }

    // ============================================================
    // 网络层：HTTP GET / PUT / DELETE（线程内调用）
    // ============================================================
    private String httpGet(String url, boolean auth, int timeoutMs) {
        HttpURLConnection c = null;
        try {
            c = (HttpURLConnection) new URL(url).openConnection();
            c.setRequestMethod("GET");
            c.setConnectTimeout(timeoutMs);
            c.setReadTimeout(timeoutMs);
            c.setInstanceFollowRedirects(true);
            c.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android) YuntaiConsole/2.0");
            c.setRequestProperty("Accept", "application/vnd.github+json");
            if (auth && !token.isEmpty())
                c.setRequestProperty("Authorization", "token " + token);
            int code = c.getResponseCode();
            if (code < 200 || code >= 300) return null;
            InputStream in = c.getInputStream();
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) bo.write(buf, 0, n);
            in.close();
            return bo.toString("UTF-8");
        } catch (Exception e) {
            return null;
        } finally {
            if (c != null) c.disconnect();
        }
    }

    private String httpPut(String url, String jsonBody, int timeoutMs) {
        HttpURLConnection c = null;
        try {
            c = (HttpURLConnection) new URL(url).openConnection();
            c.setRequestMethod("PUT");
            c.setConnectTimeout(timeoutMs);
            c.setReadTimeout(timeoutMs);
            c.setDoOutput(true);
            c.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android) YuntaiConsole/2.0");
            c.setRequestProperty("Accept", "application/vnd.github+json");
            c.setRequestProperty("Content-Type", "application/json");
            if (!token.isEmpty()) c.setRequestProperty("Authorization", "token " + token);
            byte[] body = jsonBody.getBytes("UTF-8");
            c.getOutputStream().write(body);
            c.getOutputStream().flush();
            c.getOutputStream().close();
            int code = c.getResponseCode();
            InputStream in = code < 200 || code >= 300 ? c.getErrorStream() : c.getInputStream();
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            if (in != null) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) > 0) bo.write(buf, 0, n);
                in.close();
            }
            String resp = bo.toString("UTF-8");
            if (code < 200 || code >= 300) throw new RuntimeException("HTTP " + code + " " + (resp.length() > 200 ? resp.substring(0, 200) : resp));
            return resp;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            if (c != null) c.disconnect();
        }
    }

    private String httpDelete(String url, String jsonBody, int timeoutMs) {
        HttpURLConnection c = null;
        try {
            c = (HttpURLConnection) new URL(url).openConnection();
            c.setRequestMethod("DELETE");
            c.setConnectTimeout(timeoutMs);
            c.setReadTimeout(timeoutMs);
            c.setDoOutput(true);
            c.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android) YuntaiConsole/2.0");
            c.setRequestProperty("Accept", "application/vnd.github+json");
            c.setRequestProperty("Content-Type", "application/json");
            if (!token.isEmpty()) c.setRequestProperty("Authorization", "token " + token);
            if (jsonBody != null) {
                byte[] body = jsonBody.getBytes("UTF-8");
                c.getOutputStream().write(body);
                c.getOutputStream().close();
            }
            int code = c.getResponseCode();
            InputStream in = code < 200 || code >= 300 ? c.getErrorStream() : c.getInputStream();
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            if (in != null) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) > 0) bo.write(buf, 0, n);
                in.close();
            }
            return bo.toString("UTF-8");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            if (c != null) c.disconnect();
        }
    }

    private static boolean looksLikeHtml(String t) {
        if (t == null || t.trim().isEmpty()) return false;
        String s = t.trim();
        return s.startsWith("<") && !s.startsWith("<{");
    }

    /** 读取 admin-data.json：API 优先，失败走 8 通道镜像；返回是否成功 */
    private boolean readConfig() {
        // 1) API
        String apiUrl = API_BASE + "/repos/" + OWNER + "/" + REPO + "/contents/" + CONFIG;
        String body = httpGet(apiUrl, true, 20000);
        if (body != null) {
            try {
                JSONObject d = new JSONObject(body);
                if (d.has("content")) {
                    byte[] dec = Base64.decode(d.optString("content").replaceAll("\\s", ""), Base64.DEFAULT);
                    String txt = new String(dec, "UTF-8");
                    if (!looksLikeHtml(txt)) {
                        admin = new JSONObject(txt);
                        ensureDefaults();
                        sha = d.optString("sha", "");
                        source = "GitHub API";
                        return true;
                    }
                }
            } catch (Exception e) { log("API 解析失败: " + e); }
        }
        // 2) 镜像链
        for (String[] m : MIRRORS) {
            String url = String.format(Locale.US, m[1], OWNER, REPO, CONFIG);
            String b = httpGet(url, false, 12000);
            if (b != null && !looksLikeHtml(b)) {
                try {
                    admin = new JSONObject(b);
                    ensureDefaults();
                    sha = "";
                    source = m[0] + " (只读)";
                    return true;
                } catch (Exception ignored) { }
            }
        }
        return false;
    }

    /** 保存 admin-data.json（必须带 Token） */
    private void saveConfig(String msg) {
        try {
            String json = admin.toString(2);
            String b64 = Base64.encodeToString(json.getBytes("UTF-8"), Base64.NO_WRAP);
            JSONObject payload = new JSONObject();
            payload.put("message", msg);
            payload.put("content", b64);
            if (!sha.isEmpty()) payload.put("sha", sha);
            String apiUrl = API_BASE + "/repos/" + OWNER + "/" + REPO + "/contents/" + CONFIG;
            String resp = httpPut(apiUrl, payload.toString(), 60000);
            JSONObject rd = new JSONObject(resp);
            JSONObject cc = rd.optJSONObject("content");
            if (cc != null) sha = cc.optString("sha", sha);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    // ============================================================
    // 连接 / 应用 / 发布 / 诊断
    // ============================================================
    private void doConnect(boolean silent) {
        token = tokenEt.getText().toString().trim();
        if (!silent) statusBar.setText("● 连接中…");
        new Thread(() -> {
            boolean ok;
            try {
                ok = readConfig();
            } catch (Exception e) {
                log("readConfig 异常: " + e);
                ok = false;
            }
            final boolean finalOk = ok;
            runOnUiThread(() -> {
                try {
                    if (finalOk) {
                        // 防闪退：确保当前页签已构建（首次连接时页面可能还没创建）
                        if (!pages.containsKey(currentTab)) buildPage(currentTab);
                        statusBar.setText("● 已连接 " + source + " ｜ v" + admin.optJSONObject("version").optString("name", "?")
                                + " (code " + admin.optJSONObject("version").optString("code", "?") + ")");
                        if (sha.isEmpty() && !token.isEmpty()) statusBar.append("（只读镜像，写入需 API 可达）");
                        if (sha.isEmpty() && token.isEmpty()) statusBar.append("（未填 Token，只读）");
                        // 只渲染当前页签（其余页签进入时再渲染），避免未构建页签的控件为 null
                        renderCachedPage(currentTab);
                        // 控制台自更新检查（与本体软件更新完全分离）
                        checkConsoleUpdate();
                    } else {
                        statusBar.setText("● 连接失败");
                        toast("连接失败：API 与全部镜像均不可达。\n请检查网络 / Token / 仓库名，或用「🔍 诊断」定位问题");
                    }
                } catch (Exception e) {
                    log("连接回调异常: " + e);
                    statusBar.setText("● 连接完成（有提示见下）");
                    toast("连接处理异常：" + e.getMessage());
                }
            });
        }).start();
    }

    private void doApply() {
        if (admin == null) { toast("请先连接云端"); return; }
        if (token.isEmpty()) { toast("当前为只读模式：请在顶部填入 Token 后点「连接」"); return; }
        collectAll();
        confirm("确定将当前所有页签的内容保存到云端并实时同步到本体？\n（不弹更新窗，本体数秒内无感生效）", () -> {
            statusBar.setText("● 正在应用…");
            new Thread(() -> {
                try {
                    saveConfig("console: 应用并实时同步 " + now());
                    runOnUiThread(() -> {
                        statusBar.setText("● ✅ 应用成功！已同步到云端");
                        toast("应用成功！本体软件将数秒内生效");
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> { statusBar.setText("● 应用失败"); toast("应用失败：" + e.getMessage()); });
                }
            }).start();
        });
    }

    private void doPublish() {
        if (admin == null) { toast("请先连接云端"); return; }
        if (token.isEmpty()) { toast("当前为只读模式：请填入 Token"); return; }
        collectAll();
        LinearLayout fm = new LinearLayout(this);
        fm.setOrientation(LinearLayout.VERTICAL);
        EditText noteE = new EditText(this);
        noteE.setHint("本次更新说明（写入更新日志，可留空）");
        noteE.setTextColor(Color.WHITE);
        noteE.setHintTextColor(0xFF64748B);
        noteE.setBackgroundColor(0xFF1E2438);
        noteE.setPadding(dp(8), dp(6), dp(8), dp(6));
        fm.addView(noteE);
        TextView info = new TextView(this);
        info.setText("版本码将自动 +1，本体 App 下次轮询即弹更新窗");
        info.setTextSize(12);
        info.setTextColor(0xFF94A3B8);
        info.setPadding(0, dp(6), 0, 0);
        fm.addView(info);
        new AlertDialog.Builder(this)
            .setTitle("🚀 发布新版本")
            .setView(fm)
            .setPositiveButton("确认发布", (d, w) -> doBumpPublish(noteE.getText().toString().trim()))
            .setNegativeButton("取消", null)
            .show();
    }

    private void doBumpPublish(String note) {
        try {
            JSONObject v = admin.optJSONObject("version");
            int code = v.optInt("code", 1) + 1;
            v.put("code", code);
            String name = v.optString("name", "1.0");
            String[] parts = name.split("\\.");
            if (parts.length > 1) {
                parts[parts.length - 1] = String.valueOf(Integer.parseInt(parts[parts.length - 1]) + 1);
                v.put("name", String.join(".", parts));
            } else v.put("name", name + ".1");
            // 更新日志
            String line = note.isEmpty() ? "功能与体验优化升级" : note;
            JSONArray cl = v.optJSONArray("changelog");
            JSONArray nl = new JSONArray();
            nl.put(line);
            if (cl != null) for (int i = 0; i < Math.min(cl.length(), 11); i++) {
                String x = cl.optString(i);
                if (meaningful(x)) nl.put(x);
            }
            v.put("changelog", nl);
            // updateDialog.changelog
            JSONObject ud = admin.optJSONObject("updateDialog");
            if (ud != null) {
                JSONArray ul = new JSONArray();
                ul.put(line);
                JSONArray uc = ud.optJSONArray("changelog");
                if (uc != null) for (int i = 0; i < Math.min(uc.length(), 9); i++) {
                    String x = uc.optString(i);
                    if (meaningful(x)) ul.put(x);
                }
                ud.put("changelog", ul);
            }
            statusBar.setText("● 正在发布…");
            final String fnote = note.isEmpty() ? "版本发布" : note;
            new Thread(() -> {
                try {
                    saveConfig("console: [发布新版本 v" + name + " code:" + code + "] " + fnote);
                    runOnUiThread(() -> {
                        statusBar.setText("● ✅ 已发布 v" + name + " (code " + code + ")");
                        toast("发布成功！本体 App 将弹出更新弹窗");
                        renderCachedPage("update");
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> { statusBar.setText("● 发布失败"); toast("发布失败：" + e.getMessage()); });
                }
            }).start();
        } catch (Exception e) {
            toast("发布失败：" + e.getMessage());
        }
    }

    private boolean meaningful(String x) {
        if (x == null || x.trim().isEmpty()) return false;
        String[] bad = {"【控制台推送】", "管理后台全局版本发布", "APK 制作入库", "静默同步", "更新设置版块", "控制台", "后台"};
        for (String b : bad) if (x.contains(b)) return false;
        return !x.trim().startsWith("【v") && !x.trim().startsWith("【V");
    }

    private void doDiagnose() {
        final List<String> lines = new ArrayList<>();
        statusBar.setText("● 诊断中…");
        new Thread(() -> {
            lines.add("== GitHub 连接诊断 ==");
            lines.add("仓库: " + OWNER + "/" + REPO);
            lines.add("Token: " + (token.isEmpty() ? "未填（只读）" : "已填"));
            lines.add("");
            String apiUrl = API_BASE + "/repos/" + OWNER + "/" + REPO;
            String b = httpGet(apiUrl, true, 10000);
            if (b != null) {
                try {
                    JSONObject d = new JSONObject(b);
                    lines.add("✅ API 主通道: 正常 (默认分支 " + d.optString("default_branch") + ")");
                } catch (Exception e) { lines.add("✅ API 主通道: 有响应但解析异常"); }
            } else {
                lines.add("❌ API 主通道: 不可达（请在系统设置配置代理，或换网络）");
            }
            for (String[] m : MIRRORS) {
                String u = String.format(Locale.US, m[1], OWNER, REPO, CONFIG);
                String s = httpGet(u, false, 8000);
                if (s != null && !looksLikeHtml(s)) lines.add("✅ 镜像 " + m[0] + ": 数据正常");
                else if (s != null) lines.add("❌ 镜像 " + m[0] + ": 返回网页（疑似被劫持）");
                else lines.add("❌ 镜像 " + m[0] + ": 不可达");
            }
            lines.add("");
            lines.add("提示：读取已内置多镜像兜底，通常必通；");
            lines.add("保存/发布必须能直连 api.github.com（国内可配合代理）。");
            runOnUiThread(() -> {
                statusBar.setText("● 诊断完成");
                StringBuilder sb = new StringBuilder();
                for (String l : lines) sb.append(l).append("\n");
                new AlertDialog.Builder(this)
                    .setTitle("🔍 连接诊断")
                    .setMessage(sb.toString())
                    .setPositiveButton("好", null)
                    .show();
            });
        }).start();
    }

    // ============================================================
    // 控制台自更新（旧版交互：🔍 本机检查控制台更新，下载并自动安装新控制台 APK）
    // ============================================================
    private void checkConsoleUpdate() {
        try {
            JSONObject con = admin == null ? null : admin.optJSONObject("console");
            if (con == null) return;
            int cloudCode = con.optInt("code", 0);
            String apkUrl = con.optString("apkUrl", "");
            if (cloudCode <= 0 || apkUrl.isEmpty()) return;
            final int localCode = getLocalConsoleVersionCode();
            if (cloudCode <= localCode) return;
            final String ver = con.optString("version", String.valueOf(cloudCode));
            confirm("发现控制台程序新版本 v" + ver + "（当前 code:" + localCode + " → 最新 code:" + cloudCode + "）\n\n" +
                "控制台程序更新与本体软件完全分离。点击确认将下载并自动安装新版本，替换当前控制台。", () -> doConsoleUpdate(apkUrl, ver));
        } catch (Exception e) {
            log("checkConsoleUpdate 失败: " + e);
        }
    }

    /** 读取当前控制台真实安装版本号（兼容 BuildConfig 缺失场景） */
    private int getLocalConsoleVersionCode() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (Exception e) { return 0; }
    }

    private void doConsoleUpdate(final String apkUrl, final String ver) {
        // 首次安装新版本需要「允许安装未知应用」权限：未授权先引导一次
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !getPackageManager().canRequestPackageInstalls()) {
            toast("请先允许安装未知应用（仅首次需要）");
            try {
                startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:" + getPackageName())));
            } catch (Exception e) { toast("无法打开设置：" + e.getMessage()); }
            return;
        }
        statusBar.setText("● 正在下载控制台新版本…");
        new Thread(() -> {
            try {
                final File apk = downloadToFile(apkUrl, "console-update.apk");
                runOnUiThread(() -> {
                    statusBar.setText("● 正在安装控制台新版本…");
                    boolean ok = installConsoleApk(apk);
                    if (ok) toast("已提交安装，稍后完成");
                    else toast("安装失败，请到系统设置手动安装");
                });
            } catch (Exception e) {
                runOnUiThread(() -> { statusBar.setText("● 控制台更新失败"); toast("控制台更新失败：" + e.getMessage()); });
            }
        }).start();
    }

    private File downloadToFile(String url, String name) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setConnectTimeout(20000);
        c.setReadTimeout(120000);
        c.setInstanceFollowRedirects(true);
        c.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android) YuntaiConsole/2.0");
        int code = c.getResponseCode();
        if (code < 200 || code >= 300) throw new Exception("HTTP " + code);
        File dir = new File(getCacheDir(), "console_update");
        if (!dir.exists()) dir.mkdirs();
        File f = new File(dir, name);
        InputStream in = c.getInputStream();
        FileOutputStream out = new FileOutputStream(f);
        byte[] buf = new byte[32768];
        int n;
        while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
        out.flush(); out.close(); in.close(); c.disconnect();
        return f;
    }

    /** 系统 PackageInstaller 安装（原子替换、保留数据） */
    private boolean installConsoleApk(File apkFile) {
        try {
            PackageInstaller pi = getPackageManager().getPackageInstaller();
            PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(
                    PackageInstaller.SessionParams.MODE_FULL_INSTALL);
            params.setAppPackageName("com.yuntai");
            int id = pi.createSession(params);
            PackageInstaller.Session session = pi.openSession(id);
            OutputStream out = session.openWrite("console.apk", 0, apkFile.length());
            FileInputStream fis = new FileInputStream(apkFile);
            byte[] buf = new byte[32768];
            int n;
            while ((n = fis.read(buf)) > 0) out.write(buf, 0, n);
            fis.close();
            out.close();
            session.close();
            Intent receiver = new Intent(this, ConsoleInstallReceiver.class);
            PendingIntent pending = PendingIntent.getBroadcast(this, 100, receiver,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            session.commit(pending.getIntentSender());
            return true;
        } catch (Exception e) {
            log("installConsoleApk 失败: " + e);
            return false;
        }
    }

    /** 控制台安装结果广播接收器 */
    public static class ConsoleInstallReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE);
            Toast.makeText(context,
                    status == PackageInstaller.STATUS_SUCCESS ? "控制台更新成功！" : "控制台安装失败（" + intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE) + "）",
                    Toast.LENGTH_LONG).show();
        }
    }

    // ============================================================
    // APK 仓库操作
    // ============================================================
    private void loadApkRepo(final boolean manual) {
        new Thread(() -> {
            try {
                String apiUrl = API_BASE + "/repos/" + OWNER + "/" + REPO + "/contents/dist/apk";
                String b = httpGet(apiUrl, true, 20000);
                final List<JSONObject> list = new ArrayList<>();
                if (b != null) {
                    JSONArray arr = new JSONArray(b);
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject o = arr.optJSONObject(i);
                        if (o != null && o.optString("name", "").endsWith(".apk")) list.add(o);
                    }
                }
                runOnUiThread(() -> {
                    apkCache.clear();
                    apkCache.addAll(list);
                    renderApkList();
                    if (manual) toast("已刷新，共 " + list.size() + " 个 APK");
                });
            } catch (Exception e) {
                runOnUiThread(() -> toast("读取 APK 仓库失败：" + e.getMessage()));
            }
        }).start();
    }

    private void setLatestApk() {
        if (selApk < 0 || selApk >= apkCache.size()) { toast("请先在列表选择一个 APK"); return; }
        final JSONObject a = apkCache.get(selApk);
        final String name = a.optString("name");
        final String raw = "https://raw.githubusercontent.com/" + OWNER + "/" + REPO + "/main/dist/apk/" + name;
        confirm("将「" + name + "」设为最新发布版本，并递增版本码触发本体更新弹窗？", () -> {
            new Thread(() -> {
                try {
                    JSONObject v = admin.optJSONObject("version");
                    v.put("apkUrl", raw);
                    v.put("apkUrlRaw", raw);
                    admin.put("version", v);
                    doBumpPublish("发布新安装包 " + name);
                } catch (Exception e) {
                    runOnUiThread(() -> toast("失败：" + e.getMessage()));
                }
            }).start();
        });
    }

    private void cleanupApks() {
        if (apkCache.size() <= 2) { toast("无需清理（不超过 2 个）"); return; }
        confirm("仓库仅保留「当前最新」+「最近上传」2 个，删除其余？\n（真正的删除操作）", () -> {
            new Thread(() -> {
                try {
                    List<JSONObject> sorted = new ArrayList<>(apkCache);
                    sorted.sort((x, y) -> y.optString("name", "").compareTo(x.optString("name", "")));
                    String curName = admin.optJSONObject("version").optString("apkUrl", "");
                    int slash = curName.lastIndexOf('/');
                    if (slash >= 0) curName = curName.substring(slash + 1);
                    List<String> keep = new ArrayList<>();
                    if (!curName.isEmpty()) keep.add(curName);
                    for (JSONObject x : sorted) {
                        if (keep.size() >= 2) break;
                        if (!keep.contains(x.optString("name"))) keep.add(x.optString("name"));
                    }
                    int del = 0;
                    for (JSONObject x : sorted) {
                        if (!keep.contains(x.optString("name"))) {
                            String p = "dist/apk/" + x.optString("name");
                            String u = API_BASE + "/repos/" + OWNER + "/" + REPO + "/contents/" + p;
                            JSONObject pl = new JSONObject();
                            pl.put("message", "console: 清理旧版本 APK " + x.optString("name"));
                            pl.put("sha", x.optString("sha"));
                            httpDelete(u, pl.toString(), 30000);
                            del++;
                        }
                    }
                    final int fd = del;
                    runOnUiThread(() -> { toast("清理完成，删除 " + fd + " 个"); loadApkRepo(true); });
                } catch (Exception e) {
                    runOnUiThread(() -> toast("清理失败：" + e.getMessage()));
                }
            }).start();
        });
    }

    // ============================================================
    // 备份 / 导入 JSON（旧版交互：⬇️ 备份 / ⬆️ 导入）
    // ============================================================
    private void backupJson() {
        if (admin == null) { toast("请先连接云端再备份"); return; }
        try {
            Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("application/json");
            i.putExtra(Intent.EXTRA_TITLE, "admin-data-" + now().replaceAll("[: ]", "-") + ".json");
            startActivityForResult(i, CREATE_BACKUP_JSON);
        } catch (Exception e) {
            toast("无法打开保存器：" + e.getMessage());
        }
    }

    private void importJson(Uri uri) {
        try {
            InputStream in = getContentResolver().openInputStream(uri);
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            byte[] buf = new byte[16384];
            int n;
            while ((n = in.read(buf)) > 0) bo.write(buf, 0, n);
            in.close();
            String txt = bo.toString("UTF-8");
            if (looksLikeHtml(txt)) { toast("导入失败：文件不是 JSON（疑似被拦截）"); return; }
            JSONObject obj = new JSONObject(txt);
            if (!obj.has("home") && !obj.has("version")) { toast("导入失败：不是 admin-data.json 结构"); return; }
            admin = obj;
            ensureDefaults();
            sha = "";
            collectCatFromCache();
            renderAll();
            toast("导入成功！请点「⚡ 应用」推送云端");
        } catch (Exception e) {
            toast("导入失败：" + e.getMessage());
        }
    }

    private void renderAll() {
        try {
            for (String k : new String[]{"home", "soft", "skill", "settings", "update", "other", "apk"}) {
                if (pages.containsKey(k)) renderCachedPage(k);
            }
        } catch (Exception e) { log("renderAll 失败: " + e); }
    }

    // ============================================================
    // SAF 文件选择与上传
    // ============================================================
    private void pickFile(int req) {
        try {
            Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("*/*");
            startActivityForResult(i, req);
        } catch (Exception e) {
            toast("无法打开文件选择器：" + e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null || data.getData() == null) return;
        final Uri uri = data.getData();
        final String name = queryName(uri);
        if (requestCode == PICK_APK_REPO) uploadToRepo(uri, name, null);
        else if (requestCode == PICK_APK_FOR_SOFT) uploadToRepo(uri, name, selSoft);
        else if (requestCode == PICK_IMPORT_JSON) importJson(uri);
        else if (requestCode == CREATE_BACKUP_JSON) exportJsonTo(uri);
    }

    private void exportJsonTo(Uri uri) {
        try {
            String json = admin.toString(2);
            OutputStream out = getContentResolver().openOutputStream(uri);
            out.write(json.getBytes("UTF-8"));
            out.flush();
            out.close();
            toast("✅ 备份成功");
        } catch (Exception e) {
            toast("备份失败：" + e.getMessage());
        }
    }

    private String queryName(Uri uri) {
        try {
            android.database.Cursor c = getContentResolver().query(uri, null, null, null, null);
            if (c != null && c.moveToFirst()) {
                int idx = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) return c.getString(idx);
            }
        } catch (Exception ignored) { }
        return "file" + System.currentTimeMillis() + ".bin";
    }

    private void uploadToRepo(final Uri uri, final String name, final Integer softIdx) {
        if (token.isEmpty()) { toast("请先填入 Token（上传需要权限）"); return; }
        statusBar.setText("● 正在上传 " + name + " …");
        new Thread(() -> {
            try {
                InputStream in = getContentResolver().openInputStream(uri);
                ByteArrayOutputStream bo = new ByteArrayOutputStream();
                byte[] buf = new byte[16384];
                int n;
                while ((n = in.read(buf)) > 0) bo.write(buf, 0, n);
                in.close();
                byte[] data = bo.toByteArray();
                if (data.length > 100 * 1024 * 1024) { toast("文件超过 100MB，请用 git 推送"); return; }
                String safe = name.replaceAll("[^a-zA-Z0-9._\\-]", "_");
                String remote = "dist/apk/" + System.currentTimeMillis() + "_" + safe;
                String b64 = Base64.encodeToString(data, Base64.NO_WRAP);
                JSONObject payload = new JSONObject();
                payload.put("message", "console: 上传 " + safe);
                payload.put("content", b64);
                String u = API_BASE + "/repos/" + OWNER + "/" + REPO + "/contents/" + remote;
                String resp = httpPut(u, payload.toString(), 300000);
                JSONObject rd = new JSONObject(resp);
                JSONObject cc = rd.optJSONObject("content");
                String rawUrl = "https://raw.githubusercontent.com/" + OWNER + "/" + REPO + "/main/" + remote;
                runOnUiThread(() -> {
                    statusBar.setText("● ✅ 上传成功");
                    toast("上传成功！");
                    if (softIdx != null && softIdx >= 0 && softIdx < softCache.size()) {
                        try {
                            softCache.get(softIdx).put("apkUrl", rawUrl);
                            syncSoftCache();
                            renderSoftList();
                            toast("已关联到软件「" + softCache.get(softIdx).optString("title") + "」，点「⚡ 应用」生效");
                        } catch (Exception e) { toast("关联失败：" + e.getMessage()); }
                    }
                    loadApkRepo(true);
                });
            } catch (Exception e) {
                runOnUiThread(() -> { statusBar.setText("● 上传失败"); toast("上传失败：" + e.getMessage()); });
            }
        }).start();
    }

    // ============================================================
    // 工具
    // ============================================================
    private TextView secTitle(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(14);
        t.setTextColor(0xFFA5B4FC);
        t.setTypeface(null, Typeface.BOLD);
        t.setPadding(0, dp(4), 0, dp(2));
        return t;
    }
    private LinearLayout row() {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setPadding(0, dp(6), 0, dp(2));
        return r;
    }
    private void addBtn(LinearLayout parent, String text, View.OnClickListener l) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(12);
        b.setTextColor(Color.WHITE);
        b.setAllCaps(false);
        b.setBackgroundColor(0xFF2D3550);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(38), 1f);
        p.leftMargin = dp(2); p.rightMargin = dp(2);
        b.setLayoutParams(p);
        b.setOnClickListener(l);
        parent.addView(b);
    }
    private EditText le(LinearLayout parent, String hint, String val) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setText(val == null ? "" : val);
        e.setTextColor(Color.WHITE);
        e.setHintTextColor(0xFF64748B);
        e.setTextSize(13);
        e.setBackgroundColor(0xFF1E2438);
        e.setPadding(dp(8), dp(6), dp(8), dp(6));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.topMargin = dp(6);
        parent.addView(e, p);
        return e;
    }
    private void confirm(String msg, Runnable onOk) {
        new AlertDialog.Builder(this)
            .setMessage(msg)
            .setPositiveButton("确定", (d, w) -> onOk.run())
            .setNegativeButton("取消", null)
            .show();
    }
    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }
    private void log(String s) {
        android.util.Log.d("YuntaiConsole", s);
    }
    private String now() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(new Date());
    }
}