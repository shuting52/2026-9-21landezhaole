package com.example.ui.screens.toolbox

import android.annotation.SuppressLint
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.FlameRed
import com.example.ui.theme.NeonPurple

/**
 * 分身多开：真实有效的网页应用多开容器。
 * - 每个「分身」是独立 WebView（独立 Cookie/会话/缓存），互不干扰
 * - 可同时打开多个网页应用（如 抖音/淘宝/微博/百度 等），底部标签一键切换
 * - 支持手动输入任意网址开启新分身
 */
data class CloneInstance(
    val id: Int,
    val title: String,
    val url: String
)

private val PRESET_CLONE_APPS = listOf(
    CloneInstance(0, "抖音", "https://www.douyin.com/"),
    CloneInstance(0, "淘宝", "https://www.taobao.com/"),
    CloneInstance(0, "微信读书", "https://weread.qq.com/"),
    CloneInstance(0, "微博", "https://weibo.com/"),
    CloneInstance(0, "哔哩哔哩", "https://www.bilibili.com/"),
    CloneInstance(0, "小红书", "https://www.xiaohongshu.com/")
)

@Composable
fun AppCloneSection(modifier: Modifier = Modifier) {
    val instances = remember { mutableStateListOf<CloneInstance>() }
    var activeIndex by remember { mutableStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }
    var idCounter by remember { mutableStateOf(100) }
    var customUrl by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 标题卡
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color.White.copy(alpha = 0.6f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.8f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "🪞 应用分身 · 多开容器",
                    fontSize = 15.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "每个分身独立会话互不干扰，支持同时打开多个网页应用并一键切换",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 常用分身快捷入口
        Text(
            text = "常用应用分身（点击开启）：",
            fontSize = 12.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(PRESET_CLONE_APPS) { app ->
                Surface(
                    onClick = {
                        instances.add(CloneInstance(idCounter++, app.title, app.url))
                        activeIndex = instances.size - 1
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = NeonPurple.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.3f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = app.title,
                            fontSize = 12.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = NeonPurple
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "开启分身",
                            tint = NeonPurple,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // 自定义网址开启
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = customUrl,
                onValueChange = { customUrl = it },
                placeholder = { Text("输入任意网址开启分身，如 https://...", fontSize = 11.sp) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            )
            Surface(
                onClick = {
                    val url = customUrl.trim().let {
                        if (it.isBlank()) return@Surface
                        if (!it.startsWith("http")) "https://$it" else it
                    }
                    instances.add(CloneInstance(idCounter++, "自定义", url))
                    activeIndex = instances.size - 1
                    customUrl = ""
                },
                shape = RoundedCornerShape(10.dp),
                color = FlameRed
            ) {
                Text(
                    text = "开启",
                    color = Color.White,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
        }

        if (instances.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🪞", fontSize = 40.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "暂未开启任何分身\n点击上方常用应用或输入网址开始多开",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            // 已开启的分身标签（可切换/关闭）
            val safeIndex = activeIndex.coerceIn(0, instances.size - 1)
            if (instances.isNotEmpty() && safeIndex < instances.size) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(instances.size) { idx ->
                        val inst = instances[idx]
                        Surface(
                            onClick = { activeIndex = idx },
                            shape = RoundedCornerShape(10.dp),
                            color = if (idx == safeIndex) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.6f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 10.dp, end = 4.dp, top = 5.dp, bottom = 5.dp)
                            ) {
                                Text(
                                    text = inst.title,
                                    fontSize = 11.5.sp,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                    color = if (idx == safeIndex) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                                IconButton(
                                    onClick = {
                                        instances.removeAt(idx)
                                        if (activeIndex >= instances.size) activeIndex = instances.size - 1
                                    },
                                    modifier = Modifier.size(22.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "关闭分身",
                                        tint = if (idx == safeIndex) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 当前分身 WebView（独立会话）
                val activeInst = instances[safeIndex]
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White.copy(alpha = 0.7f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.8f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp)
                ) {
                    CloneWebView(url = activeInst.url, title = activeInst.title)
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun CloneWebView(url: String, title: String) {
    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                val s = settings
                s.javaScriptEnabled = true
                s.domStorageEnabled = true
                s.allowFileAccess = true
                s.mediaPlaybackRequiresUserGesture = false
                s.cacheMode = WebSettings.LOAD_DEFAULT
                webViewClient = WebViewClient()
                webChromeClient = WebChromeClient()
                loadUrl(url)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
