package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.ui.screens.toolbox.AgeCalculatorSection
import com.example.ui.screens.toolbox.ConstellationSection
import com.example.ui.screens.toolbox.MouthpieceSection
import com.example.ui.screens.toolbox.MultiOpenScreenView
import com.example.ui.screens.toolbox.OfflineTreasureSection
import com.example.data.local.db.CloneAppEntity
import java.net.URLDecoder
import java.net.URLEncoder
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Base64
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * 极客百宝箱子功能枚举：每个功能对应独立的专业子界面
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
        desc = "嘴强嘴替 · 神级回怼生成器 · 专治杠精职场催婚 · 优雅不带脏字"
    ),
    AGE_CALC(
        title = "年龄推算",
        shortLabel = "年龄推算",
        icon = Icons.Filled.DateRange,
        desc = "精准年月日时分秒 · 生肖天干地支 · 人生进度条"
    ),
    CONSTELLATION(
        title = "精准星座",
        shortLabel = "精准星座",
        icon = Icons.Filled.Stars,
        desc = "12星座星盘档案 · 今日综合财运桃花 · 双星座契合度速配"
    ),
    OFFLINE_TREASURE(
        title = "离线百宝",
        shortLabel = "离线百宝",
        icon = Icons.Filled.Lightbulb,
        desc = "手持LED滚动弹幕 · 电子功德木鱼 · 随机做决定器 · SOS爆闪"
    ),
    BASE64(
        title = "Base64 转换",
        shortLabel = "Base64",
        icon = Icons.Filled.Lock,
        desc = "UTF-8 文本安全 Base64 编码与逆向解码"
    ),
    HASH_MD5(
        title = "Hash / MD5 计算",
        shortLabel = "MD5/SHA",
        icon = Icons.Filled.Key,
        desc = "MD5、SHA-1、SHA-256 字符串单向散列摘要"
    ),
    URL_CODEC(
        title = "URL 编解码",
        shortLabel = "URL编码",
        icon = Icons.Filled.Link,
        desc = "URLEncode / URLDecode 网址与参数解析"
    ),
    TIMESTAMP(
        title = "时间戳转换",
        shortLabel = "时间戳",
        icon = Icons.Filled.Schedule,
        desc = "Unix 时间戳与标准北京时间 (yyyy-MM-dd HH:mm:ss) 实时互转"
    ),
    UUID_GEN(
        title = "UUID 生成器",
        shortLabel = "UUID生成",
        icon = Icons.Filled.VpnKey,
        desc = "标准 RFC 4122 V4 随机唯一标识符批量/单项生成"
    ),
    TEXT_STATS(
        title = "字数与文本统计",
        shortLabel = "文本统计",
        icon = Icons.Filled.TextFormat,
        desc = "中文字数、英文单词、数字字符、无空格纯字数与行数统计"
    ),
    MULTI_OPEN(
        title = "分身多开",
        shortLabel = "分身多开",
        icon = Icons.Filled.Apps,
        desc = "应用分身多开助手 · 一键调用系统双开/分身能力，轻松同时登录多个账号"
    )
}

@Composable
fun ToolboxScreen(
    modifier: Modifier = Modifier,
    clones: List<CloneAppEntity> = emptyList(),
    onCreateClone: (packageName: String, appName: String) -> Unit = { _, _ -> },
    onDeleteClone: (id: String) -> Unit = {},
    onRenameClone: (id: String, newName: String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    // 当前选中的工具：null 表示停留在「工具网格」总览页
    var selectedTab by remember { mutableStateOf<ToolboxTab?>(null) }

    val currentTool = selectedTab
    if (currentTool == null) {
        // ============ 工具总览：全部工具以独立网格呈现（不再横向滑动） ============
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = modifier
                .fillMaxSize()
                .testTag("toolbox_grid")
        ) {
            item(span = { GridItemSpan(3) }) {
                Column(modifier = Modifier.padding(horizontal = 2.dp, vertical = 4.dp)) {
                    Text(
                        text = "极客百宝箱 · 实用工具专区",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${ToolboxTab.entries.size} 项独立工具 · 本地纯离线运算 · 零网络请求",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }
            items(ToolboxTab.entries) { tab ->
                ToolGridTile(tab = tab, onClick = { selectedTab = tab })
            }
        }
        return
    }

    // ============ 单个工具独立全屏界面（带返回总览） ============
    Column(modifier = modifier.fillMaxSize()) {
        Surface(
            color = Color.White.copy(alpha = 0.50f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.65f)),
            shape = RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                IconButton(onClick = { selectedTab = null }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回工具列表", tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = currentTool.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentTool.title,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "独立工具 · 返回总览可切换",
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }
        }

        AnimatedContent(
            targetState = currentTool,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "ToolboxScreenAnimation",
            modifier = Modifier.weight(1f)
        ) { tool ->
            when (tool) {
                ToolboxTab.MOUTHPIECE -> MouthpieceScreenView()
                ToolboxTab.AGE_CALC -> AgeCalculatorScreenView()
                ToolboxTab.CONSTELLATION -> ConstellationScreenView()
                ToolboxTab.OFFLINE_TREASURE -> OfflineTreasureScreenView()
                ToolboxTab.BASE64 -> Base64ScreenView(context = context)
                ToolboxTab.HASH_MD5 -> HashCalculatorScreenView(context = context)
                ToolboxTab.URL_CODEC -> UrlCodecScreenView(context = context)
                ToolboxTab.TIMESTAMP -> TimestampScreenView(context = context)
                ToolboxTab.UUID_GEN -> UuidGeneratorScreenView(context = context)
                ToolboxTab.TEXT_STATS -> TextStatsScreenView(context = context)
                ToolboxTab.MULTI_OPEN -> MultiOpenScreenView(
                    context = context,
                    clones = clones,
                    onCreateClone = onCreateClone,
                    onDeleteClone = onDeleteClone,
                    onRenameClone = onRenameClone
                )
            }
        }
    }
}

@Composable
private fun ToolGridTile(tab: ToolboxTab, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.60f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.75f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tool_tile_${tab.name}")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tab.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(7.dp))
            Text(
                text = tab.shortLabel,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ==========================================
// 1. 最强嘴替 专属界面 (MouthpieceScreenView)
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

// ==========================================
// 4. 年龄推算 专属界面 (AgeCalculatorScreenView)
// ==========================================
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

// ==========================================
// 5. 精准星座 专属界面 (ConstellationScreenView)
// ==========================================
@Composable
private fun ConstellationScreenView() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ConstellationSection()
        }
    }
}

// ==========================================
// 6. 离线百宝 专属界面 (OfflineTreasureScreenView)
// ==========================================
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

// ==========================================
// 2. Base64 编解码 专属界面 (Base64ScreenView)
// ==========================================
@Composable
private fun Base64ScreenView(context: Context) {
    var input by remember { mutableStateOf("") }
    var output by remember { mutableStateOf("") }
    var isDecodeMode by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ToolHeaderBanner(
                title = "Base64 编码与逆向解码",
                desc = "标准 RFC 4648 Base64 转换算法，支持 UTF-8 编码的中英文字符、符号及参数串，完全本地处理。",
                icon = Icons.Filled.Lock
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tool_base64_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.60f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.70f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "输入文本 / 待转换密文",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        placeholder = { Text("例如：Hello World 或 5L2g5aW9") },
                        minLines = 3,
                        maxLines = 6,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                try {
                                    if (input.isEmpty()) {
                                        output = ""
                                    } else {
                                        output = Base64.getEncoder().encodeToString(input.toByteArray(Charsets.UTF_8))
                                    }
                                } catch (e: Exception) {
                                    output = "编码失败：${e.message}"
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Base64 编码")
                        }

                        Button(
                            onClick = {
                                try {
                                    if (input.isEmpty()) {
                                        output = ""
                                    } else {
                                        output = String(Base64.getDecoder().decode(input.trim()), Charsets.UTF_8)
                                    }
                                } catch (e: Exception) {
                                    output = "解码失败：输入的不是合法的 Base64 格式"
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Base64 解码")
                        }
                    }

                    if (output.isNotBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "转换结果",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.85f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = output,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = {
                                    copyToClipboard(context, "Base64", output)
                                }) {
                                    Icon(Icons.Filled.ContentCopy, contentDescription = "复制", modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. Hash / MD5 计算 专属界面 (HashCalculatorScreenView)
// ==========================================
@Composable
private fun HashCalculatorScreenView(context: Context) {
    var input by remember { mutableStateOf("") }
    var md5Result by remember { mutableStateOf("") }
    var sha1Result by remember { mutableStateOf("") }
    var sha256Result by remember { mutableStateOf("") }

    val computeHashes = {
        if (input.isEmpty()) {
            md5Result = ""
            sha1Result = ""
            sha256Result = ""
        } else {
            val bytes = input.toByteArray(Charsets.UTF_8)
            md5Result = MessageDigest.getInstance("MD5").digest(bytes).joinToString("") { "%02x".format(it) }
            sha1Result = MessageDigest.getInstance("SHA-1").digest(bytes).joinToString("") { "%02x".format(it) }
            sha256Result = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ToolHeaderBanner(
                title = "散列摘要与哈希计算",
                desc = "快速计算文本的 MD5 (32位小写)、SHA-1 (40位)、SHA-256 (64位) 散列值，常用于校验、签名与密钥验证。",
                icon = Icons.Filled.Key
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.60f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.70f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "输入原始明文字符串",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = input,
                        onValueChange = {
                            input = it
                            computeHashes()
                        },
                        placeholder = { Text("输入任何字符，下方将实时计算哈希...") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    HashResultRow(label = "MD5 (32位)", value = md5Result, context = context)
                    Spacer(modifier = Modifier.height(10.dp))
                    HashResultRow(label = "SHA-1 (40位)", value = sha1Result, context = context)
                    Spacer(modifier = Modifier.height(10.dp))
                    HashResultRow(label = "SHA-256 (64位)", value = sha256Result, context = context)
                }
            }
        }
    }
}

@Composable
private fun HashResultRow(label: String, value: String, context: Context) {
    Column {
        Text(text = label, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(3.dp))
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.White.copy(alpha = 0.85f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.7f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (value.isBlank()) "等待输入中..." else value,
                    fontSize = 11.5.sp,
                    color = if (value.isBlank()) Color.Gray else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                if (value.isNotBlank()) {
                    IconButton(
                        onClick = { copyToClipboard(context, label, value) },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = "复制", modifier = Modifier.size(15.dp))
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. URL 编解码 专属界面 (UrlCodecScreenView)
// ==========================================
@Composable
private fun UrlCodecScreenView(context: Context) {
    var input by remember { mutableStateOf("") }
    var output by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ToolHeaderBanner(
                title = "URL 编码与解码 (URLEncode)",
                desc = "解析网址中含有的中文、特殊字符或查询参数，支持 UTF-8 编码的 URL 规范化与反解析。",
                icon = Icons.Filled.Link
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.60f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.70f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "输入 URL 网址或参数文本",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        placeholder = { Text("例如：https://example.com/search?q=人工智能") },
                        minLines = 3,
                        maxLines = 6,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                try {
                                    output = URLEncoder.encode(input, "UTF-8")
                                } catch (e: Exception) {
                                    output = "编码失败"
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("URL 编码 (Encode)")
                        }

                        Button(
                            onClick = {
                                try {
                                    output = URLDecoder.decode(input, "UTF-8")
                                } catch (e: Exception) {
                                    output = "解码失败"
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("URL 解码 (Decode)")
                        }
                    }

                    if (output.isNotBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "转换结果",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.85f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = output,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = {
                                    copyToClipboard(context, "URL", output)
                                }) {
                                    Icon(Icons.Filled.ContentCopy, contentDescription = "复制", modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. 时间戳转换 专属界面 (TimestampScreenView)
// ==========================================
@Composable
private fun TimestampScreenView(context: Context) {
    var currentTs by remember { mutableStateOf(System.currentTimeMillis()) }
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }
    var inputTs by remember { mutableStateOf(currentTs.toString()) }
    var formattedDate by remember { mutableStateOf(sdf.format(Date(currentTs))) }

    var inputDateText by remember { mutableStateOf(formattedDate) }
    var dateToTsOutput by remember { mutableStateOf(currentTs.toString()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ToolHeaderBanner(
                title = "时间戳与标准时间互转",
                desc = "支持毫秒 (13位) 与秒 (10位) 时间戳，以及 yyyy-MM-dd HH:mm:ss 格式的双向实时转换与当前时间获取。",
                icon = Icons.Filled.Schedule
            )
        }

        // 当前时间戳展示卡
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("当前系统时间戳", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                        Text(
                            text = "${System.currentTimeMillis()} ms",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Button(
                        onClick = {
                            val now = System.currentTimeMillis()
                            currentTs = now
                            inputTs = now.toString()
                            formattedDate = sdf.format(Date(now))
                            inputDateText = formattedDate
                            dateToTsOutput = now.toString()
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("刷新到当前", fontSize = 11.5.sp)
                    }
                }
            }
        }

        // 1. 时间戳 -> 日期时间
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tool_timestamp_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.60f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.70f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "时间戳 转 日期时间", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = inputTs,
                        onValueChange = {
                            inputTs = it
                            val ts = it.toLongOrNull()
                            if (ts != null) {
                                val millis = if (ts < 10000000000L) ts * 1000 else ts
                                formattedDate = sdf.format(Date(millis))
                            } else {
                                formattedDate = "输入格式错误"
                            }
                        },
                        label = { Text("输入毫秒(13位)或秒(10位)时间戳") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.85f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.7f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "转换结果：$formattedDate",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            IconButton(onClick = {
                                copyToClipboard(context, "DateTime", formattedDate)
                            }) {
                                Icon(Icons.Filled.ContentCopy, contentDescription = "复制", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // 2. 日期时间 -> 时间戳
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.60f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.70f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "日期时间 转 时间戳", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = inputDateText,
                        onValueChange = {
                            inputDateText = it
                            try {
                                val parsed = sdf.parse(it)
                                if (parsed != null) {
                                    dateToTsOutput = parsed.time.toString()
                                }
                            } catch (e: Exception) {
                                dateToTsOutput = "格式需为: yyyy-MM-dd HH:mm:ss"
                            }
                        },
                        label = { Text("格式：yyyy-MM-dd HH:mm:ss") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.85f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.7f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "毫秒时间戳：$dateToTsOutput",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            IconButton(onClick = {
                                copyToClipboard(context, "Timestamp", dateToTsOutput)
                            }) {
                                Icon(Icons.Filled.ContentCopy, contentDescription = "复制", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 6. UUID 生成器 专属界面 (UuidGeneratorScreenView)
// ==========================================
@Composable
private fun UuidGeneratorScreenView(context: Context) {
    var generatedUuid by remember { mutableStateOf(UUID.randomUUID().toString()) }
    var isUppercase by remember { mutableStateOf(false) }
    var removeHyphens by remember { mutableStateOf(false) }
    var batchCount by remember { mutableIntStateOf(5) }
    var batchList by remember { mutableStateOf(listOf<String>()) }

    val formatUuid = { raw: String ->
        var res = raw
        if (removeHyphens) res = res.replace("-", "")
        if (isUppercase) res = res.uppercase() else res = res.lowercase()
        res
    }

    val generateBatch = {
        batchList = List(batchCount) { formatUuid(UUID.randomUUID().toString()) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ToolHeaderBanner(
                title = "UUID V4 随机唯一标识符生成器",
                desc = "遵循 RFC 4122 标准，使用密码学强随机数生成 128 位全局唯一 UUID，支持大小写切换与批量生成。",
                icon = Icons.Filled.VpnKey
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tool_uuid_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.60f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.70f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "单个 UUID", fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    val displaySingle = formatUuid(generatedUuid)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.85f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.7f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = displaySingle,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = {
                                copyToClipboard(context, "UUID", displaySingle)
                            }) {
                                Icon(Icons.Filled.ContentCopy, contentDescription = "复制", modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = isUppercase,
                            onClick = { isUppercase = !isUppercase },
                            label = { Text("大写", fontSize = 11.5.sp) }
                        )
                        FilterChip(
                            selected = removeHyphens,
                            onClick = { removeHyphens = !removeHyphens },
                            label = { Text("去除破折号(-)", fontSize = 11.5.sp) }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { generatedUuid = UUID.randomUUID().toString() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("重新生成单个全新 UUID")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "批量快速生成 (5 个)", fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = generateBatch,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("批量生成 5 个 UUID")
                    }

                    if (batchList.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.85f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.7f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                batchList.forEach { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = item, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                        IconButton(
                                            onClick = { copyToClipboard(context, "UUID", item) },
                                            modifier = Modifier.size(22.dp)
                                        ) {
                                            Icon(Icons.Filled.ContentCopy, contentDescription = "复制", modifier = Modifier.size(13.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 7. 字数与文本统计 专属界面 (TextStatsScreenView)
// ==========================================
@Composable
private fun TextStatsScreenView(context: Context) {
    var textInput by remember { mutableStateOf("") }

    val totalChars = textInput.length
    val charsNoSpace = textInput.replace("\\s+".toRegex(), "").length
    val chineseChars = textInput.count { it in '\u4e00'..'\u9fa5' }
    val englishWords = remember(textInput) {
        if (textInput.isBlank()) 0 else textInput.trim().split("\\s+".toRegex()).count { it.any { c -> c.isLetter() } }
    }
    val lines = remember(textInput) {
        if (textInput.isEmpty()) 0 else textInput.lines().size
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ToolHeaderBanner(
                title = "字数统计与文本分析",
                desc = "实时统计中文字数、字符总数、无空格纯字数、英文单词与行数，纯前端本地计算，内容不上传。",
                icon = Icons.Filled.TextFormat
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.60f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.70f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "输入或粘贴待统计文本", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        if (textInput.isNotEmpty()) {
                            TextButton(onClick = { textInput = "" }) {
                                Text("清空文本", fontSize = 11.5.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { Text("在此处输入或粘贴任意文章、Prompt 或代码文本...") },
                        minLines = 4,
                        maxLines = 8,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(text = "实时统计指标", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatPill(title = "中文字数", value = "$chineseChars", modifier = Modifier.weight(1f))
                        StatPill(title = "总字符数", value = "$totalChars", modifier = Modifier.weight(1f))
                        StatPill(title = "不计空格", value = "$charsNoSpace", modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatPill(title = "英文单词", value = "$englishWords", modifier = Modifier.weight(1f))
                        StatPill(title = "文本行数", value = "$lines", modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun StatPill(title: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.7f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// 统一顶部功能横幅说明卡片
@Composable
private fun ToolHeaderBanner(title: String, desc: String, icon: ImageVector) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.55f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.70f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = desc, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
            }
        }
    }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText(label, text))
    Toast.makeText(context, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
}

