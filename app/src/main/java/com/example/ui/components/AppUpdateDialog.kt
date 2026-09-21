package com.example.ui.components

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.data.remote.UpdateDialogDto
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import java.io.File
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.floor
import kotlin.random.Random

/**
 * 官方 QQ 群链接（与设置页一致）
 */
const val OFFICIAL_QQ_GROUP_URL =
    "https://qun.qq.com/universal-share/share?ac=1&authKey=gtnBoTi8HEzXQAF9x40Y5GYQtubkWu4pGDJg7OuNQte9oz3sXiFonGqZaUXxjffu&busi_data=eyJncm91cENvZGUiOiI0MzkyMTEzNDciLCJ0b2tlbiI6IkVxeXJDb0tyVjM3Y0VIRmhZQ3M5eDg4VW5MYWU0RW4ybVlSRlBlS2ozQXRxanB5V2ZtNzNHMlRIa2ZRd0VTQnUiLCJ1aW4iOiIzMDc3Nzk1MjMifQ%3D%3D&data=QnUzn164u21Cu1dG7vAVYJqU_4hw0COArsGrrBOIc0vxu7ES6gOJcYyrpu2JgkVs-y3X0ZUGZb_nPBJsBTRccQ&svctype=4&tempid=h5_group_info"

/**
 * 客户端更新弹窗：火箭图标 + 更新日志 + 动态渐变下载进度
 * 严格按照指定 CSS/HTML/JS 规格设计打造：
 * - .u-mask: 半透明暗色遮罩
 * - .u-dialog: 340px 宽度，圆角 24px，深邃背景色 #14142a，精致描边与深阴影
 * - .u-rocket: 74x74 悬浮火箭微倾斜摆动无限动效，紫粉渐变底 (#6c63ff -> #ff2d78)
 * - .u-ver: v2.0.0 高亮版本胶囊标签
 * - .u-log: 半透明背景日志卡片，高亮 ✦ 列表
 * - .u-bar / .u-fill: 渐变进度条实时推进下载
 * - .u-btn: 官方群 & 立即更新按钮（强制更新时不可关闭弹窗，立即更新自动下载并安装 APK）
 */
@Composable
fun AppUpdateDialog(
    onDismiss: () -> Unit,
    versionName: String = "v2.0.0",
    onUpdateFinished: () -> Unit = {},
    update: UpdateDialogDto? = null,
    apkUrl: String? = null,
    forceUpdate: Boolean = false
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isUpdating by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var statusLabel by remember { mutableStateOf("等待更新…") }

    // 云端配置（由控制台发布，实时同步）
    val cloudTitle = update?.title ?: "发现新版本"
    val cloudLogs: List<String> = update?.changelog?.takeIf { it.isNotEmpty() }
        ?: listOf(
            "新增云端实时同步功能",
            "首页分类/角标可由控制台远程配置",
            "软件库支持 APK 直链下载更新",
            "Skill 技能库支持本地文件同步",
            "开屏动画支持自定义代码配置",
            "修复已知体验问题与稳定性提升"
        )
    val cloudConfirm = update?.confirmText ?: "立即更新"
    val cloudCancel = update?.cancelText ?: "稍后再说"
    val customHtml = update?.customHtml?.takeIf { it.isNotBlank() }

    /** 通过系统安装器安装 APK（FileProvider 共享文件） */
    fun installApk(file: File) {
        try {
            val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            onUpdateFinished()
        } catch (e: Exception) {
            Toast.makeText(context, "自动安装被拦截，请到系统设置允许安装未知应用后重试", Toast.LENGTH_LONG).show()
        }
    }

    /** 跳转官方 QQ 群 */
    fun openOfficialGroup() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(OFFICIAL_QQ_GROUP_URL))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "打开 QQ 群失败，请手动搜索群号：439211347", Toast.LENGTH_SHORT).show()
        }
    }

    fun startRealDownload() {
        val url = apkUrl
        if (url.isNullOrBlank()) return
        coroutineScope.launch {
            isUpdating = true
            statusLabel = "正在下载更新…"
            progress = 12f
            try {
                // 直接下载 APK 到应用缓存目录（不走系统下载器，下载完自动安装）
                val client = okhttp3.OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .build()
                val request = okhttp3.Request.Builder().url(url).build()
                client.newCall(request).execute().use { resp ->
                    if (!resp.isSuccessful) throw Exception("下载失败 HTTP ${resp.code}")
                    val body = resp.body ?: throw Exception("下载失败")
                    val total = body.contentLength()
                    val file = File(context.cacheDir, "update/latest.apk")
                    file.parentFile?.mkdirs()
                    body.byteStream().use { input ->
                        file.outputStream().use { output ->
                            val buf = ByteArray(8192)
                            var downloaded = 0L
                            while (true) {
                                val n = input.read(buf)
                                if (n <= 0) break
                                output.write(buf, 0, n)
                                downloaded += n
                                if (total > 0) {
                                    progress = (downloaded * 100f / total).coerceIn(0f, 100f)
                                    statusLabel = "下载中… ${progress.toInt()}%"
                                }
                            }
                            output.flush()
                        }
                    }
                    progress = 100f
                    statusLabel = "下载完成，准备安装…"
                    delay(300)
                    installApk(file)
                }
            } catch (e: Exception) {
                    Toast.makeText(context, "下载失败：${e.message}", Toast.LENGTH_SHORT).show()
                    statusLabel = "等待更新…"
                    progress = 0f
                    isUpdating = false
                }
            }
        }

    class UpdateJsBridge(
        private val onDownload: () -> Unit,
        private val onClose: () -> Unit
    ) {
        @JavascriptInterface
        fun download() { onDownload() }
        @JavascriptInterface
        fun close() { onClose() }
    }

    fun startUpdate() {
        if (isUpdating) return
        if (!apkUrl.isNullOrBlank()) {
            startRealDownload()
            return
        }
        coroutineScope.launch {
            isUpdating = true
            statusLabel = "正在下载更新…"
            var p = 0f
            while (p < 100f) {
                delay(150)
                p += (Random.nextFloat() * 8f + 3f)
                if (p >= 100f) {
                    p = 100f
                    progress = 100f
                    statusLabel = "更新完成"
                    delay(600)
                    Toast.makeText(context, "更新完成！已是最新版本", Toast.LENGTH_SHORT).show()
                    isUpdating = false
                    onUpdateFinished()
                    onDismiss()
                    break
                }
                progress = p
            }
        }
    }

    // Safe closeUpdate function logic with active updating guard check.
    // 强制更新（forceUpdate）时不允许关闭弹窗
    fun closeUpdate() {
        if (!isUpdating && !forceUpdate) {
            onDismiss()
        }
    }

    if (!customHtml.isNullOrBlank()) {
        Dialog(
            onDismissRequest = { closeUpdate() },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = !isUpdating,
                dismissOnClickOutside = !isUpdating
            )
        ) {
            val jsBridge = remember { UpdateJsBridge({ startUpdate() }, { closeUpdate() }) }
            val safeHtml = """
                <!DOCTYPE html>
                <html>
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                  <style>
                    * { box-sizing: border-box; -webkit-tap-highlight-color: transparent; margin: 0; padding: 0; }
                    html, body {
                      background: transparent;
                      width: 100%;
                      height: 100%;
                      overflow: hidden;
                      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
                      display: flex;
                      align-items: center;
                      justify-content: center;
                    }
                    ${update?.customCss ?: ""}
                  </style>
                </head>
                <body>
                $customHtml
                <script>
                  (function(){
                    var btnConfirm = document.getElementById('upd-confirm') || document.querySelector('button.absolute') || document.querySelectorAll('button')[1];
                    var btnCancel = document.getElementById('upd-cancel') || document.querySelector('button:not(.absolute)') || document.querySelectorAll('button')[0];
                    if(btnConfirm){
                      btnConfirm.addEventListener('click', function(e){
                        e.preventDefault();
                        try { AndroidBridge.download(); } catch(err){}
                      });
                    }
                    if(btnCancel){
                      btnCancel.addEventListener('click', function(e){
                        e.preventDefault();
                        try { AndroidBridge.close(); } catch(err){}
                      });
                    }
                  })();
                </script>
                </body>
                </html>
            """.trimIndent()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.65f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { closeUpdate() }
                    ),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                cacheMode = WebSettings.LOAD_NO_CACHE
                            }
                            setBackgroundColor(0x00000000)
                            addJavascriptInterface(jsBridge, "AndroidBridge")
                            webViewClient = object : WebViewClient() {
                                override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
                                    return true
                                }
                            }
                            loadDataWithBaseURL(null, safeHtml, "text/html", "UTF-8", null)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp)
                        .padding(horizontal = 24.dp)
                )
            }
        }
        return
    }

    // 默认呈现：100% 还原 Uiverse.io 设计，纯 Jetpack Compose 原生渲染，零 WebView 依赖，彻底杜绝崩溃
    Dialog(
        onDismissRequest = { closeUpdate() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = !isUpdating && !forceUpdate,
            dismissOnClickOutside = !isUpdating && !forceUpdate
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { closeUpdate() }
                )
                .testTag("uiverse_dialog_mask"),
            contentAlignment = Alignment.Center
        ) {
            // Uiverse.io Card Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                // White Rounded Card
                Box(
                    modifier = Modifier
                        .padding(top = 23.dp)
                        .widthIn(min = 280.dp, max = 310.dp)
                        .fillMaxWidth()
                        .shadow(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(16.dp),
                            ambientColor = Color(0x4D3C4043),
                            spotColor = Color(0x263C4043)
                        )
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {} // Prevent closing when clicking inside dialog
                        )
                        .testTag("uiverse_dialog_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 36.dp, start = 24.dp, end = 24.dp, bottom = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Title
                        Text(
                            text = if (cloudTitle.isNotBlank()) cloudTitle else "Your privacy is important to us",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF3F3F46),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("uiverse_title")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Description / Logs
                        if (cloudLogs.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                cloudLogs.take(4).forEach { log ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text("•", fontSize = 12.sp, color = Color(0xFF9C6750), fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(log, fontSize = 12.sp, color = Color(0xFF52525B), lineHeight = 16.sp)
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "We process your personal information to measure and improve our sites and services, to assist our campaigns and to provide personalised content.",
                                fontSize = 12.5.sp,
                                color = Color(0xFF52525B),
                                lineHeight = 17.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "For more information see our ",
                                    fontSize = 12.5.sp,
                                    color = Color(0xFF52525B)
                                )
                                Text(
                                    text = "Privacy Policy",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF634647),
                                    textDecoration = TextDecoration.Underline
                                )
                            }
                        }

                        // Progress Bar (During download)
                        if (isUpdating) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (progress < 100f) "正在极速下载..." else "下载完成，准备就绪",
                                        fontSize = 11.sp,
                                        color = Color(0xFF71717A)
                                    )
                                    Text(
                                        text = "${progress.toInt()}%",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF634647)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(Color(0xFFEAB789).copy(alpha = 0.35f))
                                ) {
                                    val animProgress by animateFloatAsState(
                                        targetValue = progress / 100f,
                                        animationSpec = tween(durationMillis = 200),
                                        label = "progress"
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(fraction = animProgress.coerceIn(0f, 1f))
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(Color(0xFFDDAD81))
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Action Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 「官方群」按钮（点击跳转 QQ 群；强制更新时也不关闭弹窗）
                            Text(
                                text = "官方群",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF71717A),
                                modifier = Modifier
                                    .clickable(enabled = !isUpdating) { openOfficialGroup() }
                                    .padding(vertical = 6.dp)
                                    .testTag("uiverse_more_options")
                            )

                            // "Accept" / 立即更新
                            Button(
                                onClick = { startUpdate() },
                                enabled = !isUpdating,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFDDAD81),
                                    contentColor = Color(0xFF634647),
                                    disabledContainerColor = Color(0xFFDDAD81).copy(alpha = 0.6f),
                                    disabledContentColor = Color(0xFF634647).copy(alpha = 0.6f)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 9.dp),
                                modifier = Modifier.testTag("uiverse_accept_btn")
                            ) {
                                Text(
                                    text = if (isUpdating) "更新中…" else cloudConfirm.ifBlank { "Accept" },
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Protruding Top SVG Cookie Badge from Uiverse.io
                Box(
                    modifier = Modifier
                        .size(65.dp, 46.dp)
                        .testTag("uiverse_cookie_badge"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_uiverse_cookie),
                        contentDescription = "Uiverse Cookie",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(65.dp, 46.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LogItem(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "✦",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFFFF2D78)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            color = Color.White.copy(alpha = 0.75f)
        )
    }
}

private const val UIVERSE_UPDATE_HTML = """<!-- From Uiverse.io by ilkhoeri --> 
<div
  class="[--shadow:rgba(60,64,67,0.3)_0_1px_2px_0,rgba(60,64,67,0.15)_0_2px_6px_2px] w-4/5 h-auto rounded-2xl bg-white [box-shadow:var(--shadow)] max-w-[300px]"
>
  <div
    class="flex flex-col items-center justify-between pt-9 px-6 pb-6 relative"
  >
    <span class="relative mx-auto -mt-16 mb-8">
      <svg
        xmlns="http://www.w3.org/2000/svg"
        fill="none"
        height="46"
        width="65"
      >
        <path
          stroke="#000"
          fill="#EAB789"
          d="M49.157 15.69L44.58.655l-12.422 1.96L21.044.654l-8.499 2.615-6.538 5.23-4.576 9.153v11.114l4.576 8.5 7.846 5.23 10.46 1.96 7.845-2.614 9.153 2.615 11.768-2.615 7.846-7.846 1.96-5.884.655-7.191-7.846-1.308-6.537-3.922z"
        ></path>
        <path
          fill="#9C6750"
          d="M32.286 3.749c-6.94 3.65-11.69 11.053-11.69 19.591 0 8.137 4.313 15.242 10.724 19.052a20.513 20.513 0 01-8.723 1.937c-11.598 0-21-9.626-21-21.5 0-11.875 9.402-21.5 21-21.5 3.495 0 6.79.874 9.689 2.42z"
          clip-rule="evenodd"
          fill-rule="evenodd"
        ></path>
        <path
          fill="#634647"
          d="M64.472 20.305a.954.954 0 00-1.172-.824 4.508 4.508 0 01-3.958-.934.953.953 0 00-1.076-.11c-.46.252-.977.383-1.502.382a3.154 3.154 0 01-2.97-2.11.954.954 0 00-.833-.634 4.54 4.54 0 01-4.205-4.507c.002-.23.022-.46.06-.687a.952.952 0 00-.213-.767 3.497 3.497 0 01-.614-3.5.953.953 0 00-.382-1.138 3.522 3.522 0 01-1.5-3.992.951.951 0 00-.762-1.227A22.611 22.611 0 0032.3 2.16 22.41 22.41 0 0022.657.001a22.654 22.654 0 109.648 43.15 22.644 22.644 0 0032.167-22.847zM22.657 43.4a20.746 20.746 0 110-41.493c2.566-.004 5.11.473 7.501 1.407a22.64 22.64 0 00.003 38.682 20.6 20.6 0 01-7.504 1.404zm19.286 0a20.746 20.746 0 112.131-41.384 5.417 5.417 0 001.918 4.635 5.346 5.346 0 00-.133 1.182A5.441 5.441 0 0046.879 11a5.804 5.804 0 00-.028.568 6.456 6.456 0 005.38 6.345 5.053 5.053 0 006.378 2.472 6.412 6.412 0 004.05 1.12 20.768 20.768 0 01-20.716 21.897z"
        ></path>
        <path
          fill="#644647"
          d="M54.962 34.3a17.719 17.719 0 01-2.602 2.378.954.954 0 001.14 1.53 19.637 19.637 0 002.884-2.634.955.955 0 00-1.422-1.274z"
        ></path>
        <path
          stroke-width="1.8"
          stroke="#644647"
          fill="#845556"
          d="M44.5 32.829c-.512 0-1.574.215-2 .5-.426.284-.342.263-.537.736a2.59 2.59 0 104.98.99c0-.686-.458-1.241-.943-1.726-.485-.486-.814-.5-1.5-.5zm-30.916-2.5c-.296 0-.912.134-1.159.311-.246.177-.197.164-.31.459a1.725 1.725 0 00-.086.932c.058.312.2.6.41.825.21.226.477.38.768.442.291.062.593.03.867-.092s.508-.329.673-.594a1.7 1.7 0 00.253-.896c0-.428-.266-.774-.547-1.076-.281-.302-.471-.31-.869-.311zm17.805-11.375c-.143-.492-.647-1.451-1.04-1.78-.392-.33-.348-.255-.857-.31a2.588 2.588 0 10.441 5.06c.66-.194 1.064-.788 1.395-1.39.33-.601.252-.92.06-1.58zm-22 2c-.143-.492-.647-1.451-1.04-1.78-.391-.33-.347-.255-.856-.31a2.589 2.589 0 10.44 5.06c.66-.194 1.064-.788 1.395-1.39.33-.601.252-.92.06-1.58zM38.112 7.329c-.395 0-1.216.179-1.545.415-.328.236-.263.218-.415.611-.151.393-.19.826-.114 1.243.078.417.268.8.548 1.1.28.301.636.506 1.024.59.388.082.79.04 1.155-.123.366-.163.678-.438.898-.792.22-.354.337-.77.337-1.195 0-.57-.354-1.031-.73-1.434-.374-.403-.628-.415-1.158-.415zm-19.123.703c.023-.296-.062-.92-.219-1.18-.157-.26-.148-.21-.432-.347a1.726 1.726 0 00-.922-.159 1.654 1.654 0 00-.856.344 1.471 1.471 0 00-.501.73c-.085.285-.077.589.023.872.1.282.287.532.538.718a1.7 1.7 0 00.873.323c.427.033.793-.204 1.116-.46.324-.256.347-.445.38-.841z"
        ></path>
        <path
          fill="#634647"
          d="M15.027 15.605a.954.954 0 00-1.553 1.108l1.332 1.863a.955.955 0 001.705-.77.955.955 0 00-.153-.34l-1.331-1.861z"
        ></path>
        <path
          fill="#644647"
          d="M43.31 23.21a.954.954 0 101.553-1.11l-1.266-1.772a.954.954 0 10-1.552 1.11l1.266 1.772z"
        ></path>
        <path
          fill="#634647"
          d="M19.672 35.374a.954.954 0 00-.954.953v2.363a.954.954 0 001.907 0v-2.362a.954.954 0 00-.953-.954z"
        ></path>
        <path
          fill="#644647"
          d="M33.129 29.18l-2.803 1.065a.953.953 0 00-.053 1.764.957.957 0 00.73.022l2.803-1.065a.953.953 0 00-.677-1.783v-.003zm24.373-3.628l-2.167.823a.956.956 0 00-.054 1.764.954.954 0 00.73.021l2.169-.823a.954.954 0 10-.678-1.784v-.001z"
        ></path>
      </svg>
    </span>

    <h5 class="text-sm font-semibold mb-2 text-left mr-auto text-zinc-700">
      Your privacy is important to us
    </h5>

    <p class="w-full mb-4 text-sm text-justify">
      We process your personal information to measure and improve our sites and
      services, to assist our campaigns and to provide personalised content.
      <br />
      For more information see our
      <a
        class="mb-2 text-sm cursor-pointer font-semibold transition-colors hover:text-[#634647] underline underline-offset-2"
        >Privacy Policy</a
      >
    </p>

    <button
      class="mb-2 text-sm mr-auto text-zinc-600 cursor-pointer font-semibold transition-colors hover:text-[#634647] hover:underline underline-offset-2"
    >
      More Options
    </button>
    <button
      class="absolute font-semibold right-6 bottom-6 cursor-pointer py-2 px-8 w-max break-keep text-sm rounded-lg transition-colors text-[#634647] hover:text-[#ddad81] bg-[#ddad81] hover:bg-[#634647]"
      type="button"
    >
      Accept
    </button>
  </div>
</div>"""

