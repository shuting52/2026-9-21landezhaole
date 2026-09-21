package com.example.ui.screens

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.local.db.UploadedResourceEntity
import com.example.ui.components.SiteBrandIcon
import com.example.ui.theme.FlameRed
import com.example.ui.theme.JadeGreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * 软件 / Skill 版块：改为「控制台文件同步 + 直接下载」模式。
 * - 不再支持本体软件上传（控制台后台采用文件方式发布 ZIP / APK / MD）
 * - 每个资源独立紧凑卡片呈现，点击即可下载文件；APK 下载后自动拉起安装
 */
@Composable
fun UploadHubScreen(
    title: String,
    subtitle: String,
    resourceType: String,
    resources: List<UploadedResourceEntity>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var downloadingId by remember { mutableStateOf<String?>(null) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .fillMaxSize()
            .testTag("download_hub_grid")
    ) {
        item(span = { GridItemSpan(2) }) {
            Column(modifier = Modifier.padding(horizontal = 2.dp, vertical = 2.dp)) {
                Text(
                    text = title,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle + " · 内容由云端控制台实时同步，点击卡片即可下载",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
                )
            }
        }

        if (resources.isEmpty()) {
            item(span = { GridItemSpan(2) }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 36.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CloudDownload,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "暂无同步资源",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "站长在控制台发布后，本版块将自动实时同步出现新内容，无需手动操作",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(resources, key = { it.id }) { item ->
                ResourceFileCard(
                    item = item,
                    isDownloading = downloadingId == item.id,
                    progress = if (downloadingId == item.id) downloadProgress else 0f,
                    onDownload = {
                        val url = item.fileUrl
                        if (url.isBlank()) {
                            Toast.makeText(context, "暂无可下载文件", Toast.LENGTH_SHORT).show()
                            return@ResourceFileCard
                        }
                        if (item.fileType.equals("APK", ignoreCase = true)) {
                            downloadingId = item.id
                            coroutineScope.launch {
                                val ok = downloadAndInstallApk(context, url, item.title)
                                downloadingId = null
                                if (!ok) {
                                    Toast.makeText(context, "APK 下载失败，请稍后重试", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            enqueueSystemDownload(context, url, item.title, item.fileType)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ResourceFileCard(
    item: UploadedResourceEntity,
    isDownloading: Boolean,
    progress: Float,
    onDownload: () -> Unit
) {
    val fileType = item.fileType.ifBlank { detectType(item.fileUrl) }
    val typeColor = when (fileType) {
        "APK" -> FlameRed
        "ZIP" -> Color(0xFF2563EB)
        "MD" -> JadeGreen
        else -> MaterialTheme.colorScheme.primary
    }
    val typeIcon: ImageVector = when (fileType) {
        "APK" -> Icons.Filled.InstallMobile
        "ZIP" -> Icons.Filled.Inventory2
        else -> Icons.Filled.Description
    }
    val typeLabel = if (fileType.isBlank()) "文件" else fileType

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = !isDownloading) { onDownload() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // 图标 + 标题
            Row(verticalAlignment = Alignment.CenterVertically) {
                SiteBrandIcon(
                    url = item.url,
                    title = item.title,
                    iconUrl = "",
                    size = 34.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (item.author.isNotBlank()) {
                        Text(
                            text = item.author,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 文件类型角标
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = typeColor.copy(alpha = 0.12f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = typeIcon,
                        contentDescription = null,
                        tint = typeColor,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = typeLabel + " 文件",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = typeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.desc,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                minLines = 2
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (isDownloading) {
                Column {
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = typeColor,
                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = "下载中 ${(progress * 100).toInt()}%",
                        fontSize = 10.sp,
                        color = typeColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Button(
                    onClick = onDownload,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = typeColor),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CloudDownload,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (fileType == "APK") "下载安装" else "下载 ${typeLabel}",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/** 从 URL 识别文件类型 */
private fun detectType(url: String): String {
    val low = url.lowercase()
    return when {
        low.endsWith(".apk") -> "APK"
        low.endsWith(".zip") -> "ZIP"
        low.endsWith(".md") -> "MD"
        else -> ""
    }
}

private fun fileNameOf(url: String): String {
    return try {
        Uri.parse(url).lastPathSegment ?: "landezhao-download"
    } catch (e: Exception) {
        url.substringAfterLast('/').substringBefore('?')
    }
}

/**
 * APK：OkHttp 下载到应用缓存 → 拉起系统安装器（FileProvider）
 */
private suspend fun downloadAndInstallApk(
    context: Context,
    url: String,
    title: String,
    onProgress: ((Float) -> Unit)? = null
): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            val client = okhttp3.OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .followRedirects(true)
                .build()
            val request = okhttp3.Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android) LzdzDownloader/2.0")
                .build()
            client.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext false
                val body = resp.body ?: return@withContext false
                val total = body.contentLength()
                val file = File(context.cacheDir, "download/${fileNameOf(url)}")
                file.parentFile?.mkdirs()
                body.byteStream().use { input ->
                    file.outputStream().use { output ->
                        val buf = ByteArray(8192)
                        var downloaded = 0L
                        while (true) {
                            val n = input.read(buf)
                            if (n <= 0) break
                            output.write(buf, 0, n)
                            downloaded += n
                            if (total > 0) onProgress?.invoke((downloaded * 1f / total).coerceIn(0f, 1f))
                        }
                        output.flush()
                    }
                }
                if (file.length() < 1024 || file.readBytes().take(2).toByteArray()
                        .contentEquals(byteArrayOf(0x50, 0x4B)).not()
                ) {
                    return@withContext false
                }
                installApk(context, file)
                true
            }
        } catch (e: Exception) {
            false
        }
    }
}

private fun installApk(context: Context, file: File) {
    try {
        val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "自动安装被拦截，请到系统设置允许安装未知应用后重试", Toast.LENGTH_LONG).show()
    }
}

/**
 * ZIP / MD 等文件：系统 DownloadManager 下载到「下载」目录（通知栏可见进度）
 */
private fun enqueueSystemDownload(context: Context, url: String, title: String, fileType: String) {
    try {
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val name = fileNameOf(url)
        val req = DownloadManager.Request(Uri.parse(url))
            .setTitle(title)
            .setDescription("懒得找了 · ${fileType.ifBlank { "文件" }}资源下载")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
            .setMimeType(
                when (fileType) {
                    "ZIP" -> "application/zip"
                    "MD" -> "text/markdown"
                    else -> "application/octet-stream"
                }
            )
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name)
        dm.enqueue(req)
        Toast.makeText(context, "已加入系统下载队列，请查看通知栏进度", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        // 兜底：浏览器直接打开
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e2: Exception) {
            Toast.makeText(context, "下载失败：${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
