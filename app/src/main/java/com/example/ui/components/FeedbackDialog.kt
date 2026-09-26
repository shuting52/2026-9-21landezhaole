package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.openQqGroup
import com.example.ui.theme.CutePeach
import com.example.ui.theme.CutePink
import com.example.ui.theme.FlameRed
import com.example.ui.theme.JadeGreen
import com.example.ui.theme.SunsetOrange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

private data class FeedbackCategoryItem(
    val title: String,
    val subtitle: String
)

private val FEEDBACK_CATEGORIES = listOf(
    FeedbackCategoryItem("🐛 软件 BUG", "异常闪退、功能报错、页面崩溃"),
    FeedbackCategoryItem("💡 功能优化", "交互体验改进、界面排版建议"),
    FeedbackCategoryItem("🎨 主题外观", "可爱主题、颜色搭配、动态效果建议"),
    FeedbackCategoryItem("🔍 资源补充", "增加新工具、扩展应用导航收录"),
    FeedbackCategoryItem("⚡ 卡顿闪退", "响应缓慢、内存占用或设备兼容"),
    FeedbackCategoryItem("❓ 其它疑问", "商务合作、交流咨询及其他反馈")
)

@Composable
fun FeedbackDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedCategory by remember { mutableStateOf(FEEDBACK_CATEGORIES[0]) }
    var feedbackContent by remember { mutableStateOf("") }
    var userContact by remember { mutableStateOf("") }
    var includeDeviceInfo by remember { mutableStateOf(true) }
    var isSending by remember { mutableStateOf(false) }

    val deviceInfoSummary = remember {
        // v1.0.1：版本号动态取自 BuildConfig，不再硬编码，避免发版后反馈信息携带旧版本号
        "应用：懒得找了 v${com.example.BuildConfig.VERSION_NAME} (code ${com.example.BuildConfig.VERSION_CODE}) | 机型：${Build.MANUFACTURER} ${Build.MODEL} | Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
    }

    val buildFullReport = {
        buildString {
            appendLine("【软件反馈】")
            appendLine("类型：${selectedCategory.title}")
            appendLine("时间：${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}")
            appendLine()
            appendLine("内容：")
            appendLine(feedbackContent.ifBlank { "（无描述）" })
            appendLine()
            appendLine("联系方式：${userContact.ifBlank { "未留" }}")
            if (includeDeviceInfo) {
                appendLine()
                appendLine("环境信息：$deviceInfoSummary")
            }
        }
    }

    val sendDirectFeedback = {
        if (feedbackContent.isBlank()) {
            Toast.makeText(context, "请先填写反馈内容", Toast.LENGTH_SHORT).show()
        } else {
            isSending = true
            coroutineScope.launch {
                val fullReport = buildFullReport()
                // 优先通过 FormSubmit 邮件服务真实送达开发者邮箱（无需用户安装邮件客户端）
                val email = "307779523@qq.com"
                val ok = withContext(Dispatchers.IO) {
                    try {
                        val client = OkHttpClient.Builder()
                            .connectTimeout(10, TimeUnit.SECONDS)
                            .readTimeout(15, TimeUnit.SECONDS)
                            .build()
                        val json = JSONObject().apply {
                            put("_subject", "【懒得找了·软件反馈】${selectedCategory.title}")
                            put("_template", "table")
                            put("_captcha", "false")
                            put("反馈类型", selectedCategory.title)
                            put("反馈内容", feedbackContent)
                            put("联系方式", userContact.ifBlank { "未留" })
                            put("设备环境", if (includeDeviceInfo) deviceInfoSummary else "未附带")
                            put("时间", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
                        }
                        val body = json.toString().toRequestBody("application/json".toMediaType())
                        val request = Request.Builder()
                            .url("https://formsubmit.co/ajax/$email")
                            .post(body)
                            .build()
                        client.newCall(request).execute().use { resp ->
                            resp.isSuccessful || resp.code == 200
                        }
                    } catch (e: Exception) {
                        false
                    }
                }
                isSending = false
                if (ok) {
                    // 本地留底
                    try {
                        val sp = context.getSharedPreferences("feedback_records", Context.MODE_PRIVATE)
                        val prev = sp.getString("history", "") ?: ""
                        sp.edit().putString("history", "$fullReport\n---\n$prev").apply()
                    } catch (_: Exception) {}
                    Toast.makeText(context, "✅ 反馈已真实送达开发者邮箱！感谢您的宝贵建议", Toast.LENGTH_LONG).show()
                    onDismiss()
                } else {
                    // 失败回退：系统邮件客户端发送
                    try {
                        val subject = Uri.encode("【懒得找了·软件反馈】${selectedCategory.title}")
                        val body = Uri.encode(fullReport)
                        val mailto = "mailto:$email?subject=$subject&body=$body"
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse(mailto))
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                        Toast.makeText(context, "网络发送失败，已打开邮件客户端代发（首次使用请在邮箱中点击确认激活）", Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "发送失败：请检查网络或安装邮箱客户端后重试", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = {
            if (!isSending) onDismiss()
        },
        modifier = modifier.fillMaxWidth(0.96f),
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
                            .clip(RoundedCornerShape(10.dp))
                            .background(Brush.linearGradient(listOf(CutePink, CutePeach))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.BugReport,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "软件反馈",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    enabled = !isSending
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "关闭", modifier = Modifier.size(20.dp))
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // 反馈类型：上下进度条式呈现
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "反馈类型",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = selectedCategory.title,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                // 上下垂直进度条容器
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        val totalSteps = FEEDBACK_CATEGORIES.size
                        val selectedIndex = FEEDBACK_CATEGORIES.indexOf(selectedCategory).coerceAtLeast(0)

                        FEEDBACK_CATEGORIES.forEachIndexed { index, item ->
                            val isSelected = index == selectedIndex
                            val isPassed = index < selectedIndex
                            val isLast = index == totalSteps - 1

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedCategory = item }
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                // 左侧：垂直进度轴与指示节点
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.width(22.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(if (isSelected) 18.dp else 14.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isSelected -> MaterialTheme.colorScheme.primary
                                                    isPassed -> JadeGreen
                                                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White)
                                            )
                                        } else if (isPassed) {
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White)
                                            )
                                        }
                                    }

                                    if (!isLast) {
                                        Box(
                                            modifier = Modifier
                                                .width(2.dp)
                                                .height(28.dp)
                                                .background(
                                                    if (index < selectedIndex) JadeGreen.copy(alpha = 0.75f)
                                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                                )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                // 右侧：类型标题与说明
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(bottom = if (isLast) 2.dp else 8.dp)
                                ) {
                                    Text(
                                        text = item.title,
                                        fontSize = 12.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(1.dp))
                                    Text(
                                        text = item.subtitle,
                                        fontSize = 10.5.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        lineHeight = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 详细描述输入框
                Text(
                    text = "反馈内容 *",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = feedbackContent,
                    onValueChange = { feedbackContent = it },
                    placeholder = {
                        Text(
                            "请描述您遇到的问题或想要的功能建议...",
                            fontSize = 12.sp
                        )
                    },
                    minLines = 4,
                    maxLines = 8,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 联系方式输入框
                Text(
                    text = "联系方式（选填）",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = userContact,
                    onValueChange = { userContact = it },
                    placeholder = { Text("QQ / 微信 / 邮箱", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 附带诊断信息勾选项
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { includeDeviceInfo = !includeDeviceInfo }
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = includeDeviceInfo,
                        onCheckedChange = { includeDeviceInfo = it },
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = "附带设备环境信息",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${Build.MANUFACTURER} ${Build.MODEL} · Android ${Build.VERSION.RELEASE}",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 反馈直达提示（真实送达开发者邮箱）
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Send, contentDescription = null, tint = CutePink, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "点击「直接发送」将真实送达开发者邮箱（307779523@qq.com），感谢您的宝贵建议！",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { sendDirectFeedback() },
                enabled = !isSending,
                colors = ButtonDefaults.buttonColors(containerColor = CutePink),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("正在发送...", fontSize = 13.sp)
                } else {
                    Icon(Icons.Filled.Send, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("直接发送", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = {
                    val fullReport = buildFullReport()
                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText("反馈信息", fullReport))
                    Toast.makeText(context, "已复制反馈文本到剪贴板！", Toast.LENGTH_SHORT).show()
                },
                enabled = !isSending,
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("复制内容", fontSize = 12.sp)
            }
        }
    )
}
