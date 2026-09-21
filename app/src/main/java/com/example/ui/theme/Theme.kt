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
    val defaultTheme = ThemePreset(
        id = "default_flame",
        name = "炽热烈焰 (默认)",
        style = "flame",
        categoryName = "经典",
        primaryColor = FlameRed,
        secondaryColor = SunsetOrange,
        bgColor = Color(0xFFF8FAFC),
        surfaceColor = Color(0xFFFFFFFF),
        textColor = Color(0xFF0F172A),
        atmosphereEffect = AtmosphereEffect.NONE
    )

    val cyberpunkTheme = ThemePreset(
        id = "cyberpunk_neon",
        name = "赛博霓虹",
        style = "cyberpunk",
        categoryName = "未来",
        primaryColor = Color(0xFF00F0FF),
        secondaryColor = Color(0xFFFF0055),
        bgColor = Color(0xFF0D0E15),
        surfaceColor = Color(0xFF16192B),
        textColor = Color(0xFFF1F5F9),
        atmosphereEffect = AtmosphereEffect.STARS
    )

    val auroraTheme = ThemePreset(
        id = "aurora_glass",
        name = "极光琉璃",
        style = "aurora",
        categoryName = "自然",
        primaryColor = Color(0xFF10B981),
        secondaryColor = Color(0xFF6366F1),
        bgColor = Color(0xFF064E3B),
        surfaceColor = Color(0xFF065F46),
        textColor = Color(0xFFECFDF5),
        atmosphereEffect = AtmosphereEffect.AURORA
    )

    val obsidianTheme = ThemePreset(
        id = "obsidian_gold",
        name = "黑曜臻金",
        style = "obsidian",
        categoryName = "奢华",
        primaryColor = Color(0xFFD4AF37),
        secondaryColor = AmberGold,
        bgColor = Color(0xFF121212),
        surfaceColor = Color(0xFF1E1E1E),
        textColor = Color(0xFFFDFBF7),
        atmosphereEffect = AtmosphereEffect.NONE
    )

    val allThemes: List<ThemePreset> = listOf(
        defaultTheme,
        cyberpunkTheme,
        auroraTheme,
        obsidianTheme
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
