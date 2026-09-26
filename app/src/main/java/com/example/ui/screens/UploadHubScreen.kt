package com.example.ui.screens

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.GridItemSpan
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.local.db.UploadedResourceEntity
import com.example.data.util.VideoCache

/** v1.8.7：资源自动归类关键词（用于自动识别软件是做什么的） */
private val AUTO_CATEGORY_RULES = listOf(
    "影视/视频" to listOf("视频", "影视", "短剧", "剧场", "电影", "剧集", "播放器", "TV", "movie", "video", "播放"),
    "阅读/小说" to listOf("小说", "阅读", "漫画", "电子书", "book", "read", "novel"),
    "音乐/听歌" to listOf("音乐", "听歌", "歌词", "music", "song", "音频"),
    "游戏/娱乐" to listOf("游戏", "steam", "game", "娱乐", "play"),
    "工具/效率" to listOf("工具", "助手", "清理", "卸载", "压缩", "转换", "下载", "tool", "utils", "效率"),
    "学习/办公" to listOf("学习", "办公", "笔记", "文档", "pdf", "office", "课程", "学"),
    "AI/智能" to listOf("AI", "ai", "智能", "GPT", "大模型", "对话", "写作", "绘画"),
    "系统/装机" to listOf("系统", "装机", "激活", "驱动", "系统优化", "windows", "win")
)

/** v1.8.7：自动识别软件类型归类 */
private fun autoCategorize(res: UploadedResourceEntity): String {
    val text = (res.title + " " + res.desc + " " + res.tags + " " + res.url).lowercase()
    for ((cat, keywords) in AUTO_CATEGORY_RULES) {
        if (keywords.any { text.contains(it, ignoreCase = true) }) return cat
    }
    return "其他资源"
}

/** v1.8.7：自动识别站点/软件 icon（Google favicon 服务，url 为空或失败时回退文字徽标） */
private fun autoFaviconUrl(url: String): String {
    return try {
        val host = java.net.URI(if (url.startsWith("http")) url else "https://$url").host ?: return ""
        "https://www.google.com/s2/favicons?domain=$host&sz=64"
    } catch (e: Exception) {
        ""
    }
}

/**
 * 资源展示页（软件 / Skill）v1.8.7：
 * - 软件版块（gridMode=true）：三列一排网格 + 自动归类分组 + 自动识别 icon
 * - Skill 版块（gridMode=false）：保持原单列大卡片（含预览/视频）
 * - 内容完全由云端控制台同步；控制台删除 → 本体实时同步移除
 */
@Composable
fun UploadHubScreen(
    title: String,
    subtitle: String,
    resourceType: String,
    resources: List<UploadedResourceEntity>,
    onDelete: (id: String) -> Unit,
    modifier: Modifier = Modifier,
    showDelete: Boolean = true,
    gridMode: Boolean = false
) {
    val context = LocalContext.current

    Box(modifier = modifier.fillMaxSize()) {
        if (gridMode && resourceType == "software") {
            // ===== 软件版块：三列网格 + 自动归类 =====
            // 自动归类分组（保持云端的顺序，仅分组显示）
            val grouped = remember(resources) {
                val map = LinkedHashMap<String, MutableList<UploadedResourceEntity>>()
                resources.forEach { map.getOrPut(autoCategorize(it)) { mutableListOf() }.add(it) }
                map.toList()
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item(span = { GridItemSpan(3) }) {
                    Column(modifier = Modifier.padding(bottom = 6.dp)) {
                        Text(
                            text = title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = subtitle + (if (resources.isNotEmpty()) "　·　共 ${resources.size} 款" else ""),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (resources.isEmpty()) {
                    item(span = { GridItemSpan(3) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Filled.Download,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "还未获取到任何资源哟 请联系作者",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    grouped.forEach { (cat, list) ->
                        item(span = { GridItemSpan(3) }) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 10.dp, bottom = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(14.dp)
                                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = cat,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${list.size} 款",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        items(list, key = { it.id }) { res ->
                            SoftwareGridCard(
                                res = res,
                                showDelete = showDelete,
                                onDelete = {
                                    onDelete(res.id)
                                    Toast.makeText(context, "已删除（云端同步）", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        } else {
            // ===== Skill / 其它：原单列大卡片 =====
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Column(modifier = Modifier.padding(bottom = 6.dp)) {
                        Text(
                            text = title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = subtitle,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (resources.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Filled.Download,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "还未获取到任何资源哟 请联系作者",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(resources, key = { it.id }) { res ->
                        ResourceFileCard(
                            res = res,
                            showDelete = showDelete,
                            onDelete = {
                                onDelete(res.id)
                                Toast.makeText(context, "已删除（云端同步）", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}

/** 软件网格小卡片：自动 icon + 标题 + 类型徽标 */
@Composable
private fun SoftwareGridCard(
    res: UploadedResourceEntity,
    showDelete: Boolean = true,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val isFileMode = res.mode != "url"
    val fileLink = res.fileUrl.ifBlank { res.url }
    val url = if (isFileMode) fileLink else res.url.ifBlank { res.fileUrl }
    val isApk = url.endsWith(".apk", ignoreCase = true)
    val isZip = url.endsWith(".zip", ignoreCase = true)
    val isMd = url.endsWith(".md", ignoreCase = true)
    val canInstall = isApk || isZip || isMd

    val badgeText = when {
        isApk -> "APK"
        isZip -> "ZIP"
        isMd -> "MD"
        else -> "直达"
    }
    val badgeColor = when {
        isApk -> Color(0xFF22C55E)
        isZip -> Color(0xFF6366F1)
        isMd -> Color(0xFFF59E0B)
        else -> MaterialTheme.colorScheme.primary
    }

    // v1.8.7：自动识别 icon（优先云端 iconUrl，其次 Google favicon 服务）
    val displayIcon = res.iconUrl.ifBlank { autoFaviconUrl(res.url.ifBlank { res.fileUrl }) }

    fun downloadToLocal(url: String, fileName: String?) {
        try {
            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val safeName = (fileName?.ifBlank { null } ?: url.substringAfterLast('/').ifBlank { "download.bin" })
                .replace(" ", "_")
                .replace(Regex("[\\\\/:*?\"<>|]"), "_")
            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle("懒得找了 · ${res.title}")
                .setDescription("正在下载 $safeName")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedOverMetered(true)
                .setMimeType(
                    when {
                        isApk -> "application/vnd.android.package-archive"
                        isZip -> "application/zip"
                        isMd -> "text/markdown"
                        else -> "application/octet-stream"
                    }
                )
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, safeName)
            dm.enqueue(request)
            Toast.makeText(context, "已开始下载到手机「下载」文件夹", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (e2: Exception) {
                Toast.makeText(context, "下载失败，请稍后重试", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val onCardClick = {
        if (url.isNotBlank()) {
            if (canInstall) {
                downloadToLocal(url, res.title + (if (isZip) ".zip" else if (isApk) ".apk" else if (isMd) ".md" else ""))
            } else {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "无法打开：$url", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "该资源暂未配置下载链接", Toast.LENGTH_SHORT).show()
        }
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 自动识别的软件 icon（云端 icon 优先，回退 favicon，再回退文字徽标）
            Box(
                modifier = Modifier.size(44.dp),
                contentAlignment = Alignment.Center
            ) {
                if (displayIcon.isNotBlank()) {
                    coil.compose.AsyncImage(
                        model = displayIcon,
                        contentDescription = res.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                }
                // 底层类型徽标（icon 加载失败时可见）
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(badgeColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = badgeColor
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = res.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            if (res.desc.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = res.desc,
                    fontSize = 10.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    minLines = 2
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            // 自动识别按钮：apk/zip/md →「安装」；URL →「直达」
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (canInstall) Color(0xFF22C55E).copy(alpha = 0.12f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = if (canInstall) Icons.Filled.Download else Icons.Filled.OpenInNew,
                    contentDescription = null,
                    tint = if (canInstall) Color(0xFF22C55E) else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = if (canInstall) "安装" else "直达",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (canInstall) Color(0xFF22C55E) else MaterialTheme.colorScheme.primary
                )
            }
            if (showDelete) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "删除（云端同步）",
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.clickable { onDelete() }.padding(4.dp)
                )
            }
        }
    }
}

@Composable
private fun ResourceFileCard(
    res: UploadedResourceEntity,
    showDelete: Boolean = true,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val isFileMode = res.mode != "url"
    val fileLink = res.fileUrl.ifBlank { res.url }
    val url = if (isFileMode) fileLink else res.url.ifBlank { res.fileUrl }
    val isFile = isFileMode && url.isNotBlank()
    val isApk = url.endsWith(".apk", ignoreCase = true)
    val isZip = url.endsWith(".zip", ignoreCase = true)
    val isMd = url.endsWith(".md", ignoreCase = true)
    val canInstall = isApk || isZip || isMd

    val badgeText = when {
        isApk -> "APK"
        isZip -> "ZIP"
        isMd -> "MD"
        else -> "链接"
    }
    val badgeColor = when {
        isApk -> Color(0xFF22C55E)
        isZip -> Color(0xFF6366F1)
        isMd -> Color(0xFFF59E0B)
        else -> MaterialTheme.colorScheme.primary
    }

    fun downloadToLocal(url: String, fileName: String?) {
        try {
            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val safeName = (fileName?.ifBlank { null } ?: url.substringAfterLast('/').ifBlank { "download.bin" })
                .replace(" ", "_")
                .replace(Regex("[\\\\/:*?\"<>|]"), "_")
            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle("懒得找了 · ${res.title}")
                .setDescription("正在下载 $safeName")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedOverMetered(true)
                .setMimeType(
                    when {
                        isApk -> "application/vnd.android.package-archive"
                        isZip -> "application/zip"
                        isMd -> "text/markdown"
                        else -> "application/octet-stream"
                    }
                )
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, safeName)
            dm.enqueue(request)
            Toast.makeText(context, "已开始下载到手机「下载」文件夹，完成后可在通知栏查看", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (e2: Exception) {
                Toast.makeText(context, "下载失败，请稍后重试", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val onCardClick = {
        if (url.isNotBlank()) {
            if (canInstall) {
                downloadToLocal(url, res.title + (if (isZip) ".zip" else if (isApk) ".apk" else if (isMd) ".md" else ""))
            } else {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "无法打开：$url", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "该资源暂未配置下载链接", Toast.LENGTH_SHORT).show()
        }
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val displayIcon = res.iconUrl.ifBlank { autoFaviconUrl(res.url.ifBlank { res.fileUrl }) }
                if (displayIcon.isNotBlank()) {
                    AsyncImageCompat(
                        url = displayIcon,
                        fallbackText = badgeText,
                        fallbackColor = badgeColor,
                        modifier = Modifier.size(40.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(badgeColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = badgeColor
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = res.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (res.desc.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = res.desc,
                            fontSize = 11.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (showDelete) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                        Icon(
                            imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = "删除",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (res.previewUrl.isNotBlank() || res.mediaUrl.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                if (res.mediaUrl.isNotBlank()) {
                    if (res.previewUrl.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    try {
                                        val vintent = Intent(Intent.ACTION_VIEW, Uri.parse(res.mediaUrl))
                                        vintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        context.startActivity(vintent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "无法播放视频", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        ) {
                            coil.compose.AsyncImage(
                                model = res.previewUrl,
                                contentDescription = res.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f))
                                        )
                                    )
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(Color.Black.copy(alpha = 0.55f))
                                    .border(1.5.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(24.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("▶", color = Color.White, fontSize = 20.sp)
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(70.dp)
                                .background(Color(0xFF1E1E24))
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    try {
                                        val vintent = Intent(Intent.ACTION_VIEW, Uri.parse(res.mediaUrl))
                                        vintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        context.startActivity(vintent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "无法播放视频", Toast.LENGTH_SHORT).show()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "▶ 点击播放视频（带声音）",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                } else {
                    coil.compose.AsyncImage(
                        model = res.previewUrl,
                        contentDescription = res.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(res.previewUrl))
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "无法打开预览图", Toast.LENGTH_SHORT).show()
                                }
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (res.author.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(5.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = res.author,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                if (res.tags.isNotBlank()) {
                    Text(
                        text = res.tags,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (canInstall) Color(0xFF22C55E).copy(alpha = 0.12f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = if (canInstall) Icons.Filled.Download else Icons.Filled.OpenInNew,
                        contentDescription = null,
                        tint = if (canInstall) Color(0xFF22C55E) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (canInstall) "安装" else "直达",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (canInstall) Color(0xFF22C55E) else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun AsyncImageCompat(
    url: String,
    fallbackText: String,
    fallbackColor: Color,
    modifier: Modifier
) {
    Box(modifier = modifier.clip(RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(fallbackColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = fallbackText,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = fallbackColor
            )
        }
        coil.compose.AsyncImage(
            model = url,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}