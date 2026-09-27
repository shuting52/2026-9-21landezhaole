package com.example.ui.screens.toolbox

import android.media.MediaPlayer
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.toolbox.BirdLibrary.Bird
import kotlinx.coroutines.delay

/**
 * 百鸟鸣 · 100 种鸟类叫声科普（v1.9.3）
 * - 纯本地离线：全部音频打包在 APK 内（res/raw/bird_XXX.ogg），无需联网 / 无需任何 API
 * - 点击鸟名即播放对应叫声，再次点击暂停；任意时刻只有一个声音播放
 * - 支持按中文名 / 学名搜索；附每种的科普简介
 * - 音频为离线合成演示音（科普用途），后续可替换为真实野外录音
 */
@Composable
fun BirdCallScreenView() {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var playingId by remember { mutableStateOf<Int?>(null) }
    var positionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(0L) }
    var preparing by remember { mutableStateOf(false) }
    // 单例播放器：同时只允许一个鸟叫响起
    val player = remember { mutableStateOf<MediaPlayer?>(null) }

    fun stopCurrent() {
        player.value?.let {
            try { if (it.isPlaying) it.stop() } catch (_: Exception) {}
            try { it.release() } catch (_: Exception) {}
        }
        player.value = null
        playingId = null
        positionMs = 0L
        durationMs = 0L
    }

    fun togglePlay(bird: Bird) {
        if (playingId == bird.id) {
            player.value?.let { mp ->
                if (mp.isPlaying) { mp.pause(); playingId = null }
                else { mp.start(); positionMs = 0L; playingId = bird.id }
            }
            return
        }
        // 切换另一只鸟 → 先停旧的
        if (playingId != null) stopCurrent()
        preparing = true
        try {
            val mp = MediaPlayer.create(context, bird.rawRes) ?: throw Exception("资源不可用")
            mp.setOnCompletionListener {
                playingId = null
                positionMs = 0L
                try { mp.release() } catch (_: Exception) {}
                if (player.value === mp) player.value = null
            }
            mp.setOnErrorListener { _, _, _ ->
                preparing = false
                playingId = null
                Toast.makeText(context, "播放失败，请重试", Toast.LENGTH_SHORT).show()
                true
            }
            durationMs = mp.duration.toLong()
            mp.start()
            positionMs = 0L
            playingId = bird.id
            player.value = mp
        } catch (e: Exception) {
            Toast.makeText(context, "播放失败：${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            preparing = false
        }
    }

    // 播放进度轮询
    LaunchedEffect(playingId) {
        while (playingId != null) {
            player.value?.let { mp ->
                try {
                    if (mp.isPlaying) positionMs = mp.currentPosition.toLong()
                } catch (_: Exception) {}
            }
            delay(120)
        }
    }

    // 退出界面释放播放器
    DisposableEffect(Unit) {
        onDispose {
            try { player.value?.release() } catch (_: Exception) {}
        }
    }

    val filtered = remember(searchQuery) {
        if (searchQuery.isBlank()) BirdLibrary.birds
        else BirdLibrary.birds.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                it.sciName.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // ===== 顶部 =====
        Surface(
            color = Color.White.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.65f)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF2E7D32), Color(0xFF66BB6A)))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Pets, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🐦 百鸟鸣 · 鸟叫科普",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "100 种鸟类叫声 · 全离线播放 · 点击鸟名即听",
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
                if (playingId != null) {
                    IconButton(onClick = { stopCurrent() }) {
                        Icon(Icons.Filled.Close, contentDescription = "停止播放", tint = Color(0xFFE53935), modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ===== 播放器状态条 =====
        if (playingId != null) {
            val cur = BirdLibrary.birds.firstOrNull { it.id == playingId }
            Surface(
                color = Color(0xFFE8F5E9),
                border = BorderStroke(1.dp, Color(0xFF2E7D32).copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "正在播放：${cur?.name ?: ""}（${cur?.sciName ?: ""}）",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f },
                            color = Color(0xFF2E7D32),
                            trackColor = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${positionMs / 1000}s",
                        fontSize = 11.sp,
                        color = Color(0xFF2E7D32).copy(alpha = 0.8f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // ===== 搜索框 =====
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("🔍 搜索鸟名 / 学名 / 分类…", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (searchQuery.isBlank()) "共 ${BirdLibrary.birds.size} 种 · 点击右侧按钮试听"
            else "搜索结果：${filtered.size} 种",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(6.dp))

        // ===== 列表 =====
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filtered, key = { it.id }) { bird ->
                BirdRow(
                    bird = bird,
                    isPlaying = playingId == bird.id,
                    isLoading = preparing && playingId == null,
                    onToggle = { togglePlay(bird) }
                )
            }
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "ℹ️ 音频为离线合成演示音（科普用途），全部存储在应用本地，不联网、不调用任何 API。",
                        fontSize = 10.5.sp,
                        lineHeight = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BirdRow(
    bird: Bird,
    isPlaying: Boolean,
    isLoading: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        onClick = onToggle,
        color = if (isPlaying) Color(0xFFE8F5E9) else Color.White.copy(alpha = 0.5f),
        border = BorderStroke(
            1.dp,
            if (isPlaying) Color(0xFF2E7D32).copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 序号圆标
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        if (isPlaying) Color(0xFF2E7D32)
                        else Brush.linearGradient(listOf(Color(0xFF66BB6A), Color(0xFF2E7D32)))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${bird.id}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = bird.name,
                        fontSize = 14.sp,
                        fontWeight = if (isPlaying) FontWeight.Black else FontWeight.Bold,
                        color = if (isPlaying) Color(0xFF1B5E20) else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(5.dp)
                    ) {
                        Text(
                            text = bird.category,
                            fontSize = 9.5.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }
                Text(
                    text = bird.sciName,
                    fontSize = 10.5.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = bird.desc,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            // 播放 / 暂停按钮
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        if (isPlaying) Color(0xFF2E7D32)
                        else Brush.linearGradient(listOf(Color(0xFF66BB6A), Color(0xFF43A047)))
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading && !isPlaying) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "暂停" else "播放",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}