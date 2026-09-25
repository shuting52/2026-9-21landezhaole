package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.data.remote.UpdateDialogDto
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * 官方 QQ 群链接（与设置页一致）
 */
const val OFFICIAL_QQ_GROUP_URL =
    "https://qun.qq.com/universal-share/share?ac=1&authKey=gtnBoTi8HEzXQAF9x40Y5GYQtubkWu4pGDJg7OuNQte9oz3sXiFonGqZaUXxjffu&busi_data=eyJncm91cENvZGUiOiI0MzkyMTEzNDciLCJ0b2tlbiI6IkVxeXJDb0tyVjM3Y0VIRmhZQ3M5eDg4VW5MYWU0RW4ybVlSRlBlS2ozQXRxanB5V2ZtNzNHMlRIa2ZRd0VTQnUiLCJ1aW4iOiIzMDc3Nzk1MjMifQ%3D%3D&data=QnUzn164u21Cu1dG7vAVYJqU_4hw0COArsGrrBOIc0vxu7ES6gOJcYyrpu2JgkVs-y3X0ZUGZb_nPBJsBTRccQ&svctype=4&tempid=h5_group_info"

/**
 * 客户端更新弹窗（v1.8.5）：固定呈现「手绘绘制弹窗」
 * 完全采用 Canvas 手绘猫咪动画（进度环 / 均衡器 / 火箭 / 彩带），屏幕中间展示：
 * - 状态流转：发现新版本 → 下载(进度环) → 安装(火箭) → 完成(彩带)
 * - 强制更新时不可关闭，仅「立即更新」+「官方群」入口
 * - 已移除旧版 WebView / customHtml 自定义弹窗分支，云端配置不再影响弹窗样式
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


    // v1.8.5：更新内容固定采用 CartoonUpdateInfo.FIXED_NOTES（叮咚文案），不随云端 changelog 变化

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
            if (installViaPackageInstaller(context, file)) {
                // PackageInstaller 会话已提交：保持弹窗显示「安装中」，由 UpdateInstallReceiver 回调驱动 Done/Error
            } else {
                installViaFileProvider(context, file)
                onUpdateFinished()
            }
        } catch (e: Exception) {
            // v1.8.4：不再引导「允许安装未知应用」——静默失败，交由系统安装器自动处理
            Toast.makeText(context, "安装未能自动完成，请重新点击更新再试", Toast.LENGTH_LONG).show()
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
     *  v1.7.8：移到 startRealDownload 之前定义（Kotlin 局部函数不支持前向引用） */
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
            client.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) throw Exception("HTTP ${resp.code}")
                val body = resp.body ?: throw Exception("无响应体")
                val total = body.contentLength()
                val dir = File(context.cacheDir, "update")
                dir.mkdirs()
                val file = File(dir, "latest.apk")
                body.byteStream().use { input ->
                    file.outputStream().use { output ->
                        val buf = ByteArray(16 * 1024)
                        var downloaded = 0L
                        var lastEmit = 0L
                        while (true) {
                            val n = input.read(buf)
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
                }
                file
            }
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

            if (!success) {
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

    // v1.8.5：更新弹窗固定采用「手绘绘制弹窗」——Canvas 手绘猫咪动画，屏幕中间呈现
    // 已移除旧版 WebView / customHtml 自定义弹窗分支，无论云端如何配置都只展示卡通手绘弹窗
    // v1.7.6：完全采用 AppUpdater 风格动态卡通弹窗（Canvas 手绘猫咪 + 进度环 + 均衡器 + 火箭 + 彩带）
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
                // v1.8.4：安装失败提示不再提「允许安装未知应用」
                statusLabel = msg.ifBlank { "安装未完成，请重新点击更新重试" }
            }
        }
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
        // 默认：发现新版本（写死叮咚文案）
        else -> CartoonUpdateState.Found(cartoonInfo)
    }

    CartoonUpdateDialog(
        state = cartoonState,
        currentVersion = com.example.BuildConfig.VERSION_NAME,
        newVersion = versionName.removePrefix("v"),
        onStartDownload = { startUpdate() },
        onInstall = { startUpdate() },
        // v1.8.2：移除「授权未知应用程序」引导，改为走系统原生安装流程（FileProvider 打开系统安装器）
        onOpenInstallSettings = {
            Toast.makeText(
                context,
                "将自动调用系统安装器完成安装，请在弹出的系统页面中确认",
                Toast.LENGTH_LONG
            ).show()
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
        // v1.8.4：FileProvider 打开系统安装器失败时不引导设置，中性提示即可
        Toast.makeText(context, "无法打开系统安装器，请稍后到文件管理器中手动安装更新包", Toast.LENGTH_LONG).show()
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
