package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.data.remote.UpdateDialogDto
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
    forceUpdate: Boolean = false,
    autoDownload: Boolean = false
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isUpdating by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var statusLabel by remember { mutableStateOf("等待更新…") }
    // 签名冲突标记：检测到旧版本签名不一致时引导先卸载再安装
    var isSignatureConflict by remember { mutableStateOf(false) }
    // v1.7.8-fix6：已请求「允许安装未知应用」权限（从系统设置返回后自动继续安装）
    var installPermissionRequested by remember { mutableStateOf(false) }


    // v1.7.4：更新弹窗内容「写死」——后续发布任何版本都不随云端 changelog 变化
    val FIXED_UPDATE_LOGS = listOf(
        "叮咚～我们又又又更新啦！",
        "赶紧快来看看新版本有什么好宝贝吧",
        "我们一直在白嫖的路上，一直在奔跑哟",
        "快点更新吧～期待您发现自己的新大陆。"
    )
    // 云端配置（由控制台发布，实时同步）；标题保留版本号，正文内容固定不变
    val cloudTitle = update?.title ?: "发现新版本"
    val cloudLogs: List<String> = FIXED_UPDATE_LOGS
    val cloudConfirm = update?.confirmText ?: "立即更新"
    val cloudCancel = update?.cancelText ?: "稍后再说"
    val customHtml = update?.customHtml?.takeIf { it.isNotBlank() }

    /**
     * 安装新版本 APK（v1.7.5 参照 AppUpdater 升级）：
     * 1. 安装前对比「已安装旧版本」与「新 APK」签名：不一致时自动引导卸载旧版本（跳系统卸载页），
     *    卸载完成后从保存的安装包重新安装，避免 INSTALL_FAILED_UPDATE_INCOMPATIBLE 安装失败。
     * 2. 签名一致 → 优先 PackageInstaller 系统安装会话（原子化替换旧版本、保留数据），
     *    失败再回退 FileProvider + 系统安装器覆盖安装。
     */
    fun installApk(file: File) {
        try {
            // 读取新 APK 签名证书（SHA-256）
            val newSig = apkSigningHash(context, file)
            // 读取已安装本应用的签名证书
            val installedSig = try {
                val installed = context.packageManager.getPackageInfo(
                    context.packageName,
                    android.content.pm.PackageManager.GET_SIGNING_CERTIFICATES
                )
                val certs = installed.signingInfo?.apkContentsSigners
                certs?.firstOrNull()?.toByteArray()?.let(::sha256Hex)
            } catch (e: Exception) { null }

            if (installedSig != null && newSig != null && installedSig != newSig) {
                // 签名不一致：先把 APK 复制到公共「下载」目录（卸载后容易找到重装），再引导卸载
                isSignatureConflict = true
                statusLabel = "旧版本签名不同，正在引导卸载…"
                // 复制到公共下载目录，卸载后用户可从文件管理器/通知栏直接安装新版本
                var publicApkPath: String? = null
                try {
                    val publicDir = android.os.Environment.getExternalStoragePublicDirectory(
                        android.os.Environment.DIRECTORY_DOWNLOADS
                    )
                    if (publicDir != null) {
                        if (!publicDir.exists()) publicDir.mkdirs()
                        val dest = File(publicDir, "landezhao-${versionName.removePrefix("v")}.apk")
                        file.inputStream().use { input -> dest.outputStream().use { output -> input.copyTo(output) } }
                        publicApkPath = dest.absolutePath
                    }
                } catch (e: Exception) { }
                Toast.makeText(
                    context,
                    if (publicApkPath != null)
                        "检测到旧版本签名不同，请卸载旧版本后，从手机「下载」文件夹安装新版本（已自动拷贝安装包到下载目录）"
                    else
                        "检测到旧版本签名不同，请卸载旧版本后再安装新版本",
                    Toast.LENGTH_LONG
                ).show()
                // 保存待安装 APK 路径，供卸载后自动安装使用
                context.getSharedPreferences("lzdz_update_prefs", Context.MODE_PRIVATE)
                    .edit().putString("pending_install_apk", publicApkPath ?: file.absolutePath).apply()
                // 打开系统卸载界面（卸载旧版本后，用户可从下载目录安装新版本）
                try {
                    val uninstallIntent = Intent(Intent.ACTION_DELETE, Uri.parse("package:" + context.packageName)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(uninstallIntent)
                    coroutineScope.launch {
                        statusLabel = "已打开系统卸载页，卸载后请到手机「下载」文件夹安装新版本"
                        delay(4000)
                        onUpdateFinished()
                        onDismiss()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "无法自动打开卸载页，请手动卸载旧版本后再安装", Toast.LENGTH_LONG).show()
                    isSignatureConflict = false
                    statusLabel = "请先手动卸载旧版本，再安装新版本"
                }
                return
            }

            // 签名一致（或全新安装）：优先 PackageInstaller 系统会话（等待接收器回调驱动 完成动画），失败回退 FileProvider
            // v1.7.8-fix6：先检查安装权限——未授权则引导开启（PackageInstaller 需要该权限）
            if (!hasInstallPermission(context)) {
                isUpdating = false
                installPermissionRequested = true
                statusLabel = "需要开启「允许安装未知应用」权限"
                Toast.makeText(context, "为直接自动安装新版本，请先允许安装未知应用（仅首次需要）", Toast.LENGTH_LONG).show()
                try {
                    val intent = Intent(
                        android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:" + context.packageName)
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                } catch (e: Exception) { }
                return
            }
            if (installViaPackageInstaller(context, file)) {
                // PackageInstaller 会话已提交：保持弹窗显示「安装中」，由 UpdateInstallReceiver 回调驱动 Done/Error
            } else {
                installViaFileProvider(context, file)
                onUpdateFinished()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "自动安装被拦截，请到系统设置允许安装未知应用后重试", Toast.LENGTH_LONG).show()
        }
    }

    /** 跳转官方 QQ 群（优先 mqq 协议直拉 QQ 群，失败则打开网页链接） */
    fun openOfficialGroup() {
        val groupNumber = "439211347"
        val intents = listOf(
            Intent(Intent.ACTION_VIEW, Uri.parse("mqqwpa://im/chat?chat_type=group&uin=$groupNumber&version=1&src_type=web&web_src=oicqzone.com")),
            Intent(Intent.ACTION_VIEW, Uri.parse(OFFICIAL_QQ_GROUP_URL))
        )
        for (intent in intents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return
            } catch (e: Exception) {
                // 继续尝试下一个
            }
        }
        Toast.makeText(context, "打开 QQ 群失败，请手动搜索群号：$groupNumber", Toast.LENGTH_LONG).show()
    }

    /** 同步执行单次下载，返回保存好的 File。本函数会跑在 IO 线程里
     *  v1.7.8：移到 startRealDownload 之前定义（Kotlin 局部函数不支持前向引用）
     *  v1.7.8-fix6：升级为多线程 Range 分块下载（默认 4 线程并行），服务器不支持 Range 时自动回退单线程 */
    suspend fun downloadWithProgress(url: String, onProgress: suspend (Float) -> Unit): File {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val client = okhttp3.OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
                .retryOnConnectionFailure(true)
                .build()
            val request = okhttp3.Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android) LzdzUpdater/1.7.8")
                .header("Accept", "*/*")
                .build()
            val dir = File(context.cacheDir, "update")
            dir.mkdirs()
            val file = File(dir, "latest.apk")

            // 先 HEAD/GET 探测文件大小与 Range 支持
            var total = -1L
            var rangeOk = false
            try {
                client.newCall(request.newBuilder().header("Range", "bytes=0-0").build()).execute().use { probe ->
                    if (probe.isSuccessful && probe.code == 206) {
                        rangeOk = true
                        val cr = probe.header("Content-Range") ?: ""
                        val slash = cr.lastIndexOf('/')
                        if (slash >= 0) total = cr.substring(slash + 1).trim().toLongOrNull() ?: -1L
                    } else if (probe.isSuccessful) {
                        total = probe.body?.contentLength() ?: -1L
                    }
                }
            } catch (_: Exception) {}

            // 多线程分块：仅当服务器支持 Range 且文件 > 3MB 时启用（4 线程并行写入）
            if (rangeOk && total > 3L * 1024 * 1024) {
                try {
                    kotlinx.coroutines.coroutineScope {
                    val threads = 4
                    val chunk = total / threads
                    java.io.RandomAccessFile(file, "rw").use { raf -> raf.setLength(total) }
                    val done = java.util.concurrent.atomic.AtomicLong(0L)
                    val errors = java.util.concurrent.atomic.AtomicInteger(0)
                    val jobs = (0 until threads).map { i ->
                        kotlinx.coroutines.async {
                            val start = i * chunk
                            val end = if (i == threads - 1) total - 1 else (i + 1) * chunk - 1
                            if (start > end) return@async
                            try {
                                val rangeReq = okhttp3.Request.Builder()
                                    .url(url)
                                    .header("User-Agent", "Mozilla/5.0 (Linux; Android) LzdzUpdater/1.7.8")
                                    .header("Range", "bytes=$start-$end")
                                    .build()
                                client.newCall(rangeReq).execute().use { resp ->
                                    if (!resp.isSuccessful) { errors.incrementAndGet(); return@use }
                                    val body = resp.body ?: run { errors.incrementAndGet(); return@use }
                                    body.byteStream().use { input ->
                                        java.io.RandomAccessFile(file, "rw").use { raf ->
                                            raf.seek(start)
                                            val buf = ByteArray(128 * 1024)
                                            while (true) {
                                                val n = input.read(buf)
                                                if (n <= 0) break
                                                raf.write(buf, 0, n)
                                                done.addAndGet(n.toLong())
                                            }
                                        }
                                    }
                                }
                            } catch (e: Exception) { errors.incrementAndGet() }
                        }
                    }
                    while (jobs.any { !it.isCompleted }) {
                        if (errors.get() >= threads) throw Exception("分块下载失败")
                        val frac = (done.get().toFloat() / total.toFloat()).coerceIn(0f, 1f)
                        kotlinx.coroutines.runBlocking { onProgress(frac) }
                        kotlinx.coroutines.delay(150)
                    }
                    jobs.forEach { it.await() }
                    if (errors.get() > 0) throw Exception("分块下载部分失败")
                    onProgress(1f)
                    file
                    }
                } catch (e: Exception) {
                    file.delete()
                    singleStreamDownload(client, request, file, onProgress)
                }
            } else {
                singleStreamDownload(client, request, file, onProgress)
            }
        }
    }

    /** 单线程流式下载（回退方案，兼容不支持 Range 的镜像） */
    suspend fun singleStreamDownload(
        client: okhttp3.OkHttpClient,
        request: okhttp3.Request,
        file: File,
        onProgress: suspend (Float) -> Unit
    ): File {
        client.newCall(request).execute().use { resp ->
            if (!resp.isSuccessful) throw Exception("HTTP ${resp.code}")
            val body = resp.body ?: throw Exception("无响应体")
            val total = body.contentLength()
            file.outputStream().use { output ->
                val buf = ByteArray(64 * 1024)
                var downloaded = 0L
                var lastEmit = 0L
                while (true) {
                    val n = body.byteStream().read(buf)
                    if (n <= 0) break
                    output.write(buf, 0, n)
                    downloaded += n
                    if (total > 0) {
                        val now = System.currentTimeMillis()
                        if (now - lastEmit > 120 || downloaded == total) {
                            lastEmit = now
                            val frac = (downloaded.toFloat() / total.toFloat()).coerceIn(0f, 1f)
                            kotlinx.coroutines.runBlocking { onProgress(frac) }
                        }
                    }
                }
                output.flush()
            }
            file
        }
    }

    /**
     * 进度条直接下载 + 安装（v1.7.8 升级版）
     * - 多源：原 URL → jsDelivr CDN → GitHub 镜像 → gitee 镜像（每个独立重试 2 次）
     * - 超时与异常都被精细捕获，不会再静默掉到系统 DownloadManager
     * - 进度条走满 100% 后立即启动 PackageInstaller 系统安装会话，完成后弹窗呈现「安装成功」
     * - 所有源最终失败时，给出「请到浏览器手动下载」的可点击兜底（不是 DM）
     */
    fun startRealDownload() {
        val url = apkUrl
        if (url.isNullOrBlank()) {
            Toast.makeText(context, "暂无下载链接，请到官方群反馈", Toast.LENGTH_SHORT).show()
            return
        }
        coroutineScope.launch {
            isUpdating = true
            statusLabel = "正在下载更新…"
            progress = 6f

            // 构造下载源列表：原 URL 优先，自动派生 jsDelivr / GitHub raw 镜像
            val candidates = buildList {
                add(url)
                Regex("^https?://raw\\.githubusercontent\\.com/([^/]+)/([^/]+)/(?:main|master)/(.+)$")
                    .find(url)?.let { m ->
                        add("https://cdn.jsdelivr.net/gh/${m.groupValues[1]}/${m.groupValues[2]}@main/${m.groupValues[3]}")
                        add("https://github.com/${m.groupValues[1]}/${m.groupValues[2]}/raw/main/${m.groupValues[3]}")
                        add("https://cdn.jsdmir.cn/gh/${m.groupValues[1]}/${m.groupValues[2]}@main/${m.groupValues[3]}")
                    }
            }.distinct()

            // 重试：每个源最多尝试 2 次（第一次失败马上重试同源，避免抖动）
            var success = false
            var permissionInterrupted = false
            var lastError: Exception? = null
            outer@ for (candidate in candidates) {
                var attempt = 0
                while (attempt < 2 && !success) {
                    attempt++
                    try {
                        statusLabel = if (attempt == 1) "下载中… 0%" else "重试下载… 0%"
                        progress = 8f
                        val file = downloadWithProgress(candidate) { p ->
                            progress = (8f + p * 92f).coerceIn(8f, 100f)
                            statusLabel = "下载中… ${progress.toInt()}%"
                        }
                        // 校验 APK 文件头 PK（ZIP/APK 魔数）
                        // v1.7.8 修复：readNBytes 仅 API 33+ 存在，minSdk 24 下 lint 报错导致构建失败，
                        // 改用兼容的 read 循环读取前 2 字节
                        val header = try {
                            file.inputStream().use { ins ->
                                val h = ByteArray(2)
                                var n = 0
                                while (n < 2) {
                                    val r = ins.read(h, n, 2 - n)
                                    if (r < 0) break
                                    n += r
                                }
                                h
                            }
                        } catch (e: Exception) { ByteArray(0) }
                        if (file.length() < 1024 * 50 ||
                            header.size < 2 ||
                            header[0] != 'P'.code.toByte() ||
                            header[1] != 'K'.code.toByte()
                        ) {
                            throw Exception("下载文件不完整（${file.length()} 字节）")
                        }
                        // 自动清理历史 update 缓存，只保留本次最新
                        try {
                            File(context.cacheDir, "update").listFiles()?.forEach { f ->
                                if (f.absolutePath != file.absolutePath) f.delete()
                            }
                        } catch (_: Exception) {}
                        progress = 100f
                        statusLabel = "下载完成，准备安装…"
                        kotlinx.coroutines.delay(300)
                        // v1.7.8-fix6：安装前检查「允许安装未知应用」权限；未授权先自动引导开启（仅首次）
                        if (!hasInstallPermission(context)) {
                            isUpdating = false
                            installPermissionRequested = true
                            statusLabel = "需要开启「允许安装未知应用」权限"
                            Toast.makeText(context, "为直接自动安装新版本，请先允许安装未知应用（仅首次需要）", Toast.LENGTH_LONG).show()
                            try {
                                val intent = Intent(
                                    android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                                    Uri.parse("package:" + context.packageName)
                                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                            permissionInterrupted = true
                            break@outer
                        }
                        installApk(file)
                        success = true
                        break@outer
                    } catch (e: Exception) {
                        lastError = e
                        statusLabel = "下载失败，重试中…"
                        kotlinx.coroutines.delay(900)
                    }
                }
                if (!success) {
                    // 当前源失败，切换到下一个
                    statusLabel = "切换下载源…"
                    kotlinx.coroutines.delay(600)
                }
            }

            if (!success && !permissionInterrupted) {
                // 所有源彻底失败：弹窗仍保持打开，提供「浏览器下载兜底」而非 DM 静默转发
                statusLabel = "下载失败，请尝试浏览器下载"
                Toast.makeText(
                    context,
                    "进度下载失败（${lastError?.message ?: "未知原因"}），已为你打开浏览器下载，请手动安装。",
                    Toast.LENGTH_LONG
                ).show()
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                } catch (_: Exception) {}
                isUpdating = false
                onUpdateFinished()
                onDismiss()
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

    // 自动下载模式：弹窗出现后自动开始下载新版本（无需手动点击「立即更新」）
    LaunchedEffect(Unit) {
        if (autoDownload && !apkUrl.isNullOrBlank() && !isUpdating) {
            // 稍作延迟，让弹窗先渲染出来
            delay(400)
            startRealDownload()
        }
    }

    fun startUpdate() {
        if (isUpdating) return
        // v1.7.8-fix6：下载/安装前先确认「允许安装未知应用」权限，未授权先引导开启（避免下载完才失败）
        if (!hasInstallPermission(context)) {
            installPermissionRequested = true
            statusLabel = "需要开启「允许安装未知应用」权限"
            Toast.makeText(context, "为直接自动安装新版本，请先允许安装未知应用（仅首次需要）", Toast.LENGTH_LONG).show()
            try {
                val intent = Intent(
                    android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:" + context.packageName)
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (e: Exception) { }
            return
        }
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
                // v1.7.8：强制更新时任何方式都不可关闭（返回键/点外部都不行），
                // 必须完成「立即更新」安装；仅非强制且非下载中才允许关闭
                dismissOnBackPress = !isUpdating && !forceUpdate,
                dismissOnClickOutside = !isUpdating && !forceUpdate
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

    // v1.7.6：默认呈现——完全采用 AppUpdater 风格动态卡通弹窗（Canvas 手绘猫咪 + 进度环 + 均衡器 + 火箭 + 彩带）
    // 屏幕中间呈现、全动画；更新内容固定写死「叮咚」文案，下载/安装/完成全程动画驱动。
    val cartoonInfo = CartoonUpdateInfo(
        versionName = versionName.removePrefix("v"),
        downloadUrl = apkUrl ?: "",
        forceUpdate = forceUpdate
    )

    // 安装结果（PackageInstaller 广播回调）驱动 安装中→完成/失败 状态
    var installOutcome by remember { mutableStateOf<Boolean?>(null) }
    LaunchedEffect(Unit) {
        UpdateInstallReceiver.Results.flow.collect { (success, msg) ->
            installOutcome = success
            if (success) {
                progress = 100f
                statusLabel = "安装完成"
            } else {
                statusLabel = msg.ifBlank { "安装失败，请检查是否已开启「允许安装未知应用」权限" }
            }
        }
    }

    // 从系统设置返回：若已开启安装权限，自动继续安装已下载的 APK（无需再次点击）
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME &&
                installPermissionRequested && hasInstallPermission(context)
            ) {
                installPermissionRequested = false
                val cached = File(context.cacheDir, "update/latest.apk")
                if (cached.exists() && cached.length() > 1024 * 50) {
                    isUpdating = true
                    installApk(cached)
                } else {
                    startUpdate()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val cartoonState: CartoonUpdateState = when {
        // 签名冲突：引导卸载（installApk 已自动跳系统卸载页）
        isSignatureConflict -> CartoonUpdateState.Error(
            "检测到旧版本签名不同，已引导卸载旧版本\n卸载完成后重新打开本软件即可自动安装新版本",
            canRetry = false
        )
        // 下载完成且安装成功：完成庆祝
        isUpdating && installOutcome == true -> CartoonUpdateState.Done(installed = true)
        // 下载完成/安装失败：错误可重试
        isUpdating && installOutcome == false -> CartoonUpdateState.Error(
            statusLabel.ifBlank { "安装失败，请重试" },
            canRetry = true
        )
        // 下载中：进度环
        isUpdating && progress < 100f -> CartoonUpdateState.Downloading(
            progress = (progress / 100f).coerceIn(0f, 1f),
            bytesDownloaded = 0L,
            totalBytes = 0L
        )
        // 下载完成准备安装：正在安装
        isUpdating -> CartoonUpdateState.Installing
        // v1.7.8-fix6：需要用户先开启「允许安装未知应用」权限（从设置返回后自动续装）
        installPermissionRequested && !hasInstallPermission(context) -> CartoonUpdateState.NeedInstallPermission
        // 默认：发现新版本（写死叮咚文案）
        else -> CartoonUpdateState.Found(cartoonInfo)
    }

    CartoonUpdateDialog(
        state = cartoonState,
        currentVersion = com.example.BuildConfig.VERSION_NAME,
        newVersion = versionName.removePrefix("v"),
        onStartDownload = { startUpdate() },
        onInstall = { startUpdate() },
        onOpenInstallSettings = {
            try {
                val intent = Intent(
                    android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:" + context.packageName)
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (e: Exception) { }
        },
        onDismiss = { closeUpdate() },
        onRetry = { startUpdate() },
        onDone = {
            onUpdateFinished()
            onDismiss()
        },
        onRestartApp = {
            onUpdateFinished()
            onDismiss()
            try {
                val launch = context.packageManager.getLaunchIntentForPackage(context.packageName)
                    ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                if (launch != null) {
                    context.startActivity(launch)
                    Runtime.getRuntime().exit(0)
                }
            } catch (e: Exception) { }
        },
        // v1.7.8：强制更新弹窗内提供官方群入口（mqq 直拉，失败跳网页）
        onOpenGroup = { openOfficialGroup() }
    )
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


// ============ 顶层辅助函数（APK 签名对比，供更新弹窗使用） ============

/**
 * PackageInstaller 系统安装会话（v1.7.5 参照 AppUpdater）：
 * 由系统原子化完成「卸载旧版本 + 安装新版本」替换，签名一致时自动覆盖、数据保留。
 * @return 是否成功提交会话
 */
private fun installViaPackageInstaller(context: Context, apkFile: File): Boolean {
    return try {
        val packageInstaller = context.packageManager.packageInstaller
        val params = android.content.pm.PackageInstaller.SessionParams(
            android.content.pm.PackageInstaller.SessionParams.MODE_FULL_INSTALL
        )
        params.setAppPackageName(context.packageName)
        val sessionId = packageInstaller.createSession(params)
        val session = packageInstaller.openSession(sessionId)
        try {
            session.openWrite("lzdz_update.apk", 0, apkFile.length()).use { out ->
                apkFile.inputStream().use { input -> input.copyTo(out) }
            }
        } finally {
            session.close()
        }
        val receiverIntent = Intent(context, UpdateInstallReceiver::class.java)
        val pending = android.app.PendingIntent.getBroadcast(
            context,
            100,
            receiverIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        session.commit(pending.intentSender)
        true
    } catch (e: Exception) {
        false
    }
}

/** FileProvider + 系统安装器（最通用的兑底方案） */
private fun installViaFileProvider(context: Context, file: File) {
    try {
        val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "自动安装被拦截，请到系统设置允许安装未知应用后重试", Toast.LENGTH_LONG).show()
    }
}

/** 提取 APK 签名证书 SHA-256（十六进制小写） */
private fun apkSigningHash(context: Context, file: File): String? {
    return try {
        val pm = context.packageManager
        val info = pm.getPackageArchiveInfo(
            file.absolutePath,
            android.content.pm.PackageManager.GET_SIGNING_CERTIFICATES
        ) ?: return null
        val certs = info.signingInfo?.apkContentsSigners ?: return null
        certs.firstOrNull()?.toByteArray()?.let(::sha256Hex)
    } catch (e: Exception) { null }
}

/** SHA-256 十六进制（用于签名对比） */
private fun sha256Hex(bytes: ByteArray): String {
    val md = java.security.MessageDigest.getInstance("SHA-256")
    return md.digest(bytes).joinToString("") { "%02x".format(it) }
}


/** 单线程流式下载（回退方案，兼容不支持 Range 的镜像）-- 顶层函数（v1.7.8-fix6 修复前向引用） */
private suspend fun singleStreamDownload(
    client: okhttp3.OkHttpClient,
    request: okhttp3.Request,
    file: File,
    onProgress: suspend (Float) -> Unit
): File {
    client.newCall(request).execute().use { resp ->
        if (!resp.isSuccessful) throw Exception("HTTP ${resp.code}")
        val body = resp.body ?: throw Exception("无响应体")
        val total = body.contentLength()
        file.outputStream().use { output ->
            val buf = ByteArray(64 * 1024)
            var downloaded = 0L
            var lastEmit = 0L
            while (true) {
                val n = body.byteStream().read(buf)
                if (n <= 0) break
                output.write(buf, 0, n)
                downloaded += n
                if (total > 0) {
                    val now = System.currentTimeMillis()
                    if (now - lastEmit > 120 || downloaded == total) {
                        lastEmit = now
                        val frac = (downloaded.toFloat() / total.toFloat()).coerceIn(0f, 1f)
                        kotlinx.coroutines.runBlocking { onProgress(frac) }
                    }
                }
            }
            output.flush()
        }
        file
    }
}

/** 是否已具备「允许安装未知应用」权限（Android 8+ 才需要检查）-- 顶层函数（v1.7.8-fix6 修复前向引用） */
private fun hasInstallPermission(ctx: Context): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.O || ctx.packageManager.canRequestPackageInstalls()
}
