package com.example.ui.screens.toolbox

import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.util.VideoLinkParser
import kotlinx.coroutines.launch

/**
 * v1.9.1：即存（jicun）工具 —— 解析下载能力已内置到本体软件
 *
 * 「即存」原为独立的视频/图文解析下载 App（包名 com.videofix.jicun）。
 * 新版不再需要安装即存 App：复制分享链接 → 粘贴 → 解析并下载视频，全程在本体完成。
 * 即存 App 仅保留为解析失败时的兜底入口。
 *
 * 支持解析：抖音/快手/微信视频号/公众号/小红书/豆包/头条/B站/微博/西瓜/红果短剧等。
 */
@Composable
fun JicunScreenView() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var installed by remember { mutableStateOf(isJicunInstalled(context)) }

    // 内置解析下载状态
    var linkInput by remember { mutableStateOf("") }
    var parsing by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("") }
    var statusOk by remember { mutableStateOf(true) }

    fun pasteClipboard() {
        try {
            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = cm.primaryClip
            if (clip != null && clip.itemCount > 0) {
                val text = clip.getItemAt(0).text?.toString()
                if (!text.isNullOrBlank()) {
                    linkInput = text.trim()
                } else {
                    Toast.makeText(context, "剪贴板里没有可用的文字链接", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "剪贴板为空，请先复制分享链接", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "读取剪贴板失败", Toast.LENGTH_SHORT).show()
        }
    }

    fun startParseAndDownload() {
        val url = linkInput.trim()
        if (url.isBlank()) {
            statusOk = false
            statusText = "请先粘贴或输入分享链接"
            return
        }
        if (parsing) return
        parsing = true
        statusOk = true
        statusText = "正在解析链接…"
        scope.launch {
            val links = try {
                VideoLinkParser.resolve(context, url)
            } catch (e: Exception) {
                emptyList()
            }
            parsing = false
            if (links.isEmpty()) {
                statusOk = false
                statusText = "未能解析到视频直链（部分平台有访问校验），可点下方「浏览器打开」查看，或安装即存 App 备用"
            } else {
                val target = links.first()
                val ok = downloadVideo(context, target)
                statusOk = ok
                statusText = if (ok) "解析成功，已开始下载到手机「下载」文件夹（通知栏可看进度）" else "解析成功，但启动系统下载失败，请重试"
            }
        }
    }

    fun openInBrowser() {
        val url = linkInput.trim()
        if (url.isBlank()) {
            Toast.makeText(context, "请先粘贴或输入链接", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (e: Exception) {
            Toast.makeText(context, "无法打开链接", Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            NewToolHeader(
                title = "即存 · 视频解析下载",
                icon = Icons.Filled.Movie,
                desc = "复制分享链接 → 粘贴 → 直接下载视频到本地（无需安装任何 App，内置解析）"
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.60f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.70f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "支持解析的平台",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "抖音 · 快手 · 微信视频号 · 公众号 · 小红书 · 豆包 · 今日头条 · 哔哩哔哩 · 微博 · 西瓜视频 · 好看视频 · 央视频 · 皮皮虾 · 红果短剧 · 汽水音乐",
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // v1.9.1：内置解析下载主入口卡片（无需安装即存 App）
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF4DE3FF).copy(alpha = 0.08f)),
                border = BorderStroke(1.dp, Color(0xFF22C55E).copy(alpha = 0.45f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(Color(0xFF22C55E)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Download,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "? 内置解析下载（无需安装即存 App）",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = linkInput,
                        onValueChange = { linkInput = it },
                        placeholder = { Text("粘贴分享链接，如 https://v.douyin.com/xxxx/", fontSize = 12.sp, maxLines = 2) },
                        singleLine = false,
                        minLines = 1,
                        maxLines = 3,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { pasteClipboard() },
                            enabled = !parsing,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.ContentPaste, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("从剪贴板粘贴", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { startParseAndDownload() },
                        enabled = !parsing,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (parsing) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("正在解析下载…", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("解析并下载视频", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (statusText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (statusOk) "? $statusText" else "? $statusText",
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp,
                            color = if (statusOk) Color(0xFF15803D) else Color(0xFFB45309)
                        )
                        if (!statusOk) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { openInBrowser() },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("用浏览器打开链接查看", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "使用三步：① 在抖音/快手/小红书等 App 分享或复制链接 → ② 回到本页粘贴 → ③ 点「解析并下载」。文件保存在手机「下载」文件夹。",
                fontSize = 10.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 15.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 即存 App 兜底入口（保留，仅作为解析失败时的备用方案）
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (installed) Color(0xFF22C55E).copy(alpha = 0.12f)
                    else Color(0xFFF59E0B).copy(alpha = 0.12f)
                ),
                border = BorderStroke(
                    1.dp,
                    (if (installed) Color(0xFF22C55E) else Color(0xFFF59E0B)).copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (installed) "? 备用方案：即存 App 已安装" else "? 备用方案：即存 App 未安装",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = if (installed) Color(0xFF15803D) else Color(0xFFB45309)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (installed)
                            "内置解析失败时，可打开即存 App 粘贴链接解析下载。"
                        else
                            "内置解析对绝大多数链接可用；万一遇到平台加密无法解析，可安装即存 App（官方 3.0.7 版，约 24MB）兜底。",
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (installed) {
                        Button(
                            onClick = {
                                if (!launchJicun(context)) {
                                    installed = isJicunInstalled(context)
                                    Toast.makeText(context, "打开失败，请手动打开即存", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("打开即存", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = { openJicunDownload(context) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("下载即存 App（备用）", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { installed = isJicunInstalled(context) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("我已安装，检查一下", fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

/** 用系统 DownloadManager 把解析到的视频直链下载到公共「下载」文件夹 */
private fun downloadVideo(context: Context, videoUrl: String): Boolean {
    return try {
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val rawName = videoUrl.substringBefore('?').substringAfterLast('/').ifBlank { "" }
        val safeName = (rawName.takeIf { it.isNotBlank() && it.length <= 60 } ?: "video_${System.currentTimeMillis()}.mp4")
            .replace(" ", "_")
            .replace(Regex("[\\\\/:*?\"<>|]"), "_")
            .takeIf { it.contains('.') } ?: "video_${System.currentTimeMillis()}.mp4"
        val request = DownloadManager.Request(Uri.parse(videoUrl))
            .setTitle("懒得找了 · 视频解析下载")
            .setDescription(safeName)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
            .setMimeType("video/mp4")
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, safeName)
        dm.enqueue(request)
        true
    } catch (e: Exception) {
        false
    }
}

/** 是否已安装即存（com.videofix.jicun） */
private fun isJicunInstalled(context: Context): Boolean {
    return try {
        context.packageManager.getPackageInfo("com.videofix.jicun", 0) != null
    } catch (e: Exception) {
        false
    }
}

/** 打开即存 App（返回是否成功拉起） */
private fun launchJicun(context: Context): Boolean {
    return try {
        val launch = context.packageManager.getLaunchIntentForPackage("com.videofix.jicun")
        if (launch != null) {
            context.startActivity(launch)
            true
        } else {
            // 无默认入口 activity 时直接尝试显式包名
            val intent = Intent(Intent.ACTION_MAIN).apply {
                setPackage("com.videofix.jicun")
                addCategory(Intent.CATEGORY_LAUNCHER)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        }
    } catch (e: ActivityNotFoundException) {
        false
    } catch (e: Exception) {
        false
    }
}

/** 打开即存官方 Release 下载页（浏览器） */
private fun openJicunDownload(context: Context) {
    try {
        val url = "https://github.com/dhvbjvvb/jicun/releases/latest"
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    } catch (e: Exception) {
        Toast.makeText(context, "无法打开下载页，请到 GitHub 搜索 jicun", Toast.LENGTH_LONG).show()
    }
}