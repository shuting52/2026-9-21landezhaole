package com.example.ui.components

import android.content.Context
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.request.ImageRequest
import okhttp3.OkHttpClient
import java.net.URI
import java.util.concurrent.TimeUnit

/**
 * Singleton FastImageLoader configured with short timeouts and aggressive caching
 * to eliminate delayed favicon display.
 */
object FastFaviconImageLoader {
    private var instance: ImageLoader? = null

    fun get(context: Context): ImageLoader {
        return instance ?: synchronized(this) {
            instance ?: run {
                val okHttp = OkHttpClient.Builder()
                    .connectTimeout(2000, TimeUnit.MILLISECONDS)
                    .readTimeout(2000, TimeUnit.MILLISECONDS)
                    .callTimeout(3000, TimeUnit.MILLISECONDS)
                    .retryOnConnectionFailure(true)
                    .build()

                ImageLoader.Builder(context.applicationContext)
                    .okHttpClient(okHttp)
                    .memoryCache {
                        MemoryCache.Builder(context.applicationContext)
                            .maxSizePercent(0.25)
                            .build()
                    }
                    .diskCache {
                        DiskCache.Builder()
                            .directory(context.applicationContext.cacheDir.resolve("site_favicons_v2"))
                            .maxSizeBytes(120L * 1024 * 1024)
                            .build()
                    }
                    .crossfade(150)
                    .build().also { instance = it }
            }
        }
    }
}

/**
 * Helper to extract canonical domain and generate multi-tier favicon icon URLs.
 * Optimized with high-speed mainland Chinese CDNs first to prevent Google 10s timeouts.
 */
object FaviconHelper {
    fun extractDomain(rawUrl: String): String {
        return try {
            val normalized = if (rawUrl.startsWith("http://") || rawUrl.startsWith("https://")) rawUrl else "https://$rawUrl"
            val uri = URI(normalized)
            var host = uri.host ?: ""
            if (host.startsWith("www.")) host = host.substring(4)
            if (host.isBlank()) {
                val stripped = rawUrl.removePrefix("https://").removePrefix("http://")
                val hostPart = stripped.substringBefore("/").substringBefore("?").substringBefore(":")
                hostPart.removePrefix("www.")
            } else {
                host
            }
        } catch (_: Exception) {
            val stripped = rawUrl.removePrefix("https://").removePrefix("http://")
            val hostPart = stripped.substringBefore("/").substringBefore("?").substringBefore(":")
            hostPart.removePrefix("www.")
        }
    }

    /**
     * Known high-resolution official CDN icons for top services (instant load)
     */
    private val directBrandIcons = mapOf(
        "chatgpt.com" to "https://cdn.oaistatic.com/_next/static/media/apple-touch-icon.82af6fe1.png",
        "deepseek.com" to "https://chat.deepseek.com/favicon.svg",
        "claude.ai" to "https://claude.ai/favicon.ico",
        "bilibili.com" to "https://www.bilibili.com/favicon.ico",
        "github.com" to "https://github.githubassets.com/favicons/favicon.png",
        "youtube.com" to "https://www.youtube.com/s/desktop/favicon.ico",
        "zhihu.com" to "https://static.zhihu.com/heifetz/favicon.ico",
        "steamcommunity.com" to "https://steamcommunity.com/favicon.ico",
        "baidu.com" to "https://www.baidu.com/favicon.ico",
        "trae.cn" to "https://www.trae.cn/favicon.ico",
        "cursor.com" to "https://www.cursor.com/favicon.ico",
        "cline.bot" to "https://cline.bot/favicon.ico",
        "lovable.dev" to "https://lovable.dev/favicon.ico",
        "bolt.new" to "https://bolt.new/favicon.ico",
        "windsurf.com" to "https://windsurf.com/favicon.ico",
        "anthropic.com" to "https://www.anthropic.com/favicon.ico",
        "openai.com" to "https://openai.com/favicon.ico",
        "lingma.aliyun.com" to "https://img.alicdn.com/imgextra/i1/O1CN01fUe7vL1Yx83p0R6Vq_!!6000000003120-2-tps-64-64.png",
        "firebase.studio" to "https://firebase.studio/favicon.ico"
    )

    /**
     * Primary favicon endpoint:
     * 1) Direct official URL if provided or in direct brand map
     * 2) High-speed Chinese CDN (api.iowen.cn) with sub-100ms response time
     */
    fun getPrimaryFaviconUrl(url: String, explicitIcon: String = ""): String {
        if (explicitIcon.isNotBlank() && (explicitIcon.startsWith("http://") || explicitIcon.startsWith("https://"))) {
            return explicitIcon
        }
        val domain = extractDomain(url)
        directBrandIcons[domain]?.let { return it }

        return "https://api.iowen.cn/favicon/$domain.png"
    }

    /**
     * Secondary fallback favicon endpoint (Fast mainland mirror CDN)
     */
    fun getSecondaryFaviconUrl(url: String): String {
        val domain = extractDomain(url)
        return "https://favicon.cccyun.cc/$domain"
    }

    /**
     * Tertiary fallback favicon endpoint (KuCat CDN)
     */
    fun getTertiaryFaviconUrl(url: String): String {
        val domain = extractDomain(url)
        return "https://ico.kucat.cn/get.php?url=$domain"
    }

    /**
     * Quaternary fallback favicon endpoint (Icon Horse)
     */
    fun getQuaternaryFaviconUrl(url: String): String {
        val domain = extractDomain(url)
        return "https://icon.horse/icon/$domain"
    }

    /**
     * Quinary fallback favicon endpoint (Google S2 / gstatic 128px)
     */
    fun getQuinaryFaviconUrl(url: String): String {
        val domain = extractDomain(url)
        return "https://t0.gstatic.com/faviconV2?client=SOCIAL&type=FAVICON&fallback_opts=TYPE,SIZE,URL&url=https://$domain&size=128"
    }
}

/**
 * Resolves a vector icon fallback for the website based on URL or title keywords.
 * STRICTLY NO TEXT IS USED.
 */
fun resolveCategoryVectorIcon(url: String, title: String = ""): ImageVector {
    val lowerUrl = url.lowercase()
    val lowerTitle = title.lowercase()

    return when {
        // AI & LLM
        lowerUrl.contains("chatgpt") || lowerUrl.contains("deepseek") || lowerUrl.contains("claude") ||
        lowerUrl.contains("ai") || lowerUrl.contains("gpt") || lowerTitle.contains("ai") ||
        lowerTitle.contains("智能") || lowerTitle.contains("大模型") || lowerTitle.contains("灵") || lowerTitle.contains("问") ->
            Icons.Filled.AutoAwesome

        // Video & Media
        lowerUrl.contains("video") || lowerUrl.contains("movie") || lowerUrl.contains("bilibili") ||
        lowerUrl.contains("youtube") || lowerUrl.contains("jianpian") || lowerUrl.contains("ys") ||
        lowerTitle.contains("影视") || lowerTitle.contains("电影") || lowerTitle.contains("剧") || lowerTitle.contains("短剧") ->
            Icons.Filled.Movie

        // Music & Audio
        lowerUrl.contains("music") || lowerUrl.contains("fm") || lowerUrl.contains("radio") ||
        lowerUrl.contains("mp3") || lowerTitle.contains("音乐") || lowerTitle.contains("电台") || lowerTitle.contains("歌") ->
            Icons.Filled.MusicNote

        // Books & Reading
        lowerUrl.contains("book") || lowerUrl.contains("novel") || lowerUrl.contains("read") ||
        lowerTitle.contains("书") || lowerTitle.contains("小说") || lowerTitle.contains("阅读") || lowerTitle.contains("轻小说") ->
            Icons.Filled.MenuBook

        // Gaming
        lowerUrl.contains("game") || lowerUrl.contains("steam") || lowerUrl.contains("poki") ||
        lowerTitle.contains("游戏") || lowerTitle.contains("红警") || lowerTitle.contains("switch") ->
            Icons.Filled.SportsEsports

        // Dev & Code
        lowerUrl.contains("git") || lowerUrl.contains("code") || lowerUrl.contains("dev") ||
        lowerUrl.contains("linux") || lowerTitle.contains("代码") || lowerTitle.contains("开发") || lowerTitle.contains("编程") ->
            Icons.Filled.Code

        // Search & Portals
        lowerUrl.contains("search") || lowerUrl.contains("baidu") || lowerUrl.contains("bing") ||
        lowerUrl.contains("google") || lowerTitle.contains("搜索") ->
            Icons.Filled.Search

        // Cloud & Drives
        lowerUrl.contains("pan") || lowerUrl.contains("cloud") || lowerTitle.contains("网盘") || lowerTitle.contains("云盘") ->
            Icons.Filled.CloudQueue

        // Shopping & Discounts
        lowerUrl.contains("jd") || lowerUrl.contains("meituan") || lowerUrl.contains("ele") ||
        lowerTitle.contains("红包") || lowerTitle.contains("打车") || lowerTitle.contains("特价") ->
            Icons.Filled.ShoppingBag

        // Learning & Academic
        lowerUrl.contains("paper") || lowerUrl.contains("study") || lowerUrl.contains("learn") ||
        lowerTitle.contains("论文") || lowerTitle.contains("白皮书") || lowerTitle.contains("指南") ->
            Icons.Filled.School

        // Tools
        lowerUrl.contains("tool") || lowerTitle.contains("工具") || lowerTitle.contains("助手") ->
            Icons.Filled.Build

        // Default Web
        else -> Icons.Filled.Language
    }
}

/**
 * Elegant brand tint for vector icon fallbacks.
 */
fun resolveBrandTint(url: String): Color {
    val lower = url.lowercase()
    return when {
        lower.contains("bilibili") -> Color(0xFFFB7299)
        lower.contains("youtube") -> Color(0xFFFF0000)
        lower.contains("deepseek") -> Color(0xFF0066FF)
        lower.contains("chatgpt") || lower.contains("openai") -> Color(0xFF10A37F)
        lower.contains("claude") -> Color(0xFFD97706)
        lower.contains("github") -> Color(0xFF24292F)
        lower.contains("zhihu") -> Color(0xFF0084FF)
        lower.contains("weibo") -> Color(0xFFE6162D)
        lower.contains("jd") -> Color(0xFFE1251B)
        lower.contains("meituan") -> Color(0xFFFFC300)
        lower.contains("steam") -> Color(0xFF171A21)
        lower.contains("baidu") -> Color(0xFF2932E1)
        lower.contains("google") || lower.contains("antigravity") -> Color(0xFF4285F4)
        lower.contains("cursor") -> Color(0xFF007ACC)
        lower.contains("trae") -> Color(0xFF1E88E5)
        lower.contains("lovable") -> Color(0xFFFF3366)
        lower.contains("bolt.new") -> Color(0xFFFF9900)
        lower.contains("windsurf") -> Color(0xFF00C9A7)
        lower.contains("kimi") -> Color(0xFF10A37F)
        else -> Color(0xFFE65100)
    }
}

/**
 * SiteBrandIcon automatically loads the genuine site icon (favicon) via multi-tier CDNs.
 * Optimizations:
 * 1. Base Layer: Renders brand vector icon immediately on frame 0 (0ms latency, zero delay).
 * 2. Multi-tier fast CDN failover with 2s timeout (api.iowen.cn -> cccyun -> kucat -> icon.horse -> gstatic).
 * 3. Aggressive memory + disk caching via FastFaviconImageLoader.
 */
@Composable
fun SiteBrandIcon(
    url: String,
    title: String,
    fallbackText: String = "",
    iconUrl: String = "",
    size: Dp = 34.dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageLoader = remember(context) { FastFaviconImageLoader.get(context) }

    val primaryFavicon = remember(url, iconUrl) { FaviconHelper.getPrimaryFaviconUrl(url, iconUrl) }
    val secondaryFavicon = remember(url) { FaviconHelper.getSecondaryFaviconUrl(url) }
    val tertiaryFavicon = remember(url) { FaviconHelper.getTertiaryFaviconUrl(url) }
    val quaternaryFavicon = remember(url) { FaviconHelper.getQuaternaryFaviconUrl(url) }
    val quinaryFavicon = remember(url) { FaviconHelper.getQuinaryFaviconUrl(url) }

    var loadStage by remember(url) { mutableIntStateOf(0) }
    var isImageSuccessfullyLoaded by remember(url) { androidx.compose.runtime.mutableStateOf(false) }

    val vector = remember(url, title) { resolveCategoryVectorIcon(url, title) }
    val tint = remember(url) { resolveBrandTint(url) }

    Box(
        modifier = modifier
            .size(size)
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(tint.copy(alpha = 0.12f))
            .border(
                0.8.dp,
                tint.copy(alpha = 0.28f),
                RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        // 1. Instant base layer: Always visible on Frame 0 (0ms delay perceived)
        Icon(
            imageVector = vector,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size * 0.55f)
        )

        // 2. Favicon Layer: Crossfades on top once network image arrives
        val currentTargetUrl = when (loadStage) {
            0 -> primaryFavicon
            1 -> secondaryFavicon
            2 -> tertiaryFavicon
            3 -> quaternaryFavicon
            4 -> quinaryFavicon
            else -> null
        }

        if (currentTargetUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(currentTargetUrl)
                    .crossfade(true)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .build(),
                imageLoader = imageLoader,
                contentDescription = title,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(3.dp),
                onSuccess = {
                    isImageSuccessfullyLoaded = true
                },
                onError = {
                    if (loadStage < 5) {
                        loadStage += 1
                    }
                }
            )
        }
    }
}
