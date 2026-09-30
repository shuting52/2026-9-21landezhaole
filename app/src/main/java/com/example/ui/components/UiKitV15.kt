package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

// ============================================================
// v1.0.15 全面洗牌 · Neo-Brutalism 新风格组件库
// 依据用户提供的 CSS 代码翻译为 Compose 组件：
//   .u-user 用户卡片（设置版块顶部主题）
//   .u-tab  选择栏（软件版块顶部）
//   .u-list 全局软件列表呈现
//   .u-setrow 设置行（设置版块底部）
//   .u-badge 弹窗徽标
// 风格：白底 + 粗描边(--ink) + 偏移硬阴影(--c1) + 圆角
// ============================================================

/** Neo-Brutalism 调色板（对应 CSS 变量） */
val V15Ink = Color(0xFF26303C)      // --ink  深蓝灰（边框 / 主文字）
val V15C1 = Color(0xFFFFB020)       // --c1   活力橙（主阴影 / 选中态）
val V15C2 = Color(0xFFFF5E8A)       // --c2   草莓粉（次色 / 头像描边 / 粉徽标）
val V15Bg = Color(0xFF4D96FF)       // --bg   天蓝（头像背景 / 蓝徽标）

/** 分隔线颜色（.u-tab button 右侧、.u-list row 底部） */
private val V15Divider = Color(0xFFE6EEF5)

/**
 * Neo-Brutalism 卡片容器：白底 + 4dp 粗描边 + 右下偏移硬阴影 + 圆角。
 * 对应 CSS：`background:#fff;border:4px solid var(--ink);border-radius:16px;
 *           box-shadow:.35em .35em 0 var(--c1)`
 */
@Composable
fun NeoCard(
    modifier: Modifier = Modifier,
    cornerRadius: Int = 16,
    borderWidth: Int = 4,
    shadowColor: Color = V15C1,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale = if (pressed) 0.97f else 1f
    val lift = if (pressed) 2 else 6

    Box(modifier = modifier) {
        // 硬阴影层（右下偏移）
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = lift.dp, y = lift.dp)
                .clip(shape)
                .background(shadowColor)
        )
        // 主体层
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .clip(shape)
                .background(Color.White)
                .border(borderWidth.dp, V15Ink, shape)
                .then(
                    if (onClick != null) {
                        Modifier.clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = onClick
                        )
                    } else Modifier
                )
        ) {
            content()
        }
    }
}

// ============================================================
// .u-user 用户卡片 —— 设置版块顶部主题
// CSS:
//   .u-user{display:flex;align-items:center;gap:14px;background:#fff;border:4px solid var(--ink);
//          border-radius:16px;padding:14px 16px;box-shadow:.35em .35em 0 var(--c1);
//          width:min(270px,100%);transition:all .3s}
//   .u-user:hover{transform:translateY(-4px);box-shadow:.45em .5em 0 var(--c1)}
//   .u-avatar{width:54px;height:54px;border-radius:50%;background:var(--bg);border:4px solid var(--c2);
//             color:#fff;font-weight:900;font-size:22px;...;box-shadow:.15em .15em 0 var(--c1)}
//   .u-user:hover .u-avatar{transform:rotate(15deg) scale(1.1)}
// ============================================================
@Composable
fun UUserCard(
    name: String,
    email: String,
    modifier: Modifier = Modifier,
    avatarText: String = "🐻",
    onClick: (() -> Unit)? = null
) {
    NeoCard(
        modifier = modifier.widthIn(max = 300.dp),
        onClick = onClick
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            // 头像：圆形，蓝底 + 粉描边 + 橙阴影
            val avatarInteraction = remember { MutableInteractionSource() }
            val avatarPressed by avatarInteraction.collectIsPressedAsState()
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .graphicsLayer {
                        scaleX = if (avatarPressed) 1.1f else 1f
                        scaleY = if (avatarPressed) 1.1f else 1f
                        rotationZ = if (avatarPressed) 15f else 0f
                    }
                    .offset(x = 3.dp, y = 3.dp)
                    .clip(RoundedCornerShape(50))
                    .background(V15Bg)
                    .border(4.dp, V15C2, RoundedCornerShape(50)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = avatarText,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = V15Ink
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = email,
                    fontSize = 12.sp,
                    color = Color(0xFF7A8CA0),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ============================================================
// .u-tab 选择栏 —— 软件版块顶部
// CSS:
//   .u-tab{display:flex;background:#fff;border:4px solid var(--ink);border-radius:14px;
//          overflow:hidden;box-shadow:.3em .3em 0 var(--c1)}
//   .u-tab button{...color:#7a8ca0;...;border-right:3px solid #e6eef5;...}
//   .u-tab button.on{background:var(--c1);color:var(--ink);animation:tabpulse...}
// ============================================================
@Composable
fun UTabRow(
    tabs: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val pulse = remember { Animatable(1f) }
    LaunchedEffect(selectedIndex) {
        pulse.snapTo(0.9f)
        pulse.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    }
    NeoCard(modifier = modifier, cornerRadius = 14, borderWidth = 4, shadowColor = V15C1) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            tabs.forEachIndexed { index, tabName ->
                val selected = index == selectedIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .graphicsLayer {
                            scaleX = if (selected) pulse.value else 1f
                            scaleY = if (selected) pulse.value else 1f
                        }
                        .background(if (selected) V15C1 else Color.Transparent)
                        .clickable { onSelect(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tabName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = if (selected) V15Ink else Color(0xFF7A8CA0)
                    )
                }
                if (index < tabs.size - 1) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .fillMaxHeight()
                            .background(V15Divider)
                    )
                }
            }
        }
    }
}

// ============================================================
// .u-list 全局软件列表 —— 软件版块主体
// CSS:
//   .u-list{width:min(300px,100%);background:#fff;border:4px solid var(--ink);border-radius:16px;
//           overflow:hidden;box-shadow:.35em .35em 0 var(--c1)}
//   .u-list .row{display:flex;align-items:center;gap:12px;padding:13px 16px;
//                border-bottom:3px solid #e6eef5;...}
//   .u-list .row .ic{width:40px;height:40px;border-radius:10px;background:var(--bg);color:#fff;...
//                    box-shadow:.15em .15em 0 var(--c1)}
//   .u-list .row:nth-child(2) .ic{background:var(--c2)}
//   .u-list .row:nth-child(3) .ic{background:var(--c1);color:var(--ink)}
// ============================================================
@Composable
fun UListContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    NeoCard(modifier = modifier, cornerRadius = 16, borderWidth = 4, shadowColor = V15C1) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

/**
 * 列表行：图标方块 + 标题/副标题 + 右侧内容（时间 / 徽标 / chevron）。
 * rowIndex 用于循环配色（第 2/3 行 icon 换色，对应 CSS nth-child）
 */
@Composable
fun UListRow(
    title: String,
    subtitle: String = "",
    iconText: String = "📦",
    iconUrl: String = "",
    trailing: String = "›",
    rowIndex: Int = 0,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    // CSS nth-child 配色：第 2 行粉色，第 3 行橙（主色），其余蓝色
    val iconBg = when (rowIndex) {
        1 -> V15C2
        2 -> V15C1
        else -> V15Bg
    }
    val iconFg = if (rowIndex == 2) V15Ink else Color.White
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .background(if (pressed) Color(0xFFFFF4E4) else Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 13.dp)
    ) {
        // 图标方块
        Box(
            modifier = Modifier
                .size(40.dp)
                .graphicsLayer {
                    scaleX = if (pressed) 1.1f else 1f
                    scaleY = if (pressed) 1.1f else 1f
                    rotationZ = if (pressed) 12f else 0f
                }
                .offset(x = 3.dp, y = 3.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            if (iconUrl.isNotBlank()) {
                coil.compose.AsyncImage(
                    model = iconUrl,
                    contentDescription = title,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp)
                )
            } else {
                Text(
                    text = iconText,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = iconFg
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = V15Ink,
                maxLines = 1
            )
            if (subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color(0xFF7A8CA0),
                    maxLines = 1
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        if (trailing.isNotBlank()) {
            Text(
                text = trailing,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF9AA8B6)
            )
        }
    }
    // 行底部分隔线
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(3.dp)
            .background(V15Divider)
    )
}

// ============================================================
// .u-setrow 设置行 —— 设置版块底部
// CSS:
//   .u-setrow{display:flex;align-items:center;gap:12px;background:#fff;border:4px solid var(--ink);
//             border-radius:14px;padding:13px 15px;box-shadow:.3em .3em 0 var(--c1);
//             width:min(300px,100%);...}
//   .u-setrow:hover{transform:translateX(6px);box-shadow:.4em .3em 0 var(--c1)}
//   .u-setrow .t{flex:1;font-weight:800;color:#26303c;font-size:14px}
//   .u-setrow .chev{color:#9aa8b6;font-weight:900;font-size:18px}
//   .u-setrow:hover .chev{transform:translateX(5px);color:var(--c2)}
// ============================================================
@Composable
fun USetRow(
    emoji: String,
    title: String,
    modifier: Modifier = Modifier,
    chevron: String = "›",
    onClick: () -> Unit = {}
) {
    NeoCard(
        modifier = modifier.widthIn(max = 300.dp),
        cornerRadius = 14,
        borderWidth = 4,
        shadowColor = V15C1,
        onClick = onClick
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp, vertical = 13.dp)
        ) {
            Text(
                text = emoji,
                fontSize = 22.sp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = V15Ink,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = chevron,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF9AA8B6)
            )
        }
    }
}

// ============================================================
// .u-badge 徽标 —— 弹窗内使用
// CSS:
//   .u-badge{display:inline-flex;align-items:center;gap:6px;background:#fff;border:3px solid var(--ink);
//            border-radius:30px;padding:5px 12px;font-weight:800;font-size:12px;color:var(--ink);
//            box-shadow:.18em .18em 0 var(--c1)}
//   .u-badge.pink{background:var(--c2);color:#fff}
//   .u-badge.blue{background:var(--bg);color:#fff;box-shadow:.18em .18em 0 var(--c2)}
//   .u-badge .x{cursor:pointer;opacity:.7;font-weight:900}
//   .u-badge .x:hover{opacity:1;transform:scale(1.3) rotate(90deg)}
// ============================================================
enum class UBadgeVariant { DEFAULT, PINK, BLUE }

@Composable
fun UBadge(
    text: String,
    variant: UBadgeVariant = UBadgeVariant.DEFAULT,
    modifier: Modifier = Modifier,
    onClose: (() -> Unit)? = null
) {
    val bg = when (variant) {
        UBadgeVariant.DEFAULT -> Color.White
        UBadgeVariant.PINK -> V15C2
        UBadgeVariant.BLUE -> V15Bg
    }
    val fg = when (variant) {
        UBadgeVariant.DEFAULT -> V15Ink
        UBadgeVariant.PINK, UBadgeVariant.BLUE -> Color.White
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(30.dp))
            .background(bg)
            .border(3.dp, V15Ink, RoundedCornerShape(30.dp))
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        // 背景偏移阴影：包一层即可
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            color = fg,
            modifier = Modifier
                .graphicsLayer { shadowElevation = 2f }
                .offset(x = 2.dp, y = 2.dp)
        )
        if (onClose != null) {
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "✕",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = fg.copy(alpha = 0.7f),
                modifier = Modifier
                    .clickable(onClick = onClose)
                    .padding(2.dp)
            )
        }
    }
}

// ============================================================
// 新版本提示弹窗（v1.0.15 取代旧更新弹窗）
// 云端检测到新版本 → 弹窗告知："有新版本更新，请退出软件。重新进入即可完成更新"
// 不再下载 / 不再安装 / 不再强制更新 —— 内容实时同步，退出重进即呈现最新版本
// ============================================================
@Composable
fun NewVersionPromptDialog(
    versionName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x88000000))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onDismiss() }
                ),
            contentAlignment = Alignment.Center
        ) {
            NeoCard(
                modifier = Modifier
                    .widthIn(min = 280.dp, max = 320.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                cornerRadius = 18,
                borderWidth = 4,
                shadowColor = V15C1,
                // 消费点击，防止点击卡片内部时误触背景关闭
                onClick = {}
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 22.dp)
                ) {
                    // u-badge 徽标：「? 新版本」
                    UBadge(
                        text = "✨ 新版本",
                        variant = UBadgeVariant.PINK
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "有新版本更新",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = V15Ink
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "请退出软件，重新进入即可完成更新",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF26303C),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    UBadge(
                        text = versionName,
                        variant = UBadgeVariant.BLUE
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    // 确认按钮：橙底粗描边（u-tab on 同款风格）
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(V15C1)
                            .border(3.dp, V15Ink, RoundedCornerShape(12.dp))
                            .clickable(onClick = onConfirm)
                            .padding(horizontal = 36.dp, vertical = 11.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "我知道了",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = V15Ink
                        )
                    }
                }
            }
        }
    }
}
