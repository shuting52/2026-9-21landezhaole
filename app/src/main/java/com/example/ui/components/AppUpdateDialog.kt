package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.example.data.remote.UpdateDialogDto
import com.example.ui.theme.CuteLemon
import com.example.ui.theme.CuteMint
import com.example.ui.theme.CutePeach
import com.example.ui.theme.CutePink
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 官方 QQ 群链接（与设置页一致）
 */
const val OFFICIAL_QQ_GROUP_URL =
    "https://qun.qq.com/universal-share/share?ac=1&authKey=gtnBoTi8HEzXQAF9x40Y5GYQtubkWu4pGDJg7OuNQte9oz3sXiFonGqZaUXxjffu&busi_data=eyJncm91cENvZGUiOiI0MzkyMTEzNDciLCJ0b2tlbiI6IkVxeXJDb0tyVjM3Y0VIRmhZQ3M5eDg4VW5MYWU0RW4ybVlSRlBlS2ozQXRxanB5V2ZtNzNHMlRIa2ZRd0VTQnUiLCJ1aW4iOiIzMDc3Nzk1MjMifQ%3D%3D&data=QnUzn164u21Cu1dG7vAVYJqU_4hw0COArsGrrBOIc0vxu7ES6gOJcYyrpu2JgkVs-y3X0ZUGZb_nPBJsBTRccQ&svctype=4&tempid=h5_group_info"

// ============================================================
// v1.0.2 更新弹窗：全新 CSS 动态动画风格（可爱卡通纯色主题）
// 内容按用户要求写死为固定叮咚文案：
//   1. 叮咚~我们又又又更新啦
//   2. 快来瞧一瞧新版本更新了什么内容吧
//   3. 我们一直在努力的收录白嫖资源
//   4. 若您有什么好的资源请联系我们
// ============================================================

/** 写死的更新文案（用户指定，任何版本发布都不随云端 changelog 变化） */
private val FIXED_UPDATE_LOGS = listOf(
    "叮咚~我们又又又更新啦",
    "快来瞧一瞧新版本更新了什么内容吧",
    "我们一直在努力的收录白嫖资源",
    "若您有什么好的资源请联系我们"
)

/** 可爱卡通 CSS 主题色板（贴近 v1.0.1 可爱主题） */
private val CSS_PINK = CutePink
private val CSS_PEACH = CutePeach
private val CSS_LEMON = CuteLemon
private val CSS_MINT = CuteMint

/**
 * 客户端更新弹窗（v1.0.2 大改）：
 * - 全新 CSS 动态动画呈现：顶部渐变流光横幅 + 漂浮粒子 + 圆点列表呼吸动画 + 渐变流动进度条 + 渐变脉冲按钮
 * - 内容写死（FIXED_UPDATE_LOGS），不随云端 changelog 变化
 * - 修复「卡在下载完成正在安装」：PackageInstaller 回调改 MUTABLE + 25s 看门狗超时回退系统安装器
 *   + 版本号轮询兜底检测（用户手动装完后弹窗自动进入完成态）
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
    var isSignatureConflict by remember { mutableStateOf(false) }
    // 安装结果：PackageInstaller 回调 / 看门狗轮询 共同驱动（true=成功 false=失败 null=进行中）
    var installOutcome by remember { mutableStateOf<Boolean?>(null) }
    // 安装前已装版本 code，用于轮询判断升级是否完成
    var oldVersionCode by remember { mutableStateOf(-1) }

    // 当前已安装版本 code（每次读取实时值）
    fun currentVersionCode(): Int = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionCode
    } catch (e: Exception) {
        -1
    }

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

    /** 安装新版本 APK：
     *  1. 签名对比：新旧签名不一致时引导先卸载旧版本再安装
     *  2. 签名一致 → PackageInstaller 系统会话（免授权直装）
     *  3. 看门狗：25 秒内未收到回调 → 自动回退 FileProvider 打开系统安装器
     *  4. 全程轮询版本号，装完自动进入完成态（彻底杜绝「卡在安装中」）
     */
    fun installApk(file: File) {
        try {
            val newSig = apkSigningHash(context, file)
            val installedSig = try {
                val installed = context.packageManager.getPackageInfo(
                    context.packageName,
                    android.content.pm.PackageManager.GET_SIGNING_CERTIFICATES
                )
                val certs = installed.signingInfo?.apkContentsSigners
                certs?.firstOrNull()?.toByteArray()?.let(::sha256Hex)
            } catch (e: Exception) { null }

            if (installedSig != null && newSig != null && installedSig != newSig) {
                // 签名冲突：引导卸载
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

            // 签名一致（或全新安装）→ 先记录安装前版本号
            oldVersionCode = currentVersionCode()
            installOutcome = null

            // 优先 PackageInstaller 系统会话（Android 12+ 需 MUTABLE 回调，见 installViaPackageInstaller）
            val committed = installViaPackageInstaller(context, file)
            if (committed) {
                statusLabel = "正在安装…"
            } else {
                // PackageInstaller 不可用 → 直接 FileProvider 打开系统安装器
                statusLabel = "正在打开系统安装器…"
                installViaFileProvider(context, file)
            }

            // ============ 看门狗 + 版本轮询兜底（v1.0.2 核心修复）============
            coroutineScope.launch {
                // 阶段一：等待 PackageInstaller 回调或版本变化（足够覆盖系统安装确认页）
                if (committed) {
                    val deadline = System.currentTimeMillis() + 60_000L
                    while (System.currentTimeMillis() < deadline && installOutcome == null) {
                        delay(600)
                        if (currentVersionCode() > oldVersionCode) break
                    }
                }
                // 阶段二：回调迟迟未到且版本未变 → FileProvider 打开系统安装器兜底（保证一定能装）
                if (installOutcome == null && currentVersionCode() <= oldVersionCode) {
                    statusLabel = "自动安装未响应，正在打开系统安装器…"
                    installViaFileProvider(context, file)
                }
                // 阶段三：持续轮询版本号（最长 120s），装完自动进入完成态
                if (installOutcome == null) {
                    val totalWait = System.currentTimeMillis() + 120_000L
                    while (installOutcome == null && System.currentTimeMillis() < totalWait) {
                        delay(1200)
                        if (currentVersionCode() > oldVersionCode) {
                            installOutcome = true
                            progress = 100f
                            statusLabel = "安装完成"
                            return@launch
                        }
                    }
                    // 长时间未安装成功：将按钮转为「重试」让用户可控
                    if (installOutcome == null) {
                        installOutcome = false
                        statusLabel = "安装未完成，请点击重试"
                    }
                }
            }
        } catch (e: Exception) {
            installOutcome = false
            statusLabel = "安装未能自动完成，请重新点击更新再试"
            Toast.makeText(context, "安装未能自动完成，请重新点击更新再试", Toast.LENGTH_LONG).show()
        }
    }

    /** 跳转官方 QQ 群（mqq 直拉 → 网页兜底 → Toast 提示群号） */
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

    /** 同步执行单次下载，返回保存好的 File（跑在 IO 线程） */
    suspend fun downloadWithProgress(url: String, onProgress: suspend (Float) -> Unit): File {
        return withContext(kotlinx.coroutines.Dispatchers.IO) {
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
                                    onProgress(frac)
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
     * 多源下载 + 安装：原 URL → jsDelivr CDN → GitHub raw → jsdmir 镜像（每源重试 2 次）
     * 下载完成校验 PK 头后进入 installApk（含看门狗兜底）
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

            val candidates = buildList {
                add(url)
                Regex("^https?://raw\\.githubusercontent\\.com/([^/]+)/([^/]+)/(?:main|master)/(.+)$")
                    .find(url)?.let { m ->
                        add("https://cdn.jsdelivr.net/gh/${m.groupValues[1]}/${m.groupValues[2]}@main/${m.groupValues[3]}")
                        add("https://github.com/${m.groupValues[1]}/${m.groupValues[2]}/raw/main/${m.groupValues[3]}")
                        add("https://cdn.jsdmir.cn/gh/${m.groupValues[1]}/${m.groupValues[2]}@main/${m.groupValues[3]}")
                    }
            }.distinct()

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
                        // 校验 APK 文件头 PK
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
                        // 清理历史 update 缓存，只保留本次最新
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
                    statusLabel = "切换下载源…"
                    kotlinx.coroutines.delay(600)
                }
            }

            if (!success) {
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

    // 自动下载模式：弹窗出现后自动开始下载新版本
    LaunchedEffect(Unit) {
        if (autoDownload && !apkUrl.isNullOrBlank() && !isUpdating) {
            delay(400)
            startRealDownload()
        }
    }

    fun startUpdate() {
        if (isUpdating && installOutcome == null) return
        // v1.0.2：安装失败重试时，先复位状态再启动
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
    // CSS 动态动画弹窗 UI（v1.0.2 全新呈现）
    // ============================================================

    // 按钮文案 & 行为
    val installingNow = isUpdating && progress >= 100f && installOutcome == null
    val btnPair: Pair<String, () -> Unit> = when {
        installOutcome == true -> "更新完成" to {
            onUpdateFinished()
            onDismiss()
        }
        installOutcome == false -> "重试" to { startUpdate() }
        installingNow -> "正在安装…" to { }
        isUpdating -> "更新中…" to { }
        else -> (update?.confirmText ?: "立即更新") to { startUpdate() }
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
                .background(Color(0x88000000))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { closeUpdate() }
                ),
            contentAlignment = Alignment.Center
        ) {
            CssUpdateCard(
                versionName = versionName,
                logs = FIXED_UPDATE_LOGS,
                installing = installingNow,
                installingText = statusLabel,
                installDone = installOutcome == true,
                isUpdating = isUpdating,
                progress = progress,
                isSignatureConflict = isSignatureConflict,
                onOpenGroup = { openOfficialGroup() },
                btnText = btnText,
                btnEnabled = !isUpdating || installOutcome != null,
                onBtnClick = btnAction
            )
        }
    }
}

// ============================================================
// CSS 动画卡片主体
// ============================================================
@Composable
private fun CssUpdateCard(
    versionName: String,
    logs: List<String>,
    installing: Boolean,
    installingText: String,
    installDone: Boolean,
    isUpdating: Boolean,
    progress: Float,
    isSignatureConflict: Boolean,
    onOpenGroup: () -> Unit,
    btnText: String,
    btnEnabled: Boolean,
    onBtnClick: () -> Unit
) {
    val infinite = rememberInfiniteTransition(label = "css_update_card")

    // 顶部横幅：渐变流光位移（CSS background-position 风格）
    val flowX by infinite.animateFloat(
        initialValue = -0.4f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "banner_flow"
    )
    // 按钮呼吸脉冲
    val btnScale by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 1.045f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "btn_pulse"
    )
    // 粒子漂浮垂直位移 + 呼吸透明度
    val floatY by infinite.animateFloat(
        initialValue = 0f,
        targetValue = -18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "particle_float"
    )
    val alphaBreath by infinite.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "particle_alpha"
    )

    // 固定粒子位置（随机散布在横幅区域）, 用 FloatArray 避免 Triple 命名参数问题
    val particles = remember {
        List(7) {
            floatArrayOf(
                0.06f + Random.nextFloat() * 0.88f,   // x 比例
                0.12f + Random.nextFloat() * 0.72f,   // y 比例
                2f + Random.nextFloat() * 3.5f        // 半径 dp
            )
        }
    }

    Box(
        modifier = Modifier
            .widthIn(min = 288.dp, max = 326.dp)
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .shadow(
                elevation = 18.dp,
                shape = RoundedCornerShape(26.dp),
                ambientColor = CSS_PINK.copy(alpha = 0.35f),
                spotColor = CSS_PEACH.copy(alpha = 0.3f)
            )
            .clip(RoundedCornerShape(26.dp))
            .background(Color.White)
            .testTag("css_update_card")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // ---------- 顶部渐变流光横幅 ----------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(74.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(CSS_PINK, CSS_PEACH, CSS_LEMON, CSS_PINK),
                            start = Offset(flowX * 900f, 0f),
                            end = Offset(flowX * 900f + 620f, 620f)
                        )
                    )
            ) {
                // CSS 漂浮粒子
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    particles.forEach { p ->
                        val px = p[0]
                        val py = p[1]
                        val pr = p[2]
                        drawCircle(
                            color = Color.White.copy(alpha = alphaBreath.coerceIn(0.15f, 0.75f)),
                            radius = pr * (density * 0.6f),
                            center = Offset(w * px, h * py + floatY * density * 0.4f)
                        )
                    }
                }
                // 标题行
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🎉", fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "发现新版本",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = "叮咚~我们又又又更新啦",
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.92f)
                            )
                        }
                    }
                    // 版本胶囊
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.28f))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = versionName.removePrefix("v").let { "v$it" },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // ---------- 内容区 ----------
            Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 18.dp)) {
                if (isSignatureConflict) {
                    // 签名冲突引导（保持简洁，CSS 风格一致）
                    Text(
                        text = installingText,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF3F3F46)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "卸载完成后重新打开本软件即可自动安装新版本",
                        fontSize = 12.sp,
                        color = Color(0xFF52525B),
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CssGroupLink(text = "官方群", onClick = onOpenGroup)
                    }
                } else {
                    // 写死的 4 条圆点列表（CSS 圆点呼吸动画）
                    logs.forEachIndexed { idx, log ->
                        CssLogItem(index = idx, text = log)
                        if (idx < logs.size - 1) Spacer(modifier = Modifier.height(7.dp))
                    }

                    // ---------- 进度区（下载/安装中） ----------
                    if (isUpdating) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = when {
                                    installDone -> "更新完成，重新打开即最新版"
                                    installing -> installingText.ifBlank { "正在安装…" }
                                    else -> "正在极速下载…"
                                },
                                fontSize = 11.sp,
                                color = Color(0xFF71717A)
                            )
                            Text(
                                text = "${progress.toInt()}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CSS_PINK
                            )
                        }
                        Spacer(modifier = Modifier.height(7.dp))

                        // CSS 渐变流动进度条
                        CssFlowProgressBar(progress = progress, flow = alphaBreath)

                        if (installing) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    color = CSS_PINK,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = installingText.ifBlank { "正在安装…" },
                                    fontSize = 11.sp,
                                    color = Color(0xFF8B8B93)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ---------- 按钮区 ----------
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 官方群（左）
                        CssGroupLink(text = "官方群", onClick = onOpenGroup)

                        // 立即更新 / 正在安装 / 重试（右）：CSS 渐变脉冲按钮
                        CssGradientButton(
                            text = btnText,
                            onClick = onBtnClick,
                            enabled = btnEnabled,
                            scale = if (btnEnabled) btnScale else 1f
                        )
                    }
                }
            }
        }
    }
}

// ---------- CSS 圆点列表项（呼吸动画） ----------
@Composable
private fun CssLogItem(index: Int, text: String) {
    val infinite = rememberInfiniteTransition(label = "log_item_$index")
    val dotAlpha by infinite.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500 + index * 250, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_alpha_$index"
    )
    val dotScale by infinite.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500 + index * 250, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_scale_$index"
    )
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .graphicsLayer {
                    scaleX = dotScale
                    scaleY = dotScale
                }
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(CSS_PINK, CSS_PEACH),
                        start = Offset(0f, 0f),
                        end = Offset(80f, 80f)
                    )
                )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 12.5.sp,
            color = Color(0xFF4A4A52),
            lineHeight = 17.sp
        )
        // 右侧呼吸光点（纯 CSS 装饰）
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(5.dp)
                .graphicsLayer { alpha = dotAlpha }
                .clip(CircleShape)
                .background(CSS_LEMON.copy(alpha = 0.8f))
        )
    }
}

// ---------- CSS 渐变流动进度条 ----------
@Composable
private fun CssFlowProgressBar(progress: Float, flow: Float) {
    val frac = progress.coerceIn(0f, 100f) / 100f
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(50))
            .background(Color(0xFFF2E8EC))
    ) {
        // 已填充部分：粉橙渐变 + 顶部白色流光高光
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = frac)
                .fillMaxSize()
                .clip(RoundedCornerShape(50))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(CSS_PINK, CSS_PEACH, CSS_MINT, CSS_LEMON),
                        startX = 0f,
                        endX = 600f
                    )
                )
        )
        // 顶部高光（玻璃反光效果，透明度随 flow 呼吸）
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = frac)
                .fillMaxHeight()
                .clip(RoundedCornerShape(50))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = (0.25f + flow * 0.2f).coerceIn(0.15f, 0.55f)),
                            Color.White.copy(alpha = 0.02f)
                        )
                    )
                )
        )
    }
}

// ---------- CSS 渐变脉冲按钮 ----------
@Composable
private fun CssGradientButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
    scale: Float
) {
    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .testTag("css_update_btn")
            .clip(RoundedCornerShape(50))
            .background(
                Brush.linearGradient(
                    colors = listOf(CSS_PINK, CSS_PEACH),
                    start = Offset(0f, 0f),
                    end = Offset(800f, 800f)
                )
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 26.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold,
            color = if (enabled) Color.White else Color.White.copy(alpha = 0.7f)
        )
    }
}

// ---------- 官方群链接 ----------
@Composable
private fun CssGroupLink(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF9C9399),
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 6.dp)
    )
}

// ============================================================
// 底层辅助：签名对比 / PackageInstaller / FileProvider
// ============================================================

/**
 * PackageInstaller 系统安装会话：
 * - v1.0.2 修复：PendingIntent 改 FLAG_MUTABLE（Android 12+ 系统需向回调 intent 注入安装状态，
 *   使用 IMMUTABLE 会导致部分设备上安装结果回调永远不送达 → 弹窗卡在「正在安装」）
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
            // v1.0.2：必须 MUTABLE，否则系统无法向回调 PendingIntent 注入 EXTRA_STATUS
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_MUTABLE
        )
        session.commit(pending.intentSender)
        true
    } catch (e: Exception) {
        false
    }
}

/** FileProvider + 系统安装器（最通用的兜底方案） */
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