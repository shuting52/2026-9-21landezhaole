package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalUiverseState
import com.example.ui.uiverse.PatternStylePreset
import com.example.ui.uiverse.UiKitPreset

// Uiverse.io by MuhammadHasann Warm Peach Wind Gradient Palette
val WindPeach1 = Color(0xFFFEC195)
val WindPeach2 = Color(0xFFFCC196)
val WindPeach3 = Color(0xFFFABD92)
val WindPeach4 = Color(0xFFFAC097)
val WindPeach5 = Color(0xFFFAC39C)

/**
 * Global Wind Background implementing the Uiverse.io warm animated wind gradient
 * and swaying wind/leaf icons (slay-1, slay-2, slay-3) applied globally across the software.
 */
@Composable
fun GlobalWindBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val uiverse = LocalUiverseState.current
    val infiniteTransition = rememberInfiniteTransition(label = "wind_background")

    // 1. Wind gradient position animation (animation: wind 2s ease-in-out infinite)
    val windShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "windShift"
    )

    // 2. Icon 1 animation: slay-1 (10deg -> -5deg -> 10deg, 3s cubic-bezier)
    val slay1Angle by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = -5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "slay1"
    )

    // 3. Icon 2 animation: slay-2 (0deg -> 15deg -> 0deg, 3s)
    val slay2Angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "slay2"
    )

    // 4. Icon 3 animation: slay-3 (0deg -> -5deg -> 0deg, 2s)
    val slay3Angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "slay3"
    )

    Box(modifier = modifier.fillMaxSize()) {
        when {
            uiverse.patternStyle == PatternStylePreset.CYBER_GRID || uiverse.activeKit == UiKitPreset.CYBERPUNK_NEON -> {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(Color(0xFF070913))
                    val gridSize = 36.dp.toPx()
                    val cols = (size.width / gridSize).toInt() + 2
                    val rows = (size.height / gridSize).toInt() + 2
                    for (i in 0..cols) {
                        drawLine(
                            color = Color(0xFF00F0FF).copy(alpha = 0.07f),
                            start = Offset(i * gridSize, 0f),
                            end = Offset(i * gridSize, size.height)
                        )
                    }
                    for (j in 0..rows) {
                        drawLine(
                            color = Color(0xFFFF0055).copy(alpha = 0.05f),
                            start = Offset(0f, j * gridSize + (windShift * gridSize % gridSize)),
                            end = Offset(size.width, j * gridSize + (windShift * gridSize % gridSize))
                        )
                    }
                }
            }
            uiverse.patternStyle == PatternStylePreset.DOT_MATRIX || uiverse.activeKit == UiKitPreset.NEO_BRUTALISM_POP -> {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(Color(0xFFFFFEE8))
                    val spacing = 28.dp.toPx()
                    val cols = (size.width / spacing).toInt() + 1
                    val rows = (size.height / spacing).toInt() + 1
                    for (i in 0..cols) {
                        for (j in 0..rows) {
                            drawCircle(
                                color = Color.Black.copy(alpha = 0.09f),
                                radius = 2.dp.toPx(),
                                center = Offset(i * spacing, j * spacing)
                            )
                        }
                    }
                }
            }
            uiverse.patternStyle == PatternStylePreset.HEXAGON_MESH || uiverse.activeKit == UiKitPreset.GLASSMORPHISM_AURORA -> {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(Color(0xFF0F172A))
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF6366F1).copy(alpha = 0.35f), Color.Transparent),
                            center = Offset(size.width * (0.2f + windShift * 0.1f), size.height * 0.25f),
                            radius = size.width * 0.7f
                        )
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFEC4899).copy(alpha = 0.25f), Color.Transparent),
                            center = Offset(size.width * (0.8f - windShift * 0.1f), size.height * 0.75f),
                            radius = size.width * 0.6f
                        )
                    )
                }
            }
            uiverse.customStyle?.backgroundBrush != null -> {
                Box(modifier = Modifier.fillMaxSize().background(uiverse.customStyle.backgroundBrush))
            }
            uiverse.customStyle?.backgroundColor != null -> {
                Box(modifier = Modifier.fillMaxSize().background(uiverse.customStyle.backgroundColor))
            }
            else -> {
                // Background Gradient Layer - warm peach breeze gradient
                val gradientColors = listOf(
                    WindPeach1,
                    WindPeach2,
                    WindPeach3,
                    WindPeach4,
                    WindPeach5
                )

                // Canvas for animated 85deg linear gradient
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height

                    val startX = -width * 0.2f + windShift * width * 0.3f
                    val startY = 0f + windShift * height * 0.1f
                    val endX = width * 1.1f + windShift * width * 0.2f
                    val endY = height * 1.0f

                    drawRect(
                        brush = Brush.linearGradient(
                            colors = gradientColors,
                            start = Offset(startX, startY),
                            end = Offset(endX, endY)
                        )
                    )
                }
            }
        }

        // Swaying Wind/Leaf Decorative Ambient Icons when in Default Wind pattern
        if (uiverse.patternStyle == PatternStylePreset.DEFAULT_WIND) {
            // .icon-1 (top right: width 25px, transform-origin 0 0, slay-1)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-16).dp, y = 14.dp)
                    .size(28.dp)
                    .rotate(slay1Angle)
                    .alpha(0.40f)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawSlayWindIcon(Color(0xFF8D5B3A), 1f)
                }
            }

            // .icon-2 (top left + 25px: width 12px, transform-origin 50% 0, slay-2)
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 42.dp, y = 12.dp)
                    .size(16.dp)
                    .rotate(slay2Angle)
                    .alpha(0.35f)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawSlayWindIcon(Color(0xFF7A4E31), 0.7f)
                }
            }

            // .icon-3 (top left: width 18px, transform-origin 50% 0, slay-3)
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 10.dp, y = 16.dp)
                    .size(22.dp)
                    .rotate(slay3Angle)
                    .alpha(0.38f)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawSlayWindIcon(Color(0xFF8D5B3A), 0.85f)
                }
            }
        }

        // Main App Content Layer
        content()
    }
}

/**
 * Draws an elegant wind breeze feather/petal icon reproducing the Uiverse.io decorative geometry
 */
private fun DrawScope.drawSlayWindIcon(color: Color, scale: Float) {
    val w = size.width * scale
    val h = size.height * scale
    val path = Path().apply {
        moveTo(w * 0.5f, 0f)
        cubicTo(w * 0.9f, h * 0.2f, w, h * 0.6f, w * 0.5f, h)
        cubicTo(w * 0.35f, h * 0.7f, 0f, h * 0.4f, w * 0.5f, 0f)
        close()
    }
    drawPath(path = path, color = color)
}
