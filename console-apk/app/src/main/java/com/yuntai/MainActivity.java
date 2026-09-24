package com.yuntai;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
    private final List<JSONObject> skillCache = new ArrayList<>();
    private final List<JSONObject> apkCache = new ArrayList<>();
    private int selCat = -1, selSite = -1, selSoft = -1, selSkill = -1, selApk = -1;

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

        TextView t2 = secTitle("—— 该分类下的站点 ——");
        page.addView(t2);

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
        List<String> names = new ArrayList<>();
        for (JSONObject s : siteCache) {
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
        final JSONObject s = (idx >= 0 && idx < siteCache.size()) ? siteCache.get(idx) : new JSONObject();
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
        if (selCat < 0 || selCat >= catCache.size() || selSite < 0 || selSite >= siteCache.size()) {
            toast("请先在列表选择站点"); return;
        }
        confirm("删除站点「" + siteCache.get(selSite).optString("title") + "」？", () -> {
            siteCache.remove(selSite);
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
        pageFields.put("other", fs);
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
            boolean ok = readConfig();
            runOnUiThread(() -> {
                if (ok) {
                    statusBar.setText("● 已连接 " + source + " ｜ v" + admin.optJSONObject("version").optString("name", "?")
                            + " (code " + admin.optJSONObject("version").optString("code", "?") + ")");
                    if (sha.isEmpty() && !token.isEmpty()) statusBar.append("（只读镜像，写入需 API 可达）");
                    if (sha.isEmpty() && token.isEmpty()) statusBar.append("（未填 Token，只读）");
                    renderHomeLists();
                    renderSoftList();
                    renderSkillList();
                    renderCachedPage(currentTab);
                } else {
                    statusBar.setText("● 连接失败");
                    toast("连接失败：API 与全部镜像均不可达。\n请检查网络 / Token / 仓库名，或用「🔍 诊断」定位问题");
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