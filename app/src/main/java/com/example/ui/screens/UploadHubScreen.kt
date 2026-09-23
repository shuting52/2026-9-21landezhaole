package com.example.ui.screens

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.local.db.UploadedResourceEntity

/**
 * 资源展示页（软件 / Skill）：
 * - 内容完全由云端控制台同步，本体不再提供本地上传
 * - 控制台发布文件（zip/apk/md）后，本体实时同步展示，点击「下载文件」直接下载
 * - 控制台来源（sw_/sk_ 前缀）可删除（云端删除会同步所有用户）
 */
@Composable
fun UploadHubScreen(
    title: String,
    subtitle: String,
    resourceType: String,
    resources: List<UploadedResourceEntity>,
    onDelete: (id: String) -> Unit,
    modifier: Modifier = Modifier,
    showDelete: Boolean = true
) {
    val context = LocalContext.current

    Box(modifier = modifier.fillMaxSize()) {
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

@Composable
private fun ResourceFileCard(
    res: UploadedResourceEntity,
    showDelete: Boolean = true,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val url = res.url.ifBlank { res.fileUrl }
    val isFile = url.contains("/dist/uploads/") || url.contains("/dist/apk/") || res.mode != "url"
    val isApk = url.endsWith(".apk", ignoreCase = true)
    val isZip = url.endsWith(".zip", ignoreCase = true)
    val isMd = url.endsWith(".md", ignoreCase = true)

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

    /** 使用系统下载管理器下载文件到手机「下载」目录（通知栏可见进度） */
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
            // 系统下载器不可用时退回浏览器下载
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (e2: Exception) {
                Toast.makeText(context, "下载失败，请稍后重试", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 点击卡片：文件（zip/apk/md）直接下载到本地 / URL 直接跳转
    val onCardClick = {
        if (url.isNotBlank()) {
            if (isFile) {
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
                // 文件类型徽标 / 软件图标
                if (res.iconUrl.isNotBlank()) {
                    AsyncImageCompat(
                        url = res.iconUrl,
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

            // 可视化预览：上传的预览图 / 演示视频在本体直接展示（不再以链接文字形式）
            if (res.previewUrl.isNotBlank() || res.mediaUrl.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                if (res.mediaUrl.isNotBlank()) {
                    // 视频预览（静音循环自动播放，点击可全屏/暂停；失败回退预览图避免黑屏）
                    var videoFailed by remember { mutableStateOf(false) }
                    if (!videoFailed) {
                        AndroidView(
                            factory = { ctx ->
                                android.widget.VideoView(ctx).apply {
                                    setVideoURI(Uri.parse(res.mediaUrl))
                                    setOnPreparedListener { mp ->
                                        mp.isLooping = true
                                        mp.setVolume(0f, 0f)
                                        mp.start()
                                    }
                                    setOnErrorListener { mp, what, extra ->
                                        videoFailed = true
                                        true
                                    }
                                    setOnClickListener {
                                        if (isPlaying) pause() else start()
                                    }
                                    layoutParams = android.view.ViewGroup.LayoutParams(
                                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                    } else if (res.previewUrl.isNotBlank()) {
                        coil.compose.AsyncImage(
                            model = res.previewUrl,
                            contentDescription = res.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(70.dp)
                                .background(Color(0xFF1E1E24))
                                .clip(RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "视频暂不支持内嵌预览，点击卡片直接下载查看",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.75f),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                } else {
                    // 预览图可视化
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

            // 作者 / 标签行 + 下载/跳转提示
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
                // 直接点击卡片即可下载/跳转的提示
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isFile) Color(0xFF22C55E).copy(alpha = 0.12f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = if (isFile) Icons.Filled.Download else Icons.Filled.OpenInNew,
                        contentDescription = null,
                        tint = if (isFile) Color(0xFF22C55E) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isFile) "点击卡片直接下载" else "点击卡片直接打开",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isFile) Color(0xFF22C55E) else MaterialTheme.colorScheme.primary
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
    // 底层放类型徽标回退，上层放云端图标（加载成功覆盖，失败不绘制则回退可见）
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
