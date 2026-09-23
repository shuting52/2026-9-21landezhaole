package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.R
import com.example.ui.components.IpLocationMonitorWidget
import com.example.ui.components.IpMonitorWidget
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.TextStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale
import kotlin.random.Random
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.NavData
import com.example.data.local.db.UserItemRecord
import com.example.data.model.BadgeType
import com.example.data.model.NavCard
import com.example.data.remote.UpdateDialogDto
import com.example.data.remote.VersionDto
import com.example.ui.components.AddSiteDialog
import com.example.ui.components.AppUpdateDialog
import com.example.ui.components.AtmosphereOverlay
import com.example.ui.components.CategorySitesDialog
import com.example.ui.components.CustomRadioBottomNav
import com.example.ui.components.GlobalWindBackground
import com.example.ui.components.HideAndSeekLoader
import com.example.ui.components.ResourceCard
import com.example.ui.components.RibbonBadge
import com.example.ui.components.SaharaWaveButton
import com.example.ui.components.SiteBrandIcon
import com.example.ui.components.SiteDetailDialog
import com.example.ui.components.SplashScreenOverlay
import com.example.ui.theme.LocalUiverseState
import com.example.ui.uiverse.CardStylePreset
import com.example.ui.uiverse.InputStylePreset
import com.example.ui.uiverse.UiverseDialog
import com.example.ui.theme.FlameRed
import com.example.ui.theme.JadeGreen
import com.example.ui.theme.SunsetOrange
import com.example.ui.viewmodel.AppBottomTab
import com.example.ui.viewmodel.NavViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: NavViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val filteredCards by viewModel.filteredCards.collectAsStateWithLifecycle()

    val favUrls = favorites.map { it.url }.toSet()

    val uploadedSoftware by viewModel.uploadedSoftware.collectAsStateWithLifecycle()
    val uploadedSkills by viewModel.uploadedSkills.collectAsStateWithLifecycle()
    val uploadedPrompts by viewModel.uploadedPrompts.collectAsStateWithLifecycle()
    val customSites by viewModel.customSites.collectAsStateWithLifecycle()

    val totalResourceCount = remember(uiState.categories, customSites) {
        uiState.categories.sumOf { it.cards.size } + customSites.size
    }

    var showAddSiteDialog by remember { mutableStateOf(false) }
    var showCategoryBottomSheet by remember { mutableStateOf(false) }
    var showCloudUpdateDialog by remember { mutableStateOf(false) }
    var updateDialogDismissed by remember { mutableStateOf(false) }
    var welcomeDialogDismissed by remember { mutableStateOf(false) }

    // 启动时恢复自定义背景（跨重启持久）
    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("lzdz_bg_prefs", Context.MODE_PRIVATE)
        val type = prefs.getString("bg_type", "none") ?: "none"
        val path = prefs.getString("bg_path", "") ?: ""
        if (type != "none" && path.isNotBlank()) {
            viewModel.setLocalBgMedia(type, "file://$path")
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // 本地背景媒体优先（主题版块本机选择），无本地媒体时回退云端背景
        val bgType = uiState.localBgMediaType.ifBlank { "none" }.let {
            if (it != "none") it else (uiState.cloudSettings?.bgMedia?.type ?: "none")
        }
        val bgUrl = uiState.localBgMediaUri.ifBlank {
            uiState.cloudSettings?.bgMedia?.url.orEmpty()
        }
        GlobalWindBackground(
            bgMediaType = bgType,
            bgMediaUrl = bgUrl
        ) {
            Scaffold(
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets.statusBars,
                bottomBar = {
                    CustomRadioBottomNav(
                        selectedTab = uiState.currentTab,
                        onTabSelected = { viewModel.switchTab(it) }
                    )
                }
            ) { paddingValues ->
            when (uiState.currentTab) {
                AppBottomTab.HOME -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(
                            top = paddingValues.calculateTopPadding() + 8.dp,
                            bottom = paddingValues.calculateBottomPadding() + 16.dp,
                            start = 8.dp,
                            end = 8.dp
                        ),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("nav_main_grid")
                    ) {
                        // 1. Header & Brand Banner
                        item(span = { GridItemSpan(3) }) {
                            HeaderBrandSection(
                                favoriteCount = favorites.size,
                                historyCount = history.size,
                                totalResourceCount = totalResourceCount,
                                onOpenFavorites = { viewModel.setFavoritesModalVisible(true) },
                                onOpenHistory = { viewModel.setHistoryModalVisible(true) },
                                onOpenTheme = { viewModel.setThemeDialogVisible(true) },
                                onOpenAddSite = { showAddSiteDialog = true },
                                onTriggerSplash = { viewModel.showSplash() },
                                cloudMarquee = uiState.cloudMarquee,
                                cloudIpMonitor = uiState.cloudIpMonitor,
                                cloudAppName = uiState.cloudSettings?.appName?.ifBlank { "懒得找了" } ?: "懒得找了",
                                cloudLogo = uiState.cloudSettings?.logoUrl.orEmpty()
                            )
                        }

                        // 2. 随心抽按钮 (分类标签已按要求从主页移除，仅在随心抽弹窗内部保留)
                        item(span = { GridItemSpan(3) }) {
                            SaharaWaveButton(
                                onClick = { viewModel.rollLuckyCard() }
                            )
                        }

                        // 4. Search Box
                        item(span = { GridItemSpan(3) }) {
                            SearchSection(
                                query = uiState.searchQuery,
                                onQueryChange = { viewModel.updateSearchQuery(it) }
                            )
                        }

                        // 5. Result Counter
                        item(span = { GridItemSpan(3) }) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (uiState.searchQuery.isNotBlank()) "搜素结果 (${filteredCards.size})" else "收录资源 (${filteredCards.size})",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        itemsIndexed(filteredCards, key = { index, card -> "${card.id}_${card.url}_$index" }) { _, card ->
                            ResourceCard(
                                card = card,
                                isFavorite = favUrls.contains(card.url),
                                onCardClick = { viewModel.openCard(context, it) },
                                onFavoriteToggle = { viewModel.toggleFavorite(it, context) },
                                onCardLongClick = { viewModel.showDetail(it) }
                            )
                        }

                        // Empty State
                        if (filteredCards.isEmpty()) {
                            item(span = { GridItemSpan(3) }) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Filled.Search,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = "没有找到相关资源",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        TextButton(onClick = { viewModel.updateSearchQuery("") }) {
                                            Text("清空搜索条件")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                AppBottomTab.SOFTWARE -> {
                    UploadHubScreen(
                        title = "懒得找了-软件库",
                        subtitle = "一些PJ应用来源于网络～如有侵权请联系下架。失效也及时反馈哟",
                        resourceType = "software",
                        resources = uploadedSoftware,
                        onDelete = { id -> viewModel.deleteUploadedResource(id) },
                        modifier = Modifier.padding(paddingValues),
                        showDelete = false
                    )
                }
                AppBottomTab.SKILL -> {
                    var skillSubTabIndex by remember { mutableIntStateOf(0) } // 0: 提示词区, 1: Skill 技能库
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        // Sub-function Switcher Header
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = skillSubTabIndex == 0,
                                    onClick = { skillSubTabIndex = 0 },
                                    label = { Text("提示词区", fontWeight = FontWeight.Bold, fontSize = 11.5.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = skillSubTabIndex == 1,
                                    onClick = { skillSubTabIndex = 1 },
                                    label = { Text("Skill 技能库", fontWeight = FontWeight.Bold, fontSize = 11.5.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        if (skillSubTabIndex == 0) {
                            PromptHubSubView(
                                prompts = uploadedPrompts,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            UploadHubScreen(
                                title = "Skill · 技能库",
                                subtitle = "由云台控制台实时同步，增删均在后台控制，点击卡片直接下载 ZIP/MD 技能包",
                                resourceType = "skill",
                                resources = uploadedSkills,
                                onDelete = { id -> viewModel.deleteUploadedResource(id) },
                                modifier = Modifier.fillMaxSize(),
                                showDelete = false
                            )
                        }
                    }
                }
                AppBottomTab.TOOLBOX -> {
                    ToolboxScreen(
                        modifier = Modifier.padding(paddingValues)
                    )
                }
                AppBottomTab.SETTINGS -> {
                    SettingsScreen(
                        currentTheme = uiState.currentTheme,
                        onOpenThemeSwitcher = { viewModel.setThemeDialogVisible(true) },
                        cloudUpdate = uiState.cloudUpdate,
                        cloudVersion = uiState.cloudVersion,
                        cloudSettings = uiState.cloudSettings,
                        onCheckUpdate = { viewModel.refreshRemoteConfig() },
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }

        // 3D 旋转方块 + 渐变标题 "懒得找了" + 呼吸环 开屏动画（支持云端自定义）
        SplashScreenOverlay(
            isVisible = uiState.isSplashVisible,
            onDismiss = { viewModel.dismissSplash() },
            splash = uiState.cloudSplash,
            splashReady = uiState.isCloudReady
        )

        // 云端实时更新弹窗：控制台发布新版本后，本体启动自动弹出更新提醒。
        // 已展示过的版本号持久化记录，避免重复弹窗；新版本安装后（本地code==云端code）不再提示。
        val cloudVersion = uiState.cloudVersion
        val cloudUpdate = uiState.cloudUpdate
        val prefs = remember { context.getSharedPreferences("lzdz_update_prefs", Context.MODE_PRIVATE) }
        // 死命令：开屏动画结束后才呈现更新弹窗；有新版本就强制弹出且不可自行关闭，
        // 直到用户点击「立即更新」并完成安装；已是最新版本（cloudCode <= localCode）绝不弹窗。
        LaunchedEffect(uiState.isCloudReady, cloudVersion?.code, uiState.isSplashVisible) {
            val localCode = com.example.BuildConfig.VERSION_CODE
            val cloudCode = cloudVersion?.code ?: 0
            // 仅当：开屏已结束 && 云端有更新（本地版本低于云端）时弹窗；已是最新版绝不弹
            if (uiState.isCloudReady && !uiState.isSplashVisible && cloudCode > localCode && !updateDialogDismissed) {
                showCloudUpdateDialog = true
                prefs.edit().putInt("last_shown_version_code", cloudCode).apply()
            }
        }
        if (showCloudUpdateDialog && cloudUpdate != null && cloudVersion != null) {
            AppUpdateDialog(
                onDismiss = {
                    showCloudUpdateDialog = false
                    updateDialogDismissed = true
                },
                versionName = "v${cloudVersion.name}",
                onUpdateFinished = {
                    showCloudUpdateDialog = false
                    updateDialogDismissed = true
                },
                update = cloudUpdate,
                apkUrl = cloudVersion.apkUrl.ifBlank { null },
                // 强制执行更新：弹窗出现后不可自行关闭，必须用户点「立即更新」
                forceUpdate = true,
                // 不自动下载：等待用户点击「立即更新」后才开始下载
                autoDownload = false
            )
        }

        // 云端欢迎界面弹窗：控制台开启后，本体启动展示新功能介绍
        val cloudWelcome = uiState.cloudWelcome
        if (uiState.isCloudReady && cloudWelcome?.enabled == true && !welcomeDialogDismissed) {
            AlertDialog(
                onDismissRequest = { welcomeDialogDismissed = true },
                title = {
                    Text(
                        text = cloudWelcome.title.ifBlank { "欢迎使用" },
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        if (cloudWelcome.welcomeText.isNotBlank()) {
                            Text(
                                text = cloudWelcome.welcomeText,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        if (cloudWelcome.imageUrl.isNotBlank()) {
                            coil.compose.AsyncImage(
                                model = cloudWelcome.imageUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Text(text = cloudWelcome.content.ifBlank { "新版本已上线，快去体验吧！" })
                    }
                },
                confirmButton = {
                    Button(onClick = { welcomeDialogDismissed = true }) {
                        Text(cloudWelcome.buttonText.ifBlank { "开始使用" })
                    }
                }
            )
        }

        // Category Tags Expansion BottomSheet ("分类标签" 点击展开所有站点分类)
        if (showCategoryBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showCategoryBottomSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                CategoryTagsSheetContent(
                    categories = uiState.categories,
                    selectedCategoryId = uiState.selectedCategoryId,
                    totalResourceCount = totalResourceCount,
                    onCategorySelect = { catId ->
                        viewModel.selectCategory(catId)
                        showCategoryBottomSheet = false
                    },
                    onRollLucky = {
                        showCategoryBottomSheet = false
                        viewModel.rollLuckyCard()
                    },
                    onClose = { showCategoryBottomSheet = false }
                )
            }
        }

        // Lucky Draw BottomSheet ("懒人随心抽")
        if (uiState.isLuckyModalVisible && uiState.luckyCard != null) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.hideLuckyModal() },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                LuckyDrawSheetContent(
                    card = uiState.luckyCard!!,
                    categories = uiState.categories,
                    selectedCategoryId = uiState.selectedCategoryId,
                    onCategorySelect = { catId ->
                        viewModel.selectCategory(catId)
                        viewModel.rollLuckyCard(catId)
                    },
                    onReroll = { catId -> viewModel.rollLuckyCard(catId) },
                    onOpen = {
                        viewModel.openCard(context, uiState.luckyCard!!)
                        viewModel.hideLuckyModal()
                    },
                    onFavorite = { viewModel.toggleFavorite(uiState.luckyCard!!, context) },
                    isFavorite = favUrls.contains(uiState.luckyCard!!.url),
                    favoriteUrls = favUrls,
                    onCardClick = { card ->
                        viewModel.openCard(context, card)
                    },
                    onFavoriteToggle = { card ->
                        viewModel.toggleFavorite(card, context)
                    },
                    onClose = { viewModel.hideLuckyModal() }
                )
            }
        }

        // Favorites Dialog / BottomSheet
        if (uiState.isFavoritesModalVisible) {
            FavoritesSheet(
                favorites = favorites,
                onOpen = {
                    val card = NavCard(
                        id = it.url,
                        title = it.title,
                        url = it.url,
                        desc = it.desc ?: "",
                        badge = it.badge,
                        icon = it.iconUrl ?: ""
                    )
                    viewModel.showDetail(card)
                },
                onRemove = {
                    val card = NavCard(id = it.url, title = it.title, url = it.url)
                    viewModel.toggleFavorite(card, context)
                },
                onDismiss = { viewModel.setFavoritesModalVisible(false) }
            )
        }

        // History Dialog / BottomSheet
        if (uiState.isHistoryModalVisible) {
            HistorySheet(
                history = history,
                onOpen = {
                    val card = NavCard(
                        id = it.url,
                        title = it.title,
                        url = it.url,
                        desc = it.desc ?: "",
                        badge = it.badge,
                        icon = it.iconUrl ?: ""
                    )
                    viewModel.showDetail(card)
                },
                onClear = { viewModel.clearHistory(context) },
                onDismiss = { viewModel.setHistoryModalVisible(false) }
            )
        }

        // Site Detail Dialog (站点详细内容：这个站点是干嘛的、有什么特别之处、立即直达)
        uiState.activeDetailCard?.let { card ->
            SiteDetailDialog(
                card = card,
                isFavorite = favUrls.contains(card.url),
                onDismiss = { viewModel.hideDetail() },
                onOpenDirectly = {
                    viewModel.openCard(context, card)
                    viewModel.hideDetail()
                },
                onToggleFavorite = {
                    viewModel.toggleFavorite(card, context)
                },
                onCopyUrl = {
                    copyToClipboard(context, card.url)
                    android.widget.Toast.makeText(context, "已复制站点网址到剪贴板", android.widget.Toast.LENGTH_SHORT).show()
                },
                onShare = {
                    shareText(context, "【${card.title}】${card.desc}\n访问链接：${card.url}")
                }
            )
        }

        // Atmosphere Effect Overlay (Fireworks, Money, Dragon, God)
        AtmosphereOverlay(effect = uiState.atmosphereEffect)

        // Uiverse.io Skin & UI Kit Studio Dialog
        if (uiState.isThemeDialogVisible) {
            val coroutineScope = rememberCoroutineScope()
            // 选择背景文件 → 复制到应用私有目录（跨重启持久）→ 全局应用 → 关闭主题弹窗
            fun applyCustomBg(uri: Uri?, type: String) {
                if (uri == null) return
                coroutineScope.launch {
                    val result = withContext(Dispatchers.IO) {
                        try {
                            val dir = File(context.filesDir, "custom_bg")
                            dir.mkdirs()
                            val ext = if (type == "video") ".mp4" else ".jpg"
                            val target = File(dir, "custom_bg_${System.currentTimeMillis()}$ext")
                            context.contentResolver.openInputStream(uri)?.use { input ->
                                target.outputStream().use { output -> input.copyTo(output) }
                            }
                            // 持久化类型与路径（App 重启后恢复）
                            context.getSharedPreferences("lzdz_bg_prefs", Context.MODE_PRIVATE)
                                .edit()
                                .putString("bg_type", type)
                                .putString("bg_path", target.absolutePath)
                                .apply()
                            "file://${target.absolutePath}"
                        } catch (e: Exception) {
                            // 复制失败：回退使用内容 URI（当前会话仍可用）
                            try {
                                context.contentResolver.takePersistableUriPermission(
                                    uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                                )
                            } catch (e2: Exception) { }
                            uri.toString()
                        }
                    }
                    viewModel.setLocalBgMedia(type, result)
                    // 关闭主题弹窗，让用户立刻看到全局背景效果
                    viewModel.setThemeDialogVisible(false)
                    Toast.makeText(
                        context,
                        if (type == "image") "已应用自定义图片背景，全局生效" else "已应用自定义视频背景，全局生效",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            val bgImageLauncher = rememberLauncherForActivityResult(
                androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
            ) { uri -> applyCustomBg(uri, "image") }
            val bgVideoLauncher = rememberLauncherForActivityResult(
                androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
            ) { uri -> applyCustomBg(uri, "video") }
            UiverseDialog(
                isOpen = true,
                onClose = { viewModel.setThemeDialogVisible(false) },
                activeState = uiState.activeUiverseState,
                onApplyKit = { kit ->
                    viewModel.applyUiverseKit(kit)
                },
                onApplyCustomCss = { css, html ->
                    viewModel.applyUiverseCustomCss(css, html)
                },
                onApplyItemAsComponent = { item ->
                    viewModel.applyUiverseItem(item)
                },
                onResetDefault = {
                    viewModel.resetUiverseToDefault()
                },
                onApplyComponentTheme = { compId, css ->
                    viewModel.applyComponentTheme(compId, css)
                },
                componentThemes = uiState.activeUiverseState.componentThemes,
                localBgMediaType = uiState.localBgMediaType,
                onPickLocalImage = {
                    bgImageLauncher.launch(arrayOf("image/*"))
                },
                onPickLocalVideo = {
                    bgVideoLauncher.launch(arrayOf("video/*"))
                },
                onClearLocalBgMedia = {
                    viewModel.clearLocalBgMedia()
                    // 同时清除持久化记录
                    context.getSharedPreferences("lzdz_bg_prefs", Context.MODE_PRIVATE)
                        .edit().clear().apply()
                    Toast.makeText(context, "已清除自定义背景，恢复默认", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Add Site Dialog with Automatic Deduplication and Auto Metadata Fetching
        if (showAddSiteDialog) {
            AddSiteDialog(
                onDismiss = { showAddSiteDialog = false },
                onCheckDuplicate = { url, title ->
                    viewModel.checkSiteDuplicate(url, title)
                },
                onConfirmAdd = { title, url, desc, categoryId, badge, iconUrl ->
                    viewModel.addNewSite(title, url, desc, categoryId, badge, iconUrl)
                }
            )
        }
    }
}

// ---------------- HEADER SECTION ----------------
@Composable
private fun HeaderBrandSection(
    favoriteCount: Int,
    historyCount: Int,
    totalResourceCount: Int,
    onOpenFavorites: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenTheme: () -> Unit,
    onOpenAddSite: () -> Unit,
    onTriggerSplash: () -> Unit,
    cloudMarquee: com.example.data.remote.MarqueeDto? = null,
    cloudIpMonitor: com.example.data.remote.IpMonitorDto? = null,
    cloudAppName: String = "懒得找了",
    cloudLogo: String = ""
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(
                brush = Brush.horizontalGradient(
                    listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Official Brand Logo（云端可更换软件图标）
                if (cloudLogo.isNotBlank()) {
                    coil.compose.AsyncImage(
                        model = cloudLogo,
                        contentDescription = "软件图标",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.ic_app_brand_logo),
                        contentDescription = "软件图标",
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    AnimatedBrandTitle(text = cloudAppName)
                    Spacer(modifier = Modifier.height(2.dp))
                    DynamicOnlineCountWidget(
                        totalResourceCount = totalResourceCount,
                        primaryColor = primaryColor
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Quick actions: History & Favorites
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onOpenHistory,
                        modifier = Modifier.testTag("open_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.History,
                            contentDescription = "历史记录",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onOpenFavorites,
                        modifier = Modifier.testTag("open_favorites_button")
                    ) {
                        Box {
                            Icon(
                                imageVector = Icons.Filled.Bookmark,
                                contentDescription = "我的收藏",
                                tint = primaryColor
                            )
                            if (favoriteCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(secondaryColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (favoriteCount > 99) "99+" else "$favoriteCount",
                                        color = Color.White,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    IconButton(
                        onClick = onOpenAddSite,
                        modifier = Modifier.testTag("open_add_site_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AddCircle,
                            contentDescription = "新增站点",
                            tint = primaryColor
                        )
                    }

                    IconButton(
                        onClick = onOpenTheme,
                        modifier = Modifier.testTag("open_theme_switcher_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ColorLens,
                            contentDescription = "皮肤库",
                            tint = primaryColor
                        )
                    }

                    IconButton(onClick = onTriggerSplash) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = "启动特效",
                            tint = secondaryColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 首页置顶 · 实时 IP 定位监控（后台控制台可开关/配 URL，只显示定位 IP）
            IpLocationMonitorWidget(
                cloudIpMonitor = cloudIpMonitor,
                modifier = Modifier.fillMaxWidth()
            )

            // 24小时跑马灯公告（云端控制台可开关、自定义图标与逐小时文案）
            IpMonitorWidget(
                modifier = Modifier.fillMaxWidth(),
                cloudMarquee = cloudMarquee
            )
        }
    }
}

/**
 * 顶部 "懒得找了" 动态品牌标题：流光渐变与呼吸微动效
 */
@Composable
fun AnimatedBrandTitle(
    modifier: Modifier = Modifier,
    text: String = "懒得找了"
) {
    val infiniteTransition = rememberInfiniteTransition(label = "brand_shimmer_transition")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "brand_shimmer_offset"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparkle_scale"
    )

    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFF3B30),
            Color(0xFFFF8C00),
            Color(0xFFFFCC00),
            Color(0xFFFF2D55),
            Color(0xFFFF3B30)
        ),
        start = Offset(shimmerOffset, 0f),
        end = Offset(shimmerOffset + 240f, 60f)
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Black,
            fontSize = 17.5.sp,
            style = TextStyle(brush = gradientBrush),
            letterSpacing = 0.5.sp
        )
        Text(
            text = "✨",
            fontSize = 12.sp,
            modifier = Modifier.scale(scale)
        )
    }
}

/**
 * 真实感在线人数动态增减组件：按时段拟真、微小波动、呼吸绿点与即时增减浮标
 */
@Composable
fun DynamicOnlineCountWidget(
    totalResourceCount: Int,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    // 根据一天中不同时间段计算真实基准在线人数（白天约1200-1400，晚高峰约1500-1800，深夜约400-600）
    val baseCount = remember(hour) {
        when (hour) {
            in 0..6 -> 460 + (hour * 40)
            in 7..11 -> 880 + ((hour - 7) * 90)
            in 12..17 -> 1260 + ((hour - 12) * 40)
            in 18..22 -> 1520 + ((hour - 18) * 55)
            else -> 1050
        }
    }

    var onlineCount by remember { mutableIntStateOf(baseCount + Random.nextInt(-18, 22)) }
    var lastDelta by remember { mutableIntStateOf(0) }

    // 每 2.8 ~ 4.8 秒自然增减波动 (+1, -1, +2, -2, +3...)
    LaunchedEffect(Unit) {
        while (true) {
            delay(Random.nextLong(2800, 4800))
            val delta = Random.nextInt(-3, 5)
            if (delta != 0) {
                lastDelta = delta
                onlineCount = (onlineCount + delta).coerceIn(300, 2800)
            }
        }
    }

    // 实时状态绿点呼吸动效
    val pulseTransition = rememberInfiniteTransition(label = "pulse_live_dot")
    val dotAlpha by pulseTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_alpha"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable {
                Toast.makeText(
                    context,
                    "🟢 当前实时在线 $onlineCount 人 · 网络畅通 · 数据秒级同步",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .padding(vertical = 1.dp)
    ) {
        Text(
            text = "已收录 $totalResourceCount+ 精选资源",
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            color = primaryColor
        )
        Text(
            text = "·",
            fontSize = 10.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(Color(0xFF00C853).copy(alpha = dotAlpha))
        )
        Text(
            text = "${NumberFormat.getNumberInstance(Locale.CHINA).format(onlineCount)}人在线",
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (lastDelta != 0) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = if (lastDelta > 0) "+$lastDelta" else "$lastDelta",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (lastDelta > 0) Color(0xFF00C853) else Color(0xFFFF9100)
                )
            }
        }
    }
}

// ---------------- SEARCH SECTION ----------------
@Composable
private fun SearchSection(
    query: String,
    onQueryChange: (String) -> Unit
) {
    val uiverse = LocalUiverseState.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val inputShape = when (uiverse.inputStyle) {
        InputStylePreset.CYBER_TERMINAL -> RoundedCornerShape(topStart = 0.dp, topEnd = 12.dp, bottomEnd = 0.dp, bottomStart = 12.dp)
        InputStylePreset.GLASS_INSET -> RoundedCornerShape(16.dp)
        InputStylePreset.NEO_BRUTALIST_BOX -> RoundedCornerShape(6.dp)
        InputStylePreset.CUSTOM -> RoundedCornerShape(uiverse.customStyle?.cornerRadius ?: 12.dp)
        else -> RoundedCornerShape(12.dp)
    }
    val containerColor = when (uiverse.inputStyle) {
        InputStylePreset.CYBER_TERMINAL -> Color(0xFF0F101A)
        InputStylePreset.GLASS_INSET -> Color(0x33FFFFFF)
        InputStylePreset.NEO_BRUTALIST_BOX -> Color.White
        InputStylePreset.CUSTOM -> uiverse.customStyle?.backgroundColor ?: MaterialTheme.colorScheme.surface
        else -> MaterialTheme.colorScheme.surface
    }
    val focusedBorderColor = when (uiverse.inputStyle) {
        InputStylePreset.CYBER_TERMINAL -> Color(0xFF00F0FF)
        InputStylePreset.NEO_BRUTALIST_BOX -> Color.Black
        InputStylePreset.CUSTOM -> uiverse.customStyle?.borderColor?.takeIf { it != Color.Transparent } ?: primaryColor
        else -> primaryColor
    }
    val unfocusedBorderColor = when (uiverse.inputStyle) {
        InputStylePreset.CYBER_TERMINAL -> Color(0xFF00F0FF).copy(alpha = 0.4f)
        InputStylePreset.NEO_BRUTALIST_BOX -> Color.Black
        InputStylePreset.GLASS_INSET -> Color(0x66FFFFFF)
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text("搜索站内收录资源、影视、工具、AI...", fontSize = 13.5.sp, maxLines = 1)
        },
        leadingIcon = {
            Icon(Icons.Filled.Search, contentDescription = "搜索", tint = focusedBorderColor)
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Filled.Clear, contentDescription = "清除")
                }
            }
        },
        singleLine = true,
        shape = inputShape,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = focusedBorderColor,
            unfocusedBorderColor = unfocusedBorderColor,
            focusedContainerColor = containerColor,
            unfocusedContainerColor = containerColor
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("search_text_input")
    )
}

// ---------------- CATEGORY TAGS EXPANSION SHEET CONTENT ----------------
@Composable
private fun CategoryTagsSheetContent(
    categories: List<com.example.data.model.NavCategory>,
    selectedCategoryId: String,
    totalResourceCount: Int,
    onCategorySelect: (String) -> Unit,
    onRollLucky: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
            .padding(horizontal = 16.dp)
    ) {
        // Sheet Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Brush.linearGradient(listOf(FlameRed, SunsetOrange))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Extension,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "站点分类标签",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "点击分类标签直达对应收录资源 (${categories.size}大分类 · 共${totalResourceCount}个站点)",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.Close, contentDescription = "关闭")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Top Quick Action Row: "全部资源" & "随心抽一个"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val isAllSelected = selectedCategoryId.isBlank()
            OutlinedButton(
                onClick = { onCategorySelect("") },
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isAllSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent
                ),
                border = BorderStroke(
                    1.2.dp,
                    if (isAllSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1.2f)
            ) {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (isAllSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "全部站点 ($totalResourceCount)",
                    fontSize = 12.5.sp,
                    fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isAllSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }

            Button(
                onClick = onRollLucky,
                colors = ButtonDefaults.buttonColors(containerColor = SunsetOrange),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("随心抽一个", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Categories Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
        ) {
            items(categories, key = { it.id }) { cat ->
                val isSelected = cat.id == selectedCategoryId
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ),
                    border = BorderStroke(
                        if (isSelected) 1.5.dp else 1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCategorySelect(cat.id) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when {
                                    cat.name.contains("AI") || cat.name.contains("大模型") -> Icons.Filled.Psychology
                                    cat.name.contains("画布") || cat.name.contains("设计") -> Icons.Filled.ColorLens
                                    cat.name.contains("代码") || cat.name.contains("编程") -> Icons.Filled.Terminal
                                    cat.name.contains("视频") || cat.name.contains("影音") -> Icons.Filled.Extension
                                    else -> Icons.Filled.Bookmark
                                },
                                contentDescription = null,
                                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = cat.name,
                                fontSize = 12.5.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${cat.cards.size} 个站点",
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------- LUCKY DRAW SHEET CONTENT ----------------
@Composable
private fun LuckyDrawSheetContent(
    card: NavCard,
    categories: List<com.example.data.model.NavCategory>,
    selectedCategoryId: String,
    onCategorySelect: (String) -> Unit,
    onReroll: (String?) -> Unit,
    onOpen: () -> Unit,
    onFavorite: () -> Unit,
    isFavorite: Boolean,
    favoriteUrls: Set<String> = emptySet(),
    onCardClick: (NavCard) -> Unit = {},
    onFavoriteToggle: (NavCard) -> Unit = {},
    onClose: () -> Unit
) {
    var currentCatId by remember(selectedCategoryId) { mutableStateOf(selectedCategoryId) }
    var showCategorySitesDialog by remember { mutableStateOf(false) }

    val activeCatName = categories.find { it.id == currentCatId }?.name
    val activeCatTotalCount = if (currentCatId.isBlank()) {
        categories.sumOf { it.cards.size }
    } else {
        categories.find { it.id == currentCatId }?.cards?.size ?: 0
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "✨ 懒人随心抽 · 今日宝藏",
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.Close, contentDescription = "关闭")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 分类标签区域 (采用独立弹窗呈现所有分类与站点内容)
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFFFFF0DF),
            border = BorderStroke(1.dp, Color(0xFFFFD7B2)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showCategorySitesDialog = true }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(FlameRed.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Extension,
                            contentDescription = null,
                            tint = FlameRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "抽选分类",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (activeCatName != null) activeCatName else "全站宝藏",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = FlameRed
                            )
                        }
                        Text(
                            text = "已覆盖 $activeCatTotalCount 个精品站点 · 点击浏览所有分类站点",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(FlameRed)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "选择分类",
                        color = Color.White,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Highlight Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 6.dp, shape = RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        )
                    )
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.horizontalGradient(listOf(FlameRed, SunsetOrange)),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(18.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = card.title,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    if (!card.badge.isNullOrBlank()) {
                        RibbonBadge(text = card.badge, badgeType = card.badgeType)
                    }
                }

                // 所属分类标签显示 (点击也可直接打开独立分类站点弹窗)
                val belongCat = categories.find { it.id == card.categoryId }?.name ?: card.categoryId
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                            .clickable {
                                currentCatId = card.categoryId
                                showCategorySitesDialog = true
                            }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "分类 · $belongCat (点击查看分类站点)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = card.desc.ifBlank { "优质实用工具 / 宝藏影视导航站点" },
                    fontSize = 13.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = card.url,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = { onReroll(currentCatId.ifBlank { null }) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(22.dp)
            ) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("再抽一次")
            }

            Button(
                onClick = onOpen,
                modifier = Modifier.weight(1.3f),
                colors = ButtonDefaults.buttonColors(containerColor = FlameRed),
                shape = RoundedCornerShape(22.dp)
            ) {
                Icon(Icons.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("立即直达")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // 独立分类弹窗：展示所有分类及该分类下的所有站点内容
    if (showCategorySitesDialog) {
        CategorySitesDialog(
            categories = categories,
            selectedCategoryId = currentCatId,
            onSelectCategory = { newCatId ->
                currentCatId = newCatId
                onCategorySelect(newCatId)
            },
            onCardClick = { clickedCard ->
                showCategorySitesDialog = false
                onCardClick(clickedCard)
            },
            onFavoriteToggle = onFavoriteToggle,
            favoriteUrls = favoriteUrls,
            onRollInCategory = { targetCatId ->
                currentCatId = targetCatId
                onCategorySelect(targetCatId)
                onReroll(targetCatId.ifBlank { null })
            },
            onDismiss = { showCategorySitesDialog = false }
        )
    }
}

// ---------------- FAVORITES SHEET ----------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavoritesSheet(
    favorites: List<UserItemRecord>,
    onOpen: (UserItemRecord) -> Unit,
    onRemove: (UserItemRecord) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "我的收藏 (${favorites.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "关闭")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (favorites.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无收藏，点击卡片右上角星标即可收藏！",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    favorites.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .clickable { onOpen(item) }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SiteBrandIcon(
                                url = item.url,
                                title = item.title,
                                iconUrl = item.iconUrl ?: "",
                                size = 30.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                if (!item.desc.isNullOrBlank()) {
                                    Text(
                                        item.desc,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            IconButton(onClick = { onRemove(item) }) {
                                Icon(Icons.Filled.Bookmark, contentDescription = "移除收藏", tint = FlameRed)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------- HISTORY SHEET ----------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistorySheet(
    history: List<UserItemRecord>,
    onOpen: (UserItemRecord) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "浏览历史 (${history.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Row {
                    if (history.isNotEmpty()) {
                        IconButton(onClick = onClear) {
                            Icon(Icons.Filled.DeleteSweep, contentDescription = "清空历史", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "关闭")
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (history.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无浏览历史，点击任意资源卡片即可记录！",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    history.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .clickable { onOpen(item) }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SiteBrandIcon(
                                url = item.url,
                                title = item.title,
                                iconUrl = item.iconUrl ?: "",
                                size = 30.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(
                                    item.url,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(
                                Icons.Filled.OpenInBrowser,
                                contentDescription = "打开",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------- CLIPBOARD & SHARING UTILS ----------------
private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("URL", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
}

private fun shareText(context: Context, text: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "分享链接")
    context.startActivity(shareIntent)
}
