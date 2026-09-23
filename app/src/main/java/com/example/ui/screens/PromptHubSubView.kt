package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.local.db.UploadedResourceEntity
import com.example.ui.theme.FlameRed
import com.example.ui.theme.JadeGreen
import com.example.ui.theme.SunsetOrange

/**
 * 提示词区：展示控制台云端同步的提示词（图片提示词 / 视频提示词）。
 * 支持预览图展示、点击查看大图、一键复制提示词。
 */
@Composable
fun PromptHubSubView(
    prompts: List<UploadedResourceEntity>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedType by remember { mutableStateOf<String?>(null) } // null=全部, image, video
    var previewing by remember { mutableStateOf<UploadedResourceEntity?>(null) }
    // v1.7.4：全局只有一个视频正在播放（带声音）——避免多个视频同时出声
    var activeVideoId by remember { mutableStateOf<String?>(null) }
    fun stopOtherVideos(activeId: String?) {
        if (activeVideoId != activeId) activeVideoId = activeId
    }

    val imageList = prompts.filter { it.type == "prompt_image" }
    val videoList = prompts.filter { it.type == "prompt_video" }

    val filteredList = remember(selectedType, prompts) {
        when (selectedType) {
            "image" -> imageList
            "video" -> videoList
            else -> prompts
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.Top
    ) {
        // 分类筛选：全部 / 图片提示词 / 视频提示词
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedType == null,
                onClick = { selectedType = null },
                label = { Text("全部 (${prompts.size})", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.White.copy(alpha = 0.45f),
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
                ),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.65f))
            )
            FilterChip(
                selected = selectedType == "image",
                onClick = { selectedType = "image" },
                label = { Text("图片提示词 (${imageList.size})", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.White.copy(alpha = 0.45f),
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
                ),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.65f))
            )
            FilterChip(
                selected = selectedType == "video",
                onClick = { selectedType = "video" },
                label = { Text("视频提示词 (${videoList.size})", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.White.copy(alpha = 0.45f),
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
                ),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.65f))
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 50.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.Palette,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
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
        } else {
            // v1.7.3：修复列表自动置底问题——LazyColumn 用 weight 撑满剩余空间并从顶部排列
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Top,
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(filteredList, key = { it.id }) { prompt ->
                    CloudPromptCard(
                        prompt = prompt,
                        activeVideoId = activeVideoId,
                        onVideoActivate = { id -> stopOtherVideos(id) },
                        onImageClick = {
                            previewing = prompt
                            // 打开全屏预览时停掉列表中的卡片视频，避免同时出声
                            stopOtherVideos("preview_" + prompt.id)
                        },
                        onCopy = { text ->
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cm.setPrimaryClip(ClipData.newPlainText("Prompt", text))
                            Toast.makeText(context, "已复制提示词！可以直接在Midjourney/FLUX/Sora中使用", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    // 大图预览 Dialog
    previewing?.let { prompt ->
        CloudPromptPreviewDialog(
            prompt = prompt,
            onDismiss = { previewing = null },
            onCopy = { text ->
                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(ClipData.newPlainText("Prompt", text))
                Toast.makeText(context, "已复制提示词！", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun CloudPromptCard(
    prompt: UploadedResourceEntity,
    activeVideoId: String? = null,
    onVideoActivate: (String) -> Unit = {},
    onImageClick: () -> Unit,
    onCopy: (String) -> Unit
) {
    val isVideo = prompt.type == "prompt_video"
    // v1.7.4：当前卡片是否为“正在播放（带声音）”的视频
    val isActiveVideo = isVideo && activeVideoId == prompt.id
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.58f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.72f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            // 可视化预览：视频提示词优先播放演示视频（失败时回退预览图，避免黑屏），图片提示词展示预览图
            if (isVideo && prompt.mediaUrl.isNotBlank()) {
                var videoFailed by remember { mutableStateOf(false) }
                if (!videoFailed) {
                    // v1.7.4 单视频播放方案：
                    // - 仅「当前激活视频」播放声音；其他视频静音暂停，避免多个视频同时出声
                    // - 点击视频区域 → 激活该视频（其他自动暂停）
                    // - activeVideoId 变化时自动暂停/静音本视频
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                    ) {
                        androidx.compose.ui.viewinterop.AndroidView(
                            factory = { ctx ->
                                android.widget.VideoView(ctx).apply {
                                    setVideoURI(android.net.Uri.parse(prompt.mediaUrl))
                                    setOnPreparedListener { mp ->
                                        mp.isLooping = true
                                        if (isActiveVideo) {
                                            mp.setVolume(1f, 1f)
                                            mp.start()
                                        } else {
                                            mp.setVolume(0f, 0f)
                                            mp.pause()
                                        }
                                    }
                                    setOnErrorListener { mp, what, extra ->
                                        videoFailed = true
                                        true
                                    }
                                    setOnClickListener {
                                        if (isActiveVideo) {
                                            if (isPlaying) pause() else start()
                                        } else {
                                            onVideoActivate(prompt.id)
                                        }
                                    }
                                }
                            },
                            update = { view ->
                                // activeVideoId 变化时：成为激活视频则继续播放，否则暂停（VideoView 无 setVolume，直接暂停即可静音）
                                if (view.isPlaying && !isActiveVideo) {
                                    view.pause()
                                } else if (isActiveVideo) {
                                    if (!view.isPlaying) view.start()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(170.dp)
                        )
                        // 视频预览：点击即可激活播放（仅当前一个带声音）/暂停
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable {
                                    if (isActiveVideo) {
                                        onVideoActivate("")
                                    } else {
                                        onVideoActivate(prompt.id)
                                    }
                                }
                        )
                        // 声音状态角标
                        if (isActiveVideo) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.Black.copy(alpha = 0.55f),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "🔊 播放中",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.Black.copy(alpha = 0.4f),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "🔇 点击播放",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                } else if (prompt.previewUrl.isNotBlank()) {
                    // 视频加载失败：展示预览图
                    AsyncImage(
                        model = prompt.previewUrl,
                        contentDescription = prompt.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                    )
                } else {
                    // 无预览图：显示提示条
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .background(Color(0xFF1E1E24))
                            .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "视频暂不支持内嵌预览，点击卡片右上角编辑/复制提示词使用",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.75f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            } else if (prompt.previewUrl.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                        .clickable { onImageClick() }
                ) {
                    AsyncImage(
                        model = prompt.previewUrl,
                        contentDescription = prompt.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.15f),
                                        Color.Black.copy(alpha = 0.45f)
                                    )
                                )
                            )
                    )
                    // 类型角标
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.55f),
                        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isVideo) FlameRed else JadeGreen)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (isVideo) "🎬 视频提示词" else "🖼 图片提示词",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    // 查看大图
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp)
                            .clickable { onImageClick() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Filled.ZoomIn, contentDescription = "查看大图", tint = Color.Black, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(text = "预览大图", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 内容区
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = prompt.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (prompt.desc.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = prompt.desc,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (prompt.prompt.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.50f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.60f))
                    ) {
                        Text(
                            text = prompt.prompt,
                            fontSize = 11.5.sp,
                            lineHeight = 17.sp,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (prompt.author.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.White.copy(alpha = 0.60f),
                            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.80f))
                        ) {
                            Text(
                                text = "作者: ${prompt.author}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Button(
                        onClick = { onCopy(prompt.prompt) },
                        enabled = prompt.prompt.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 5.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("复制提示词", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun CloudPromptPreviewDialog(
    prompt: UploadedResourceEntity,
    onDismiss: () -> Unit,
    onCopy: (String) -> Unit
) {
    // 是否进入全屏视频模式（沉浸式播放）
    var fullscreenVideo by remember { mutableStateOf(false) }

    if (fullscreenVideo) {
        // 全屏视频播放（点击退出全屏）
        Dialog(
            onDismissRequest = { fullscreenVideo = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable { fullscreenVideo = false }
            ) {
                androidx.compose.ui.viewinterop.AndroidView(
                    factory = { ctx ->
                        android.widget.VideoView(ctx).apply {
                            setVideoURI(android.net.Uri.parse(prompt.mediaUrl))
                            setOnPreparedListener { mp ->
                                mp.isLooping = true
                                // v1.7.3：全屏视频支持声音播放
                                mp.setVolume(1f, 1f)
                                mp.start()
                            }
                            layoutParams = android.view.ViewGroup.LayoutParams(
                                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                android.view.ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                // 顶部返回按钮
                Surface(
                    onClick = { fullscreenVideo = false },
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "退出全屏",
                        tint = Color.White,
                        modifier = Modifier.padding(10.dp).size(20.dp)
                    )
                }
                Text(
                    text = "点击任意处退出全屏",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                )
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .clickable { onDismiss() }
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E24).copy(alpha = 0.95f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.20f))
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = prompt.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (prompt.type == "prompt_video") "🎬 视频提示词" else "🖼 图片提示词",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.65f)
                            )
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Filled.Close, contentDescription = "关闭", tint = Color.White)
                        }
                    }

                    // 视频提示词：优先内嵌播放演示视频 + 全屏按钮；图片提示词展示预览图
                    if (prompt.type == "prompt_video" && prompt.mediaUrl.isNotBlank()) {
                        var videoFailed by remember { mutableStateOf(false) }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                                .background(Color.Black)
                        ) {
                            if (!videoFailed) {
                                androidx.compose.ui.viewinterop.AndroidView(
                                    factory = { ctx ->
                                        android.widget.VideoView(ctx).apply {
                                            setVideoURI(android.net.Uri.parse(prompt.mediaUrl))
                                            setOnPreparedListener { mp ->
                                                mp.isLooping = true
                                                // v1.7.3：弹窗内视频支持声音播放
                                                mp.setVolume(1f, 1f)
                                                mp.start()
                                            }
                                            setOnErrorListener { mp, what, extra ->
                                                videoFailed = true
                                                true
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(240.dp)
                                )
                            } else if (prompt.previewUrl.isNotBlank()) {
                                AsyncImage(
                                    model = prompt.previewUrl,
                                    contentDescription = prompt.title,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            // 视频预览：点击视频区域即进入全屏播放（无文字标记，轻触即开）
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable { fullscreenVideo = true }
                            )
                        }
                    } else if (prompt.previewUrl.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                                .background(Color.Black)
                        ) {
                            AsyncImage(
                                model = prompt.previewUrl,
                                contentDescription = prompt.title,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Column(modifier = Modifier.padding(16.dp)) {
                        if (prompt.desc.isNotBlank()) {
                            Text(
                                text = prompt.desc,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.85f),
                                lineHeight = 17.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        if (prompt.prompt.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.08f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "AI PROMPT (可直接粘贴到生图/视频模型):",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFD54F)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = prompt.prompt,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp,
                                        color = Color.White.copy(alpha = 0.90f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { onCopy(prompt.prompt) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("一键复制提示词")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
