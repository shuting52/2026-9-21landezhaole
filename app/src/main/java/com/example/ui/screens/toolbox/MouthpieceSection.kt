package com.example.ui.screens.toolbox

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.FlameRed
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SunsetOrange

data class MouthpieceCategory(
    val id: String,
    val name: String,
    val icon: String,
    val triggers: List<String>,
    val templates: List<Pair<String, String>> // (Tone, Response)
)

private val MOUTHPIECE_CATEGORIES = listOf(
    MouthpieceCategory(
        id = "keyboard",
        name = "怼杠精网络喷子",
        icon = "💥",
        triggers = listOf(
            "就我觉得一般吗？",
            "不会吧不会吧，真有人信？",
            "你怎么这么玻璃心啊",
            "你要这么想我也没办法",
            "认真你就输了"
        ),
        templates = listOf(
            "犀利暴击" to "对对对，宇宙起源于你的觉得，连大爆炸都得先向你的审美报备。",
            "优雅体面" to "您独特的思维方式确实填补了人类未曾设想的认知空白，叹为观止。",
            "幽默自嘲" to "你说得对，但我这人有个毛病，就是不爱听正确的话，尤其是你说的。",
            "降维打击" to "如果脑子是用来做装饰的，那您今天这身极简风格确实很有品味。"
        )
    ),
    MouthpieceCategory(
        id = "workplace",
        name = "职场高情商反击",
        icon = "💼",
        triggers = listOf(
            "能者多劳，周末辛苦加个班",
            "年轻人不要光看眼前待遇，要有格局",
            "这点小事你都做不好？",
            "公司不是慈善机构",
            "大家都在加班，你怎么先走了"
        ),
        templates = listOf(
            "犀利暴击" to "能者多劳的前提是能者多得，您只画饼不给面粉，这叫无米之炊。",
            "优雅体面" to "非常理解领导的期待，为了周一能以充沛精力产出高质量成果，我按劳动法进行科学休整。",
            "幽默自嘲" to "格局太大容易着凉，我这人胃浅，目前只吃得下按时足额发放的加班费。",
            "降维打击" to "加班是能力的补丁，准时下班是效率的体现。工作已高质量交付，告辞。"
        )
    ),
    MouthpieceCategory(
        id = "relatives",
        name = "亲戚催婚问工资",
        icon = "🧧",
        triggers = listOf(
            "多大年纪了还不结婚，眼光太高了",
            "现在一个月能挣多少钱啊？",
            "女孩子读那么多书有什么用",
            "什么时候买房买车啊？",
            "我家孩子今年考上编制了，你呢"
        ),
        templates = listOf(
            "犀利暴击" to "国家都提倡优生优育，我不急着结婚，正是为了对下一代的基因和未来高度负责。",
            "优雅体面" to "托您的福，衣食无忧身体健康，倒是听说表哥房贷压力不小，您可得多帮衬点。",
            "幽默自嘲" to "赚得不多，刚好够买几把锁，把自己生活的门锁好，不让无关的人随便进来指点。",
            "降维打击" to "结婚是人生的体验卡，不是打卡机；我单身是因为选择多，不是因为凑合快。"
        )
    ),
    MouthpieceCategory(
        id = "yinyang",
        name = "优雅阴阳怪气",
        icon = "🍵",
        triggers = listOf(
            "你平时看着挺朴素的",
            "你人真好，就是太好说话了",
            "你怎么又换发型了，之前挺好的",
            "哎呀我这个人说话比较直"
        ),
        templates = listOf(
            "犀利暴击" to "您说话不是直，是根本没走脑子，直接从造化弄人那里抄了条捷径流出来的。",
            "优雅体面" to "您这份未经雕琢的原始野性，在大自然纪录片里一定很受保护动物的青睐。",
            "幽默自嘲" to "我朴素是因为气质由内而发，不像有些人，全靠声音大撑场面。",
            "降维打击" to "既然您承认自己说话不过脑子，那我就当是对讲机漏电，原谅您这一次。"
        )
    ),
    MouthpieceCategory(
        id = "borrow_money",
        name = "委婉拒绝借钱",
        icon = "💰",
        triggers = listOf(
            "在吗？借我五千周转一下，下周还",
            "咱们这交情，你还不信我？",
            "你不是刚发了年终奖吗"
        ),
        templates = listOf(
            "犀利暴击" to "交情归交情，钱归账户。为了咱们友谊长青，借钱免谈，请你喝杯奶茶倒是随时欢迎。",
            "优雅体面" to "最近刚定投了不可撤回的理财和房贷自动扣款，流动资金实在捉襟见肘，深表歉意。",
            "幽默自嘲" to "不瞒你说，我现在每天翻钱包，里面的余额都用可怜的眼神看着我呢。",
            "降维打击" to "原则第一条：不向朋友借钱，也不借钱给朋友。规矩立在前面，大家都舒坦。"
        )
    )
)

private val RANDOM_PUNCHLINES = listOf(
    "认知不同，不必强融；三观不在一个平流层，再聊就是对氧气的浪费。",
    "您有空操心我的生活，不如多给自己的发际线加点油。",
    "我活着是为了取悦自己，不是来参加你主持的人生辩论赛。",
    "你的建议很宝贵，但我建议你保留给更有需要的人，比如你自己。",
    "把你的优越感收一收，大清都亡了一百多年了，别乱发圣旨。",
    "我的脾气取决于你的态度，你若礼貌相待，我便是春风拂面；你若无理取闹，我便是冷血杀手。",
    "不尊重别人隐私的人，就像没有密码的公共WiFi，谁都能连，但没啥安全感。",
    "别拿你的尺子来量我的人生，尺寸不合，容易折断。"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MouthpieceSection(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    var selectedCategoryId by remember { mutableStateOf(MOUTHPIECE_CATEGORIES[0].id) }
    var selectedTone by remember { mutableStateOf("全部语气") }
    var customInput by remember { mutableStateOf("") }
    var generatedReplies by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    val savedFavorites = remember { mutableStateListOf<String>() }

    val currentCategory = remember(selectedCategoryId) {
        MOUTHPIECE_CATEGORIES.first { it.id == selectedCategoryId }
    }

    val generateRepliesForText = { input: String ->
        val list = mutableListOf<Pair<String, String>>()
        currentCategory.templates.forEach { (tone, reply) ->
            if (selectedTone == "全部语气" || selectedTone == tone) {
                val enhancedReply = if (input.isNotBlank()) {
                    "“$input”？—— $reply"
                } else {
                    reply
                }
                list.add(tone to enhancedReply)
            }
        }
        if (list.isEmpty()) {
            list.addAll(currentCategory.templates)
        }
        generatedReplies = list
    }

    // 初始化默认回答
    remember(selectedCategoryId, selectedTone) {
        generateRepliesForText(customInput)
        true
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Hero Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.58f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.75f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.linearGradient(listOf(FlameRed, SunsetOrange))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Chat,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "嘴强嘴替 · 神级回怼生成器",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "专治杠精、职场推锅、亲戚催婚 · 优雅不带脏字",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = {
                        val random = RANDOM_PUNCHLINES.random()
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("神级回怼", random))
                        Toast.makeText(context, "🎲 已随机复制一句神回复到剪贴板！\n$random", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SunsetOrange),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.Casino, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("随机一句", fontSize = 12.sp)
                }
            }
        }

        // 2. 场景类别选择
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(MOUTHPIECE_CATEGORIES) { category ->
                val isSelected = category.id == selectedCategoryId
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedCategoryId = category.id
                        customInput = ""
                        generateRepliesForText("")
                    },
                    label = {
                        Text(
                            text = "${category.icon} ${category.name}",
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = FlameRed,
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        // 3. 常见挑衅金句速选
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Filled.Lightbulb, contentDescription = null, tint = SunsetOrange, modifier = Modifier.size(16.dp))
                    Text(
                        text = "常见挑衅/送命题快速点击：",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    currentCategory.triggers.forEach { trigger ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.7f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                            modifier = Modifier.clickable {
                                customInput = trigger
                                generateRepliesForText(trigger)
                            }
                        ) {
                            Text(
                                text = trigger,
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }

        // 4. 自定义挑衅输入框
        OutlinedTextField(
            value = customInput,
            onValueChange = {
                customInput = it
                generateRepliesForText(it)
            },
            placeholder = { Text("输入对方说的刻薄/杠精/催婚话，一键生成绝杀回怼...", fontSize = 12.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            trailingIcon = {
                if (customInput.isNotBlank()) {
                    IconButton(onClick = {
                        customInput = ""
                        generateRepliesForText("")
                    }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "清空", modifier = Modifier.size(16.dp))
                    }
                }
            }
        )

        // 5. 语气选择器
        val toneOptions = listOf("全部语气", "犀利暴击", "优雅体面", "幽默自嘲", "降维打击")
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(toneOptions) { tone ->
                val isSelected = selectedTone == tone
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) SunsetOrange.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, if (isSelected) SunsetOrange else Color.Transparent),
                    modifier = Modifier.clickable {
                        selectedTone = tone
                        generateRepliesForText(customInput)
                    }
                ) {
                    Text(
                        text = tone,
                        fontSize = 11.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) SunsetOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // 6. 生成结果卡片列表
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            generatedReplies.forEachIndexed { index, (tone, text) ->
                val isFav = savedFavorites.contains(text)
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.65f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.85f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = when (tone) {
                                    "犀利暴击" -> FlameRed.copy(alpha = 0.15f)
                                    "优雅体面" -> ElectricCyan.copy(alpha = 0.15f)
                                    "幽默自嘲" -> SunsetOrange.copy(alpha = 0.15f)
                                    else -> NeonPurple.copy(alpha = 0.15f)
                                }
                            ) {
                                Text(
                                    text = tone,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (tone) {
                                        "犀利暴击" -> FlameRed
                                        "优雅体面" -> Color(0xFF007A87)
                                        "幽默自嘲" -> SunsetOrange
                                        else -> NeonPurple
                                    },
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(
                                    onClick = {
                                        if (isFav) savedFavorites.remove(text) else savedFavorites.add(text)
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isFav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                        contentDescription = "收藏",
                                        tint = if (isFav) FlameRed else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        try {
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_TEXT, text)
                                            }
                                            context.startActivity(Intent.createChooser(shareIntent, "分享回怼文案"))
                                        } catch (_: Exception) {}
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Filled.Share, contentDescription = "分享", modifier = Modifier.size(16.dp))
                                }

                                IconButton(
                                    onClick = {
                                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        cm.setPrimaryClip(ClipData.newPlainText("最强嘴替", text))
                                        Toast.makeText(context, "已复制回怼话术！", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Filled.ContentCopy, contentDescription = "复制", modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = text,
                            fontSize = 13.5.sp,
                            lineHeight = 19.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
