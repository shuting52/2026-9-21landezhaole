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

    // v1.1.6 需求 3：底部导航改为「白色大胶囊容器 + 圆形按钮」
    // 对应 CSS：background:#fff; border-radius:50px; box-shadow 立体投影;
    // 每个 tab 为圆形按钮，选中态主题色实心圆填充 + 白色图标，未选中浅灰圆。
    val pillShape = RoundedCornerShape(50)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(WindowInsets.navigationBars.asPaddingValues())
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("custom_radio_bottom_nav")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(pillShape)
                .background(Color.White)
                .shadow(elevation = 8.dp, shape = pillShape, clip = false)
                .border(2.dp, Color(0xFFE6EEF5), pillShape)
                .padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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

    // v1.1.6 需求 3：胶囊导航中的圆形按钮
    // 选中：主题色实心圆 + 白色图标 + 轻微放大 + 底部小圆点指示
    // 未选中：浅灰圆 + 灰色图标
    val circleSize = if (isSelected) 46.dp else 40.dp
    val iconSize = if (isSelected) 22.dp else 19.dp
    val circleColor = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFE9EEF4)

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.0f else 0.94f,
        animationSpec = tween(220),
        label = "circleScale"
    )

    Column(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 圆形按钮（图标在圆内）
        Box(
            modifier = Modifier
                .size(circleSize)
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .clip(CircleShape)
                .background(circleColor)
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) Color.White.copy(alpha = 0.85f) else Color.Transparent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = tabItem.icon,
                contentDescription = tabItem.title,
                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(iconSize)
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = tabItem.title,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
        // 选中态底部小圆点
        Box(
            modifier = Modifier
                .size(if (isSelected) 5.dp else 0.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}
