package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.data.model.NavCard
import com.example.ui.theme.FlameRed
import com.example.ui.theme.LocalUiverseState
import com.example.ui.theme.SunsetOrange
import com.example.ui.uiverse.CardStylePreset

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ResourceCard(
    card: NavCard,
    isFavorite: Boolean,
    onCardClick: (NavCard) -> Unit,
    onFavoriteToggle: (NavCard) -> Unit,
    onCardLongClick: ((NavCard) -> Unit)? = null,
    modifier: Modifier = Modifier,
    // v1.1.4 主题分支：组件级主题定制（componentThemes）真正生效
    componentThemes: Map<String, String> = emptyMap()
) {
    val uiverse = LocalUiverseState.current

    // v1.1.4：若用户在主题切换中定制了「站点卡片」组件，则覆盖默认 cardStyle
    val cardCompStyle = ComponentThemeResolver.resolve(componentThemes, "card_item")

    val cardShape = when (uiverse.cardStyle) {
        CardStylePreset.CYBERPUNK -> RoundedCornerShape(topStart = 0.dp, topEnd = 14.dp, bottomEnd = 0.dp, bottomStart = 14.dp)
        CardStylePreset.GLASSMORPHISM -> RoundedCornerShape(16.dp)
        CardStylePreset.NEUMORPHISM -> RoundedCornerShape(16.dp)
        CardStylePreset.NEO_BRUTALISM -> RoundedCornerShape(6.dp)
        CardStylePreset.RETRO_PIXEL -> RoundedCornerShape(2.dp)
        CardStylePreset.HOLOGRAPHIC -> RoundedCornerShape(14.dp)
        CardStylePreset.LUXURY_GOLD -> RoundedCornerShape(12.dp)
        CardStylePreset.CUSTOM -> RoundedCornerShape(uiverse.customStyle?.cornerRadius ?: 10.dp)
        else -> RoundedCornerShape(10.dp)
    }.let { if (cardCompStyle != null) RoundedCornerShape(cardCompStyle.cornerRadius) else it }

    val cardElevation = when (uiverse.cardStyle) {
        CardStylePreset.CYBERPUNK -> 4.dp
        CardStylePreset.GLASSMORPHISM -> 6.dp
        CardStylePreset.NEUMORPHISM -> 4.dp
        CardStylePreset.NEO_BRUTALISM -> 4.dp
        CardStylePreset.RETRO_PIXEL -> 3.dp
        CardStylePreset.HOLOGRAPHIC -> 5.dp
        CardStylePreset.LUXURY_GOLD -> 4.dp
        CardStylePreset.CUSTOM -> uiverse.customStyle?.shadowElevation ?: 1.dp
        else -> 1.dp
    }

    val shadowSpotColor = when (uiverse.cardStyle) {
        CardStylePreset.CYBERPUNK -> Color(0xFF00F0FF)
        CardStylePreset.GLASSMORPHISM -> Color(0xFF6366F1)
        CardStylePreset.NEO_BRUTALISM -> Color.Black
        CardStylePreset.RETRO_PIXEL -> Color.Black
        CardStylePreset.LUXURY_GOLD -> Color(0xFFD4AF37)
        CardStylePreset.CUSTOM -> uiverse.customStyle?.shadowColor ?: Color.Black
        else -> Color.Black
    }

    val titleColor = when (uiverse.cardStyle) {
        CardStylePreset.CYBERPUNK -> Color(0xFF00F0FF)
        CardStylePreset.GLASSMORPHISM -> Color(0xFF1E293B)
        CardStylePreset.NEUMORPHISM -> Color(0xFF1E293B)
        CardStylePreset.NEO_BRUTALISM -> Color.Black
        CardStylePreset.RETRO_PIXEL -> Color(0xFFE94560)
        CardStylePreset.LUXURY_GOLD -> Color(0xFFD4AF37)
        CardStylePreset.CUSTOM -> uiverse.customStyle?.textColor ?: MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurface
    }.let { if (compText != null) compText else it }

    val descColor = when (uiverse.cardStyle) {
        CardStylePreset.CYBERPUNK -> Color(0xFF94A3B8)
        CardStylePreset.NEO_BRUTALISM -> Color(0xFF334155)
        CardStylePreset.CUSTOM -> (uiverse.customStyle?.textColor ?: MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.75f)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    // v1.1.4 主题分支：组件定制覆盖（卡片背景/文字色由 componentThemes 决定时优先）
    val compBg = cardCompStyle?.backgroundColor
    val compText = cardCompStyle?.textColor

    val cardBgModifier: Modifier = when (uiverse.cardStyle) {
        CardStylePreset.CYBERPUNK -> Modifier.background(Color(0xFF0F101A))
        CardStylePreset.GLASSMORPHISM -> Modifier.background(Color(0xE6FFFFFF))
        CardStylePreset.NEUMORPHISM -> Modifier.background(Color(0xFFF1F5F9))
        CardStylePreset.NEO_BRUTALISM -> Modifier.background(Color(0xFFFFFFFF))
        CardStylePreset.RETRO_PIXEL -> Modifier.background(Color(0xFF1A1A2E))
        CardStylePreset.HOLOGRAPHIC -> Modifier.background(
            Brush.linearGradient(listOf(Color(0xFFFAF5FF), Color(0xFFF0FDF4)))
        )
        CardStylePreset.LUXURY_GOLD -> Modifier.background(Color(0xFF1A1A1A))
        CardStylePreset.CUSTOM -> {
            val custom = uiverse.customStyle
            val bgBrush = custom?.backgroundBrush
            val bgColor = custom?.backgroundColor
            when {
                bgBrush != null -> Modifier.background(bgBrush)
                bgColor != null -> Modifier.background(bgColor)
                else -> Modifier.background(MaterialTheme.colorScheme.surface)
            }
        }
        else -> Modifier.background(MaterialTheme.colorScheme.surface)
    }.let { if (compBg != null) it.then(Modifier.background(compBg)) else it }

    val cardBorderModifier: Modifier = when (uiverse.cardStyle) {
        CardStylePreset.CYBERPUNK -> Modifier.border(1.5.dp, Color(0xFF00F0FF), cardShape)
        CardStylePreset.GLASSMORPHISM -> Modifier.border(
            1.2.dp,
            Brush.linearGradient(listOf(Color(0x88FFFFFF), Color(0x44A855F7), Color(0x336366F1))),
            cardShape
        )
        CardStylePreset.NEUMORPHISM -> Modifier.border(1.dp, Color(0xFFCBD5E1), cardShape)
        CardStylePreset.NEO_BRUTALISM -> Modifier.border(2.5.dp, Color(0xFF000000), cardShape)
        CardStylePreset.RETRO_PIXEL -> Modifier.border(2.dp, Color(0xFFE94560), cardShape)
        CardStylePreset.HOLOGRAPHIC -> Modifier.border(
            1.5.dp,
            Brush.linearGradient(listOf(Color(0xFFA855F7), Color(0xFF06B6D4))),
            cardShape
        )
        CardStylePreset.LUXURY_GOLD -> Modifier.border(1.2.dp, Color(0xFFD4AF37), cardShape)
        CardStylePreset.CUSTOM -> {
            val custom = uiverse.customStyle
            val customBorder = custom?.borderColor
            val bColor = if (customBorder != null && customBorder != Color.Transparent) customBorder else MaterialTheme.colorScheme.outline
            val bWidth = custom?.borderWidth ?: 1.dp
            Modifier.border(bWidth, bColor, cardShape)
        }
        else -> Modifier.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), cardShape)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 4.dp, end = 2.dp)
            .testTag("resource_card_${card.id}")
    ) {
        // Main Card Body
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = cardElevation,
                    shape = cardShape,
                    spotColor = shadowSpotColor,
                    ambientColor = shadowSpotColor.copy(alpha = 0.2f)
                )
                .clip(cardShape)
                .then(cardBgModifier)
                .then(cardBorderModifier)
                .combinedClickable(
                    onClick = { onCardClick(card) },
                    onLongClick = { onCardLongClick?.invoke(card) }
                )
                .padding(horizontal = 7.dp, vertical = 7.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Automatic Site Favicon Icon (Automatic fetch, strictly zero text fallback)
                SiteBrandIcon(
                    url = card.url,
                    title = card.title,
                    fallbackText = card.fallbackText,
                    iconUrl = card.icon,
                    size = 24.dp
                )

                Spacer(modifier = Modifier.width(4.dp))

                // Title and description（三排布局下保证站点名称完整可见）
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = card.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = titleColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (card.desc.isNotBlank()) {
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = card.desc,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 8.5.sp,
                                color = descColor
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Favorite Toggle Button
                IconButton(
                    onClick = { onFavoriteToggle(card) },
                    modifier = Modifier
                        .size(20.dp)
                        .testTag("favorite_button_${card.id}")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = if (isFavorite) "已收藏" else "加入收藏",
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else descColor.copy(alpha = 0.5f),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }

        // v1.0.19 角标修复：遵循原 RibbonBadge 动态设计，且不遮挡站点内容。
        // 角标统一骑跨在卡片右上角边框（向上偏移，主体在卡片外），不再压住收藏按钮/标题/描述；
        // 公司角标同样呈现动态流光扫过 + 呼吸缩放 + 摇摆。
        val company = detectCompanyBadge(card)
        if (company != null) {
            // 动画值在 Composable 上下文计算（graphicsLayer/Brush 参数均为非 Composable 上下文）
            val flowOffset = badgeFlow()
            val breathe = badgeBreathe()
            val wobble = badgeWobble()
            // 检测到公司：动态流光胶囊角标（不叠加、不遮挡内容）
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 8.dp, y = (-9).dp)
                    .graphicsLayer {
                        scaleX = breathe
                        scaleY = breathe
                        rotationZ = wobble
                    }
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                company.color,
                                company.color.copy(alpha = 0.72f),
                                company.color
                            ),
                            start = Offset(flowOffset * 120f, 0f),
                            end = Offset(flowOffset * 120f + 80f, 0f)
                        )
                    )
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 品牌色圆点
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.95f))
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = company.name,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        maxLines = 1
                    )
                }
            }
        } else if (!card.badge.isNullOrBlank()) {
            // 未识别到公司：保留原 RibbonBadge（动态，骑跨右上角不遮挡）
            RibbonBadge(
                text = card.badge,
                badgeType = card.badgeType,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 10.dp, y = (-8).dp)
            )
        }
    }
}

// ============================================================
// v1.0.19 角标动态特效（沿用原 RibbonBadge 的 CSS keyframes 动态设计）
// 流光扫过 + 呼吸缩放 + 摇摆，所有角标统一动态呈现，不遮挡站点内容
// ============================================================
@Composable
private fun badgeFlow(): Float {
    val transition = rememberInfiniteTransition(label = "company_badge_flow")
    val flow by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "company_badge_flow_v"
    )
    return flow
}

@Composable
private fun badgeBreathe(): Float {
    val transition = rememberInfiniteTransition(label = "company_badge_breathe")
    val s by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "company_badge_breathe_v"
    )
    return s
}

@Composable
private fun badgeWobble(): Float {
    val transition = rememberInfiniteTransition(label = "company_badge_wobble")
    val w by transition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "company_badge_wobble_v"
    )
    return w
}
