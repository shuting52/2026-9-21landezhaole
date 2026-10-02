@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.screens.toolbox.AgeCalculatorSection
import com.example.ui.components.UiverseAmber
import com.example.ui.components.UiverseBlue
import com.example.ui.components.UiverseInk
import com.example.ui.components.UiversePink
import com.example.ui.components.UiverseTextMuted
import com.example.ui.components.UiverseTrack
import com.example.ui.components.neoShadow
import com.example.ui.screens.toolbox.EmergencyPhoneSection
import com.example.ui.screens.toolbox.FoodPickerScreenView
import com.example.ui.screens.toolbox.MouthpieceSection
import com.example.ui.screens.toolbox.OfflineTreasureSection

/**
 * 工具箱（v1.0.4 精简版）：
 * 只保留 4 个精工具（嘴强嘴替 / 年龄推算 / 离线百宝 / 今天吃什么）+ 紧急电话新工具。
 * 其余工具及其代码/记录已全面删除。
 */
enum class ToolboxTab(
    val title: String,
    val shortLabel: String,
    val icon: ImageVector,
    val desc: String
) {
    MOUTHPIECE(
        title = "妙语连珠 · 国风嘴替",
        shortLabel = "妙语嘴替",
        icon = Icons.Filled.Chat,
        desc = "神级回怼生成器 · 专治杠精职场催婚 · 优雅不带脏字"
    ),
    AGE_CALC(
        title = "华夏时令 · 年龄生肖",
        shortLabel = "时令生肖",
        icon = Icons.Filled.DateRange,
        desc = "精准年月日时分秒 · 生肖天干地支 · 人生进度条"
    ),
    OFFLINE_TREASURE(
        title = "传世锦囊 · 离线百宝",
        shortLabel = "离线锦囊",
        icon = Icons.Filled.Lightbulb,
        desc = "LED滚动弹幕 · 电子功德木鱼 · 随机做决定器 · SOS爆闪"
    ),
    FOOD_PICKER(
        title = "锦鲤摇签 · 今天吃什么",
        shortLabel = "锦鲤摇签",
        icon = Icons.Filled.Restaurant,
        desc = "随机摇签 · 各大菜系 · 配料调味料 · 华夏美食宝库"
    ),
    EMERGENCY_PHONE(
        title = "安康守护 · 华夏应急热线",
        shortLabel = "安康热线",
        icon = Icons.Filled.Call,
        desc = "全域公职服务 · 一键快捷呼出 · 覆盖全国地区守护平安"
    )
}

/** 工具箱合集分类 */
enum class ToolCategory(
    val id: String,
    val displayName: String,
    val icon: String,
    val defaultExpanded: Boolean = false
) {
    // v1.0.7：分类默认收起（收纳形式呈现），点击标题栏才展开
    CLOUD("cloud", "云端工具", "☁️", false),
    CORE("core", "精选工具", "✨", false)
}

@Composable
fun ToolboxScreen(
    modifier: Modifier = Modifier,
    // v1.8.7：云端工具箱扩展工具（控制台增删，实时同步）
    cloudTools: List<com.example.data.remote.ToolDto> = emptyList()
) {
    // 弹窗交互：点击工具弹出独立交互框
    var activeTool by remember { mutableStateOf<ToolboxTab?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current

    // v1.0.19 取消展开收纳标签：工具箱直接平铺展示全部工具，无需点击展开/收起
    val expanded = remember {
        mutableStateMapOf<String, Boolean>().apply {
            // 全部默认展开（直接平铺）
            ToolCategory.entries.forEach { put(it.id, true) }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .testTag("toolbox_screen")
        ) {
            // 顶部标题区（国潮红金盛世风格）
            Surface(
                color = Color(0xFFFFFDF9).copy(alpha = 0.88f),
                border = BorderStroke(1.5.dp, Color(0xFFFFD700).copy(alpha = 0.70f)),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🏮 懒得找了百宝箱",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFDE2910)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFDE2910))
                                    .border(0.6.dp, Color(0xFFFFD700), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text("国庆特辑", color = Color(0xFFFFD700), fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "传世锦囊 · 华夏时令 · 离线神兵 · 纯净实用",
                            fontSize = 11.sp,
                            color = Color(0xFF7A4A45)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // v1.1.4 工具箱分支：取消「精选工具/云端工具」分类标签，直接平铺展示全部工具
            // ===== 本地工具（嘴强嘴替/年龄推算/离线百宝/今天吃什么/紧急电话）=====
            ToolGrid(tabs = ToolboxTab.entries.toList()) { activeTool = it }

            // ===== 云端工具（控制台实时同步，直接平铺）=====
            if (cloudTools.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                CloudToolGrid(cloudTools = cloudTools, context = context)
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        // 弹窗形式展示每个工具
        activeTool?.let { tool ->
            Dialog(
                onDismissRequest = { activeTool = null },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.96f)
                        .fillMaxHeight(0.92f)
                        .clip(RoundedCornerShape(28.dp)), // v1.1.7 胶囊化
                    color = MaterialTheme.colorScheme.background,
                    tonalElevation = 6.dp
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // 弹窗头部：工具名 + 关闭
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = tool.icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tool.title,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = tool.desc,
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { activeTool = null }) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "关闭",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        androidx.compose.material3.HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                        // 工具内容
                        Box(modifier = Modifier.fillMaxSize()) {
                            when (tool) {
                                ToolboxTab.MOUTHPIECE -> MouthpieceScreenView()
                                ToolboxTab.AGE_CALC -> AgeCalculatorScreenView()
                                ToolboxTab.OFFLINE_TREASURE -> OfflineTreasureScreenView()
                                ToolboxTab.FOOD_PICKER -> FoodPickerScreenView()
                                ToolboxTab.EMERGENCY_PHONE -> EmergencyPhoneSection()
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ==================== 组件 ==================== */

/** v1.0.19：静态分类标题栏（取消展开/收起标签，直接平铺呈现） */
@Composable
private fun ToolCategoryHeader(
    category: ToolCategory,
    count: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp), // v1.1.7 胶囊化
        color = Color.White.copy(alpha = 0.55f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp)
        ) {
            Text(text = category.icon, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = category.displayName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "$count 个工具",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 6.dp)
            )
        }
    }
}

/** v1.1.12：工具箱列表重写为 UiverseListGroup 样式（白底 + 琥珀硬阴影 + 墨色描边 3.5dp + 圆角 16dp + 分隔线） */
@Composable
private fun ToolGrid(tabs: List<ToolboxTab>, onTabClick: (ToolboxTab) -> Unit) {
    val colors = listOf(UiverseBlue, UiversePink, UiverseAmber, UiverseBlue, UiversePink)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .neoShadow(
                offsetX = 4.dp,
                offsetY = 4.dp,
                shadowColor = UiverseAmber,
                borderColor = UiverseInk,
                borderWidth = 3.5.dp,
                shape = RoundedCornerShape(16.dp),
                backgroundColor = Color.White
            )
    ) {
        tabs.forEachIndexed { index, tab ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTabClick(tab) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(colors[index % colors.size], RoundedCornerShape(10.dp))
                        .border(2.dp, UiverseInk, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = tab.shortLabel,
                    fontWeight = FontWeight.Bold,
                    color = UiverseInk,
                    modifier = Modifier.weight(1f)
                )
                Text(tab.desc.substringBefore("·").trim(), color = UiverseTextMuted, fontSize = 12.sp, maxLines = 1)
            }
            if (index < tabs.size - 1) {
                HorizontalDivider(color = UiverseTrack, thickness = 2.dp)
            }
        }
    }
}

/** 云端工具两列网格（点击打开 URL） */
@Composable
private fun CloudToolGrid(
    cloudTools: List<com.example.data.remote.ToolDto>,
    context: Context
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(top = 8.dp)
    ) {
        cloudTools.chunked(2).forEach { rowTools ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowTools.forEach { tool ->
                    CloudToolCell(tool = tool, context = context, modifier = Modifier.weight(1f))
                }
                if (rowTools.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

/** 云端工具小卡片（控制台实时同步，点击打开 URL） */
@Composable
private fun CloudToolCell(
    tool: com.example.data.remote.ToolDto,
    context: Context,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = {
            if (tool.url.isNotBlank()) {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(tool.url))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "无法打开：${tool.url}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "该云端工具未配置跳转链接", Toast.LENGTH_SHORT).show()
            }
        },
        shape = RoundedCornerShape(24.dp), // v1.1.7 胶囊化
        color = Color.White.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp)
        ) {
            Text(
                text = tool.icon.ifBlank { "?" },
                fontSize = 18.sp,
                modifier = Modifier.padding(end = 8.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tool.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (tool.desc.isNotBlank()) {
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = tool.desc,
                        fontSize = 9.5.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ==========================================
// 各工具专属界面（保留工具）
// ==========================================

@Composable
private fun MouthpieceScreenView() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            MouthpieceSection()
        }
    }
}

@Composable
private fun AgeCalculatorScreenView() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            AgeCalculatorSection()
        }
    }
}

@Composable
private fun OfflineTreasureScreenView() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OfflineTreasureSection()
        }
    }
}