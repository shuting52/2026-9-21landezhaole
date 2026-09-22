package com.example.ui.uiverse

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun UiverseDialog(
    isOpen: Boolean,
    onClose: () -> Unit,
    activeState: ActiveUiverseState,
    onApplyKit: (UiKitPreset) -> Unit,
    onApplyCustomCss: (css: String, html: String) -> Unit,
    onResetDefault: () -> Unit,
    onApplyItemAsComponent: (UiverseItem) -> Unit,
    onApplyComponentTheme: (compId: String, css: String) -> Unit = { _, _ -> },
    componentThemes: Map<String, String> = emptyMap()
) {
    if (!isOpen) return

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // 主题切换仅保留「自定义代码应用」，原英文分类入口已移除

    // Custom CSS / HTML inputs
    var customCssText by remember {
        mutableStateOf(
            activeState.customCssInput.ifBlank { UiverseCssEngine.templateNeonCard }
        )
    }
    var customHtmlText by remember {
        mutableStateOf(
            activeState.customHtmlInput.ifBlank { "<div class=\"uiverse-card\"><h3>Custom UI Card</h3></div>" }
        )
    }

    // Code view modal
    var viewingCodeItem by remember { mutableStateOf<UiverseItem?>(null) }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.94f)
                .clip(RoundedCornerShape(24.dp))
                .shadow(24.dp, RoundedCornerShape(24.dp))
                .testTag("uiverse_dialog"),
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar
                UiverseHeader(
                    activeKit = activeState.activeKit,
                    onResetDefault = {
                        onResetDefault()
                        Toast.makeText(context, "已恢复系统默认主题", Toast.LENGTH_SHORT).show()
                        onClose()
                    },
                    onClose = onClose,
                    onOpenWebsite = {
                        // 支持直接访问 uiverse.io 官网挑选主题
                        try {
                            val intent = android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse("https://uiverse.io/")
                            )
                            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "无法打开 uiverse.io，请手动在浏览器访问", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                // 主题切换：仅保留「自定义代码应用」，不再展示英文分类入口（UI Kits/Buttons/Cards 等）
                // 顶部简单提示条
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F121C))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Code,
                        contentDescription = null,
                        tint = Color(0xFF8B5CF6),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "自定义代码应用（识别软件全部 UI 组件，全局生效）",
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), thickness = 1.dp)

                // 主题子页切换：全局自定义代码 / 组件级定制 / 背景媒体（图片·视频·歌手海报）
                var themeSubTab by remember { mutableIntStateOf(0) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F121C))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (themeSubTab == 0) Color(0xFF6366F1) else Color(0xFF1E2333))
                            .clickable { themeSubTab = 0 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("全局代码", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (themeSubTab == 1) Color(0xFF8B5CF6) else Color(0xFF1E2333))
                            .clickable { themeSubTab = 1 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("组件定制", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (themeSubTab == 2) Color(0xFFEC4899) else Color(0xFF1E2333))
                            .clickable { themeSubTab = 2 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("背景媒体", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Body content
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    when (themeSubTab) {
                        0 -> {
                            // 全局自定义代码应用
                            CustomCodeEditorSection(
                                cssText = customCssText,
                                htmlText = customHtmlText,
                                onCssChange = { customCssText = it },
                                onHtmlChange = { customHtmlText = it },
                                onApply = {
                                    onApplyCustomCss(customCssText, customHtmlText)
                                    Toast.makeText(context, "已成功解析并应用自定义主题代码到软件全部 UI！", Toast.LENGTH_SHORT).show()
                                    onClose()
                                },
                                onReset = {
                                    onResetDefault()
                                    onClose()
                                }
                            )
                        }
                        1 -> {
                            // 组件级定制：识别软件每个 UI 组件，独立代码输入
                            ComponentThemeSection(
                                componentThemes = componentThemes,
                                onApplyComponentCss = { compId, css ->
                                    onApplyComponentTheme(compId, css)
                                    Toast.makeText(context, "已应用组件定制样式！", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                        else -> {
                            // 背景媒体：图片/视频 + 内置歌手海报
                            BackgroundMediaSection(
                                onPickImage = {
                                    Toast.makeText(context, "请在控制台-设置-背景媒体中上传图片/视频", Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    }
                }
            }
        }

        // View Code Sheet / Modal
        viewingCodeItem?.let { item ->
            CodeViewerDialog(
                item = item,
                onDismiss = { viewingCodeItem = null },
                onApply = {
                    onApplyItemAsComponent(item)
                    viewingCodeItem = null
                    Toast.makeText(context, "已成功将该组件 UI 样式应用到软件！", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
private fun UiverseHeader(
    activeKit: UiKitPreset,
    onResetDefault: () -> Unit,
    onClose: () -> Unit,
    onOpenWebsite: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF6366F1), Color(0xFFEC4899))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Palette,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "主题切换",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF22C55E).copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "当前: ${activeKit.displayName.substringBefore(" ")}",
                            color = Color(0xFF4ADE80),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = "自定义代码应用 · 识别软件全部 UI 组件并全局生效",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.5.sp
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // 访问 uiverse.io 官网：支持直接访问并挑选主题
            OutlinedButton(
                onClick = onOpenWebsite,
                modifier = Modifier.height(32.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Language,
                    contentDescription = null,
                    tint = Color(0xFF6366F1),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("uiverse.io", fontSize = 11.sp, color = Color(0xFF6366F1))
            }
            Spacer(modifier = Modifier.width(6.dp))
            OutlinedButton(
                onClick = onResetDefault,
                modifier = Modifier.height(32.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("恢复默认", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(6.dp))
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun UiverseCategoryBar(
    selectedCategory: UiverseCategory,
    isCustomCodeActive: Boolean,
    onSelectCategory: (UiverseCategory) -> Unit,
    onSelectCustomCode: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F121C))
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Categories list
        UiverseCategory.values().forEach { category ->
            val isSelected = !isCustomCodeActive && selectedCategory == category
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSelected) Color(0xFF6366F1)
                        else Color(0xFF1E2333)
                    )
                    .clickable { onSelectCategory(category) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = category.displayName,
                        color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                    if (category.hasNewBadge) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFEF4444))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "New",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }

        // Custom Code Tab button
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (isCustomCodeActive) Brush.horizontalGradient(listOf(Color(0xFFEC4899), Color(0xFF8B5CF6)))
                    else Brush.horizontalGradient(listOf(Color(0x33EC4899), Color(0x338B5CF6)))
                )
                .clickable { onSelectCustomCode() }
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Code,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "</> 自定义代码应用",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun UiKitsSection(
    activeKit: UiKitPreset,
    onSelectKit: (UiKitPreset) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF1E1B4B), Color(0xFF311042))
                        )
                    )
                    .padding(14.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Palette,
                            contentDescription = null,
                            tint = Color(0xFFF43F5E),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "UI Kits · 全套设计系统一键应用",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "选择任意 UI Kit 将直接彻底改造全软件卡片、按钮、输入框、加载动画、背景纹理与色彩，不再仅局限于切换背景颜色！",
                        color = Color(0xFFCBD5E1),
                        fontSize = 11.sp
                    )
                }
            }
        }

        items(UiverseRepository.uiKits) { kit ->
            val isActive = activeKit == kit
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onSelectKit(kit) }
                    .border(
                        if (isActive) 2.5.dp else 1.dp,
                        if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        RoundedCornerShape(16.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = kit.displayName,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                if (isActive) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFF22C55E))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("已应用", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Text(
                                text = "by @${kit.author} · ${kit.desc}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Button(
                            onClick = { onSelectKit(kit) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isActive) Color(0xFF22C55E) else MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(if (isActive) "当前使用中" else "一键应用到全套UI", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Component preview bar inside card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(86.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(kit.backgroundColor)
                            .border(1.dp, kit.primaryColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        UiKitPreview(kit)
                    }
                }
            }
        }
    }
}

@Composable
private fun ComponentGallerySection(
    category: UiverseCategory,
    onApply: (UiverseItem) -> Unit,
    onViewCode: (UiverseItem) -> Unit
) {
    val items = remember(category) { UiverseRepository.getItemsByCategory(category) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(items, key = { it.id }) { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131722))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    // Preview
                    UiverseItemPreview(item = item)

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = item.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "by @${item.author}",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onViewCode(item) },
                            modifier = Modifier
                                .weight(1f)
                                .height(30.dp),
                            contentPadding = PaddingValues(0.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("代码", fontSize = 10.5.sp, color = Color(0xFFCBD5E1))
                        }

                        Button(
                            onClick = { onApply(item) },
                            modifier = Modifier
                                .weight(1f)
                                .height(30.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                            contentPadding = PaddingValues(0.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("应用UI", fontSize = 10.5.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomCodeEditorSection(
    cssText: String,
    htmlText: String,
    onCssChange: (String) -> Unit,
    onHtmlChange: (String) -> Unit,
    onApply: () -> Unit,
    onReset: () -> Unit
) {
    var activeCodeTab by remember { mutableStateOf("CSS") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Info Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF831843), Color(0xFF4C1D95))
                    )
                )
                .padding(12.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "自定义代码应用",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "支持直接粘贴或编写标准 CSS+HTML 代码，自动应用到软件全局 UI。",
                    color = Color(0xFFF1F5F9),
                    fontSize = 11.sp
                )
            }
        }

        // Live Preview Box of the current CSS
        val parsedStyle = remember(cssText, htmlText) {
            UiverseCssEngine.parseCss(cssText, htmlText)
        }
        Text(
            text = "👀 实时解析预览效果：",
            color = Color(0xFF94A3B8),
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF07090E))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(55.dp)
                    .clip(RoundedCornerShape(parsedStyle.cornerRadius))
                    .then(
                        if (parsedStyle.backgroundBrush != null) {
                            Modifier.background(parsedStyle.backgroundBrush)
                        } else {
                            Modifier.background(parsedStyle.backgroundColor ?: Color(0xFF1E293B))
                        }
                    )
                    .then(
                        if (parsedStyle.borderWidth > 0.dp) {
                            Modifier.border(parsedStyle.borderWidth, parsedStyle.borderColor, RoundedCornerShape(parsedStyle.cornerRadius))
                        } else Modifier
                    )
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(parsedStyle.textColor ?: Color.White)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "UI 动态解析效果 · 应用全软件",
                        color = parsedStyle.textColor ?: Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Code Tabs (CSS / HTML)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1E2333))
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (activeCodeTab == "CSS") Color(0xFF6366F1) else Color.Transparent)
                    .clickable { activeCodeTab = "CSS" }
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("CSS 样式代码", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (activeCodeTab == "HTML") Color(0xFF6366F1) else Color.Transparent)
                    .clickable { activeCodeTab = "HTML" }
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("HTML 结构代码", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (activeCodeTab == "CSS") {
            OutlinedTextField(
                value = cssText,
                onValueChange = onCssChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF38BDF8),
                    unfocusedTextColor = Color(0xFF38BDF8),
                    focusedContainerColor = Color(0xFF090B10),
                    unfocusedContainerColor = Color(0xFF090B10),
                    focusedBorderColor = Color(0xFF6366F1),
                    unfocusedBorderColor = Color(0x33FFFFFF)
                ),
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            )
        } else {
            OutlinedTextField(
                value = htmlText,
                onValueChange = onHtmlChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF4ADE80),
                    unfocusedTextColor = Color(0xFF4ADE80),
                    focusedContainerColor = Color(0xFF090B10),
                    unfocusedContainerColor = Color(0xFF090B10),
                    focusedBorderColor = Color(0xFF6366F1),
                    unfocusedBorderColor = Color(0x33FFFFFF)
                ),
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            )
        }

        // Action Buttons
        Button(
            onClick = onApply,
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6366F1)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(6.dp))
            Text("🚀 智能解析并直接应用到软件全部UI", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
        }

        OutlinedButton(
            onClick = onReset,
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("还原为默认经典皮肤", color = Color(0xFF94A3B8), fontSize = 12.sp)
        }
    }
}

@Composable
private fun PresetPill(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF1E2333))
            .border(1.dp, Color(0x44FFFFFF), RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = label, color = Color(0xFFCBD5E1), fontSize = 11.sp)
    }
}

@Composable
private fun CodeViewerDialog(
    item: UiverseItem,
    onDismiss: () -> Unit,
    onApply: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf("CSS") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(20.dp)),
            color = Color(0xFF0F121C)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = item.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "by @${item.author} · ${item.category.displayName}",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = null, tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1A202C))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (activeTab == "CSS") Color(0xFF6366F1) else Color.Transparent)
                            .clickable { activeTab = "CSS" }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("CSS", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (activeTab == "HTML") Color(0xFF6366F1) else Color.Transparent)
                            .clickable { activeTab = "HTML" }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("HTML", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                SelectionContainer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF07090E))
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = if (activeTab == "CSS") item.cssCode else item.htmlCode,
                        color = if (activeTab == "CSS") Color(0xFF38BDF8) else Color(0xFF4ADE80),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val code = if (activeTab == "CSS") item.cssCode else item.htmlCode
                            clipboardManager.setText(AnnotatedString(code))
                            Toast.makeText(context, "代码已复制到剪贴板！", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.ContentCopy, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("复制代码", color = Color.White, fontSize = 12.sp)
                    }

                    Button(
                        onClick = onApply,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("应用此样式到软件", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}


// ==========================================
// 组件级定制：自动识别软件全部 UI 组件，独立代码输入精准修改单个组件
// ==========================================
private data class UiComponentInfo(
    val id: String,
    val name: String,
    val desc: String,
    val defaultCss: String
)

private val UI_COMPONENTS = listOf(
    UiComponentInfo("home_header", "首页头部", "品牌区 / 统计信息 / 操作按钮栏", "background: rgba(255,255,255,0.60);\nborder-radius: 14px;"),
    UiComponentInfo("bottom_nav", "底部导航栏", "五个主 Tab 切换栏", "background: rgba(15,18,28,0.92);\nborder-radius: 18px 18px 0 0;"),
    UiComponentInfo("search_box", "搜索框", "首页搜索输入区", "border-radius: 20px;\nbackground: rgba(255,255,255,0.80);"),
    UiComponentInfo("card_item", "站点卡片", "首页导航资源卡片", "background: rgba(255,255,255,0.70);\nborder-radius: 12px;"),
    UiComponentInfo("prompt_card", "提示词卡片", "提示词区图片/视频卡片", "background: rgba(255,255,255,0.60);\nborder-radius: 18px;"),
    UiComponentInfo("software_card", "软件/Skill卡片", "软件库与技能库资源卡", "background: rgba(255,255,255,0.60);\nborder-radius: 14px;"),
    UiComponentInfo("toolbox_card", "工具箱卡片", "小工具网格卡片", "background: rgba(255,255,255,0.70);\nborder-radius: 12px;"),
    UiComponentInfo("dialog", "弹窗/对话框", "更新弹窗 / 详情弹窗等", "border-radius: 20px;\nbackground: #14142a;"),
    UiComponentInfo("button", "按钮", "主要操作按钮", "border-radius: 10px;\nbackground: linear-gradient(135deg,#6c63ff,#ff2d78);"),
    UiComponentInfo("text_field", "输入框", "搜索/反馈等输入组件", "border-radius: 12px;\nbackground: rgba(255,255,255,0.70);")
)

@Composable
private fun ComponentThemeSection(
    componentThemes: Map<String, String>,
    onApplyComponentCss: (compId: String, css: String) -> Unit
) {
    var editingComp by remember { mutableStateOf<UiComponentInfo?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "UI 组件自动识别 · 独立定制",
            color = Color(0xFF94A3B8),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "已自动识别软件全部 UI 组件，点击任意组件即可独立输入代码，精准修改该布局组件样式（不影响其他组件）。",
            color = Color(0xFF64748B),
            fontSize = 11.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(UI_COMPONENTS.size) { i ->
                val comp = UI_COMPONENTS[i]
                val applied = componentThemes[comp.id]
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { editingComp = comp },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (applied != null) Color(0xFF1E1B4B) else Color(0xFF14162A)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (applied != null) Color(0xFF8B5CF6).copy(alpha = 0.6f) else Color(0x22FFFFFF)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (applied != null) Color(0xFF8B5CF6) else Color(0xFF1E2333)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Code,
                                contentDescription = null,
                                tint = if (applied != null) Color.White else Color(0xFF8B5CF6),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(comp.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(comp.desc, color = Color(0xFF94A3B8), fontSize = 10.5.sp)
                            if (applied != null) {
                                Text("✅ 已定制", color = Color(0xFF8B5CF6), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    // 组件代码编辑弹窗
    editingComp?.let { comp ->
        var cssInput by remember { mutableStateOf(componentThemes[comp.id] ?: comp.defaultCss) }
        Dialog(
            onDismissRequest = { editingComp = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .fillMaxHeight(0.9f)
                    .clip(RoundedCornerShape(20.dp)),
                color = Color(0xFF0F121C)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("定制组件 · ${comp.name}", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                            Text(comp.desc, color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }
                        IconButton(onClick = { editingComp = null }) {
                            Icon(Icons.Filled.Close, contentDescription = "关闭", tint = Color.White)
                        }
                    }
                    androidx.compose.material3.HorizontalDivider(color = Color(0x22FFFFFF))
                    // CSS 输入区
                    OutlinedTextField(
                        value = cssInput,
                        onValueChange = { cssInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF38BDF8),
                            unfocusedTextColor = Color(0xFF38BDF8),
                            focusedContainerColor = Color(0xFF090B10),
                            unfocusedContainerColor = Color(0xFF090B10),
                            focusedBorderColor = Color(0xFF8B5CF6),
                            unfocusedBorderColor = Color(0x33FFFFFF)
                        ),
                        textStyle = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { editingComp = null },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("取消", color = Color(0xFF94A3B8))
                        }
                        Button(
                            onClick = {
                                onApplyComponentCss(comp.id, cssInput)
                                editingComp = null
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
                        ) {
                            Text("应用该组件样式", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 背景媒体：图片/视频全局背景 + 内置歌手海报
// ==========================================
@Composable
private fun BackgroundMediaSection(
    onPickImage: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("全局背景 · 图片 / 视频", color = Color(0xFF94A3B8), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(
                "在控制台-设置-背景媒体上传图片/视频后自动应用到软件全局背景；也可直接选用内置歌手海报。",
                color = Color(0xFF64748B),
                fontSize = 11.sp
            )
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF14162A)),
                border = BorderStroke(1.dp, Color(0xFFEC4899).copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Filled.Image, contentDescription = null, tint = Color(0xFFEC4899), modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("本地上传图片 / 视频作为全局背景", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("请在控制台-设置-背景媒体上传", color = Color(0xFF94A3B8), fontSize = 10.5.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onPickImage,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("去上传", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        item {
            Text("内置歌手海报 · 一键应用全局背景", color = Color(0xFF94A3B8), fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        listOf("张韶涵", "张含韵", "卓依婷", "凤凰传奇").forEach { name ->
            item {
                val gradient = when (name) {
                    "张韶涵" -> listOf(Color(0xFFFF6B9D), Color(0xFF6B5BFF))
                    "张含韵" -> listOf(Color(0xFFFFB199), Color(0xFF8E54E9))
                    "卓依婷" -> listOf(Color(0xFF43E97B), Color(0xFF38F9D7))
                    else -> listOf(Color(0xFFFFD200), Color(0xFFF7971E))
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                            .background(Brush.linearGradient(gradient)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(name, color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                            Text("歌手海报 · 控制台上传后可替换", color = Color.White.copy(alpha = 0.85f), fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}
