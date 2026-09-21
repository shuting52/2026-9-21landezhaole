package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayCircle
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.local.DefaultPromptData
import com.example.data.model.PromptItem
import com.example.data.model.PromptMediaType
import com.example.ui.theme.FlameRed
import com.example.ui.theme.JadeGreen
import com.example.ui.theme.SunsetOrange

@Composable
fun PromptHubSubView(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prompts = remember { DefaultPromptData.initialPrompts }
    var selectedMediaType by remember { mutableStateOf<PromptMediaType?>(null) }
    var previewingPrompt by remember { mutableStateOf<PromptItem?>(null) }

    val filteredList = remember(selectedMediaType) {
        if (selectedMediaType == null) prompts else prompts.filter { it.mediaType == selectedMediaType }
    }

    Column(modifier = modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
        // Filter Chips with translucent glassmorphic style to blend with global background
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedMediaType == null,
                onClick = { selectedMediaType = null },
                label = { Text("全部提示词 (${prompts.size})", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.White.copy(alpha = 0.45f),
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
                ),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.65f))
            )
            FilterChip(
                selected = selectedMediaType == PromptMediaType.IMAGE,
                onClick = { selectedMediaType = PromptMediaType.IMAGE },
                label = { Text("生图 Prompt", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Filled.Palette, contentDescription = null, modifier = Modifier.size(14.dp)) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.White.copy(alpha = 0.45f),
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
                ),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.65f))
            )
            FilterChip(
                selected = selectedMediaType == PromptMediaType.VIDEO,
                onClick = { selectedMediaType = PromptMediaType.VIDEO },
                label = { Text("视频 Prompt", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Filled.Movie, contentDescription = null, modifier = Modifier.size(14.dp)) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.White.copy(alpha = 0.45f),
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
                ),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.65f))
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            items(filteredList, key = { it.id }) { prompt ->
                PromptCard(
                    prompt = prompt,
                    onImageClick = { previewingPrompt = prompt },
                    onCopy = { text ->
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("Prompt", text))
                        Toast.makeText(context, "已复制提示词！可以直接在Midjourney/FLUX/Sora中使用", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    // High-Resolution Preview Dialog
    previewingPrompt?.let { prompt ->
        PromptImagePreviewDialog(
            prompt = prompt,
            onDismiss = { previewingPrompt = null },
            onCopy = { text ->
                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(ClipData.newPlainText("Prompt", text))
                Toast.makeText(context, "已复制提示词！", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun PromptCard(
    prompt: PromptItem,
    onImageClick: () -> Unit,
    onCopy: (String) -> Unit
) {
    // Glassmorphic translucent card blending smoothly into the software's animated background
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("prompt_card_${prompt.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.58f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.72f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            // 🌟 1. Preview Image with seamless background gradient integration
            val previewRes = prompt.previewDrawableRes ?: R.drawable.img_preview_cyberpunk
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                    .clickable { onImageClick() }
            ) {
                // The actual preview image
                Image(
                    painter = painterResource(id = previewRes),
                    contentDescription = prompt.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Bottom gradient mask blending seamlessly into the card container background
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

                // Top row tags over preview image
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category & Media Type Pill
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.55f),
                        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.35f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (prompt.mediaType == PromptMediaType.VIDEO) FlameRed else JadeGreen)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (prompt.mediaType == PromptMediaType.VIDEO) "🎬 视频生成" else "🎨 图像生成",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Target AI Model Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.55f),
                        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = prompt.targetModel,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD54F),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                // Bottom row over preview image: Aspect ratio / Duration + Zoom hint
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.Black.copy(alpha = 0.55f)
                        ) {
                            Text(
                                text = prompt.aspectRatio,
                                fontSize = 10.sp,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        if (prompt.videoDuration != null) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = FlameRed.copy(alpha = 0.85f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Filled.PlayCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(11.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(text = prompt.videoDuration, fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Click to Zoom Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.clickable { onImageClick() }
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

            // 🌟 2. Content Section
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = prompt.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (prompt.chineseDesc.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = prompt.chineseDesc,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Prompt Text box with translucent styling
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.50f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.60f))
                ) {
                    Text(
                        text = prompt.promptText,
                        fontSize = 11.5.sp,
                        lineHeight = 17.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom parameters and Copy Action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.White.copy(alpha = 0.60f),
                            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.80f))
                        ) {
                            Text(
                                text = "# ${prompt.category}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
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
                        onClick = { onCopy(prompt.promptText) },
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
private fun PromptImagePreviewDialog(
    prompt: PromptItem,
    onDismiss: () -> Unit,
    onCopy: (String) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        // Semi-transparent acrylic backdrop blending with software's background
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
                    .clickable(enabled = false) {}, // prevent dismiss on card click
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E24).copy(alpha = 0.95f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.20f))
            ) {
                Column {
                    // Header with title and close button
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
                                text = "${prompt.targetModel} · ${prompt.category} · ${prompt.aspectRatio}",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.65f)
                            )
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Filled.Close, contentDescription = "关闭", tint = Color.White)
                        }
                    }

                    // High-resolution preview image
                    val previewRes = prompt.previewDrawableRes ?: R.drawable.img_preview_cyberpunk
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .background(Color.Black)
                    ) {
                        Image(
                            painter = painterResource(id = previewRes),
                            contentDescription = prompt.title,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Details and Prompt copy area
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = prompt.chineseDesc,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

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
                                    text = prompt.promptText,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp,
                                    color = Color.White.copy(alpha = 0.90f)
                                )
                                if (prompt.parameters.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "参数: ${prompt.parameters}",
                                        fontSize = 10.sp,
                                        color = Color(0xFF64B5F6)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { onCopy(prompt.promptText) },
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
