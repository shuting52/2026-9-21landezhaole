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
    val bgColor: Color,
    val surfaceColor: Color,
    val textColor: Color,
    val atmosphereEffect: AtmosphereEffect = AtmosphereEffect.NONE
)

object ThemePresetsRepository {
    // v1.0.19 软件主题全新升级：以「国庆节」为核心主体设计的可爱风格主题（默认）。
    // 中国红主色 + 金星金辅色 + 暖米底色 + 半透玻璃卡片，氛围动态暖光（烟花/星星光斑）。
    // 原有主题风格（清爽浅红/可爱卡通/炽热烈焰/赛博霓虹/极光琉璃/黑曜臻金）已全部移除。
    val defaultTheme = ThemePreset(
        id = "national_day_cute",
        name = "盛世华诞 · 国庆可爱 (默认)",
        style = "national_day",
        categoryName = "国庆",
        primaryColor = Color(0xFFE60012),
        secondaryColor = Color(0xFFFFD700),
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
    val colorScheme = lightColorScheme(
        primary = themePreset.primaryColor,
        secondary = themePreset.secondaryColor,
        background = themePreset.bgColor,
        surface = themePreset.surfaceColor,
        onPrimary = Color.White,
        onSecondary = Color.White,
        onBackground = themePreset.textColor,
        onSurface = themePreset.textColor
    )

    CompositionLocalProvider(LocalUiverseState provides uiverseState) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
