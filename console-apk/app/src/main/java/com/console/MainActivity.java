package com.yuntai;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * 懒得找了 · 云端控制台（WebView 壳应用）
 * 加载内置的 console/index.html，通过 GitHub API 完全对接仓库，
 * 在手机上即可后台维护更新项目全部内容。
 *
 * 关键：必须实现 WebChromeClient.onShowFileChooser，
 * 否则页面里的 <input type="file"> 无法打开系统文件选择器（本地上传没反应）。
 *
 * AndroidBridge：页面调用 window.AndroidBridge.download(url, name) 时
 * 走系统 DownloadManager 真下载（通知栏可见进度），修复手机端 APK/ZIP/MD 无法下载的问题。
 */
public class MainActivity extends Activity {

    private static final int FILE_CHOOSER_RESULT_CODE = 101;

    private WebView webView;
    private ValueCallback<Uri[]> uploadMessage;

    /** 原生下载桥：供页面 JS 调用，走系统 DownloadManager */
    public class AndroidBridge {
        @JavascriptInterface
        public void download(String url, String name) {
            if (url == null || url.isEmpty()) return;
            try {
                String fileName = (name == null || name.trim().isEmpty())
                        ? "landezhao-download"
                        : name.trim();
                if (!fileName.contains(".")) {
                    String ext = "apk";
                    String low = url.toLowerCase();
                    if (low.contains(".zip")) ext = "zip";
                    else if (low.contains(".md")) ext = "md";
                    fileName = fileName + "." + ext;
                }
                DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                DownloadManager.Request req = new DownloadManager.Request(Uri.parse(url))
                        .setTitle(fileName)
                        .setDescription("懒得找了 · 云端仓库文件")
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        .setAllowedOverMetered(true)
                        .setAllowedOverRoaming(true);
                if (fileName.toLowerCase().endsWith(".apk")) {
                    req.setMimeType("application/vnd.android.package-archive");
                } else if (fileName.toLowerCase().endsWith(".zip")) {
                    req.setMimeType("application/zip");
                } else if (fileName.toLowerCase().endsWith(".md")) {
                    req.setMimeType("text/markdown");
                }
                req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
                dm.enqueue(req);
            } catch (Exception ignored) { }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowFileAccessFromFileURLs(true);
        s.setAllowUniversalAccessFromFileURLs(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);

        // 原生下载桥：修复手机端 APK / ZIP / MD 下载
        webView.addJavascriptInterface(new AndroidBridge(), "AndroidBridge");

        webView.setWebViewClient(new WebViewClient());

        // 关键修复：WebChromeClient 支持文件选择（本地上传 APK/MD/ZIP/图片/视频）
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(
                    WebView wv,
                    ValueCallback<Uri[]> filePathCallback,
                    FileChooserParams fileChooserParams
            ) {
                // 清理上一次未完成的回调，避免泄漏
                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(null);
                    uploadMessage = null;
                }
                uploadMessage = filePathCallback;

                Intent intent = fileChooserParams.createIntent();
                try {
                    // 优先使用系统文件选择器（支持多选取决于页面 accept 属性）
                    startActivityForResult(intent, FILE_CHOOSER_RESULT_CODE);
                } catch (ActivityNotFoundException e) {
                    uploadMessage = null;
                    return false;
                }
                return true;
            }

            @Override
            public void onPermissionRequest(android.webkit.PermissionRequest request) {
                // 页面需要摄像头/麦克风权限时直接授权，保证视频/图片上传体验
                try {
                    runOnUiThread(() -> request.grant(request.getResources()));
                } catch (Exception e) {
                    request.deny();
                }
            }
        });

        setContentView(webView);
        webView.loadUrl("file:///android_asset/console/index.html");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (uploadMessage == null) {
                super.onActivityResult(requestCode, resultCode, data);
                return;
            }
            Uri[] results = null;
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    String dataString = data.getDataString();
                    if (dataString != null) {
                        results = new Uri[]{Uri.parse(dataString)};
                    }
                }
            }
            uploadMessage.onReceiveValue(results);
            uploadMessage = null;
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}
