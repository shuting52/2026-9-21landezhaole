package com.example.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.NavData
import com.example.data.local.db.UserItemRecord
import com.example.data.model.NavCard
import com.example.data.model.NavCategory
import com.example.data.model.SearchEngine
import com.example.data.remote.AdminData
import com.example.data.remote.CategoryDto
import com.example.data.remote.RemoteConfigRepository
import com.example.data.remote.SettingsDto
import com.example.data.remote.SplashDto
import com.example.data.remote.UpdateDialogDto
import com.example.data.remote.VersionDto
import com.example.data.remote.WelcomeDto
import com.example.data.remote.toNavCategory
import com.example.data.remote.toNavCard
import com.example.data.repository.NavRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.URLEncoder

import com.example.data.local.db.UploadedResourceEntity
import com.example.data.local.db.CloneAppEntity
import com.example.ui.theme.AtmosphereEffect
import com.example.ui.theme.ThemePreset
import com.example.ui.theme.ThemePresetsRepository
import com.example.ui.uiverse.ActiveUiverseState
import com.example.ui.uiverse.UiKitPreset
import com.example.ui.uiverse.UiverseItem
import com.example.ui.uiverse.UiverseCssEngine
import com.example.ui.uiverse.CardStylePreset
import com.example.ui.uiverse.ButtonStylePreset
import com.example.ui.uiverse.InputStylePreset
import com.example.ui.uiverse.LoaderStylePreset
import com.example.ui.uiverse.PatternStylePreset
import java.util.UUID

enum class AppBottomTab(val title: String) {
    HOME("首页"),
    SOFTWARE("软件"),
    SKILL("SKill"),
    TOOLBOX("工具箱"),
    SETTINGS("设置")
}

data class NavUiState(
    val currentTab: AppBottomTab = AppBottomTab.HOME,
    val currentTheme: ThemePreset = ThemePresetsRepository.defaultTheme,
    val atmosphereEffect: AtmosphereEffect = AtmosphereEffect.NONE,
    val isThemeDialogVisible: Boolean = false,
    val activeUiverseState: ActiveUiverseState = ActiveUiverseState(),
    val categories: List<NavCategory> = NavData.categories,
    val selectedCategoryId: String = NavData.categories.firstOrNull()?.id ?: "ai_models",
    val selectedSubcatId: String = "all",
    val searchQuery: String = "",
    val selectedEngineIndex: Int = 0,
    val luckyCard: NavCard? = null,
    val isLuckyModalVisible: Boolean = false,
    val isFavoritesModalVisible: Boolean = false,
    val isHistoryModalVisible: Boolean = false,
    val activeDetailCard: NavCard? = null,
    val isSplashVisible: Boolean = true,
    val isCloudReady: Boolean = false,
    val cloudVersion: VersionDto? = null,
    val cloudSettings: SettingsDto? = null,
    val cloudSplash: SplashDto? = null,
    val cloudWelcome: WelcomeDto? = null,
    val cloudUpdate: UpdateDialogDto? = null
)

class NavViewModel(
    private val repository: NavRepository,
    private val remoteConfigRepository: RemoteConfigRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(NavUiState())
    val uiState: StateFlow<NavUiState> = _uiState

    val favorites: StateFlow<List<UserItemRecord>> = repository.favorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val history: StateFlow<List<UserItemRecord>> = repository.history
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uploadedSoftware: StateFlow<List<UploadedResourceEntity>> = repository.getUploadedSoftware()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uploadedSkills: StateFlow<List<UploadedResourceEntity>> = repository.getUploadedSkills()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customSites: StateFlow<List<UploadedResourceEntity>> = repository.getCustomSites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val clones: StateFlow<List<CloneAppEntity>> = repository.getAllClones()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Pre-seed some popular initial software and skills if empty
        viewModelScope.launch {
            seedInitialResources()
        }
        // 拉取云端配置：分类 / 开屏 / 欢迎 / 更新弹窗，实时覆盖本地数据
        viewModelScope.launch {
            refreshRemoteConfig()
        }
        // 定期后台检测云端更新（每 6 秒轮询一次）：控制台点击「应用」后，本体软件数秒内即可实时感知并同步生效，接近零延迟
        viewModelScope.launch {
            while (isActive) {
                kotlinx.coroutines.delay(6_000L)
                refreshRemoteConfig()
            }
        }
    }

    suspend fun refreshRemoteConfig(): Pair<Boolean, VersionDto?> {
        val remote = remoteConfigRepository ?: return Pair(false, null)
        val data: AdminData? = remote.fetchAdminData()
        if (data == null) {
            _uiState.value = _uiState.value.copy(isCloudReady = true)
            return Pair(false, null)
        }
        val cloudCats = data.home?.categories ?: emptyList()
        val localCats = NavData.categories
        val newCats = if (cloudCats.isNotEmpty()) {
            val cloudCardCount = cloudCats.sumOf { it.cards.size }
            val localCardCount = localCats.sumOf { it.cards.size }
            if (cloudCardCount >= localCardCount) {
                cloudCats.map { it.toNavCategory() }
            } else {
                // Merge cloud categories with local categories so all 400+ unique sites are always preserved
                val cloudCatMap = cloudCats.associateBy { it.id }
                localCats.map { localCat ->
                    val cloudCat = cloudCatMap[localCat.id]
                    if (cloudCat != null && cloudCat.cards.isNotEmpty()) {
                        val cloudNavCards = cloudCat.cards.map { it.toNavCard() }
                        val mergedCards = (cloudNavCards + localCat.cards).distinctBy { it.url }
                        localCat.copy(cards = mergedCards)
                    } else {
                        localCat
                    }
                }
            }
        } else {
            localCats
        }
        val current = _uiState.value
        val validSelectedId = newCats.any { it.id == current.selectedCategoryId }
        // 新站点自动置顶：带 NEW/新 角标的卡片排到每个分类最前，让最新更新第一时间呈现在首页
        val sortedCats = newCats.map { cat ->
            cat.copy(
                cards = cat.cards.sortedByDescending { card ->
                    val badge = card.badge ?: ""
                    when {
                        badge.contains("NEW", ignoreCase = true) -> 3
                        badge.contains("新") -> 2
                        badge.equals("HOT", ignoreCase = true) -> 1
                        else -> 0
                    }
                }
            )
        }
        _uiState.value = current.copy(
            categories = sortedCats,
            selectedCategoryId = if (validSelectedId) current.selectedCategoryId else (newCats.firstOrNull()?.id ?: "all"),
            isCloudReady = true,
            cloudVersion = data.version,
            cloudSettings = data.settings,
            cloudSplash = data.splash,
            cloudWelcome = data.welcome,
            cloudUpdate = data.updateDialog
        )
        // 控制台软件/Skill 增删改 → 本体实时同步（删除：云端已移除的条目从本地库同步删除）
        syncCloudResources(data)
        val hasNewVersion = (data.version?.code ?: 0) > com.example.BuildConfig.VERSION_CODE
        return Pair(hasNewVersion, data.version)
    }

    /**
     * 将控制台发布的 software / skills 数组同步到本地数据库：
     * - 云端有的条目 upsert（新增/更新），含文件下载资源（APK / ZIP / MD）
     * - 控制台来源（id 以 sw_/sk_ 开头）但云端已删除的条目从本地删除
     */
    private suspend fun syncCloudResources(data: AdminData) {
        try {
            val cloudSoftwares = data.software
            val cloudSkills = data.skills

            cloudSoftwares.forEach { sw ->
                val (dlUrl, dlType) = resolveDownload(sw.apkUrl, sw.fileUrl)
                repository.saveUploadedResource(
                    UploadedResourceEntity(
                        id = sw.id,
                        type = "software",
                        title = sw.title,
                        desc = sw.desc,
                        url = sw.url,
                        author = sw.author,
                        badge = sw.badge.ifBlank { "站长推荐" },
                        tags = sw.tags,
                        fileUrl = dlUrl,
                        fileType = dlType
                    )
                )
            }
            cloudSkills.forEach { sk ->
                val (dlUrl, dlType) = resolveDownload("", sk.fileUrl)
                repository.saveUploadedResource(
                    UploadedResourceEntity(
                        id = sk.id,
                        type = "skill",
                        title = sk.title,
                        desc = sk.desc,
                        url = sk.url,
                        author = sk.author,
                        badge = sk.badge.ifBlank { "站长推荐" },
                        tags = sk.tags,
                        fileUrl = dlUrl,
                        fileType = dlType
                    )
                )
            }

            // 删除同步：控制台来源（sw_/sk_ 前缀）但云端已不存在的条目
            val cloudSwIds = cloudSoftwares.map { it.id }.toSet()
            val cloudSkIds = cloudSkills.map { it.id }.toSet()
            val localAll = repository.getAllUploadedResources()
            localAll.forEach { local ->
                val cloudManaged = when (local.type) {
                    "software" -> local.id.startsWith("sw_")
                    "skill" -> local.id.startsWith("sk_")
                    else -> false
                }
                if (cloudManaged) {
                    val stillInCloud = if (local.type == "software") local.id in cloudSwIds else local.id in cloudSkIds
                    if (!stillInCloud) {
                        repository.deleteUploadedResource(local.id)
                    }
                }
            }
        } catch (e: Exception) {
            // 同步失败不阻断主流程
        }
    }

    /** 解析下载资源与类型：优先 APK，其次 ZIP/MD */
    private fun resolveDownload(apkUrl: String, fileUrl: String): Pair<String, String> {
        if (apkUrl.isNotBlank()) return apkUrl to "APK"
        val f = fileUrl.trim()
        if (f.isBlank()) return "" to ""
        val low = f.lowercase()
        return when {
            low.endsWith(".zip") -> f to "ZIP"
            low.endsWith(".md") -> f to "MD"
            else -> f to "文件"
        }
    }

    private suspend fun seedInitialResources() {
        // Pre-seed 3 software and 3 skills for initial showcase
        val defaultSoftware = listOf(
            UploadedResourceEntity(
                id = "init-sw-1",
                type = "software",
                title = "Geek Uninstaller",
                desc = "极简高效的Windows彻底卸载清理神器，免安装单文件，强制剔除注册表残留。",
                url = "https://geekuninstaller.com/",
                author = "Thomas",
                badge = "站长自用",
                tags = "Windows / 清理"
            ),
            UploadedResourceEntity(
                id = "init-sw-2",
                type = "software",
                title = "PotPlayer 64bit 绿色优化版",
                desc = "全球极佳的高清影音播放器，内置无缝解码器，支持4K HDR与杜比视界蓝光直出。",
                url = "https://potplayer.daum.net/",
                author = "Daum",
                badge = "装机必备",
                tags = "影音播放 / 4K"
            ),
            UploadedResourceEntity(
                id = "init-sw-3",
                type = "software",
                title = "Everything 极速搜盘",
                desc = "1秒内秒搜百万本地文件的搜索神器，毫秒级响应，几乎零内存占用。",
                url = "https://www.voidtools.com/",
                author = "Voidtools",
                badge = "神器",
                tags = "生产力 / 搜索"
            )
        )

        val defaultSkills = listOf(
            UploadedResourceEntity(
                id = "init-sk-1",
                type = "skill",
                title = "Clean Architecture 架构设计指南",
                desc = "指导编写符合MVVM高内聚低耦合的现代Android Kotlin架构与依赖注入规范。",
                url = "https://developer.android.com/topic/architecture",
                author = "Android Team",
                badge = "架构Skill",
                tags = "Kotlin / M3"
            ),
            UploadedResourceEntity(
                id = "init-sk-2",
                type = "skill",
                title = "Gemini 顶级 Prompt 提示词工程",
                desc = "让大模型思考更深度、代码生成更严谨的结构化提示词设计模式与角色设定方案。",
                url = "https://ai.google.dev/",
                author = "DeepMind Lab",
                badge = "AI特化",
                tags = "Prompt / LLM"
            ),
            UploadedResourceEntity(
                id = "init-sk-3",
                type = "skill",
                title = "Robolectric 极速本地无头单元测试",
                desc = "无须真机与模拟器即可在JVM上瞬间执行Android UI与逻辑测试的高效方案。",
                url = "https://robolectric.org/",
                author = "Google OpenSource",
                badge = "测试Skill",
                tags = "CI/CD / JVM"
            )
        )

        defaultSoftware.forEach { repository.saveUploadedResource(it) }
        defaultSkills.forEach { repository.saveUploadedResource(it) }
    }

    fun switchTab(tab: AppBottomTab) {
        _uiState.value = _uiState.value.copy(currentTab = tab)
    }

    fun setTheme(preset: ThemePreset) {
        _uiState.value = _uiState.value.copy(
            currentTheme = preset,
            atmosphereEffect = preset.atmosphereEffect
        )
    }

    fun setAtmosphereEffect(effect: AtmosphereEffect) {
        _uiState.value = _uiState.value.copy(atmosphereEffect = effect)
    }

    fun setThemeDialogVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(isThemeDialogVisible = visible)
    }

    fun applyUiverseKit(kit: UiKitPreset) {
        val newTheme = ThemePreset(
            id = kit.id,
            name = kit.displayName,
            style = kit.id,
            categoryName = "Uiverse",
            primaryColor = kit.primaryColor,
            secondaryColor = kit.secondaryColor,
            bgColor = kit.backgroundColor,
            surfaceColor = kit.surfaceColor,
            textColor = kit.textColor
        )
        val newCardStyle = when (kit) {
            UiKitPreset.STYLE_1_TILT_MAGNETIC -> CardStylePreset.TILT_3D_MAGNETIC
            UiKitPreset.STYLE_3_THICK_BUTTON -> CardStylePreset.STYLE_3_THICK_CARD
            UiKitPreset.STYLE_4_BOTTOMBAR_APPBAR -> CardStylePreset.STYLE_4_BOLD_FRAME
            UiKitPreset.STYLE_5_CAPSULE_SETROW -> CardStylePreset.STYLE_5_CAPSULE_ROW
            UiKitPreset.CYBERPUNK_NEON -> CardStylePreset.CYBERPUNK
            UiKitPreset.GLASSMORPHISM_AURORA -> CardStylePreset.GLASSMORPHISM
            UiKitPreset.NEUMORPHISM_CLAY -> CardStylePreset.NEUMORPHISM
            UiKitPreset.NEO_BRUTALISM_POP -> CardStylePreset.NEO_BRUTALISM
            UiKitPreset.RETRO_8BIT_ARCADE -> CardStylePreset.RETRO_PIXEL
            UiKitPreset.HOLOGRAPHIC_PRISM -> CardStylePreset.HOLOGRAPHIC
            UiKitPreset.LUXURY_OBSIDIAN_GOLD -> CardStylePreset.LUXURY_GOLD
            else -> CardStylePreset.DEFAULT
        }
        val newBtnStyle = when (kit) {
            UiKitPreset.STYLE_2_GLASS_LOADER -> ButtonStylePreset.STYLE_2_GLASS_PILL
            UiKitPreset.STYLE_3_THICK_BUTTON -> ButtonStylePreset.STYLE_3_THICK_SHADOW
            UiKitPreset.STYLE_4_BOTTOMBAR_APPBAR -> ButtonStylePreset.STYLE_3_THICK_SHADOW
            UiKitPreset.CYBERPUNK_NEON -> ButtonStylePreset.CYBERPUNK_GLOW
            UiKitPreset.GLASSMORPHISM_AURORA -> ButtonStylePreset.GLASS_AURORA
            UiKitPreset.NEUMORPHISM_CLAY -> ButtonStylePreset.NEUMORPHIC_PUSH
            UiKitPreset.NEO_BRUTALISM_POP -> ButtonStylePreset.BRUTALIST_OFFSET
            UiKitPreset.RETRO_8BIT_ARCADE -> ButtonStylePreset.RETRO_COIN
            else -> ButtonStylePreset.DEFAULT
        }
        val newInputStyle = when (kit) {
            UiKitPreset.STYLE_2_GLASS_LOADER -> InputStylePreset.STYLE_2_GLASS_GLOW
            UiKitPreset.STYLE_3_THICK_BUTTON -> InputStylePreset.NEO_BRUTALIST_BOX
            UiKitPreset.CYBERPUNK_NEON -> InputStylePreset.CYBER_TERMINAL
            UiKitPreset.GLASSMORPHISM_AURORA -> InputStylePreset.GLASS_INSET
            UiKitPreset.NEO_BRUTALISM_POP -> InputStylePreset.NEO_BRUTALIST_BOX
            else -> InputStylePreset.DEFAULT
        }
        val newLoaderStyle = when (kit) {
            UiKitPreset.STYLE_2_GLASS_LOADER -> LoaderStylePreset.STYLE_2_DUAL_RING
            UiKitPreset.CYBERPUNK_NEON -> LoaderStylePreset.CYBER_GLITCH
            UiKitPreset.GLASSMORPHISM_AURORA -> LoaderStylePreset.ORBIT_PLANETS
            UiKitPreset.NEUMORPHISM_CLAY -> LoaderStylePreset.NEUMORPHIC_PULSE
            UiKitPreset.RETRO_8BIT_ARCADE -> LoaderStylePreset.CYBER_GLITCH
            else -> LoaderStylePreset.BOUNCING_BARS
        }
        val newPatternStyle = when (kit) {
            UiKitPreset.STYLE_1_TILT_MAGNETIC -> PatternStylePreset.HEXAGON_MESH
            UiKitPreset.STYLE_4_BOTTOMBAR_APPBAR -> PatternStylePreset.DOT_MATRIX
            UiKitPreset.CYBERPUNK_NEON -> PatternStylePreset.CYBER_GRID
            UiKitPreset.GLASSMORPHISM_AURORA -> PatternStylePreset.HEXAGON_MESH
            UiKitPreset.NEUMORPHISM_CLAY -> PatternStylePreset.DEFAULT_WIND
            UiKitPreset.NEO_BRUTALISM_POP -> PatternStylePreset.DOT_MATRIX
            UiKitPreset.RETRO_8BIT_ARCADE -> PatternStylePreset.BLUEPRINT
            else -> PatternStylePreset.DEFAULT_WIND
        }

        _uiState.value = _uiState.value.copy(
            currentTheme = newTheme,
            activeUiverseState = _uiState.value.activeUiverseState.copy(
                activeKit = kit,
                cardStyle = newCardStyle,
                buttonStyle = newBtnStyle,
                inputStyle = newInputStyle,
                loaderStyle = newLoaderStyle,
                patternStyle = newPatternStyle,
                customStyle = null
            )
        )
    }

    fun applyUiverseCustomCss(css: String, html: String) {
        val parsed = UiverseCssEngine.parseCss(css, html)
        val customTheme = ThemePreset(
            id = "custom_css",
            name = "自定义代码驱动",
            style = "custom",
            categoryName = "Custom",
            primaryColor = parsed.textColor ?: androidx.compose.ui.graphics.Color(0xFF6366F1),
            secondaryColor = parsed.borderColor.takeIf { it != androidx.compose.ui.graphics.Color.Transparent }
                ?: androidx.compose.ui.graphics.Color(0xFFEC4899),
            bgColor = parsed.backgroundColor ?: androidx.compose.ui.graphics.Color(0xFF0F172A),
            surfaceColor = parsed.backgroundColor ?: androidx.compose.ui.graphics.Color(0xFF1E293B),
            textColor = parsed.textColor ?: androidx.compose.ui.graphics.Color(0xFFF8FAFC)
        )
        _uiState.value = _uiState.value.copy(
            currentTheme = customTheme,
            activeUiverseState = _uiState.value.activeUiverseState.copy(
                activeKit = UiKitPreset.CUSTOM_CODE,
                cardStyle = CardStylePreset.CUSTOM,
                buttonStyle = ButtonStylePreset.CUSTOM,
                inputStyle = InputStylePreset.CUSTOM,
                loaderStyle = LoaderStylePreset.CUSTOM,
                patternStyle = PatternStylePreset.CUSTOM,
                customStyle = parsed,
                customCssInput = css,
                customHtmlInput = html
            )
        )
    }

    fun applyUiverseItem(item: UiverseItem) {
        item.associatedKit?.let {
            applyUiverseKit(it)
            return
        }
        if (item.cssCode.isNotBlank()) {
            applyUiverseCustomCss(item.cssCode, item.htmlCode)
        }
    }

    fun resetUiverseToDefault() {
        _uiState.value = _uiState.value.copy(
            currentTheme = ThemePresetsRepository.defaultTheme,
            activeUiverseState = ActiveUiverseState()
        )
    }

    fun uploadResource(type: String, title: String, desc: String, url: String, author: String, tags: String) {
        viewModelScope.launch {
            val entity = UploadedResourceEntity(
                id = "res_${UUID.randomUUID().toString().take(8)}",
                type = type,
                title = title,
                desc = desc,
                url = url,
                author = author,
                badge = "作者投递",
                tags = tags
            )
            repository.saveUploadedResource(entity)
        }
    }

    fun deleteUploadedResource(id: String) {
        viewModelScope.launch {
            repository.deleteUploadedResource(id)
        }
    }

    // ================= 分身多开 =================
    /** 创建分身（桌面快捷入口 + 本地记录）；返回分身名 */
    fun createClone(
        packageName: String,
        originalAppName: String,
        onResult: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            val maxIndex = repository.getMaxCloneIndex(packageName) ?: 0
            val nextIndex = maxIndex + 1
            val cloneName = when (nextIndex) {
                1 -> originalAppName + "分身"
                2 -> originalAppName + "分身二"
                3 -> originalAppName + "分身三"
                else -> originalAppName + "分身" + nextIndex
            }
            val entity = CloneAppEntity(
                id = "${packageName}_$nextIndex",
                originalAppName = originalAppName,
                packageName = packageName,
                cloneName = cloneName,
                cloneIndex = nextIndex
            )
            repository.saveClone(entity)
            onResult(true)
        }
    }

    fun deleteClone(id: String) {
        viewModelScope.launch {
            repository.deleteClone(id)
        }
    }

    fun renameClone(id: String, newName: String) {
        viewModelScope.launch {
            repository.renameClone(id, newName)
        }
    }

    /**
     * URL normalizer to reliably detect and exclude duplicate sites:
     * e.g. "https://www.bilibili.com/" -> "bilibili.com"
     */
    fun normalizeSiteUrl(rawUrl: String): String {
        return rawUrl.trim()
            .lowercase()
            .removePrefix("https://")
            .removePrefix("http://")
            .removePrefix("www.")
            .substringBefore("?")
            .substringBefore("#")
            .trimEnd('/')
    }

    /**
     * Checks if a site with the same normalized URL or exact title already exists.
     * Returns the duplicate NavCard if found, or null if completely unique.
     */
    fun checkSiteDuplicate(url: String, title: String): NavCard? {
        val normUrl = normalizeSiteUrl(url)
        val cleanTitle = title.trim().lowercase()

        val allExisting = (NavData.getAllCards() + customSites.value.map { entity ->
            NavCard(
                id = entity.id,
                title = entity.title,
                desc = entity.desc,
                url = entity.url,
                icon = "",
                categoryId = entity.author.ifBlank { "tools" },
                subcatId = "all",
                badge = entity.badge
            )
        }).distinctBy { normalizeSiteUrl(it.url) }

        return allExisting.firstOrNull { card ->
            val cardNormUrl = normalizeSiteUrl(card.url)
            val cardTitle = card.title.trim().lowercase()
            (normUrl.isNotEmpty() && cardNormUrl == normUrl) ||
                    (cleanTitle.isNotEmpty() && cardTitle == cleanTitle)
        }
    }

    /**
     * Adds a new site with automatic duplicate exclusion.
     * Returns true if successfully added, or false if excluded as duplicate.
     */
    fun addNewSite(
        title: String,
        url: String,
        desc: String,
        categoryId: String,
        badge: String = "NEW",
        iconUrl: String = ""
    ): Boolean {
        val duplicate = checkSiteDuplicate(url, title)
        if (duplicate != null) {
            return false // Automatically excluded!
        }
        viewModelScope.launch {
            val entity = UploadedResourceEntity(
                id = "custom_site_${UUID.randomUUID().toString().take(8)}",
                type = "custom_site",
                title = title,
                desc = desc,
                url = url,
                author = categoryId,
                badge = badge.ifBlank { "NEW" },
                tags = if (iconUrl.isNotBlank()) iconUrl else "custom_site"
            )
            repository.saveUploadedResource(entity)
        }
        return true
    }

    val filteredCards: StateFlow<List<NavCard>> = combine(_uiState, favorites, customSites) { state, _, customList ->
        val query = state.searchQuery.trim().lowercase()

        // Map custom sites into NavCards with NEW badge type
        val customNavCards = customList.map { entity ->
            val customIcon = if (entity.tags.startsWith("http://") || entity.tags.startsWith("https://")) {
                entity.tags
            } else ""
            NavCard(
                id = entity.id,
                title = entity.title,
                desc = entity.desc,
                url = entity.url,
                icon = customIcon,
                categoryId = entity.author.ifBlank { "ai_models" },
                subcatId = "all",
                badge = entity.badge.ifBlank { "NEW" },
                badgeType = com.example.data.model.BadgeType.NEW
            )
        }

        // Automatic global deduplication across user-added and built-in sites (custom takes precedence on top)
        val allCards = (customNavCards + NavData.getAllCards()).distinctBy { normalizeSiteUrl(it.url) }

        // Pin new sites (custom sites and sites marked with NEW badge) to the top
        fun prioritizeNewSites(cards: List<NavCard>): List<NavCard> {
            return cards.sortedWith(
                compareByDescending<NavCard> { card ->
                    when {
                        // User customized new sites first
                        customNavCards.any { normalizeSiteUrl(it.url) == normalizeSiteUrl(card.url) } -> 3
                        // Built-in cards with NEW badge
                        card.badgeType == com.example.data.model.BadgeType.NEW ||
                                card.badge?.equals("NEW", ignoreCase = true) == true ||
                                card.badge?.contains("新", ignoreCase = true) == true -> 2
                        // Other hot/featured badges
                        card.badgeType == com.example.data.model.BadgeType.ROSE ||
                                card.badgeType == com.example.data.model.BadgeType.GOLD -> 1
                        else -> 0
                    }
                }
            )
        }

        if (query.isNotEmpty()) {
            val searchResults = allCards.filter {
                it.title.lowercase().contains(query) ||
                        it.desc.lowercase().contains(query) ||
                        (it.badge?.lowercase()?.contains(query) == true)
            }
            prioritizeNewSites(searchResults)
        } else {
            val currentCategory = state.categories.firstOrNull { it.id == state.selectedCategoryId }
                ?: state.categories.first()

            val categoryCustomCards = customNavCards.filter {
                it.categoryId == currentCategory.id || (currentCategory.id == "ai_models" && (it.categoryId == "ai" || it.categoryId == "ai_models"))
            }
            // Newly added sites pinned on top of the category
            val mergedCategoryCards = (categoryCustomCards + currentCategory.cards).distinctBy { normalizeSiteUrl(it.url) }

            val rawList = if (state.selectedSubcatId == "all") {
                mergedCategoryCards
            } else {
                mergedCategoryCards.filter { it.subcatId == state.selectedSubcatId }
            }
            prioritizeNewSites(rawList)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectCategory(categoryId: String) {
        _uiState.value = _uiState.value.copy(
            selectedCategoryId = categoryId,
            selectedSubcatId = "all",
            searchQuery = ""
        )
    }

    fun selectSubcategory(subcatId: String) {
        _uiState.value = _uiState.value.copy(selectedSubcatId = subcatId)
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun selectSearchEngine(index: Int) {
        _uiState.value = _uiState.value.copy(selectedEngineIndex = index)
    }

    fun executeSearch(context: Context) {
        val query = _uiState.value.searchQuery.trim()
        val engine = NavData.searchEngines.getOrNull(_uiState.value.selectedEngineIndex) ?: NavData.searchEngines[0]

        if (engine.urlTemplate != null && query.isNotEmpty()) {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val targetUrl = String.format(engine.urlTemplate, encoded)
            openUrl(context, targetUrl, "${engine.name}: $query")
        }
    }

    fun openCard(context: Context, card: NavCard) {
        viewModelScope.launch {
            repository.recordVisit(card)
        }
        openUrl(context, card.url, card.title)
    }

    fun openUrl(context: Context, url: String, label: String = "") {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "无法打开链接: $url", Toast.LENGTH_SHORT).show()
        }
    }

    fun toggleFavorite(card: NavCard, context: Context? = null) {
        viewModelScope.launch {
            val isFav = repository.toggleFavorite(card)
            if (context != null) {
                val msg = if (isFav) "已收藏: ${card.title}" else "已取消收藏"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun showDetail(card: NavCard) {
        _uiState.value = _uiState.value.copy(activeDetailCard = card)
    }

    fun hideDetail() {
        _uiState.value = _uiState.value.copy(activeDetailCard = null)
    }

    fun rollLuckyCard(categoryId: String? = null) {
        val pool = if (!categoryId.isNullOrBlank()) {
            NavData.categories.find { it.id == categoryId }?.cards?.takeIf { it.isNotEmpty() } ?: NavData.getAllCards()
        } else {
            NavData.getAllCards()
        }
        val randomCard = pool.randomOrNull() ?: return
        _uiState.value = _uiState.value.copy(
            luckyCard = randomCard,
            isLuckyModalVisible = true
        )
    }

    fun hideLuckyModal() {
        _uiState.value = _uiState.value.copy(isLuckyModalVisible = false)
    }

    fun setFavoritesModalVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(isFavoritesModalVisible = visible)
    }

    fun setHistoryModalVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(isHistoryModalVisible = visible)
    }

    fun clearHistory(context: Context? = null) {
        viewModelScope.launch {
            repository.clearHistory()
            context?.let {
                Toast.makeText(it, "已清空浏览历史", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun dismissSplash() {
        _uiState.value = _uiState.value.copy(isSplashVisible = false)
    }

    fun showSplash() {
        _uiState.value = _uiState.value.copy(isSplashVisible = true)
    }
}

class NavViewModelFactory(
    private val repository: NavRepository,
    private val remoteConfigRepository: RemoteConfigRepository? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NavViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NavViewModel(repository, remoteConfigRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
