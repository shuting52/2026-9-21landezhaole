package com.yuntai;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;

/**
 * 云台-懒得找了 · 云端总控台（WebView 壳应用）
 * 加载内置的 console/index.html，通过 GitHub API 完全对接仓库，
 * 在手机上即可后台维护更新项目全部内容。
 *
 * 1. WebChromeClient.onShowFileChooser：页面 <input type="file"> 打开系统文件选择器（本地上传）
 * 2. JS 桥 Android.download(url, name)：页面调用原生系统下载管理器下载 APK/文件
 * 3. JS 桥 Android.downloadAndInstall(url, name)：控制台自更新——下载新版本 APK 并自动安装替换老版本
 *    （控制台程序更新与本体软件完全分离）
 */
public class MainActivity extends Activity {

    private static final int FILE_CHOOSER_RESULT_CODE = 101;

    private WebView webView;
    private ValueCallback<Uri[]> uploadMessage;

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

        webView.setWebViewClient(new WebViewClient());

        // 关键：WebChromeClient 支持文件选择（本地上传 APK/MD/ZIP/图片/视频）
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(
                    WebView wv,
                    ValueCallback<Uri[]> filePathCallback,
                    FileChooserParams fileChooserParams
            ) {
                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(null);
                    uploadMessage = null;
                }
                uploadMessage = filePathCallback;
                Intent intent = fileChooserParams.createIntent();
                try {
                    startActivityForResult(intent, FILE_CHOOSER_RESULT_CODE);
                } catch (ActivityNotFoundException e) {
                    uploadMessage = null;
                    return false;
                }
                return true;
            }

            @Override
            public void onPermissionRequest(android.webkit.PermissionRequest request) {
                try {
                    runOnUiThread(() -> request.grant(request.getResources()));
                } catch (Exception e) {
                    request.deny();
                }
            }
        });

        // JS 桥：Android.download(url, name) -> 系统下载管理器下载 APK/文件
        webView.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void download(String url, String name) {
                try {
                    String safeName = (name == null || name.trim().isEmpty())
                            ? "yuntai_download.bin"
                            : name.replaceAll("[^a-zA-Z0-9._\\-]", "_");
                    DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                    DownloadManager.Request req = new DownloadManager.Request(Uri.parse(url));
                    req.setTitle("云台-懒得找了 · 文件下载");
                    req.setDescription(safeName);
                    req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    req.setAllowedOverMetered(true);
                    req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, safeName);
                    dm.enqueue(req);
                } catch (Exception e) {
                    // 下载器失败时退回浏览器打开
                    runOnUiThread(() -> {
                        try {
                            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(i);
                        } catch (Exception ignored) {
                        }
                    });
                }
            }

            // 控制台自更新：下载新版本 APK 到公共下载目录，然后自动触发安装（替换老版本）
            @JavascriptInterface
            public void downloadAndInstall(String url, String name) {
                try {
                    String safeName = (name == null || name.trim().isEmpty())
                            ? "console-update.apk"
                            : name.replaceAll("[^a-zA-Z0-9._\\-]", "_");
                    File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    if (!dir.exists()) dir.mkdirs();
                    final File apkFile = new File(dir, safeName);

                    // 用 OkHttp/HttpURLConnection 下载（需下载完成后才能安装）
                    new Thread(() -> {
                        try {
                            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
                            conn.setConnectTimeout(15000);
                            conn.setReadTimeout(120000);
                            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android) YuntaiUpdater/1.0");
                            conn.setInstanceFollowRedirects(true);
                            java.io.InputStream in = conn.getInputStream();
                            java.io.FileOutputStream out = new java.io.FileOutputStream(apkFile);
                            byte[] buf = new byte[8192];
                            int n;
                            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
                            out.flush(); out.close(); in.close();
                            // 下载完成：触发安装
                            runOnUiThread(() -> installApk(apkFile));
                        } catch (final Exception e) {
                            runOnUiThread(() -> {
                                try {
                                    Toast.makeText(MainActivity.this, "控制台更新下载失败，请稍后重试", Toast.LENGTH_LONG).show();
                                } catch (Exception ignored) {}
                            });
                        }
                    }).start();
                } catch (Exception e) {
                    try {
                        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                    } catch (Exception ignored) {}
                }
            }

            // 使用 FileProvider 打开系统安装器安装 APK
            private void installApk(File apkFile) {
                try {
                    Uri apkUri = FileProvider.getUriForFile(MainActivity.this, "com.yuntai.fileprovider", apkFile);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    Toast.makeText(MainActivity.this, "控制台新版本已下载，请点击安装完成更新", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "安装被拦截，请到系统设置允许安装未知应用后重试", Toast.LENGTH_LONG).show();
                }
            }
        }, "Android");

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
            if (resultCode == Activity.RESULT_OK && data != null) {
                String dataString = data.getDataString();
                if (dataString != null) {
                    results = new Uri[]{Uri.parse(dataString)};
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
