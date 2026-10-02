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
    // v1.0.19 软件主题：以「国庆节」为核心主体的可爱风格主题（保留可选）。
    // 中国红主色 + 金星金辅色 + 暖米底色 + 半透玻璃卡片，氛围动态暖光（烟花/星星光斑）。
    val nationalDayTheme = ThemePreset(
        id = "national_day_cute",
        name = "盛世华诞 · 国庆可爱",
        style = "national_day",
        categoryName = "国庆",
        primaryColor = Color(0xFFE60012),
        secondaryColor = Color(0xFFFFD700),
        bgColor = Color(0xFFFFF6EF),
        surfaceColor = Color(0xD9FFFFFF),
        textColor = Color(0xFF4A1E22),
        atmosphereEffect = AtmosphereEffect.FIREFLIES
    )

    // v1.1.3 新增「霓虹地图 · 荧光绿」主题（默认）：深色地图底 + 荧光绿地标(#00C080) + 白字，
    // 氛围星空光点，呼应「地图+城市地标」Uiverse 组件配色。
    // v1.1.11：默认主题改为「简约白」——本体软件背景采用白色，
    // 透明磨砂玻璃改为不透明白色胶囊质感（用户需求：白色背景 + 胶囊形式）
    val mapNeonTheme = ThemePreset(
        id = "map_neon_green",
        name = "简约白 · 清爽绿 (默认)",
        style = "map_neon",
        categoryName = "简约",
        primaryColor = Color(0xFF00C080),
        secondaryColor = Color(0xFF00E5A0),
        bgColor = Color(0xFFFFFFFF),
        surfaceColor = Color(0xFFFFFFFF),
        textColor = Color(0xFF1E293B),
        atmosphereEffect = AtmosphereEffect.STARS
    )

    val defaultTheme = mapNeonTheme

    val allThemes: List<ThemePreset> = listOf(
        defaultTheme,
        nationalDayTheme
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
