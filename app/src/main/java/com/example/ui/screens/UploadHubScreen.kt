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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
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
import com.example.ui.components.SkillDetailDialog
import com.example.ui.components.UTabRow

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
    showDelete: Boolean = false,
    gridMode: Boolean = false,
    // v1.0.18 软件版块 .u-tab 推荐/关注/热门：favoriteUrls 用于「关注」筛选
    favoriteUrls: Set<String> = emptySet()
) {
    val context = LocalContext.current

    // Skill 技能库：点击卡片弹独立详情（视频预览/提示词/下载/跳转）
    var skillDetail by remember { mutableStateOf<UploadedResourceEntity?>(null) }

    // v1.0.18 软件版块 .u-tab：0=推荐（全部）/ 1=关注（收藏）/ 2=热门（badge 含 热门/推荐/精选/官方）
    var softTabIndex by remember { mutableStateOf(0) }
    val softTabList = listOf("推荐", "关注", "热门")
    val displayResources = remember(softTabIndex, resources, favoriteUrls) {
        when (softTabIndex) {
            1 -> resources.filter { it.url in favoriteUrls || it.id in favoriteUrls }
            2 -> resources.filter {
                val b = it.badge + it.desc + it.title
                b.contains("热门") || b.contains("推荐") || b.contains("精选") || b.contains("官方")
            }
            else -> resources
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (gridMode && (resourceType == "software" || resourceType == "skill")) {
            // ===== 软件/Skill：v1.0.19 取消展开收纳标签，直接平铺呈现 =====
            // 软件版块：保留 .u-tab 推荐/关注/热门筛选，分类分组标题仅作静态分区（不可点、不收纳）
            val grouped = remember(displayResources) {
                val map = LinkedHashMap<String, MutableList<UploadedResourceEntity>>()
                displayResources.forEach { map.getOrPut(autoCategorize(it)) { mutableListOf() }.add(it) }
                map.toList()
            }
            // 主题色渐变（软件卡片专用，v1.0.9 主题升级）
            val themePrimary = MaterialTheme.colorScheme.primary
            val themeSecondary = MaterialTheme.colorScheme.secondary
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // v1.0.18 软件版块 .u-tab：推荐 / 关注 / 热门（仅 software 显示）
                if (resourceType == "software") {
                    item {
                        UTabRow(
                            tabs = softTabList,
                            selectedIndex = softTabIndex,
                            onSelect = { softTabIndex = it },
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }
                item {
                    Column(modifier = Modifier.padding(bottom = 6.dp)) {
                        // v1.0.9 主题横幅：主题色渐变 + 圆点装饰，软件版块主题感立现
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            themePrimary.copy(alpha = 0.85f),
                                            themeSecondary.copy(alpha = 0.7f)
                                        )
                                    )
                                )
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📦", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = title,
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                    Text(
                                        text = subtitle + (if (displayResources.isNotEmpty()) "　·　共 ${displayResources.size} 款" else ""),
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.9f),
                                        maxLines = 2
                                    )
                                }
                            }
                        }
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
                    grouped.forEach { (cat, list) ->
                        // v1.0.19：分类标题静态分区（不再可点击展开/收起）
                        item(key = "cat_$cat") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(themePrimary.copy(alpha = 0.10f))
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // 主题色小图标块
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .background(themePrimary, RoundedCornerShape(7.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(cat.firstOrNull()?.toString() ?: "📁", fontSize = 11.sp, color = Color.White)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
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
                                        color = themePrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        // 全部平铺显示（不展开/收起）
                        items(list, key = { it.id }) { res ->
                            if (resourceType == "skill") {
                                SkillGridCard(
                                    res = res,
                                    onClick = { skillDetail = res },
                                    showDelete = showDelete,
                                    onDelete = {
                                        onDelete(res.id)
                                        Toast.makeText(context, "已删除（云端同步）", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            } else {
                                // 软件竖排卡片（全宽，图标在左、信息在右，自动获取软件 icon）
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
                            resourceType = resourceType,
                            showDelete = showDelete,
                            onClickOverride = if (resourceType == "skill") {
                                { skillDetail = res }
                            } else null,
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

    // Skill 技能详情独立弹窗
    skillDetail?.let { res ->
        SkillDetailDialog(
            res = res,
            onDismiss = { skillDetail = null },
            onJump = { url ->
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "无法打开跳转链接", Toast.LENGTH_SHORT).show()
                }
            },
            onDownload = { url, fileName ->
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
                                url.endsWith(".apk", true) -> "application/vnd.android.package-archive"
                                url.endsWith(".zip", true) -> "application/zip"
                                url.endsWith(".md", true) -> "text/markdown"
                                else -> "application/octet-stream"
                            }
                        )
                        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, safeName)
                    dm.enqueue(request)
                    Toast.makeText(context, "已开始下载到手机「下载」文件夹，完成后可在通知栏查看", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "下载失败，请稍后重试", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

/** Skill 技能包卡片（v1.0.12：取消横屏滑动，改为竖排全宽卡片；取消 icon 图标功能）：
 *  顶部预览区（视频/图片缩略图或渐变占位）+ 标题与作者 + 底部操作按钮
 *  （严格遵循控制台上传形式：文件形式→「下载」到本地，URL 形式→「跳转」，仅这两个按钮） */
@Composable
private fun SkillGridCard(
    res: UploadedResourceEntity,
    onClick: () -> Unit,
    showDelete: Boolean = false,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val isVideo = res.mediaUrl.isNotBlank() &&
        Regex("\\.(mp4|webm|mov|m4v)(\\?.*)?$", RegexOption.IGNORE_CASE).containsMatchIn(res.mediaUrl)
    // v1.0.12：严格遵循控制台上传形式——file 模式 url 存文件直链 →「下载」；url 模式 →「跳转」
    val isFileMode = res.mode != "url"
    val fileLink = res.fileUrl.ifBlank { res.url }.ifBlank {
        // 兼容旧数据：控制台早期把 zip/md 技能包直链放在 mediaUrl 字段
        if (res.mediaUrl.endsWith(".zip", true) || res.mediaUrl.endsWith(".md", true)) res.mediaUrl else ""
    }
    val jumpUrl = if (isFileMode) "" else res.url.ifBlank { res.fileUrl }
    val canDownload = isFileMode && fileLink.isNotBlank()
    val canJump = !isFileMode && jumpUrl.isNotBlank()
    val isZip = fileLink.endsWith(".zip", ignoreCase = true)
    val isMd = fileLink.endsWith(".md", ignoreCase = true)

    fun downloadSkill(url: String) {
        try {
            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val ext = if (isZip) ".zip" else if (isMd) ".md" else ""
            val safeName = (res.title + ext).replace(" ", "_").replace(Regex("[\\\\/:*?\"<>|]"), "_")
            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle("懒得找了 · ${res.title}")
                .setDescription("正在下载技能包 $safeName")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedOverMetered(true)
                .setMimeType(if (isZip) "application/zip" else if (isMd) "text/markdown" else "application/octet-stream")
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, safeName)
            dm.enqueue(request)
            Toast.makeText(context, "技能包已开始下载到手机「下载」文件夹", Toast.LENGTH_LONG).show()
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

    fun jumpSkill(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "无法打开跳转链接", Toast.LENGTH_SHORT).show()
        }
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // 预览区：有视频/预览图则展示缩略图，否则渐变占位（取消 icon 图标功能）
            if (res.previewUrl.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    coil.compose.AsyncImage(
                        model = res.previewUrl,
                        contentDescription = res.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    if (isVideo) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.55f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "视频",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.18f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (isVideo) "🎬" else if (isZip) "📦" else if (isMd) "📄" else "🧠", fontSize = 22.sp)
                }
            }
            // 标题 + 作者（取消 icon 图标功能，仅展示控制台上传的标题与作者信息）
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                Text(
                    text = res.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = when {
                            isVideo -> "视频"
                            isZip -> "ZIP"
                            isMd -> "MD"
                            else -> "Skill"
                        },
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (res.author.isNotBlank()) {
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = res.author,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            // 底部操作按钮：严格遵循控制台上传形式——仅「下载」（文件形式）/「跳转」（URL 形式）两个按钮
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
            ) {
                if (canDownload) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF22C55E).copy(alpha = 0.12f))
                            .clickable { downloadSkill(fileLink) }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Download,
                                contentDescription = null,
                                tint = Color(0xFF22C55E),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "下载",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF22C55E)
                            )
                        }
                    }
                }
                if (canJump) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                            .clickable { jumpSkill(jumpUrl) }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.OpenInNew,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "跳转",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                if (showDelete) {
                    Text(
                        text = "删除",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.clickable { onDelete() }.padding(4.dp)
                    )
                }
            }
        }
    }
}

/** 软件横排小卡片（v1.8.7：图标在左、信息在右的横排布局，不再竖排堆叠） */
@Composable
private fun SoftwareGridCard(
    res: UploadedResourceEntity,
    showDelete: Boolean = false,
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
        // v1.8.7：横排呈现——图标在左、标题/描述/按钮在右，卡片更短更紧凑
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp)
        ) {
            // 左侧：自动识别的软件 icon（云端 icon 优先，回退 favicon，再回退文字徽标）
            Box(
                modifier = Modifier.size(42.dp),
                contentAlignment = Alignment.Center
            ) {
                if (displayIcon.isNotBlank()) {
                    coil.compose.AsyncImage(
                        model = displayIcon,
                        contentDescription = res.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                }
                // 底层类型徽标（icon 加载失败时可见）
                Box(
                    modifier = Modifier
                        .size(42.dp)
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
            Spacer(modifier = Modifier.width(8.dp))
            // 右侧：标题 + 描述 + 按钮横排底部
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = res.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    minLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (res.desc.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = res.desc,
                        fontSize = 10.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(5.dp))
                // 自动识别按钮：apk/zip/md →「安装」；URL →「直达」（右侧对齐，横排不占高度）
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (canInstall) Color(0xFF22C55E).copy(alpha = 0.12f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (canInstall) Icons.Filled.Download else Icons.Filled.OpenInNew,
                                contentDescription = null,
                                tint = if (canInstall) Color(0xFF22C55E) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (canInstall) "安装" else "直达",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (canInstall) Color(0xFF22C55E) else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    if (showDelete) {
                        Text(
                            text = "删除",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.clickable { onDelete() }.padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 软件横屏卡片（v1.0.9 全新横屏呈现）：固定宽度横向滑动，图标在上、信息在下，
 * 主题渐变描边 + 主题色角标，适合一排排横向滑动浏览（LazyRow）。
 */
@Composable
private fun SoftwareHorizontalCard(
    res: UploadedResourceEntity,
    showDelete: Boolean = false,
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

    // 自动识别 icon（优先云端 iconUrl，其次 Google favicon 服务）
    val displayIcon = res.iconUrl.ifBlank { autoFaviconUrl(res.url.ifBlank { res.fileUrl }) }
    // v1.0.9 主题：卡片描边用主题主色渐变（清爽浅红系）
    val themePrimary = MaterialTheme.colorScheme.primary
    val themeSecondary = MaterialTheme.colorScheme.secondary

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

    // 横屏卡片：固定宽 176dp，主题渐变描边，适合横向滑动
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        border = BorderStroke(
            1.dp,
            Brush.linearGradient(listOf(themePrimary.copy(alpha = 0.7f), themeSecondary.copy(alpha = 0.4f)))
        ),
        modifier = Modifier
            .width(176.dp)
            .clickable { onCardClick() }
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // 顶部：icon + 角标
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (displayIcon.isNotBlank()) {
                        coil.compose.AsyncImage(
                            model = displayIcon,
                            contentDescription = res.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                    }
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
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(themePrimary.copy(alpha = 0.18f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (canInstall) "安装" else "直达",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = themePrimary
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            // 标题
            Text(
                text = res.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                minLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (res.desc.isNotBlank()) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = res.desc,
                    fontSize = 10.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (showDelete) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "删除",
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.clickable { onDelete() }.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun ResourceFileCard(
    res: UploadedResourceEntity,
    resourceType: String = "software",
    showDelete: Boolean = false,
    onClickOverride: (() -> Unit)? = null,
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

    // v1.9.1：Skill 技能库遵循「zip/md 文件 → 可下载到本地、URL → 直接跳转」：
    // 文件类（apk/zip/md）按钮在 Skill 场景显示「下载」（下载技能包到本地，即下即用），
    // 软件场景文件类保持「安装」；URL 形式均显示「直达」（点击直接跳转）。
    val actionText = when {
        canInstall && resourceType == "skill" -> "下载"
        canInstall -> "安装"
        else -> "直达"
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
            .clickable { (onClickOverride ?: onCardClick)() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // v1.0.12：Skill 场景取消 icon 图标功能，统一使用类型文字徽标呈现
                val displayIcon = if (resourceType == "skill")
                    ""
                else
                    res.iconUrl.ifBlank { autoFaviconUrl(res.url.ifBlank { res.fileUrl }) }
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
                        text = actionText,
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
