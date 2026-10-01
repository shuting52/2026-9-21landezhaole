@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
        title = "嘴强嘴替",
        shortLabel = "嘴强嘴替",
        icon = Icons.Filled.Chat,
        desc = "神级回怼生成器 · 专治杠精职场催婚 · 优雅不带脏字"
    ),
    AGE_CALC(
        title = "年龄推算",
        shortLabel = "年龄推算",
        icon = Icons.Filled.DateRange,
        desc = "精准年月日时分秒 · 生肖天干地支 · 人生进度条"
    ),
    OFFLINE_TREASURE(
        title = "离线百宝",
        shortLabel = "离线百宝",
        icon = Icons.Filled.Lightbulb,
        desc = "LED滚动弹幕 · 电子功德木鱼 · 随机做决定器 · SOS爆闪"
    ),
    FOOD_PICKER(
        title = "今天吃什么？",
        shortLabel = "今天吃什么",
        icon = Icons.Filled.Restaurant,
        desc = "随机色子 · 各大菜系 · 配料调味料 · 制作教程"
    ),
    EMERGENCY_PHONE(
        title = "紧急电话",
        shortLabel = "紧急电话",
        icon = Icons.Filled.Call,
        desc = "全域服务电话分类 · 一键快捷呼出 · 覆盖全国地区选择"
    )
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

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .testTag("toolbox_screen")
        ) {
            // 顶部标题区
            Surface(
                color = Color.White.copy(alpha = 0.50f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.65f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "🧰 工具箱",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "精选工具 · 本地纯离线运算 · 云端工具实时同步",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // v1.1.2：删除分类标签（精选工具/云端工具标题栏），直接平铺展示全部工具
            // ===== 本地精选工具（本地4工具 + 紧急电话）=====
            ToolGrid(tabs = ToolboxTab.entries.toList()) { activeTool = it }

            // ===== ☁️ 云端工具（控制台实时同步）=====
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
                        .clip(RoundedCornerShape(20.dp)),
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

/** 本地工具两列网格 */
@Composable
private fun ToolGrid(tabs: List<ToolboxTab>, onTabClick: (ToolboxTab) -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(top = 8.dp)
    ) {
        tabs.chunked(2).forEach { rowTabs ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowTabs.forEach { tab ->
                    ToolCell(tab = tab, onClick = { onTabClick(tab) }, modifier = Modifier.weight(1f))
                }
                if (rowTabs.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

/** 本地工具小卡片 */
@Composable
private fun ToolCell(tab: ToolboxTab, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp)
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tab.shortLabel,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (tab.desc.isNotBlank()) {
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = tab.desc,
                        fontSize = 9.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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
        shape = RoundedCornerShape(12.dp),
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