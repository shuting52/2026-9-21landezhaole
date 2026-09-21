package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    modifier: Modifier = Modifier
) {
    val uiverse = LocalUiverseState.current

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
    }

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
    }

    val descColor = when (uiverse.cardStyle) {
        CardStylePreset.CYBERPUNK -> Color(0xFF94A3B8)
        CardStylePreset.NEO_BRUTALISM -> Color(0xFF334155)
        CardStylePreset.CUSTOM -> (uiverse.customStyle?.textColor ?: MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.75f)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

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
    }

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
                    size = 28.dp
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Title and description
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = card.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
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
                                fontSize = 9.5.sp,
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
                        .size(22.dp)
                        .testTag("favorite_button_${card.id}")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = if (isFavorite) "已收藏" else "加入收藏",
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else descColor.copy(alpha = 0.5f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // Ribbon Badge positioned at top right
        if (!card.badge.isNullOrBlank()) {
            RibbonBadge(
                text = card.badge,
                badgeType = card.badgeType,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-4).dp, y = (-3).dp)
            )
        }
    }
}
