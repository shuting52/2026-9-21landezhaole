package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 24小时不断轮播跑马灯公告内容
 */
const val MARQUEE_ANNOUNCEMENT_TEXT = "欢迎使用懒得找应用软件，这里的资源丰富，很多资源都是可以白嫖的。请自寻探索~~现在是早上的9:00，请工作的时候注意适当不要太累生命才是最大的财富"

/**
 * 24小时不断轮播跑马灯公告组件（由原IP定位系统改造升级）
 * 轮播文案："欢迎使用懒得找应用软件，这里的资源丰富，很多资源都是可以白嫖的。请自寻探索~~现在是早上的9:00，请工作的时候注意适当不要太累生命才是最大的财富"
 */
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
    val context = LocalContext.current
    var showDetailDialog by remember { mutableStateOf(false) }

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
            .clickable { showDetailDialog = true }
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
                text = text,
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

            // 24h 轮播中动态指示灯
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(primaryColor.copy(alpha = 0.1f))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981))
                )
                Text(
                    text = "24h轮播",
                    color = primaryColor,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // 点击弹窗：查看完整公告详情与复制
    if (showDetailDialog) {
        AlertDialog(
            onDismissRequest = { showDetailDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Campaign,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "懒得找 · 系统温馨公告",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = text,
                            fontSize = 13.5.sp,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "✦ 提示：本广播 24 小时全天候滚动播报，随时为您提醒休息与探索最新福利！",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDetailDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text("我知道了")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("系统公告", text))
                        Toast.makeText(context, "已复制公告内容到剪贴板", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("复制")
                }
            }
        )
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
