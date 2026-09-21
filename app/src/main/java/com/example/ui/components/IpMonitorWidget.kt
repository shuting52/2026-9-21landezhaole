package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar
import kotlinx.coroutines.delay

/**
 * 24小时不断轮播跑马灯公告内容
 */
const val MARQUEE_ANNOUNCEMENT_TEXT = "欢迎使用懒得找应用软件，这里的资源丰富，很多资源都是可以白嫖的。请自寻探索~~现在是早上的9:00，请工作的时候注意适当不要太累生命才是最大的财富"

/**
 * 根据当前小时（0-23点）动态生成对应时间段播报文案。
 * 每个时间段（深夜/清晨/早上/上午/中午/下午/傍晚/晚上）播放不同的问候文字，
 * 并实时显示当前时间（HH:mm）。
 */
fun getHourlyMarqueeText(calendar: Calendar = Calendar.getInstance()): String {
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)
    val hh = "%02d".format(hour)
    val mm = "%02d".format(minute)
    val prefix = "欢迎使用懒得找应用软件，这里的资源丰富，很多资源都是可以白嫖的。请自寻探索~~"
    val tail = "，请工作的时候注意适当不要太累，生命才是最大的财富"
    val periodText = when (hour) {
        in 0..4 -> "现在是深夜$hh:$mm，夜深人静，早点休息养足精神，明天继续探索～"
        in 5..6 -> "现在是清晨$hh:$mm，新的一天开始啦，早起的人运气不会差，快去发现宝藏吧！"
        in 7..9 -> "现在是早上$hh:$mm，早上好呀！一日之计在于晨，工作学习注意劳逸结合～"
        in 10..11 -> "现在是上午$hh:$mm，上午好！专注工作学习的同时，记得起身活动一下哦～"
        in 12..13 -> "现在是中午$hh:$mm，中午好！记得按时吃饭，饭后小憩，下午继续加油～"
        in 14..16 -> "现在是下午$hh:$mm，下午好！来杯水提提神，继续探索无限资源吧～"
        in 17..18 -> "现在是傍晚$hh:$mm，傍晚好！忙碌了一天辛苦了，给自己一点放松时间吧～"
        else -> "现在是晚上$hh:$mm，晚上好！注意休息，不要太累～"
    }
    return prefix + periodText + tail
}
@Composable
fun IpMonitorWidget(
    modifier: Modifier = Modifier
) {
    MarqueeNoticeWidget(modifier = modifier)
}

/**
 * 跑马灯公告栏主组件
 */
@Composable
fun MarqueeNoticeWidget(
    modifier: Modifier = Modifier,
    text: String = MARQUEE_ANNOUNCEMENT_TEXT
) {
    // 按当前小时动态生成播报文案，每分钟刷新一次（时间与时间段文字实时变化）
    var displayText by remember { mutableStateOf(getHourlyMarqueeText()) }
    LaunchedEffect(Unit) {
        while (true) {
            displayText = getHourlyMarqueeText()
            delay(60_000L)
        }
    }
    // 外部显式传入自定义文字时优先使用外部文字，否则使用按小时动态文案
    val effectiveText = if (text == MARQUEE_ANNOUNCEMENT_TEXT) displayText else text

    val infiniteTransition = rememberInfiniteTransition(label = "marquee_horn_pulse")
    val hornScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hornScale"
    )

    val primaryColor = MaterialTheme.colorScheme.primary

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = 1.dp,
                color = primaryColor.copy(alpha = 0.22f),
                shape = RoundedCornerShape(10.dp)
            )
            .testTag("ip_monitor_widget"),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 5.dp)
                .fillMaxWidth()
        ) {
            // 广播小喇叭徽标
            Box(
                modifier = Modifier
                    .scale(hornScale)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                primaryColor,
                                primaryColor.copy(alpha = 0.85f)
                            )
                        )
                    )
                    .padding(horizontal = 5.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Campaign,
                        contentDescription = "公告播报",
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "公告",
                        color = Color.White,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 24小时不断轮播跑马灯文字
            Text(
                text = effectiveText,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                modifier = Modifier
                    .weight(1f)
                    .basicMarquee(
                        iterations = Int.MAX_VALUE,
                        velocity = 35.dp
                    )
                    .testTag("marquee_announcement_text")
            )

            Spacer(modifier = Modifier.width(6.dp))

            Spacer(modifier = Modifier.width(6.dp))
        }
    }
}

/**
 * 兼容性存根，保证任何旧类型引用安全
 */
data class IpLocationInfo(
    val ip: String = "127.0.0.1",
    val location: String = "中国",
    val xhsLocation: String = "IP属地：中国",
    val latencyMs: Long = 20L
)

object IpRepository {
    val cachedInfo: IpLocationInfo? = IpLocationInfo()
}
