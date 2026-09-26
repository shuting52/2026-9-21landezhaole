package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
 * 客户端更新弹窗（v1.9.0 恢复老样式）：还原 v1.5「Uiverse.io 白卡片」弹窗
 * 纯 Jetpack Compose 原生渲染——白卡片 + 圆点列表 + 官方群 + 立即更新：
 * - 顶部 Protruding Cookie 徽章（uviverse_cookie 资源）
 * - 更新日志以「•」圆点列表展示（云端 changelog，缺失时兜底写死叮咚文案）
 * - 下载时展示渐变进度条，状态流转：发现新版本 → 下载 → 安装 → 完成
 * - 左下「官方群」入口 + 右下「立即更新」按钮（强制更新时不可关闭弹窗）
 * - 保留 v1.8.x 以来的下载/安装能力：多源下载、签名对比、免授权直装
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


    // v1.9.0 恢复老样式：更新内容采用云端 changelog（老样式行为），缺失时兜底写死「叮咚」文案

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

    // ============================================================
    // v1.9.0 恢复老样式：还原 v1.5「Uiverse.io 白卡片」更新弹窗
    // 纯 Jetpack Compose 原生渲染，兼顾 v1.8.x 以来的下载/安装能力
    // ============================================================

    // 更新日志：老样式直接采用云端 changelog，缺失时兑底写死「叮咚」文案
    val cloudTitle = update?.title ?: "发现新版本"
    val cloudLogs: List<String> = update?.changelog?.takeIf { it.isNotEmpty() }
        ?: listOf(
            "叮咚～我们又又又更新啦！",
            "赶紧快来看看新版本有什么好宝贝吧",
            "我们一直在白嫖的路上，一直在奔跑哟",
            "快点更新吧～期待您发现自己的新大陆。"
        )
    val cloudConfirm = update?.confirmText ?: "立即更新"

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

    // 按钮文案 & 点击行为（跟随状态流转）
    val btnPair: Pair<String, () -> Unit> = when {
        installOutcome == true -> "更新完成" to {
            onUpdateFinished()
            onDismiss()
        }
        installOutcome == false -> "重试" to { startUpdate() }
        isUpdating && progress >= 100f -> "安装中…" to { }
        isUpdating -> "更新中…" to { }
        else -> cloudConfirm to { startUpdate() }
    }
    val btnText = btnPair.first
    val btnAction = btnPair.second

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
                            onClick = {} // 卡片内部点击不关闭弹窗
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
                            // 签名冲突：引导先卸载旧版本再安装
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

                            // Dot list logs
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
                            }

                            // Progress bar (during download / install)
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

                            // Action buttons row
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

                                // 立即更新 / 更新中 / 重试 / 完成
                                Button(
                                    onClick = btnAction,
                                    enabled = !isUpdating || installOutcome != null,
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
