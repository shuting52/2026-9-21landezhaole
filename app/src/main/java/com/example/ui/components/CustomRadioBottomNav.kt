package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.AppBottomTab

/**
 * v1.1.12：底部 Tab 重写为 Uiverse Neo-Brutalism 样式（用户指定）：
 * 白色卡片 + 天蓝硬阴影 + 深蓝墨描边 3.5dp + 圆角 20dp；
 * 选中项天蓝实底 + 白色文字 + 底部草莓粉指示条。
 * Tab 数量保留本体 5 个（首页/软件/Skill/工具箱/设置）。
 */
@Composable
fun CustomRadioBottomNav(
    selectedTab: AppBottomTab,
    onTabSelected: (AppBottomTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        AppBottomTab.HOME to "⌂ 首页",
        AppBottomTab.SOFTWARE to "▦ 软件",
        AppBottomTab.SKILL to "♥ Skill",
        AppBottomTab.TOOLBOX to "☰ 工具箱",
        AppBottomTab.SETTINGS to "⚙ 设置"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(WindowInsets.navigationBars.asPaddingValues())
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("custom_radio_bottom_nav")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .neoShadow(
                    offsetX = 4.dp,
                    offsetY = 4.dp,
                    shadowColor = UiverseBlue,
                    borderColor = UiverseInk,
                    borderWidth = 3.5.dp,
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = Color.White
                )
                .clip(RoundedCornerShape(20.dp))
        ) {
            items.forEachIndexed { index, (tab, pair) ->
                val isSelected = selectedTab == tab
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (isSelected) UiverseBlue else Color.White)
                        .clickable { onTabSelected(tab) }
                        .padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val text = pair.substringAfter(' ').ifBlank { pair }
                    val icon = pair.substringBefore(' ')
                    Text(
                        text = icon,
                        fontSize = 20.sp,
                        color = if (isSelected) Color.White else UiverseTextMuted
                    )
                    Text(
                        text = text,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else UiverseTextMuted
                    )
                    if (isSelected) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .height(3.dp)
                                .background(UiversePink, RoundedCornerShape(2.dp))
                        )
                    }
                }
            }
        }
    }
}
