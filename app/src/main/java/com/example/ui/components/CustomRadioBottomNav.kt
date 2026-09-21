package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FlameRed
import com.example.ui.theme.LocalUiverseState
import com.example.ui.uiverse.CardStylePreset
import com.example.ui.viewmodel.AppBottomTab

/**
 * Custom metallic tactile radio bar inspired by Uiverse.io by Cksunandh
 * Re-creates the multi-stop linear gradient and inset mechanical push button effect
 * for tabs: 首页 / 软件 / SKill / 设置 / 工具箱
 * Dynamically adapts to active Uiverse skin styles!
 */
@Composable
fun CustomRadioBottomNav(
    selectedTab: AppBottomTab,
    onTabSelected: (AppBottomTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiverse = LocalUiverseState.current
    val tabs = listOf(
        TabItem(AppBottomTab.HOME, "首页", Icons.Filled.Home),
        TabItem(AppBottomTab.SOFTWARE, "软件", Icons.Filled.Extension),
        TabItem(AppBottomTab.SKILL, "SKill", Icons.Filled.Terminal),
        TabItem(AppBottomTab.TOOLBOX, "工具箱", Icons.Filled.Psychology),
        TabItem(AppBottomTab.SETTINGS, "设置", Icons.Filled.Settings)
    )

    val surfaceBg = when (uiverse.cardStyle) {
        CardStylePreset.CYBERPUNK -> Color(0xFF0A0C14)
        CardStylePreset.NEO_BRUTALISM -> Color.White
        CardStylePreset.GLASSMORPHISM -> Color(0xEEFFFFFF)
        CardStylePreset.RETRO_PIXEL -> Color(0xFF16213E)
        CardStylePreset.LUXURY_GOLD -> Color(0xFF141414)
        CardStylePreset.CUSTOM -> uiverse.customStyle?.backgroundColor ?: MaterialTheme.colorScheme.surface
        else -> MaterialTheme.colorScheme.surface
    }

    val topBorderModifier = when (uiverse.cardStyle) {
        CardStylePreset.CYBERPUNK -> Modifier.border(1.dp, Color(0xFF00F0FF).copy(alpha = 0.5f))
        CardStylePreset.NEO_BRUTALISM -> Modifier.border(2.5.dp, Color.Black)
        CardStylePreset.LUXURY_GOLD -> Modifier.border(1.dp, Color(0xFFD4AF37).copy(alpha = 0.5f))
        else -> Modifier
    }

    Surface(
        color = surfaceBg,
        tonalElevation = 6.dp,
        shadowElevation = 12.dp,
        modifier = modifier
            .fillMaxWidth()
            .then(topBorderModifier)
            .testTag("custom_radio_bottom_nav")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WindowInsets.navigationBars.asPaddingValues())
                .padding(top = 10.dp, bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Container imitating .custom-radio-group
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEachIndexed { index, item ->
                    val isSelected = selectedTab == item.tab
                    val isFirst = index == 0
                    val isLast = index == tabs.size - 1

                    RadioNavItem(
                        tabItem = item,
                        isSelected = isSelected,
                        isFirst = isFirst,
                        isLast = isLast,
                        onClick = { onTabSelected(item.tab) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

private data class TabItem(
    val tab: AppBottomTab,
    val title: String,
    val icon: ImageVector
)

@Composable
private fun RadioNavItem(
    tabItem: TabItem,
    isSelected: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    // Multi-stop gradients matching the CSS specification
    // Unchecked: #ffffff 33%, #414751 58%, #837b52, #c5baa1, #c3adaa
    val uncheckedGradient = Brush.verticalGradient(
        0.0f to Color(0xFFFFFFFF),
        0.33f to Color(0xFFFFFFFF),
        0.58f to Color(0xFF414751),
        0.75f to Color(0xFF837B52),
        0.88f to Color(0xFFC5BAA1),
        1.0f to Color(0xFFC3ADAA)
    )

    // Checked: #ffffff 33%, #414751 58%, #827a7b, #c0b6ac, #c3adaa
    val checkedGradient = Brush.verticalGradient(
        0.0f to Color(0xFFFFFFFF),
        0.33f to Color(0xFFFFFFFF),
        0.58f to Color(0xFF414751),
        0.75f to Color(0xFF827A7B),
        0.88f to Color(0xFFC0B6AC),
        1.0f to Color(0xFFC3ADAA)
    )

    val labelOpacity by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.65f,
        animationSpec = tween(300),
        label = "labelOpacity"
    )

    val barHeight by animateDpAsState(
        targetValue = if (isSelected) 10.dp else 8.dp,
        animationSpec = tween(300),
        label = "barHeight"
    )

    val shape = when {
        isFirst -> RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp)
        isLast -> RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp)
        else -> RoundedCornerShape(0.dp)
    }

    Column(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 1.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Metallic Bar (.radio-input)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .shadow(
                    elevation = if (isSelected) 4.dp else 2.dp,
                    shape = shape,
                    clip = false
                )
                .clip(shape)
                .background(if (isSelected) checkedGradient else uncheckedGradient)
                .border(
                    width = 0.8.dp,
                    color = if (isSelected) Color(0xFF847A62) else Color(0xFFAFA490).copy(alpha = 0.6f),
                    shape = shape
                )
        )

        Spacer(modifier = Modifier.height(5.dp))

        // Icon + Label with opacity transition (.radio-input + span)
        Column(
            modifier = Modifier
                .alpha(labelOpacity)
                .padding(bottom = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = tabItem.icon,
                contentDescription = tabItem.title,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = tabItem.title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}
