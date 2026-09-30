package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FlameRed
import com.example.ui.theme.SunsetOrange

const val APP_SHARE_TEXT = """【宝藏神器推荐】《懒得找了》极简综合资源导航
🚀 聚合海量AI大模型、无限画布设计、开发者工具、影视影视直达
✨ 纯净无广，无需注册，开箱即用，拒绝无效搜索！
推荐给你，找资源不求人！"""

/**
 * Third-party sharing dialog for SettingsScreen.
 * Provides targeted third-party shortcuts (WeChat, QQ) and the universal Android system share chooser.
 */
@Composable
fun ShareSoftwareDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.fillMaxWidth(0.95f),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF2E7D32).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "分享「懒得找了」",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = "关闭")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "好工具值得与身边好友一同分享，点击下方图标直达第三方应用",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // App Preview Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Brush.linearGradient(listOf(FlameRed, SunsetOrange))),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "懒",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "懒得找了 · 极简资源导航",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "纯净无广 · 聚合宝藏 · 开箱即用",
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "纯净无广 · 聚合宝藏 · 开箱即用",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Third-Party Platform Share Action Buttons
                Text(
                    text = "选择分享渠道",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ShareChannelItem(
                        icon = Icons.Filled.Chat,
                        label = "微信好友",
                        bgColor = Color(0xFF07C160),
                        onClick = {
                            shareToThirdPartyApp(context, "com.tencent.mm", APP_SHARE_TEXT)
                            onDismiss()
                        }
                    )

                    ShareChannelItem(
                        icon = Icons.Filled.Forum,
                        label = "QQ好友",
                        bgColor = Color(0xFF12B7F5),
                        onClick = {
                            shareToThirdPartyApp(context, "com.tencent.mobileqq", APP_SHARE_TEXT)
                            onDismiss()
                        }
                    )

                    ShareChannelItem(
                        icon = Icons.Filled.Share,
                        label = "系统分享",
                        bgColor = Color(0xFF673AB7),
                        onClick = {
                            openSystemShare(context, APP_SHARE_TEXT)
                            onDismiss()
                        }
                    )

                    ShareChannelItem(
                        icon = Icons.Filled.ContentCopy,
                        label = "复制文案",
                        bgColor = Color(0xFFFF9800),
                        onClick = {
                            copyShareText(context, APP_SHARE_TEXT)
                            onDismiss()
                        }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    openSystemShare(context, APP_SHARE_TEXT)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("一键系统分享", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun ShareChannelItem(
    icon: ImageVector,
    label: String,
    bgColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 11.5.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Universal Android System Share Sheet to support all installed third-party apps
 */
fun openSystemShare(context: Context, text: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        putExtra(Intent.EXTRA_SUBJECT, "推荐宝藏神器：《懒得找了》")
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "分享《懒得找了》至第三方平台")
    shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        context.startActivity(shareIntent)
    } catch (_: Exception) {
        Toast.makeText(context, "未能唤起系统分享面板", Toast.LENGTH_SHORT).show()
    }
}

/**
 * Attempts direct share to targeted package (e.g. WeChat, QQ) with automatic fallback to system share
 */
fun shareToThirdPartyApp(context: Context, packageName: String, text: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        putExtra(Intent.EXTRA_SUBJECT, "推荐宝藏神器：《懒得找了》")
        type = "text/plain"
        `package` = packageName
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(sendIntent)
    } catch (_: Exception) {
        // Fallback to system share chooser if targeted app isn't installed
        openSystemShare(context, text)
    }
}

/**
 * Copies the share message to system clipboard
 */
fun copyShareText(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("懒得找了分享推荐", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "分享文案已成功复制到剪贴板！可直接粘贴至任意社交平台", Toast.LENGTH_SHORT).show()
}
