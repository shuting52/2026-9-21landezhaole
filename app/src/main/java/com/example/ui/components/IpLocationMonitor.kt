package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.IpMonitorDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * 首页置顶 · 实时 IP 定位监控组件
 * - 由后台控制台配置：开关 + 数据源 URL
 * - UI 只展示「定位 IP + 所在地区」，不出现任何网站/数据源字样
 * - 每 30 秒自动刷新一次；控制台改动后配置实时同步生效
 */
@Composable
fun IpLocationMonitorWidget(
    cloudIpMonitor: IpMonitorDto?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var ipText by remember { mutableStateOf("定位获取中…") }
    var failed by remember { mutableStateOf(false) }

    val enabled = cloudIpMonitor?.enabled == true
    val url = cloudIpMonitor?.url?.trim().orEmpty()

    LaunchedEffect(enabled, url) {
        if (!enabled || url.isBlank()) return@LaunchedEffect
        while (true) {
            val result = withContext(Dispatchers.IO) { fetchIpInfo(url) }
            if (result != null) {
                ipText = result
                failed = false
            } else {
                failed = true
            }
            delay(30_000L)
        }
    }

    // 关闭或未配置时不渲染（本体首页不再呈现）
    if (!enabled || url.isBlank()) return

    val primaryColor = MaterialTheme.colorScheme.primary

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, primaryColor.copy(alpha = 0.20f), RoundedCornerShape(8.dp))
            .clickable {
                Toast.makeText(context, "IP 定位：$ipText", Toast.LENGTH_SHORT).show()
            }
            .testTag("ip_location_monitor"),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(primaryColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = "定位",
                    tint = primaryColor,
                    modifier = Modifier.size(12.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "定位",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (failed) "定位获取失败，稍后重试…" else ipText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(4.dp))
            // 呼吸状态点
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (failed) Color(0xFFEF4444) else Color(0xFF22C55E))
            )
        }
    }
}

/**
 * 请求 IP 定位数据源，解析为「IP · 所在地区」中文精准字符串。
 * 优先适配 ip-api.com 中文（?lang=zh-CN），兼容 ipinfo.io / ip.useragentinfo.com 等常见格式。
 */
private fun fetchIpInfo(url: String): String? {
    return try {
        val client = OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 (Linux; Android) Lzdz/1.6.4")
            .build()
        client.newCall(request).execute().use { resp ->
            if (!resp.isSuccessful) return null
            val body = resp.body?.string() ?: return null
            val obj = JSONObject(body)
            // ip-api.com 中文格式（?lang=zh-CN）：query / city / regionName / country / isp
            val ip = obj.optString("query").ifBlank { obj.optString("ip") }
            val city = obj.optString("city")
            val region = obj.optString("regionName")
            val province = obj.optString("province") // ip.useragentinfo.com 中文
            val country = obj.optString("country")
            // 优先用中文地区信息，去重
            val loc = listOf(city, region, province, country)
                .filter { it.isNotBlank() && it != "N/A" && it != "--" }
                .distinct()
                .joinToString(" ")
            when {
                ip.isNotBlank() && loc.isNotBlank() -> "$ip · $loc"
                ip.isNotBlank() -> ip
                else -> null
            }
        }
    } catch (e: Exception) {
        null
    }
}
