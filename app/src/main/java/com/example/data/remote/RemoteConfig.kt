package com.example.data.remote

import com.example.data.model.BadgeType
import com.example.data.model.NavCard
import com.example.data.model.NavCategory
import com.example.data.model.NavSubCategory

/**
 * 云端配置数据模型（admin-data.json）
 * 由「懒得找了·云端控制台」程序发布，本体启动时拉取并覆盖本地硬编码数据。
 * 字段缺失时全部使用默认值兜底，保证解析容错。
 */

data class AdminData(
    val version: VersionDto? = null,
    val home: HomeDto? = null,
    val software: List<SoftwareDto> = emptyList(),
    val skills: List<SkillDto> = emptyList(),
    val settings: SettingsDto? = null,
    val splash: SplashDto? = null,
    val welcome: WelcomeDto? = null,
    val updateDialog: UpdateDialogDto? = null,
    val marquee: MarqueeDto? = null
)

data class VersionDto(
    val code: Int = 0,
    val name: String = "1.0",
    val changelog: List<String> = emptyList(),
    val force: Boolean = false,
    val apkUrl: String = "",
    val apkUrlRaw: String = ""
)

data class HomeDto(
    val categories: List<CategoryDto> = emptyList()
)

data class CategoryDto(
    val id: String = "",
    val name: String = "",
    val iconKey: String = "grid",
    val desc: String = "",
    val subcategories: List<SubCategoryDto> = emptyList(),
    val cards: List<CardDto> = emptyList()
)

data class SubCategoryDto(
    val id: String = "",
    val name: String = ""
)

data class CardDto(
    val id: String = "",
    val title: String = "",
    val url: String = "",
    val icon: String = "",
    val fallbackText: String = "",
    val badge: String? = null,
    val badgeType: String = "ROSE",
    val desc: String = "",
    val categoryId: String = "",
    val subcatId: String = "",
    val highlights: String = ""
)

data class SoftwareDto(
    val id: String = "",
    val type: String = "software",
    val title: String = "",
    val desc: String = "",
    val url: String = "",
    val author: String = "",
    val badge: String = "",
    val tags: String = "",
    val apkUrl: String = "",
    val fileUrl: String = "",
    val previewUrl: String = "",
    val iconUrl: String = "",
    val mode: String = "file" // file=文件下载 / url=URL跳转下载
)

data class SkillDto(
    val id: String = "",
    val type: String = "skill",
    val promptType: String = "skill", // skill / prompt_image / prompt_video
    val title: String = "",
    val desc: String = "",
    val prompt: String = "",
    val url: String = "",
    val author: String = "",
    val badge: String = "",
    val tags: String = "",
    val previewUrl: String = "",
    val mediaUrl: String = "",
    val fileUrl: String = "",
    val iconUrl: String = "",
    val mode: String = "file" // file=文件下载 / url=URL跳转
)

data class SettingsDto(
    val appName: String = "懒得找了",
    val slogan: String = "",
    val aboutText: String = "",
    val contactQQ: String = "",
    val contactWechat: String = "",
    val contactAlipay: String = "",
    val qqGroupUrl: String = "",
    val qqGroupUin: String = "",
    val officialWebsite: String = "",
    val feedbackEmail: String = "",
    val customThemeCss: String = "",
    val customThemeHtml: String = ""
)

data class SplashDto(
    val type: String = "default",
    val customHtml: String = "",
    val mediaUrl: String = "",
    val durationSeconds: Int = 3,
    val bgColor: String = "#0B0B1A"
)

data class WelcomeDto(
    val enabled: Boolean = false,
    val title: String = "欢迎使用",
    val content: String = "",
    val welcomeText: String = "",
    val imageUrl: String = "",
    val buttonText: String = "开始使用"
)

data class UpdateDialogDto(
    val title: String = "发现新版本",
    val changelog: List<String> = emptyList(),
    val confirmText: String = "立即更新",
    val cancelText: String = "稍后再说",
    val customCss: String = "",
    val customHtml: String = ""
)

/**
 * 主页跑马灯公告：支持 24 小时时间段轮播，控制台可改图标与文案。
 */
data class MarqueeDto(
    val enabled: Boolean = true,
    val icon: String = "📢",
    val defaultText: String = "欢迎使用懒得找应用软件，这里的资源丰富，很多资源都是可以白嫖的。请自寻探索~~",
    val segments: List<MarqueeSegmentDto> = emptyList()
)

data class MarqueeSegmentDto(
    val start: Int = 0,
    val end: Int = 23,
    val text: String = ""
)

// ---------- 转换函数：云端 DTO -> 本体领域模型 ----------

fun CategoryDto.toNavCategory(): NavCategory {
    val subs = subcategories.map { NavSubCategory(it.id, it.name) }
    val cardList = cards.map { it.toNavCard() }
    return NavCategory(
        id = id,
        name = name,
        iconKey = iconKey,
        desc = desc,
        subcategories = subs,
        cards = cardList
    )
}

fun CardDto.toNavCard(): NavCard {
    return NavCard(
        id = id,
        title = title,
        url = url,
        icon = icon,
        fallbackText = fallbackText,
        badge = badge,
        badgeType = badgeType.toBadgeType(),
        desc = desc,
        categoryId = categoryId,
        subcatId = subcatId,
        highlights = highlights
    )
}

fun String.toBadgeType(): BadgeType = when (uppercase()) {
    "NEW" -> BadgeType.NEW
    "GOLD" -> BadgeType.GOLD
    "BLUE" -> BadgeType.BLUE
    else -> BadgeType.ROSE
}
