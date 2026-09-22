package com.example.ui.uiverse

import android.widget.Toast
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
    onApplyItemAsComponent: (UiverseItem) -> Unit
) {
    if (!isOpen) return

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Category selection: matching the exact 12 from screenshot plus Custom Code
    var selectedCategory by remember { mutableStateOf(UiverseCategory.UI_KITS) }
    var isCustomCodeTabActive by remember { mutableStateOf(false) }

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

                // Top Category Navigation (Horizontal scrollable pill list mirroring the screenshot)
                UiverseCategoryBar(
                    selectedCategory = selectedCategory,
                    isCustomCodeActive = isCustomCodeTabActive,
                    onSelectCategory = {
                        selectedCategory = it
                        isCustomCodeTabActive = false
                    },
                    onSelectCustomCode = {
                        isCustomCodeTabActive = true
                    }
                )

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), thickness = 1.dp)

                // Body content
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    // 主题切换：只保留「自定义代码应用」版块，识别每个 UI 组件单独改动、支持全局应用
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
                    text = "内置款式1~5与全套UI主题一键应用 · 自动适配全屏背景",
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
                        text = "自定义代码驱动软件UI (CSS + HTML)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "支持直接粘贴或编写标准 CSS+HTML 代码，系统将智能提取背景渐变、描边圆角、发光阴影与模糊特效，一键应用到整个软件的所有界面与UI元素中！",
                    color = Color(0xFFF1F5F9),
                    fontSize = 11.sp
                )
            }
        }

        // Quick Preset Templates
        Text(
            text = "⚡ 快速载入预设代码模板：",
            color = Color(0xFF94A3B8),
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PresetPill("赛博发光") { onCssChange(UiverseCssEngine.templateNeonCard) }
            PresetPill("极光玻璃") { onCssChange(UiverseCssEngine.templateGlassAurora) }
            PresetPill("新野蛮主义") { onCssChange(UiverseCssEngine.templateNeoBrutalism) }
            PresetPill("新拟物浮雕") { onCssChange(UiverseCssEngine.templateNeumorphicClay) }
            PresetPill("全息棱镜") { onCssChange(UiverseCssEngine.templateHolographic) }
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
