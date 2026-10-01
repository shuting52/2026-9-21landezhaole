package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.ui.uiverse.ActiveUiverseState

enum class AtmosphereEffect {
    NONE,
    STARS,
    SNOW,
    RAIN,
    FIREFLIES,
    AURORA
}

data class ThemePreset(
    val id: String,
    val name: String,
    val style: String,
    val categoryName: String = "经典",
    val primaryColor: Color,
    val secondaryColor: Color,
    val tertiaryColor: Color = Color(0xFFFF8C1A),
    val bgColor: Color,
    val surfaceColor: Color,
    val textColor: Color,
    val atmosphereEffect: AtmosphereEffect = AtmosphereEffect.NONE
)

object ThemePresetsRepository {
    // v1.1.2 软件主题升级：以「国庆节」为核心主体的卡通可爱风格主题（默认）。
    // 中国红主色 + 金星金辅色 + 国庆橙点缀 + 暖米底色 + 半透玻璃卡片，氛围动态暖光（烟花/星星光斑）。
    // 全 UI 组件/文字/图标统一采用本主题色板呈现。
    val defaultTheme = ThemePreset(
        id = "national_day_cute",
        name = "盛世华诞 · 国庆卡通 (默认)",
        style = "national_day",
        categoryName = "国庆",
        primaryColor = Color(0xFFE60012),
        secondaryColor = Color(0xFFFFD700),
        tertiaryColor = Color(0xFFFF8C1A),
        bgColor = Color(0xFFFFF6EF),
        surfaceColor = Color(0xD9FFFFFF),
        textColor = Color(0xFF4A1E22),
        atmosphereEffect = AtmosphereEffect.FIREFLIES
    )

    val allThemes: List<ThemePreset> = listOf(
        defaultTheme
    )
}

val LocalUiverseState = staticCompositionLocalOf { ActiveUiverseState() }

@Composable
fun MyApplicationTheme(
    themePreset: ThemePreset = ThemePresetsRepository.defaultTheme,
    uiverseState: ActiveUiverseState = ActiveUiverseState(),
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // v1.1.2 国庆卡通主题：完整填充 Material3 colorScheme 各语义字段，
    // 确保全部 UI 组件（按钮/卡片/输入框/弹窗/文字/图标）统一呈现国庆卡通风格。
    val primary = themePreset.primaryColor
    val secondary = themePreset.secondaryColor
    val tertiary = themePreset.tertiaryColor
    val bg = themePreset.bgColor
    val surface = themePreset.surfaceColor
    val text = themePreset.textColor

    val colorScheme = lightColorScheme(
        primary = primary,
        onPrimary = Color.White,
        primaryContainer = primary.copy(alpha = 0.14f),
        onPrimaryContainer = primary,
        secondary = secondary,
        onSecondary = Color(0xFF4A1E22),
        secondaryContainer = secondary.copy(alpha = 0.16f),
        onSecondaryContainer = Color(0xFF4A1E22),
        tertiary = tertiary,
        onTertiary = Color.White,
        tertiaryContainer = tertiary.copy(alpha = 0.15f),
        onTertiaryContainer = tertiary,
        background = bg,
        onBackground = text,
        surface = surface,
        onSurface = text,
        surfaceVariant = Color.White.copy(alpha = 0.72f),
        onSurfaceVariant = text.copy(alpha = 0.72f),
        outline = primary.copy(alpha = 0.38f),
        outlineVariant = primary.copy(alpha = 0.20f),
        error = Color(0xFFB3261E),
        onError = Color.White
    )

    CompositionLocalProvider(LocalUiverseState provides uiverseState) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
