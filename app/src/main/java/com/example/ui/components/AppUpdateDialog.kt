package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.remote.UpdateDialogDto
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 官方 QQ 群链接（与设置页一致）
 */
const val OFFICIAL_QQ_GROUP_URL =
    // v1.0.11 修复：同步为官方 qm.qq.com 短链（与 admin-data.json settings.qqGroupUrl 一致）。
    // 旧 qun.qq.com/universal-share 长链带 authKey 签名，部分环境下被拦截/打开异常，导致「官方群跳不过去」。
    "https://qm.qq.com/q/BnUzn164u21Cu1dG7vAVYJqU_4hw0COArsGrrBOIc0vxu7ES6gOJcYyrpu2JgkVs"

// ============================================================
// v1.0.2+ 更新弹窗：Uiverse.io 白卡片老样式（v1.5 原版还原）
// 纯 Jetpack Compose 原生渲染——白卡片 + 圆点列表 + 官方群 + 立即更新
// - 内容写死（不再随云端 changelog 变化）
// - v1.0.11 追溯 1.0.3 下载/安装方式：多源 OkHttp 串行下载（raw/jsDelivr/jsdmir/github 四源）→
//   流式 PK 头校验（修复 readBytes 整读 OOM）→ PackageInstaller 会话直装（免授权），
//   仅由 receiver 回调驱动结果，不再叠加看门狗/FileProvider/版本轮询兜底（避免安装界面打架）
// - 官方群：mqqapi(sharecard)/universal-share 网页/qqmobile/mqqwpa 逐级 try/catch 直拉
//   （修复 source=qrcode 扫码页假成功阻断后续兜底，导致「官方群点了没反应」）
// （旧手绘猫咪弹窗已彻底删除，统一由本入口接管）
// ============================================================

/** 写死的更新文案（用户指定，任何版本发布都不变） */
private val FIXED_UPDATE_LOGS = listOf(
    "叮咚~我们又又又更新啦",
    "快来瞧一瞧新版本更新了什么内容吧",
    "我们一直在努力的收录白嫖资源",
    "若您有什么好的资源请联系我们"
)

/**
 * 客户端更新弹窗（白卡片老样式版）：
 * 状态流转：发现新版本 → 下载(渐变进度条) → 安装 → 完成/重试
 * 强制更新不可关闭；自动下载模式开启时弹窗即自动开始下载
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
    // 安装结果：PackageInstaller 回调驱动（true=成功 false=失败 null=进行中）
    var installOutcome by remember { mutableStateOf<Boolean?>(null) }

    // 订阅 PackageInstaller 回调（成功/失败直接驱动状态）
    LaunchedEffect(Unit) {
        UpdateInstallReceiver.Results.flow.collect { (success, msg) ->
            installOutcome = success
            if (success) {
                progress = 100f
                statusLabel = "安装完成"
            } else {
                statusLabel = msg.ifBlank { "安装未完成，请重新点击更新重试" }
            }
        }
    }

    /** 安装新版本 APK（v1.0.10 追溯 1.0.3 方式）：
     *  1. 签名对比：新旧签名不一致时引导先卸载旧版本再安装
     *  2. 签名一致 → PackageInstaller 系统会话（免授权直装），仅由 receiver 回调驱动结果
     *  3. 会话提交失败 → 驱动弹窗进入失败态（可重试），不做需授权的兑底安装
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
                context.getSharedPreferences("lzdz_update_prefs", Context.MODE_PRIVATE)
                    .edit().putString("pending_install_apk", publicApkPath ?: file.absolutePath).apply()
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

            // 签名一致（或全新安装）：PackageInstaller 系统安装会话（免授权直装，无任何「允许未知应用」申请）
            // v1.0.10 追溯 1.0.3 方式：提交会话后只等 receiver 回调驱动结果，
            // 不再叠加看门狗/FileProvider/版本轮询兜底——避免「系统确认页 + 兜底安装器」两个安装界面打架导致安装错误
            if (!installViaPackageInstaller(context, file)) {
                // 会话提交失败：驱动弹窗进入失败态（可重试），不做任何需授权的兑底安装
                UpdateInstallReceiver.Results.emit(success = false, message = "系统安装会话提交失败，请重新点击更新重试")
            }
        } catch (e: Exception) {
            UpdateInstallReceiver.Results.emit(success = false, message = "安装过程异常，请重新点击更新重试")
        }
    }

    /** 跳转官方 QQ 群（v1.0.11 修复「点了没反应/跳不到群」）：
     *  1. mqqapi 加群卡片协议（source=sharecard 新版分享卡片；旧版 source=qrcode 是扫码场景，
     *     QQ 打开后只显示扫码页不显示群卡片，且 startActivity 成功即 return 阻断后续兜底——这是「无法跳转」根因）
     *  2. 官方 universal-share 长链网页（qun.qq.com 官方加群页，浏览器必开，页内「打开QQ」可二次拉起 QQ 群）
     *  3. qqmobile 网页（老版加群页兜底）
     *  4. mqqwpa 老协议（QQ 新版已停用，放最后避免无效拉起阻断网页兜底）
     *  5. 全部失败 Toast 群号
     *  v1.0.10 修复：去掉 resolveActivity 预检（Android 11+ 对自定义 scheme 预检常误判 null 而跳过 QQ 协议），
     *  直接 startActivity try/catch 逐级尝试，确保能拉起 QQ 或浏览器。 */
    fun openOfficialGroup() {
        val groupNumber = "439211347"
        val now = System.currentTimeMillis() / 1000
        // 新版 QQ 加群卡片：source=sharecard（分享卡片场景，直接展示群信息）；source=qrcode 是扫码场景不适用
        val intents = listOf(
            Intent(Intent.ACTION_VIEW, Uri.parse("mqqapi://card/show_pslcard?src_type=internal&version=1&uin=$groupNumber&card_type=group&source=sharecard")),
            Intent(Intent.ACTION_VIEW, Uri.parse(OFFICIAL_QQ_GROUP_URL)),
            Intent(Intent.ACTION_VIEW, Uri.parse("https://qun.qq.com/qqmobile/?uin=$groupNumber&clicktime=$now")),
            Intent(Intent.ACTION_VIEW, Uri.parse("mqqwpa://im/chat?chat_type=group&uin=$groupNumber&version=1&src_type=web&web_src=oicqzone.com"))
        )
        for (intent in intents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return
            } catch (e: Exception) {
                // 未安装 QQ / 无法解析：继续尝试下一个（网页链接总能被浏览器接收）
            }
        }
        Toast.makeText(context, "打开 QQ 群失败，请手动搜索群号：$groupNumber", Toast.LENGTH_LONG).show()
    }

    /**
     * 多源下载 + 安装（v1.0.11 追溯 1.0.3 方式）：
     * 原 URL → jsDelivr CDN → jsdmir 国内镜像 → GitHub raw 串行尝试（每源一次），
     * 下载流式校验 PK 头后进入 installApk。
     * 全部失败时复位状态并提示重试（不做浏览器/DownloadManager 等需授权的兜底）。
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
            progress = 12f
            try {
                // 多源下载：raw.githubusercontent 不可达时自动切换 jsDelivr CDN 镜像 / jsdmir 国内镜像 / github 直链
                val candidates = buildList {
                    add(url)
                    // 转换 raw.githubusercontent.com/owner/repo/main/path -> cdn.jsdelivr.net/gh/owner/repo@main/path
                    Regex("^https?://raw\\.githubusercontent\\.com/([^/]+)/([^/]+)/(?:main|master)/(.+)$")
                        .find(url)?.let { m ->
                            add("https://cdn.jsdelivr.net/gh/${m.groupValues[1]}/${m.groupValues[2]}@main/${m.groupValues[3]}")
                            add("https://cdn.jsdmir.cn/gh/${m.groupValues[1]}/${m.groupValues[2]}@main/${m.groupValues[3]}")
                            add("https://github.com/${m.groupValues[1]}/${m.groupValues[2]}/raw/main/${m.groupValues[3]}")
                        }
                }.distinct()

                var lastError: Exception? = null
                var installed = false
                for (candidate in candidates) {
                    if (installed) break
                    try {
                        // 带 User-Agent 的 OkHttp 下载（raw 可能拒绝无 UA 请求）
                        val client = okhttp3.OkHttpClient.Builder()
                            .connectTimeout(15, TimeUnit.SECONDS)
                            .readTimeout(90, TimeUnit.SECONDS)
                            .followRedirects(true)
                            .build()
                        val request = okhttp3.Request.Builder()
                            .url(candidate)
                            .header("User-Agent", "Mozilla/5.0 (Linux; Android) LzdzUpdater/1.5.2")
                            .build()
                        client.newCall(request).execute().use { resp ->
                            if (!resp.isSuccessful) throw Exception("HTTP ${resp.code}")
                            val body = resp.body ?: throw Exception("无响应体")
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
                            // 校验 APK 文件头 PK（ZIP/APK 魔数）。
                            // v1.0.11 修复（追溯 1.0.3）：禁止 readBytes() 整读——大 APK 会 OOM 崩溃导致「下载完安装错误」，
                            // 改为流式读前 2 字节（read 循环兼容 minSdk 24，避免 readNBytes 仅 API 33+ 的 lint 报错）。
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
                            // 自动删除旧版本的 APK 缓存文件（只保留最新下载的安装包）
                            try {
                                val oldDir = File(context.cacheDir, "update")
                                oldDir.listFiles()?.forEach { f ->
                                    if (f.absolutePath != file.absolutePath) f.delete()
                                }
                            } catch (_: Exception) {}
                            progress = 100f
                            statusLabel = "下载完成，准备安装…"
                            delay(300)
                            installApk(file)
                            installed = true
                        }
                    } catch (e: Exception) {
                        lastError = e
                        progress = 8f
                        statusLabel = "切换下载源…"
                    }
                }
                if (!installed) {
                    throw lastError ?: Exception("所有下载源均失败")
                }
            } catch (e: Exception) {
                // 免授权直装模式：不再提供 DownloadManager / 浏览器等需授权安装的兑底，直接提示失败并复位
                Toast.makeText(
                    context,
                    "自动下载失败，请检查网络后重试（官方群号 439211347）",
                    Toast.LENGTH_LONG
                ).show()
                statusLabel = "等待更新…"
                progress = 0f
                isUpdating = false
            }
        }
    }

    // 自动下载模式：弹窗出现后自动开始下载新版本
    LaunchedEffect(Unit) {
        if (autoDownload && !apkUrl.isNullOrBlank() && !isUpdating) {
            delay(400)
            startRealDownload()
        }
    }

    fun startUpdate() {
        if (isUpdating && installOutcome == null) return
        // 安装失败重试时，先复位状态再启动
        if (installOutcome == false) {
            installOutcome = null
            isUpdating = false
            progress = 0f
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

    // 强制更新（forceUpdate）时不允许关闭弹窗
    fun closeUpdate() {
        if (!isUpdating && !forceUpdate) {
            onDismiss()
        }
    }

    // ============================================================
    // Uiverse.io 白卡片弹窗 UI（v1.5 原版还原）
    // ============================================================

    // 标题：固定「发现新版本」（不随云端变化，保持老样式行为）
    val cloudTitle = update?.title?.takeIf { it.isNotBlank() } ?: "发现新版本"
    val cloudLogs: List<String> = FIXED_UPDATE_LOGS
    val cloudConfirm = update?.confirmText?.takeIf { it.isNotBlank() } ?: "立即更新"

    // 按钮文案 & 点击行为
    val installingNow = isUpdating && progress >= 100f && installOutcome == null
    val btnText: String
    val btnAction: () -> Unit
    val btnEnabled: Boolean
    when {
        installOutcome == true -> {
            btnText = "更新完成"
            btnAction = {
                onUpdateFinished()
                onDismiss()
            }
            btnEnabled = true
        }
        installOutcome == false -> {
            btnText = "重试"
            btnAction = { startUpdate() }
            btnEnabled = true
        }
        installingNow -> {
            btnText = "安装中…"
            btnAction = {}
            btnEnabled = false
        }
        isUpdating -> {
            btnText = "更新中…"
            btnAction = {}
            btnEnabled = false
        }
        else -> {
            btnText = cloudConfirm
            btnAction = { startUpdate() }
            btnEnabled = true
        }
    }

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
                            onClick = {}
                        )
                        .testTag("uiverse_dialog_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 36.dp, start = 24.dp, end = 24.dp, bottom = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isSignatureConflict) {
                            // 签名冲突：白卡片内提示卸载引导
                            Text(
                                text = statusLabel,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF3F3F46),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "卸载完成后重新打开本软件即可自动安装新版本",
                                fontSize = 12.sp,
                                color = Color(0xFF52525B),
                                lineHeight = 16.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "官方群",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF71717A),
                                    modifier = Modifier
                                        .clickable { openOfficialGroup() }
                                        .padding(vertical = 6.dp)
                                )
                            }
                        } else {
                            // Title
                            Text(
                                text = cloudTitle,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF3F3F46),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("uiverse_title")
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // 圆点列表（写死文案）
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
                                            Text(
                                                text = "•",
                                                fontSize = 12.sp,
                                                color = Color(0xFF9C6750),
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = log,
                                                fontSize = 12.sp,
                                                color = Color(0xFF52525B),
                                                lineHeight = 16.sp
                                            )
                                        }
                                    }
                                }
                            }

                            // 渐变进度条（下载/安装中）
                            if (isUpdating) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = when {
                                                installOutcome == true -> "更新完成，重新打开即最新版"
                                                progress >= 100f -> "下载完成，正在安装…"
                                                else -> "正在极速下载…"
                                            },
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

                            // 按钮行：左下官方群 + 右下立即更新
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 官方群（强制更新时也保留入口）
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

                                // 立即更新 / 更新中 / 安装中 / 完成 / 重试
                                Button(
                                    onClick = btnAction,
                                    enabled = btnEnabled,
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
                                        text = btnText,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
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

// ============================================================
// 底层辅助：签名对比 / PackageInstaller 直装（v1.0.10 追溯 1.0.3，免授权直装）
// ============================================================

/**
 * PackageInstaller 系统安装会话（v1.7.5 参照 AppUpdater）：
 * - 修复：PendingIntent 强制 FLAG_MUTABLE（Android 12+ 系统需向回调 intent 注入安装状态，
 *   IMMUTABLE 会导致部分设备上安装结果回调永远不送达 → 弹窗卡在「正在安装」）
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
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_MUTABLE
        )
        session.commit(pending.intentSender)
        true
    } catch (e: Exception) {
        false
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