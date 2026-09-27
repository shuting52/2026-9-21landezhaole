package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import android.media.MediaPlayer
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.VideoView
import coil.compose.AsyncImage
import com.example.data.remote.SplashDto
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.FlameRed
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SunsetOrange
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.withFrameNanos
import kotlin.math.sin
import kotlin.random.Random

/**
 * 全功能高阶开屏动画（v1.0.4 升级）：
 * 1. 最新酷炫 CSS 粒子动态特效：上百颗流光粒子漂移 + 粒子间动态连线 + 中心旋转光环 + 星光闪烁
 *    （Canvas 实现，粒子网络实时流动，酷炫不卡顿，完全离线）
 * 2. 多重呼吸脉冲光环 (Breathing Halo)
 * 3. 动态光泽流光标题 "懒得找了" 与副标题渐进呈现
 * 4. 右上角倒计时跳过组件与平滑退出转场
 *
 * 时长：默认 5 秒；云端控制台设定 durationSeconds 后严格跟随后台设定。
 * splashReady=true（云端配置加载完成）后才开始计时，避免云端未加载完就按默认 2 秒提前进入。
 */
@Composable
fun SplashScreenOverlay(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    splash: SplashDto? = null,
    splashReady: Boolean = false
) {
    var countdownSeconds by remember(splash?.durationSeconds) {
        // v1.0.4：默认 5 秒；云端控制台配置了展示时长则严格跟随后台设定
        mutableIntStateOf((splash?.durationSeconds ?: 5).coerceIn(1, 15))
    }

    val entryScale = remember { Animatable(0.7f) }
    val entryAlpha = remember { Animatable(0f) }

    LaunchedEffect(isVisible, splashReady) {
        if (isVisible) {
            launch {
                entryScale.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
            }
            launch {
                entryAlpha.animateTo(1f, tween(450))
            }
            // 等待云端开屏配置就绪（最多等 3.5 秒，避免网络异常时一直卡在开屏）
            var waited = 0
            while (!splashReady && waited < 3500) {
                delay(100)
                waited += 100
            }
            // 严格按设定时长倒计时（withFrameNanos 每帧校准系统时间，主线程繁忙也不会跳秒）
            val totalMillis = countdownSeconds * 1000L
            val startTime = System.currentTimeMillis()
            while (true) {
                val elapsed = System.currentTimeMillis() - startTime
                val remainSec = ((totalMillis - elapsed) / 1000L).coerceAtLeast(0L).toInt()
                if (remainSec != countdownSeconds) {
                    countdownSeconds = remainSec
                }
                if (elapsed >= totalMillis) break
                withFrameNanos { }
            }
            delay(150)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(250)),
        exit = fadeOut(tween(350)) + scaleOut(targetScale = 1.08f, animationSpec = tween(350))
    ) {
        val sp = splash
        if (sp != null && (sp.type == "html" || sp.type == "media")) {
            CloudSplashContent(splash = sp, onDismiss = onDismiss)
        } else {
        // ================= v1.0.4：CSS 粒子动态特效开屏 =================
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF14101F),
                            Color(0xFF0B0B1A)
                        ),
                        radius = 1300f
                    )
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismiss() }
        ) {
            // ---- CSS 粒子动态特效层（粒子网络 + 星光 + 流动光晕）----
            ParticleSplashCanvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = entryAlpha.value }
            )

            // 右上角跳过按钮
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.1f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 44.dp, end = 20.dp)
                    .clickable { onDismiss() }
            ) {
                Text(
                    text = "跳过 $countdownSeconds s",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            // 中心内容区
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .scale(entryScale.value)
                    .graphicsLayer { alpha = entryAlpha.value }
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(200.dp)
                ) {
                    // 呼吸脉冲光环 2（CSS box-shadow 风格扩散）
                    val haloPulse2 by rememberInfiniteTransition(label = "halo2").animateFloat(
                        initialValue = 0.9f,
                        targetValue = 1.5f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "haloPulse2"
                    )
                    Box(
                        modifier = Modifier
                            .size(170.dp)
                            .scale(haloPulse2)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        NeonPurple.copy(alpha = 0.30f),
                                        SunsetOrange.copy(alpha = 0.10f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // 呼吸脉冲光环 1（渐变描边旋转）
                    val haloPulse1 by rememberInfiniteTransition(label = "halo1").animateFloat(
                        initialValue = 0.85f,
                        targetValue = 1.35f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1500, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "haloPulse1"
                    )
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .scale(haloPulse1)
                            .clip(CircleShape)
                            .border(
                                width = 1.5.dp,
                                brush = Brush.sweepGradient(
                                    listOf(FlameRed, SunsetOrange, ElectricCyan, NeonPurple, FlameRed)
                                ),
                                shape = CircleShape
                            )
                    )

                    // 3D 旋转立体水晶方块
                    val rotationAngle by rememberInfiniteTransition(label = "rotation").animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(4000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "rotationAngle"
                    )
                    Surface(
                        shape = RoundedCornerShape(26.dp),
                        shadowElevation = 14.dp,
                        color = Color.Transparent,
                        modifier = Modifier
                            .size(92.dp)
                            .graphicsLayer {
                                rotationY = rotationAngle
                                rotationX = 18f
                                cameraDistance = 16 * density
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            FlameRed,
                                            SunsetOrange,
                                            NeonPurple
                                        )
                                    )
                                )
                                .border(
                                    width = 2.dp,
                                    brush = Brush.linearGradient(
                                        listOf(Color.White.copy(alpha = 0.85f), Color.White.copy(alpha = 0.15f))
                                    ),
                                    shape = RoundedCornerShape(26.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier
                                    .size(48.dp)
                                    .rotate(rotationAngle * 0.5f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 渐变标题 "懒得找了" + 流光微光（CSS text 流光效果）
                val shimmerOffset by rememberInfiniteTransition(label = "shimmer").animateFloat(
                    initialValue = -300f,
                    targetValue = 600f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2200, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "shimmerOffset"
                )
                Text(
                    text = "懒得找了",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        brush = Brush.linearGradient(
                            colors = listOf(FlameRed, SunsetOrange, NeonPurple, ElectricCyan),
                            start = Offset(shimmerOffset, 0f),
                            end = Offset(shimmerOffset + 240f, 100f)
                        )
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // v1.0.4：动态文字——逐字浮现打字机效果 + 上下浮动 + 渐变流光
                val splashDynamicText = "每天少走弯路 · 尽情探索互联网宝藏资源"
                var typedCount by remember { mutableIntStateOf(0) }
                LaunchedEffect(Unit) {
                    while (typedCount < splashDynamicText.length) {
                        delay(85)
                        typedCount++
                    }
                }
                val textFloatY by rememberInfiniteTransition(label = "text_float").animateFloat(
                    initialValue = -5f,
                    targetValue = 5f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "textFloatY"
                )
                Text(
                    text = splashDynamicText.take(typedCount) +
                        (if (typedCount < splashDynamicText.length) "▌" else "✨"),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        brush = Brush.linearGradient(
                            colors = listOf(SunsetOrange, NeonPurple, ElectricCyan),
                            start = Offset(shimmerOffset, 0f),
                            end = Offset(shimmerOffset + 160f, 60f)
                        )
                    ),
                    modifier = Modifier.graphicsLayer {
                        translationY = textFloatY * density
                        alpha = entryAlpha.value
                    }
                )
            }
        }
        }
    }
}

/** 粒子网络数据（固定随机，仅颜色/坐标/尺寸/连线关系） */
private data class SplashParticle(
    val x: Float,
    val y: Float,
    val speed: Float,
    val size: Float,
    val depth: Int,      // 0：近（亮大） 1：远（暗小）
    val hue: Int,        // 0..3 对应色板
    val phase: Float     // 相位偏移（呼吸/连线用）
)

private val SPLASH_COLORS = listOf(
    Color(0xFFFF6B9D),   // 粉
    Color(0xFFFFB199),   // 蜜桃橙
    Color(0xFF00E5FF),   // 电光青
    Color(0xFF7C4DFF)    // 霓虹紫
)

/**
 * CSS 风格粒子动态特效画布：
 * - 上百颗粒子沿各自方向漂移（负方向回流，形成连绵不绝的粒子流）
 * - 近邻粒子之间绘制动态连线（距离越近越亮），模拟 particles.js 网络特效
 * - 少量大粒子带光晕，模拟星光
 * - 两路 phase 叠加出无规律、可循环的流动
 */
@Composable
private fun ParticleSplashCanvas(modifier: Modifier = Modifier) {
    // 预生成 132 颗粒子（固定随机，尺寸用 dp 值，绘制时转 px）
    val particles = remember {
        List(132) { i ->
            SplashParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                speed = 0.008f + Random.nextFloat() * 0.02f,
                size = if (Random.nextFloat() < 0.12f) (2.6f + Random.nextFloat() * 2.2f) else (1.0f + Random.nextFloat() * 1.8f),
                depth = if (i % 5 == 0) 0 else 1,
                hue = Random.nextInt(SPLASH_COLORS.size),
                phase = Random.nextFloat() * 6.283f
            )
        }
    }
    // 预生成近邻连线对（固定），保证任意时刻画布上都有网络感
    val links = remember {
        val pairs = mutableListOf<Pair<Int, Int>>()
        var guard = 0
        while (pairs.size < 96 && guard < 4000) {
            guard++
            val a = Random.nextInt(particles.size)
            val b = Random.nextInt(particles.size)
            if (a != b && pairs.none { (x, y) -> (x == a && y == b) || (x == b && y == a) }) {
                pairs.add(a to b)
            }
        }
        pairs
    }

    val transition = rememberInfiniteTransition(label = "particle_network")
    val phaseA by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase_a"
    )
    val phaseB by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(13000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase_b"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        // 双相位归一化坐标（0..1 流动）
        val ta = phaseA
        val tb = phaseB

        // ---- 1) 大背景光晕（暗紫蓝，随粒子的流动微移）----
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    NeonPurple.copy(alpha = 0.10f),
                    ElectricCyan.copy(alpha = 0.05f),
                    Color.Transparent
                ),
                center = Offset(cx + (ta - 0.5f) * w * 0.2f, cy + (tb - 0.5f) * h * 0.2f),
                radius = w * 0.85f
            )
        )

        // ---- 2) 粒子坐标计算：x = (origX + tA*speedA) mod 1, y 用 tB 慢漂 ----
        val pts = FloatArray(particles.size * 2)
        particles.forEachIndexed { i, p ->
            val dir = (if (i % 2 == 0) 1 else -1)
            var px = p.x + ta * p.speed * dir
            var py = p.y + tb * p.speed * 0.6f
            // 回流（取模保持 0..1）
            px = (px % 1.0f + 1.0f) % 1.0f
            py = (py % 1.0f + 1.0f) % 1.0f
            pts[i * 2] = px
            pts[i * 2 + 1] = py
        }

        // ---- 3) 粒子间连线（近邻闪烁，CSS 网络特效）----
        for ((a, b) in links) {
            val ax = pts[a * 2] * w
            val ay = pts[a * 2 + 1] * h
            val bx = pts[b * 2] * w
            val by = pts[b * 2 + 1] * h
            val dx = ax - bx
            val dy = ay - by
            val dist = kotlin.math.sqrt(dx * dx + dy * dy)
            val maxDist = w * 0.16f
            if (dist < maxDist) {
                val fade = (1f - dist / maxDist).coerceIn(0f, 1f)
                // 呼吸闪烁
                val breathe = (0.5f + 0.5f * sin((ta + tb) * 6.283f).toFloat())
                val alpha = fade * (0.10f + 0.20f * breathe)
                drawLine(
                    color = Color(0xFF8B5CF6).copy(alpha = alpha),
                    start = Offset(ax, ay),
                    end = Offset(bx, by),
                    strokeWidth = 1f * density
                )
            }
        }

        // ---- 4) 绘制粒子 ----
        particles.forEachIndexed { i, p ->
            val px = pts[i * 2] * w
            val py = pts[i * 2 + 1] * h
            val color = SPLASH_COLORS[p.hue]
            val breathe = 0.5f + 0.5f * sin((ta + tb) * 6.283f + p.phase).toFloat()
            if (p.depth == 0) {
                // 近景大星：光晕 + 高亮
                val r = p.size * density * (0.9f + 0.25f * breathe)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(color.copy(alpha = 0.55f), color.copy(alpha = 0.06f), Color.Transparent),
                        center = Offset(px, py),
                        radius = r * 3f
                    ),
                    radius = r * 3f,
                    center = Offset(px, py)
                )
                drawCircle(color = color, radius = r, center = Offset(px, py))
            } else {
                drawCircle(
                    color = color.copy(alpha = 0.22f + 0.25f * breathe),
                    radius = p.size * density,
                    center = Offset(px, py)
                )
            }
        }
    }
}

@Composable
private fun CloudSplashContent(
    splash: SplashDto,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(parseHexColor(splash.bgColor))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onDismiss() }
    ) {
        when (splash.type) {
            "html" -> {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                            settings.javaScriptEnabled = true
                            setBackgroundColor(0x00000000)
                            webViewClient = object : WebViewClient() {
                                override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
                                    return true
                                }
                            }
                            loadDataWithBaseURL(null, splash.customHtml, "text/html", "UTF-8", null)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            "media" -> {
                val url = splash.mediaUrl
                if (url.isNullOrBlank()) return@Box
                if (url.contains(".mp4", ignoreCase = true) || url.contains(".webm", ignoreCase = true) ||
                    url.contains(".mov", ignoreCase = true) || url.contains(".m4v", ignoreCase = true)
                ) {
                    AndroidView(
                        factory = { ctx ->
                            VideoView(ctx).apply {
                                setVideoURI(Uri.parse(url))
                                setOnPreparedListener { mp ->
                                    mp.isLooping = true
                                    mp.setVolume(0f, 0f)
                                    mp.start()
                                }
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    AsyncImage(
                        model = url,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

private fun parseHexColor(hex: String): Color {
    return try {
        val clean = hex.removePrefix("#")
        val v = clean.toLong(16)
        Color(
            red = ((v shr 16) and 0xFF) / 255f,
            green = ((v shr 8) and 0xFF) / 255f,
            blue = (v and 0xFF) / 255f,
            alpha = 1f
        )
    } catch (e: Exception) {
        Color(0xFF0B0B1A)
    }
}