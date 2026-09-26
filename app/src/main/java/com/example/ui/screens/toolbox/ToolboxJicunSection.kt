package com.example.ui.screens.toolbox

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * v1.9.0：即存（jicun）解析下载入口工具
 *
 * 「即存」是一款视频/图文解析下载 App（Flutter 独立应用，包名 com.videofix.jicun）。
 * 本工具作为工具箱入口：
 *  - 已安装即存 → 点击直接打开，粘贴链接解析下载
 *  - 未安装 → 引导下载官方 Release APK
 *
 * 支持解析：抖音/快手/微信视频号/公众号/小红书/豆包/头条/B站/微博/西瓜/红果短剧等。
 */
@Composable
fun JicunScreenView() {
    val context = LocalContext.current
    var installed by remember { mutableStateOf(isJicunInstalled(context)) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            NewToolHeader(
                title = "即存 · 视频解析下载",
                icon = Icons.Filled.Movie,
                desc = "粘贴分享链接 → 预览 → 挑清晰度 → 下载到相册（抖音/快手/小红书/B站/视频号…）"
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.60f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.70f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "支持解析的平台",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "抖音 · 快手 · 微信视频号 · 公众号 · 小红书 · 豆包 · 今日头条 · 哔哩哔哩 · 微博 · 西瓜视频 · 好看视频 · 央视频 · 皮皮虾 · 红果短剧 · 汽水音乐",
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (installed) Color(0xFF22C55E).copy(alpha = 0.12f)
                    else Color(0xFFF59E0B).copy(alpha = 0.12f)
                ),
                border = BorderStroke(
                    1.dp,
                    (if (installed) Color(0xFF22C55E) else Color(0xFFF59E0B)).copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (installed) "? 即存已安装" else "? 即存未安装",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = if (installed) Color(0xFF15803D) else Color(0xFFB45309)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (installed)
                            "点击下方按钮直接打开即存，粘贴分享链接即可解析下载。"
                        else
                            "需要先安装即存 App（官方 3.0.7 版，约 24MB），安装后即可粘贴链接解析下载。",
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (installed) {
                        Button(
                            onClick = {
                                if (!launchJicun(context)) {
                                    installed = isJicunInstalled(context)
                                    Toast.makeText(context, "打开失败，请手动打开即存", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("打开即存", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = { openJicunDownload(context) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("下载即存 App", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { installed = isJicunInstalled(context) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("我已安装，检查一下", fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "提示：即存为独立的第三方 App，「懒得找了」仅提供入口引导，实际解析能力以即存自身为准。",
                fontSize = 10.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/** 是否已安装即存（com.videofix.jicun） */
private fun isJicunInstalled(context: Context): Boolean {
    return try {
        context.packageManager.getPackageInfo("com.videofix.jicun", 0) != null
    } catch (e: Exception) {
        false
    }
}

/** 打开即存 App（返回是否成功拉起） */
private fun launchJicun(context: Context): Boolean {
    return try {
        val launch = context.packageManager.getLaunchIntentForPackage("com.videofix.jicun")
        if (launch != null) {
            context.startActivity(launch)
            true
        } else {
            // 无默认入口 activity 时直接尝试显式包名
            val intent = Intent(Intent.ACTION_MAIN).apply {
                setPackage("com.videofix.jicun")
                addCategory(Intent.CATEGORY_LAUNCHER)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        }
    } catch (e: ActivityNotFoundException) {
        false
    } catch (e: Exception) {
        false
    }
}

/** 打开即存官方 Release 下载页（浏览器） */
private fun openJicunDownload(context: Context) {
    try {
        val url = "https://github.com/dhvbjvvb/jicun/releases/latest"
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    } catch (e: Exception) {
        Toast.makeText(context, "无法打开下载页，请到 GitHub 搜索 jicun", Toast.LENGTH_LONG).show()
    }
}