package com.example.ui.uiverse

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Uiverse.io Categories matching the exact categories from user's screenshot:
 * 1. UI Kits (New)
 * 2. All
 * 3. Buttons
 * 4. Checkboxes
 * 5. Toggle switches
 * 6. Cards
 * 7. Loaders
 * 8. Inputs
 * 9. Radio buttons
 * 10. Forms
 * 11. Patterns
 * 12. Tooltips
 */
enum class UiverseCategory(
    val categoryId: String,
    val displayName: String,
    val zhName: String,
    val iconName: String,
    val hasNewBadge: Boolean = false
) {
    UI_KITS("ui_kits", "UI Kits", "全套套件", "category", true),
    ALL("all", "All", "全站收录", "all_inclusive", false),
    BUTTONS("buttons", "Buttons", "交互按钮", "smart_button", false),
    CHECKBOXES("checkboxes", "Checkboxes", "复选框", "check_box", false),
    TOGGLE_SWITCHES("toggle_switches", "Toggle switches", "切换开关", "toggle_on", false),
    CARDS("cards", "Cards", "展示卡片", "view_carousel", false),
    LOADERS("loaders", "Loaders", "加载动画", "progress_activity", false),
    INPUTS("inputs", "Inputs", "文本输入框", "input", false),
    RADIO_BUTTONS("radio_buttons", "Radio buttons", "单选框", "radio_button_checked", false),
    FORMS("forms", "Forms", "表单卡片", "assignment", false),
    PATTERNS("patterns", "Patterns", "背景纹理", "texture", false),
    TOOLTIPS("tooltips", "Tooltips", "提示气泡", "help_outline", false)
}

enum class UiKitPreset(
    val id: String,
    val displayName: String,
    val author: String,
    val desc: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val backgroundColor: Color,
    val surfaceColor: Color,
    val textColor: Color
) {
    STYLE_1_TILT_MAGNETIC(
        id = "style_1_tilt_magnetic",
        displayName = "款式1：磁吸卡 3D旋转流光",
        author = "Uiverse Style 1",
        desc = "磁吸卡片跟随旋转、conic炫彩外发光环绕、磨砂透光玻璃面板",
        primaryColor = Color(0xFF6C63FF),
        secondaryColor = Color(0xFFFF2D78),
        backgroundColor = Color(0xFF0B0C16),
        surfaceColor = Color(0xFF16192B),
        textColor = Color(0xFFFFFFFF)
    ),
    STYLE_2_GLASS_LOADER(
        id = "style_2_glass_loader",
        displayName = "款式2：双色旋转环与呼吸核心",
        author = "Uiverse Style 2",
        desc = "双色旋转流光环、呼吸核心光球、5连弹跳玻璃珠、流光渐变按钮与发光输入框",
        primaryColor = Color(0xFF8B84FF),
        secondaryColor = Color(0xFFFF2D78),
        backgroundColor = Color(0xFF0F1123),
        surfaceColor = Color(0xFF1E2238),
        textColor = Color(0xFFFFFFFF)
    ),
    STYLE_3_THICK_BUTTON(
        id = "style_3_thick_button",
        displayName = "款式3：新野蛮立体厚边阴影",
        author = "Uiverse Style 3",
        desc = "4px深蓝厚实描边、.35em立体硬投影位移、高对比度经典按压反馈",
        primaryColor = Color(0xFFFF6B4A),
        secondaryColor = Color(0xFF0A3D63),
        backgroundColor = Color(0xFFF0F7FB),
        surfaceColor = Color(0xFFFFFFFF),
        textColor = Color(0xFF0A3D63)
    ),
    STYLE_4_BOTTOMBAR_APPBAR(
        id = "style_4_bottombar_appbar",
        displayName = "款式4：极客厚边导航栏与顶栏",
        author = "Uiverse Style 4",
        desc = "4px深蓝描边圆角底部导航、内嵌-6px高光指示条、立体描边顶栏与浮雕字效",
        primaryColor = Color(0xFF0A3D63),
        secondaryColor = Color(0xFF00D2D3),
        backgroundColor = Color(0xFFEEF5FA),
        surfaceColor = Color(0xFFFFFFFF),
        textColor = Color(0xFF0A3D63)
    ),
    STYLE_5_CAPSULE_SETROW(
        id = "style_5_capsule_setrow",
        displayName = "款式5：厚边设置行与弹跳开关",
        author = "Uiverse Style 5",
        desc = "4px描边圆角设置行卡片、橙黄.3em立体投影、弹性触感开关与醒目箭头",
        primaryColor = Color(0xFF0A3D63),
        secondaryColor = Color(0xFFFF9F43),
        backgroundColor = Color(0xFFF7F9FB),
        surfaceColor = Color(0xFFFFFFFF),
        textColor = Color(0xFF26303C)
    ),
    DEFAULT_CLASSIC(
        id = "default_classic",
        displayName = "默认经典 (Classic)",
        author = "Official",
        desc = "系统原生纯净经典红金高定风格，极简优雅温润",
        primaryColor = Color(0xFFD32F2F),
        secondaryColor = Color(0xFFFFD700),
        backgroundColor = Color(0xFFFFEBEE),
        surfaceColor = Color(0xFFFFFFFF),
        textColor = Color(0xFF212121)
    ),
    CYBERPUNK_NEON(
        id = "cyberpunk_neon",
        displayName = "赛博朋克霓虹 (Cyberpunk 2077)",
        author = "Galahhad",
        desc = "HUD切角科技边框、赛博蓝紫激光霓虹微光、深空终端暗黑质感",
        primaryColor = Color(0xFF00F0FF),
        secondaryColor = Color(0xFFFF003C),
        backgroundColor = Color(0xFF05050A),
        surfaceColor = Color(0xFF0F101A),
        textColor = Color(0xFFE2E8F0)
    ),
    GLASSMORPHISM_AURORA(
        id = "glassmorphism_aurora",
        displayName = "极光磨砂玻璃 (Glassmorphism)",
        author = "mrhyddenn",
        desc = "通透磨砂半透明质感、流光渐变细边框、梦幻悬浮光斑",
        primaryColor = Color(0xFF6366F1),
        secondaryColor = Color(0xFFA855F7),
        backgroundColor = Color(0xFF0F172A),
        surfaceColor = Color(0x33334155),
        textColor = Color(0xFFF8FAFC)
    ),
    NEUMORPHISM_CLAY(
        id = "neumorphism_clay",
        displayName = "新拟物微浮雕 (Neumorphism)",
        author = "alexmaracinaru",
        desc = "双向柔和阴影凸起凹陷、温和轻软陶土质感、按压微回弹反馈",
        primaryColor = Color(0xFF3B82F6),
        secondaryColor = Color(0xFF60A5FA),
        backgroundColor = Color(0xFFE2E8F0),
        surfaceColor = Color(0xFFE2E8F0),
        textColor = Color(0xFF1E293B)
    ),
    NEO_BRUTALISM_POP(
        id = "neo_brutalism_pop",
        displayName = "新野蛮主义 (Neo-Brutalism)",
        author = "andrew-demchenk0",
        desc = "高对比度粗实黑线、4px纯黑硬位移投影、高饱和波普柠檬黄对撞",
        primaryColor = Color(0xFFFFE600),
        secondaryColor = Color(0xFFFF5252),
        backgroundColor = Color(0xFFFFFDF0),
        surfaceColor = Color(0xFFFFFFFF),
        textColor = Color(0xFF000000)
    ),
    RETRO_8BIT_ARCADE(
        id = "retro_8bit_arcade",
        displayName = "8-Bit复古街机 (Retro Pixel)",
        author = "Pradeepsahu",
        desc = "阶梯像素切边、CRT扫描线纹理、投币街机黄金按键",
        primaryColor = Color(0xFFFFCC00),
        secondaryColor = Color(0xFF00E5FF),
        backgroundColor = Color(0xFF1A1A2E),
        surfaceColor = Color(0xFF16213E),
        textColor = Color(0xFFE94560)
    ),
    HOLOGRAPHIC_PRISM(
        id = "holographic_prism",
        displayName = "全息棱镜 (Holographic Prism)",
        author = "NelsonTheDeveloper",
        desc = "七彩光谱流动渐变、水晶折射薄膜倒影、未来空间科幻",
        primaryColor = Color(0xFF38EF7D),
        secondaryColor = Color(0xFF11998E),
        backgroundColor = Color(0xFF0A0E1A),
        surfaceColor = Color(0xFF151D2A),
        textColor = Color(0xFFE0E7FF)
    ),
    LUXURY_OBSIDIAN_GOLD(
        id = "luxury_obsidian_gold",
        displayName = "黑曜石金 (Obsidian & Gold)",
        author = "vinodjangid07",
        desc = "哑光玄武岩超黑底蕴、香槟拉丝金微光边框、尊崇质感",
        primaryColor = Color(0xFFD4AF37),
        secondaryColor = Color(0xFFFFDF73),
        backgroundColor = Color(0xFF0D0D0D),
        surfaceColor = Color(0xFF171717),
        textColor = Color(0xFFF5E8C7)
    ),
    CUSTOM_CODE(
        id = "custom_code",
        displayName = "自定义代码驱动 (Custom CSS+HTML)",
        author = "User Creator",
        desc = "由用户输入的自定义 CSS / HTML 代码实时解析生成的独家软件UI",
        primaryColor = Color(0xFF6366F1),
        secondaryColor = Color(0xFFEC4899),
        backgroundColor = Color(0xFF0F172A),
        surfaceColor = Color(0xFF1E293B),
        textColor = Color(0xFFF8FAFC)
    )
}

enum class CardStylePreset {
    DEFAULT,
    CYBERPUNK,
    GLASSMORPHISM,
    NEUMORPHISM,
    NEO_BRUTALISM,
    RETRO_PIXEL,
    HOLOGRAPHIC,
    LUXURY_GOLD,
    TILT_3D_MAGNETIC,
    STYLE_3_THICK_CARD,
    STYLE_4_BOLD_FRAME,
    STYLE_5_CAPSULE_ROW,
    CUSTOM
}

enum class ButtonStylePreset {
    DEFAULT,
    CYBERPUNK_GLOW,
    GLASS_AURORA,
    NEUMORPHIC_PUSH,
    BRUTALIST_OFFSET,
    RETRO_COIN,
    STYLE_2_GLASS_PILL,
    STYLE_3_THICK_SHADOW,
    CUSTOM
}

enum class InputStylePreset {
    DEFAULT,
    CYBER_TERMINAL,
    GLASS_INSET,
    NEO_BRUTALIST_BOX,
    NEUMORPHIC_CONCAVE,
    STYLE_2_GLASS_GLOW,
    CUSTOM
}

enum class LoaderStylePreset {
    DEFAULT,
    CYBER_GLITCH,
    ORBIT_PLANETS,
    NEUMORPHIC_PULSE,
    BOUNCING_BARS,
    STYLE_2_DUAL_RING,
    CUSTOM
}

enum class PatternStylePreset {
    DEFAULT_WIND,
    CYBER_GRID,
    DOT_MATRIX,
    HEXAGON_MESH,
    BLUEPRINT,
    CUSTOM
}

/**
 * Parsed dynamic style from user's custom CSS/HTML input
 */
data class ParsedCssStyle(
    val backgroundColor: Color? = null,
    val backgroundBrush: Brush? = null,
    val textColor: Color? = null,
    val borderColor: Color = Color.Transparent,
    val borderWidth: Dp = 0.dp,
    val cornerRadius: Dp = 12.dp,
    val shadowColor: Color = Color.Black.copy(alpha = 0.2f),
    val shadowElevation: Dp = 2.dp,
    val isGlassmorphic: Boolean = false,
    val isCyberpunk: Boolean = false,
    val isBrutalist: Boolean = false,
    val glowColor: Color? = null,
    val rawCss: String = "",
    val rawHtml: String = ""
)

/**
 * Global active state for the UI
 */
data class ActiveUiverseState(
    val activeKit: UiKitPreset = UiKitPreset.DEFAULT_CLASSIC,
    val cardStyle: CardStylePreset = CardStylePreset.DEFAULT,
    val buttonStyle: ButtonStylePreset = ButtonStylePreset.DEFAULT,
    val inputStyle: InputStylePreset = InputStylePreset.DEFAULT,
    val loaderStyle: LoaderStylePreset = LoaderStylePreset.DEFAULT,
    val patternStyle: PatternStylePreset = PatternStylePreset.DEFAULT_WIND,
    val customStyle: ParsedCssStyle? = null,
    val customCssInput: String = "",
    val customHtmlInput: String = ""
)

/**
 * An individual UI component entry from Uiverse.io
 */
data class UiverseItem(
    val id: String,
    val name: String,
    val author: String,
    val category: UiverseCategory,
    val description: String,
    val cssCode: String,
    val htmlCode: String,
    val associatedKit: UiKitPreset? = null,
    val badge: String? = null
)

object UiverseCssEngine {
    const val templateNeonCard = """
background: #0f172a;
color: #38bdf8;
border: 2px solid #0284c7;
border-radius: 16px;
box-shadow: 0 0 15px rgba(56, 189, 248, 0.4);
"""

    const val templateGlassAurora = """
background: rgba(255, 255, 255, 0.15);
color: #ffffff;
border: 1px solid rgba(255, 255, 255, 0.25);
border-radius: 20px;
backdrop-filter: blur(12px);
"""

    const val templateNeoBrutalism = """
background: #ffffff;
color: #000000;
border: 3px solid #000000;
border-radius: 10px;
box-shadow: 5px 5px 0px #000000;
"""

    const val templateNeumorphicClay = """
background: #e2e8f0;
color: #334155;
border-radius: 18px;
box-shadow: 8px 8px 16px #cbd5e1, -8px -8px 16px #ffffff;
"""

    const val templateHolographic = """
background: linear-gradient(135deg, #a855f7, #06b6d4);
color: #ffffff;
border-radius: 16px;
border: 1.5px solid rgba(255, 255, 255, 0.5);
"""

    fun parseCss(css: String, html: String = ""): ParsedCssStyle {
        var bgColor: Color? = null
        var textColor: Color? = null
        var borderColor: Color = Color.Transparent
        var borderWidth: Dp = 0.dp
        var cornerRadius: Dp = 12.dp

        val lines = css.lines()
        for (line in lines) {
            val trimmed = line.trim().trimEnd(';')
            val parts = trimmed.split(":")
            if (parts.size >= 2) {
                val prop = parts[0].trim().lowercase()
                val value = parts.subList(1, parts.size).joinToString(":").trim().lowercase()
                when (prop) {
                    "background", "background-color" -> {
                        when {
                            value.contains("#0f172a") -> bgColor = Color(0xFF0F172A)
                            value.contains("#ffffff") || value.contains("white") -> bgColor = Color.White
                            value.contains("#000000") || value.contains("black") -> bgColor = Color.Black
                            value.contains("#e2e8f0") -> bgColor = Color(0xFFE2E8F0)
                            value.contains("rgba") -> bgColor = Color(0x33FFFFFF)
                            else -> bgColor = Color(0xFF1E293B)
                        }
                    }
                    "color" -> {
                        when {
                            value.contains("#38bdf8") -> textColor = Color(0xFF38BDF8)
                            value.contains("#ffffff") || value.contains("white") -> textColor = Color.White
                            value.contains("#000000") || value.contains("black") -> textColor = Color.Black
                            value.contains("#334155") -> textColor = Color(0xFF334155)
                            else -> textColor = Color.White
                        }
                    }
                    "border" -> {
                        if (value.contains("solid")) {
                            borderWidth = when {
                                value.contains("3px") -> 3.dp
                                value.contains("2px") -> 2.dp
                                else -> 1.dp
                            }
                            borderColor = when {
                                value.contains("#0284c7") -> Color(0xFF0284C7)
                                value.contains("#000000") || value.contains("black") -> Color.Black
                                value.contains("#ffffff") || value.contains("white") -> Color.White
                                else -> Color(0xFF6366F1)
                            }
                        }
                    }
                    "border-radius" -> {
                        val num = value.filter { it.isDigit() }.toIntOrNull()
                        if (num != null) cornerRadius = num.dp
                    }
                }
            }
        }

        return ParsedCssStyle(
            backgroundColor = bgColor ?: Color(0xFF1E293B),
            textColor = textColor ?: Color.White,
            borderColor = borderColor,
            borderWidth = borderWidth,
            cornerRadius = cornerRadius,
            rawCss = css,
            rawHtml = html
        )
    }
}

