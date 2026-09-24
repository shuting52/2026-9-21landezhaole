@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
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
import androidx.compose.ui.draw.rotate
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
import com.example.ui.screens.toolbox.BmiCalculatorScreenView
import com.example.ui.screens.toolbox.ColorCardScreenView
import com.example.ui.screens.toolbox.ConstellationSection
import com.example.ui.screens.toolbox.FoodPickerScreenView
import com.example.ui.screens.toolbox.MouthpieceSection
import com.example.ui.screens.toolbox.OfflineTreasureSection
import com.example.ui.screens.toolbox.PasswordGeneratorScreenView
import com.example.ui.screens.toolbox.QrCodeTextScreenView
import com.example.ui.screens.toolbox.RandomNumberScreenView
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
    RELATION_KIN(
        title = "关系认知",
        shortLabel = "关系认知",
        icon = Icons.Filled.Groups,
        desc = "亲戚称呼智能查询 · 覆盖中国56个民族的不同叫法 · 地区选择"
    ),
    FOOD_PICKER(
        title = "今天吃什么？",
        shortLabel = "今天吃什么",
        icon = Icons.Filled.Restaurant,
        desc = "随机色子 · 各大菜系 · 配料调味料 · 制作教程 · 每菜系独一无二"
    ),
    RANDOM_NUMBER(
        title = "随机数/色子",
        shortLabel = "随机色子",
        icon = Icons.Filled.Dns,
        desc = "指定区间随机整数 · 可设结果上限/下限 · 一次性多抽 · 用于抽奖抽签"
    ),
    BMI_CALC(
        title = "BMI 计算器",
        shortLabel = "BMI",
        icon = Icons.Filled.Speed,
        desc = "身高体重一键得出 BMI 值 · 体重偏胖偏瘦 · 成年人标准范围"
    ),
    PASSWORD_GEN(
        title = "密码生成器",
        shortLabel = "密码生成",
        icon = Icons.Filled.VpnKey,
        desc = "可选字符组合 · 批量导出 · 本地生成不上传 · 可控长度 6-64 位"
    ),
    QRCODE_TEXT(
        title = "文本二维码",
        shortLabel = "文本二维码",
        icon = Icons.Filled.Code,
        desc = "文本/网址转二维码图片 · 可分享/保存到手机 · 本地生成不上传"
    ),
    COLOR_CARD(
        title = "调色卡与取色",
        shortLabel = "调色卡",
        icon = Icons.Filled.FormatColorFill,
        desc = "HEX/RGB/HSL 互转 · 提取主题色 · 复制颜色值 · 调出好看的高级色调"
    ),
    UNIT_CONV(
        title = "单位换算器",
        shortLabel = "单位换算",
        icon = Icons.Filled.Menu,
        desc = "长度/重量/温度/面积/体积 实时换算 · 本地离线秒出结果"
    ),
    BASE_CONV(
        title = "进制转换",
        shortLabel = "进制转换",
        icon = Icons.Filled.List,
        desc = "2/8/10/16 进制互转 · 二进制补码/进制前缀 · 程序员利器"
    ),
    DATE_CALC(
        title = "日期计算",
        shortLabel = "日期计算",
        icon = Icons.Filled.DateRange,
        desc = "两个日期相差几天 · 日期加减天数 · 今天是第几周/第几天"
    ),
    CALCULATOR(
        title = "简易计算器",
        shortLabel = "计算器",
        icon = Icons.Filled.Add,
        desc = "加减乘除四则运算 · 括号与小数支持 · 本地计算不联网"
    ),
    EMOJI_PICKER(
        title = "Emoji 表情库",
        shortLabel = "Emoji",
        icon = Icons.Filled.SentimentSatisfied,
        desc = "常用表情一键复制 · 分类收藏 · 聊天斗图不发愁"
    ),
}

@Composable
fun ToolboxScreen(
    modifier: Modifier = Modifier
) {
    // 弹窗交互：主界面为工具分类网格，点击任意工具弹出独立交互框
    var activeTool by remember { mutableStateOf<ToolboxTab?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 10.dp)
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
                            text = "懒得找了小工具",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "点击工具卡片 · 弹窗即开即用 · 本地纯离线运算",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 工具分类网格（2列），点击弹出对应工具交互框
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxSize()
            ) {
                gridItems(ToolboxTab.entries) { tab ->
                    Surface(
                        onClick = { activeTool = tab },
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.6f),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                        ),
                        modifier = Modifier.fillMaxWidth()
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
            }
        }

        // 弹窗形式展示每个工具（分类多功能交互框）
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
                                ToolboxTab.CONSTELLATION -> ConstellationScreenView()
                                ToolboxTab.OFFLINE_TREASURE -> OfflineTreasureScreenView()
                                ToolboxTab.BASE64 -> Base64ScreenView(context = LocalContext.current)
                                ToolboxTab.HASH_MD5 -> HashCalculatorScreenView(context = LocalContext.current)
                                ToolboxTab.URL_CODEC -> UrlCodecScreenView(context = LocalContext.current)
                                ToolboxTab.TIMESTAMP -> TimestampScreenView(context = LocalContext.current)
                                ToolboxTab.UUID_GEN -> UuidGeneratorScreenView(context = LocalContext.current)
                                ToolboxTab.TEXT_STATS -> TextStatsScreenView(context = LocalContext.current)
                                ToolboxTab.RELATION_KIN -> RelationKinScreenView(context = LocalContext.current)
                                ToolboxTab.FOOD_PICKER -> FoodPickerScreenView()
                                ToolboxTab.RANDOM_NUMBER -> RandomNumberScreenView()
                                ToolboxTab.BMI_CALC -> BmiCalculatorScreenView()
                                ToolboxTab.PASSWORD_GEN -> PasswordGeneratorScreenView()
                                ToolboxTab.QRCODE_TEXT -> QrCodeTextScreenView()
                                ToolboxTab.COLOR_CARD -> ColorCardScreenView()
                                ToolboxTab.UNIT_CONV -> UnitConverterScreenView()
                                ToolboxTab.BASE_CONV -> BaseConverterScreenView()
                                ToolboxTab.DATE_CALC -> DateCalcScreenView()
                                ToolboxTab.CALCULATOR -> CalculatorScreenView()
                                ToolboxTab.EMOJI_PICKER -> EmojiPickerScreenView()
                            }
                        }
                    }
                }
            }
        }
    }
}

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


// ==========================================
// 关系认知小工具：亲戚称呼智能查询（覆盖中国56个民族，地区可收纳选择）
// ==========================================
private data class KinRelation(
    val ask: String,       // 问题：如"姐姐的弟弟"
    val common: String,    // 普通话通用称呼
    val tip: String = ""   // 补充说明
)

// 常见亲属关系问题（通用普通话称谓）
private val KIN_RELATIONS = listOf(
    KinRelation("姐姐的弟弟", "弟弟", "比自己小的叫弟弟，比自己大的叫哥哥"),
    KinRelation("姐姐的儿子", "外甥", "姐妹的儿子称外甥，兄弟的儿子称侄子"),
    KinRelation("哥哥的儿子", "侄子", "兄弟的儿子称侄子"),
    KinRelation("爸爸的哥哥", "伯父（大伯）", "父亲的哥哥称伯父，口语称大伯/大爷"),
    KinRelation("爸爸的弟弟", "叔叔", "父亲的弟弟称叔叔"),
    KinRelation("妈妈的兄弟", "舅舅", "母亲的兄弟称舅舅"),
    KinRelation("爸爸的姐姐", "姑妈（姑姑）", "父亲的姐妹称姑妈/姑姑"),
    KinRelation("妈妈的姐妹", "姨妈（阿姨）", "母亲的姐妹称姨妈/阿姨"),
    KinRelation("儿子的儿子", "孙子", "儿子的儿子称孙子"),
    KinRelation("女儿的女儿", "外孙女", "女儿的女儿称外孙女"),
    KinRelation("爸爸的爸爸", "爷爷", "父亲的父亲称爷爷（部分地区称爹爹/阿公）"),
    KinRelation("妈妈的妈妈", "外婆（姥姥）", "母亲的母亲称外婆/姥姥"),
    // 新增：不同辈分的称呼
    KinRelation("父亲的兄弟", "伯父/叔父", "父亲的兄长称伯父，父亲之弟称叔父"),
    KinRelation("母亲的兄弟", "舅舅", "母亲的兄弟称舅舅"),
    KinRelation("父亲的姐妹", "姑妈", "父亲的姐妹称姑妈"),
    KinRelation("母亲的姐妹", "姨妈", "母亲的姐妹称姨妈"),
    KinRelation("堂兄弟姐妹的父亲", "伯父/叔父", "堂兄弟姐妹的父亲即父亲的兄弟"),
    KinRelation("表兄弟姐妹的母亲", "姨母/姑母", "表兄弟姐妹的母亲是母亲的姐妹或父亲的姐妹")
)

// 中国各省份（全国 34 个省级行政区，含 23 省 5 自治区 4 直辖市 2 特别行政区）
private val CHINA_PROVINCES = listOf(
    "北京市","天津市","河北省","山西省","内蒙古自治区","辽宁省","吉林省","黑龙江省",
    "上海市","江苏省","浙江省","安徽省","福建省","江西省","山东省","河南省",
    "湖北省","湖南省","广东省","广西壮族自治区","海南省","重庆市","四川省","贵州省",
    "云南省","西藏自治区","陕西省","甘肃省","青海省","宁夏回族自治区","新疆维吾尔自治区",
    "台湾省","香港特别行政区","澳门特别行政区"
)

// 各省份方言版亲属称呼（示例：省份 -> 关系问题 -> 方言叫法）
private val PROVINCE_KIN_DIALECT = mapOf(
    "北京市" to mapOf(
        "爸爸的爸爸" to "爷爷",
        "妈妈的妈妈" to "姥姥",
        "爸爸的哥哥" to "大爷（大伯）",
        "爸爸的弟弟" to "叔叔",
        "妈妈的兄弟" to "舅舅"
    ),
    "上海市" to mapOf(
        "爸爸的爸爸" to "爷爷（阿爷）",
        "妈妈的妈妈" to "外婆（阿婆）",
        "爸爸的哥哥" to "伯伯",
        "爸爸的弟弟" to "叔叔",
        "妈妈的兄弟" to "舅舅"
    ),
    "广东省" to mapOf(
        "爸爸的爸爸" to "爷爷（阿公）",
        "妈妈的妈妈" to "外婆（婆婆）",
        "爸爸的哥哥" to "伯爷",
        "爸爸的弟弟" to "叔父（阿叔）",
        "妈妈的兄弟" to "舅父（阿舅）",
        "姐姐的儿子" to "外甥"
    ),
    "四川省" to mapOf(
        "爸爸的爸爸" to "爷爷",
        "妈妈的妈妈" to "外婆（家家）",
        "爸爸的哥哥" to "大伯",
        "爸爸的弟弟" to "幺爸",
        "妈妈的兄弟" to "舅舅",
        "姐姐的儿子" to "外甥"
    ),
    "福建省" to mapOf(
        "爸爸的爸爸" to "阿公",
        "妈妈的妈妈" to "阿嬷",
        "爸爸的哥哥" to "阿伯",
        "爸爸的弟弟" to "阿叔",
        "妈妈的兄弟" to "阿舅"
    ),
    "山东省" to mapOf(
        "爸爸的爸爸" to "爷爷",
        "妈妈的妈妈" to "姥姥",
        "爸爸的哥哥" to "大爷",
        "爸爸的弟弟" to "叔叔",
        "妈妈的兄弟" to "舅舅"
    ),
    "陕西省" to mapOf(
        "爸爸的爸爸" to "爷爷（爷）",
        "妈妈的妈妈" to "外婆（婆）",
        "爸爸的哥哥" to "伯父（大伯）",
        "爸爸的弟弟" to "叔父",
        "妈妈的兄弟" to "舅舅"
    ),
    "湖南省" to mapOf(
        "爸爸的爸爸" to "嗲嗲",
        "妈妈的妈妈" to "外婆（家家）",
        "爸爸的哥哥" to "伯伯",
        "爸爸的弟弟" to "满满",
        "妈妈的兄弟" to "舅舅"
    ),
    "浙江省" to mapOf(
        "爸爸的爸爸" to "爷爷（阿爷）",
        "妈妈的妈妈" to "外婆（阿婆）",
        "爸爸的哥哥" to "伯伯",
        "爸爸的弟弟" to "叔叔",
        "妈妈的兄弟" to "舅舅"
    ),
    "江苏省" to mapOf(
        "爸爸的爸爸" to "爷爷",
        "妈妈的妈妈" to "外婆（好婆）",
        "爸爸的哥哥" to "伯伯",
        "爸爸的弟弟" to "叔叔",
        "妈妈的兄弟" to "舅舅"
    ),
    "河南省" to mapOf(
        "爸爸的爸爸" to "爷爷",
        "妈妈的妈妈" to "外婆（姥姥）",
        "爸爸的哥哥" to "大爷",
        "爸爸的弟弟" to "叔叔",
        "妈妈的兄弟" to "舅爷"
    ),
    "黑龙江省" to mapOf(
        "爸爸的爸爸" to "爷爷",
        "妈妈的妈妈" to "姥姥",
        "爸爸的哥哥" to "大爷",
        "爸爸的弟弟" to "老叔",
        "妈妈的兄弟" to "舅舅"
    ),
    "重庆市" to mapOf(
        "爸爸的爸爸" to "爷爷",
        "妈妈的妈妈" to "外婆",
        "爸爸的哥哥" to "伯伯（大爷）",
        "爸爸的弟弟" to "幺爸",
        "妈妈的兄弟" to "舅舅"
    )
)

// 中国56个民族
private val CHINA_ETHNIC_GROUPS = listOf(
    "汉族","蒙古族","回族","藏族","维吾尔族","苗族","彝族","壮族","布依族","朝鲜族",
    "满族","侗族","瑶族","白族","土家族","哈尼族","哈萨克族","傣族","黎族","傈僳族",
    "佤族","畲族","高山族","拉祜族","水族","东乡族","纳西族","景颇族","柯尔克孜族","土族",
    "达斡尔族","仫佬族","羌族","布朗族","撒拉族","毛南族","仡佬族","锡伯族","阿昌族","普米族",
    "塔吉克族","怒族","乌孜别克族","俄罗斯族","鄂温克族","德昂族","保安族","裕固族","京族","塔塔尔族",
    "独龙族","鄂伦春族","赫哲族","门巴族","珞巴族","基诺族"
)

// 部分民族的特色称呼（示例：民族 -> 关系问题 -> 特色叫法）
private val ETHNIC_KIN_SPECIAL = mapOf(
    "藏族" to mapOf(
        "爸爸的哥哥" to "阿古（Aku）",
        "爸爸的弟弟" to "阿古（Aku）",
        "妈妈的兄弟" to "阿古（Aku）"
    ),
    "维吾尔族" to mapOf(
        "爸爸的哥哥" to "大爸爸（Chong Dada）",
        "爸爸的弟弟" to "小爸爸（Kichik Dada）",
        "妈妈的兄弟" to "舅舅（Taga）"
    ),
    "壮族" to mapOf(
        "爸爸的哥哥" to "伯伯",
        "爸爸的弟弟" to "叔叔",
        "妈妈的兄弟" to "舅舅"
    ),
    "满族" to mapOf(
        "爸爸的哥哥" to "大爷（Amba De）",
        "爸爸的弟弟" to "叔叔（Aja）"
    ),
    "苗族" to mapOf(
        "爸爸的哥哥" to "大伯",
        "妈妈的兄弟" to "舅舅"
    ),
    "彝族" to mapOf(
        "爸爸的哥哥" to "阿波（Abo）",
        "爸爸的弟弟" to "阿窝（Awo）"
    ),
    "回族" to mapOf(
        "爸爸的哥哥" to "大大（大伯）",
        "妈妈的兄弟" to "舅舅"
    ),
    "朝鲜族" to mapOf(
        "爸爸的哥哥" to "大爷（Keun Abeoji）",
        "爸爸的弟弟" to "叔叔（Jageun Abeoji）",
        "妈妈的兄弟" to "舅舅（Oesamchon）"
    )
)

@Composable
private fun RelationKinScreenView(context: Context) {
    var selectedRelation by remember { mutableStateOf(KIN_RELATIONS[0]) }
    var selectedEthnic by remember { mutableStateOf("汉族") }
    var selectedProvince by remember { mutableStateOf("北京市") }
    var regionExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ToolHeaderBanner(
                title = "关系认知 · 亲戚称呼查询",
                desc = "姐姐的弟弟叫什么？姐姐的儿子叫什么？支持全国省份选择 + 56个民族 + 方言版叫法。",
                icon = Icons.Filled.Groups
            )
        }

        // 亲属关系选择（chips 横滑）
        item {
            Text(
                text = "选择关系",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(KIN_RELATIONS.size) { i ->
                    val r = KIN_RELATIONS[i]
                    FilterChip(
                        selected = selectedRelation == r,
                        onClick = { selectedRelation = r },
                        label = { Text(r.ask, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // 查询结果卡
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "「${selectedRelation.ask}」怎么称呼？",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    // 方言版叫法（按省份优先）
                    val dialect = PROVINCE_KIN_DIALECT[selectedProvince]?.get(selectedRelation.ask)
                    val special = ETHNIC_KIN_SPECIAL[selectedEthnic]?.get(selectedRelation.ask)
                    // 展示顺序：方言版 → 民族特色 → 普通话通用
                    val answer = dialect ?: special ?: selectedRelation.common
                    Text(
                        text = "【$selectedProvince · $selectedEthnic】称呼：",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = answer,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (dialect != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "（方言版叫法，源自$selectedProvince）",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else if (special != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "（$selectedEthnic 民族特色叫法）",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "（普通话通用称谓；不同地区方言可能有差异）",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (selectedRelation.tip.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "提示：${selectedRelation.tip}",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 省份选择（全国 34 省级行政区）
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.6f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.7f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    var provinceExpanded by remember { mutableStateOf(false) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { provinceExpanded = !provinceExpanded }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "选择省份（全国）",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "当前：$selectedProvince",
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(20.dp)
                                .rotate(if (provinceExpanded) 180f else 0f)
                        )
                    }
                    if (provinceExpanded) {
                        Spacer(modifier = Modifier.height(8.dp))
                        CHINA_PROVINCES.chunked(4).forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                row.forEach { province ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (selectedProvince == province) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                            )
                                            .clickable { selectedProvince = province; provinceExpanded = false }
                                            .padding(vertical = 7.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = province.replace("省", "").replace("市", "").replace("自治区", "").replace("特别行政区", "").replace("壮族", "桂").replace("回族", "宁").replace("维吾尔", "新").replace("藏族", "藏"),
                                            fontSize = 10.sp,
                                            fontWeight = if (selectedProvince == province) FontWeight.Bold else FontWeight.Medium,
                                            color = if (selectedProvince == province) Color.White else MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1
                                        )
                                    }
                                }
                                repeat(4 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
                            }
                        }
                    }
                }
            }
        }

        // 地区/民族选择（收纳式进度条：点击展开56个民族）
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.6f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.7f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // 收纳式选择头
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { regionExpanded = !regionExpanded }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Groups,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "选择地区 / 民族",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "当前：$selectedEthnic",
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(20.dp)
                                .rotate(if (regionExpanded) 180f else 0f)
                        )
                    }

                    // 展开后的民族收纳列表（进度条式横滑分区）
                    if (regionExpanded) {
                        Spacer(modifier = Modifier.height(8.dp))
                        CHINA_ETHNIC_GROUPS.chunked(6).forEachIndexed { groupIdx, group ->
                            Column(modifier = Modifier.padding(bottom = 4.dp)) {
                                Text(
                                    text = "第 ${groupIdx + 1} 区",
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    group.forEach { ethnic ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (selectedEthnic == ethnic) MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                                )
                                                .clickable {
                                                    selectedEthnic = ethnic
                                                    regionExpanded = false
                                                }
                                                .padding(vertical = 7.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = ethnic,
                                                fontSize = 10.sp,
                                                fontWeight = if (selectedEthnic == ethnic) FontWeight.Bold else FontWeight.Medium,
                                                color = if (selectedEthnic == ethnic) Color.White else MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                    // 补齐最后一行空位
                                    repeat(6 - group.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "说明：关系认知工具收录了普通话通用称谓与部分民族的特色叫法，不同地区方言差异较大，以当地长辈口语为准。",
                fontSize = 10.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}
