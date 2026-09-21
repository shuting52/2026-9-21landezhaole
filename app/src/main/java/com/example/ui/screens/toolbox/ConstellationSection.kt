package com.example.ui.screens.toolbox

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.FlameRed
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SunsetOrange

data class ZodiacSign(
    val name: String,
    val symbol: String,
    val dateRange: String,
    val element: String, // 火象, 土象, 风象, 水象
    val ruler: String,
    val traits: String,
    val luckyColor: String,
    val luckyNumber: String,
    val bestMatch: String,
    val fortuneOverall: Int,
    val fortuneWealth: Int,
    val fortuneLove: Int,
    val fortuneCareer: Int,
    val dailyAdvice: String,
    val dailyWarning: String
)

private val ALL_ZODIACS = listOf(
    ZodiacSign("白羊座", "♈", "03.21 - 04.19", "火象星座", "火星", "热情果断、勇往直前、赤子之心", "正红色", "7, 9", "狮子座、射手座", 5, 4, 5, 4, "今日行动力爆棚，非常适合启动新项目或主动联系重要伙伴。", "切忌急躁冒进，说话前先过三秒思考。"),
    ZodiacSign("金牛座", "♉", "04.20 - 05.20", "土象星座", "金星", "稳重踏实、审美卓越、理财高手", "森林绿", "6, 8", "处女座、摩羯座", 4, 5, 4, 5, "财运旺盛，在理财规划或长期投资上有灵感闪现。", "不要过分固执己见，多听取团队建议。"),
    ZodiacSign("双子座", "♊", "05.21 - 06.21", "风象星座", "水星", "灵动多变、机智过人、社交达人", "亮黄色", "3, 5", "天秤座、水瓶座", 5, 4, 5, 4, "思维极其敏捷，头脑风暴和社交聚会中你能成为焦点。", "避免三分钟热度，把手头的事做彻底。"),
    ZodiacSign("巨蟹座", "♋", "06.22 - 07.22", "水象星座", "月亮", "温和细腻、顾家重情、共情力强", "珍珠白", "2, 7", "天蝎座、双鱼座", 4, 4, 5, 4, "直觉敏锐，与家人和密友的沟通温馨且能获得情感支持。", "别把别人的无心之语过度脑补。"),
    ZodiacSign("狮子座", "♌", "07.23 - 08.22", "火象星座", "太阳", "自信霸气、慷慨仗义、领袖魅力", "金黄色", "1, 9", "白羊座、射手座", 5, 5, 4, 5, "气场全开，重要决策上展现决断力能赢得赞誉。", "放下不必要的偶像包袱，放低身段更受人欢迎。"),
    ZodiacSign("处女座", "♍", "08.23 - 09.22", "土象星座", "水星", "严谨细致、追求完美、逻辑大师", "雾霾蓝", "4, 8", "金牛座、摩羯座", 4, 4, 4, 5, "工作与规划条理清晰，梳理复杂事务能事半功倍。", "不必苛求每个人都达到你的满分标准。"),
    ZodiacSign("天秤座", "♎", "09.23 - 10.23", "风象星座", "金星", "优雅和谐、审美高级、追求公平", "淡粉色", "6, 9", "双子座、水瓶座", 5, 4, 5, 4, "人际交往如鱼得水，今日适合合作谈判与美学创作。", "拒绝选择困难症，相信直觉快速拍板。"),
    ZodiacSign("天蝎座", "♏", "10.24 - 11.22", "水象星座", "冥王星", "深沉专注、洞察秋毫、意志坚定", "暗紫色", "2, 8", "巨蟹座、双鱼座", 5, 5, 4, 5, "深度思考能力出众，能一针见血发现潜在关键漏洞。", "适当展示温度，别用冷漠做防护罩。"),
    ZodiacSign("射手座", "♐", "11.23 - 12.21", "火象星座", "木星", "乐观豁达、向往自由、哲学思维", "天蓝色", "3, 7", "白羊座、狮子座", 5, 4, 5, 4, "心态阳光豁达，遇到困难能轻松化解，户外出行有惊喜。", "注意细节承诺，答应别人的事情记在本子上。"),
    ZodiacSign("摩羯座", "♑", "12.22 - 01.19", "土象星座", "土星", "沉稳坚韧、务实自律、大器晚成", "深灰色", "4, 8", "金牛座、处女座", 4, 5, 3, 5, "事业运稳步上升，长远规划正在步入正轨。", "给自己预留休息时间，别让神经紧绷太久。"),
    ZodiacSign("水瓶座", "♒", "01.20 - 02.18", "风象星座", "天王星", "独具匠心、前卫通透、人道主义", "电光青", "1, 5", "双子座、天秤座", 5, 4, 4, 5, "灵感如泉涌，你的前沿思路能打破常规框架。", "多点耐心解释你的想法，别人可能慢半拍。"),
    ZodiacSign("双鱼座", "♓", "02.19 - 03.20", "水象星座", "海王星", "浪漫梦幻、善良纯真、灵性感知", "海藻绿", "2, 6", "巨蟹座、天蝎座", 4, 4, 5, 4, "艺术创作与情感交流运极佳，温和包容让人倍感安心。", "区分现实与幻想，把美好的愿景落到执行清单上。")
)

@Composable
fun ConstellationSection(modifier: Modifier = Modifier) {
    var selectedIndex by remember { mutableIntStateOf(0) }
    val currentZodiac = ALL_ZODIACS[selectedIndex]

    // 速配星座选择
    var matchIndex1 by remember { mutableIntStateOf(0) }
    var matchIndex2 by remember { mutableIntStateOf(4) } // 狮子座

    val matchScore = remember(matchIndex1, matchIndex2) {
        val z1 = ALL_ZODIACS[matchIndex1]
        val z2 = ALL_ZODIACS[matchIndex2]
        if (z1.bestMatch.contains(z2.name) || z2.bestMatch.contains(z1.name)) {
            98
        } else if (z1.element == z2.element) {
            92
        } else if ((z1.element.contains("火") && z2.element.contains("风")) || (z1.element.contains("土") && z2.element.contains("水"))) {
            88
        } else {
            76
        }
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
                            .background(Brush.linearGradient(listOf(NeonPurple, SunsetOrange))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Stars,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "精准星座 · 12星盘与运势速查",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "守护星 · 性格档案 · 今日综合运势 · 星座契合度速配",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 2. 12 星座横向滑块
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(ALL_ZODIACS.indices.toList()) { idx ->
                val zodiac = ALL_ZODIACS[idx]
                val isSelected = selectedIndex == idx
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedIndex = idx },
                    label = {
                        Text(
                            text = "${zodiac.symbol} ${zodiac.name}",
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonPurple,
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        // 3. 当前星座核心档案卡
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.65f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.8f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = currentZodiac.symbol,
                            fontSize = 32.sp,
                            color = NeonPurple
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currentZodiac.name,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = NeonPurple.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = currentZodiac.element,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NeonPurple,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "公历出生区间：${currentZodiac.dateRange}",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SunsetOrange.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "守护星：${currentZodiac.ruler}",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = SunsetOrange,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "特质定位：${currentZodiac.traits}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ZodiacMiniPill(label = "幸运色", value = currentZodiac.luckyColor, modifier = Modifier.weight(1f))
                    ZodiacMiniPill(label = "幸运数字", value = currentZodiac.luckyNumber, modifier = Modifier.weight(1f))
                    ZodiacMiniPill(label = "天作之合", value = currentZodiac.bestMatch.split("、").first(), modifier = Modifier.weight(1f))
                }
            }
        }

        // 4. 今日星盘运势
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.65f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.8f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = SunsetOrange, modifier = Modifier.size(18.dp))
                    Text(
                        text = "今日运势指数星盘",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FortuneStarCard(title = "综合运势", stars = currentZodiac.fortuneOverall, modifier = Modifier.weight(1f))
                    FortuneStarCard(title = "财运指数", stars = currentZodiac.fortuneWealth, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FortuneStarCard(title = "桃花情缘", stars = currentZodiac.fortuneLove, modifier = Modifier.weight(1f))
                    FortuneStarCard(title = "事业学业", stars = currentZodiac.fortuneCareer, modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "✨ 今日宜：${currentZodiac.dailyAdvice}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text(text = "⚠️ 今日忌：${currentZodiac.dailyWarning}", fontSize = 12.sp, color = FlameRed)
                    }
                }
            }
        }

        // 5. 双星座契合度速配查询
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.65f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.8f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Filled.Favorite, contentDescription = null, tint = FlameRed, modifier = Modifier.size(18.dp))
                    Text(
                        text = "双星座契合度速配",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 星座 1 选择
                    ZodiacPickerChip(
                        selected = ALL_ZODIACS[matchIndex1],
                        onSelectNext = { matchIndex1 = (matchIndex1 + 1) % ALL_ZODIACS.size }
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$matchScore%",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = FlameRed
                        )
                        Text(
                            text = if (matchScore > 90) "天作之合" else if (matchScore > 80) "默契十足" else "互补磨合",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // 星座 2 选择
                    ZodiacPickerChip(
                        selected = ALL_ZODIACS[matchIndex2],
                        onSelectNext = { matchIndex2 = (matchIndex2 + 1) % ALL_ZODIACS.size }
                    )
                }
            }
        }
    }
}

@Composable
private fun ZodiacMiniPill(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun FortuneStarCard(title: String, stars: Int, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurface)
            Row {
                repeat(5) { i ->
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = if (i < stars) SunsetOrange else Color.LightGray.copy(alpha = 0.6f),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ZodiacPickerChip(
    selected: ZodiacSign,
    onSelectNext: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = NeonPurple.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.3f)),
        modifier = Modifier.clickable { onSelectNext() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(text = selected.symbol, fontSize = 18.sp, color = NeonPurple)
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = selected.name, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "▼", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
