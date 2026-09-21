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
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

/**
 * 真实邮件直发：通过 QQ 邮箱 SMTP（SSL 465）把反馈发送到站长邮箱。
 * 需在「QQ邮箱 → 设置 → 账户 → 开启SMTP服务」获取授权码后填入 SMTP_AUTH_CODE。
 * 未配置或发送失败时自动回退到系统邮件客户端（收件人已预填）。
 */
object FeedbackMailer {
    // 收件人邮箱（站长）
    const val TO_EMAIL = "307779523@qq.com"
    // 发件配置：请把 SMTP_AUTH_CODE 换成你自己的 QQ 邮箱授权码（不含空格）
    const val SMTP_HOST = "smtp.qq.com"
    const val SMTP_PORT = 465
    const val FROM_EMAIL = "307779523@qq.com"
    const val SMTP_AUTH_CODE = "" // TODO: QQ邮箱设置->账户->开启SMTP服务后，填入生成的16位授权码

    /** 通过 SMTP 直发邮件；返回是否成功 */
    fun send(subject: String, body: String): Boolean {
        val authCode = SMTP_AUTH_CODE.trim()
        if (authCode.isEmpty()) return false
        var socket: SSLSocket? = null
        return try {
            val factory: SSLSocketFactory = SSLSocketFactory.getDefault()
            socket = factory.createSocket(SMTP_HOST, SMTP_PORT) as SSLSocket
            socket.soTimeout = 15000
            val reader = BufferedReader(InputStreamReader(socket.getInputStream(), Charsets.UTF_8))
            val writer = BufferedWriter(OutputStreamWriter(socket.getOutputStream(), Charsets.UTF_8))

            fun readResponse(): String = reader.readLine() ?: ""
            fun cmd(line: String): String {
                writer.write(line + "\r\n")
                writer.flush()
                return readResponse()
            }

            val welcome = readResponse()
            if (!welcome.startsWith("220")) return false

            // EHLO（QQ 邮箱需要读取多行响应直到以 250 结尾）
            writer.write("EHLO landezhaole\r\n")
            writer.flush()
            var ehlo = readResponse()
            while (ehlo.startsWith("250-")) ehlo = readResponse()
            if (!ehlo.startsWith("250")) return false

            // AUTH LOGIN
            val authReq = cmd("AUTH LOGIN")
            if (!authReq.startsWith("334")) return false
            val userResp = cmd(android.util.Base64.encodeToString(FROM_EMAIL.toByteArray(Charsets.UTF_8), android.util.Base64.NO_WRAP))
            if (!userResp.startsWith("334")) return false
            val passResp = cmd(android.util.Base64.encodeToString(authCode.toByteArray(Charsets.UTF_8), android.util.Base64.NO_WRAP))
            if (!passResp.startsWith("235")) return false

            val mailFrom = cmd("MAIL FROM:<$FROM_EMAIL>")
            if (!mailFrom.startsWith("250")) return false
            val rcptTo = cmd("RCPT TO:<$TO_EMAIL>")
            if (!rcptTo.startsWith("250")) return false

            val dataCmd = cmd("DATA")
            if (!dataCmd.startsWith("354")) return false
            val message = buildString {
                append("From: <$FROM_EMAIL>\n")
                append("To: <$TO_EMAIL>\n")
                append("Subject: =?UTF-8?B?" + android.util.Base64.encodeToString(subject.toByteArray(Charsets.UTF_8), android.util.Base64.NO_WRAP) + "?=\n")
                append("MIME-Version: 1.0\n")
                append("Content-Type: text/plain; charset=UTF-8\n")
                append("Content-Transfer-Encoding: 8bit\n")
                append("\n")
                append(body)
            }
            // 统一转换为 SMTP 要求的 CRLF 行结束符
            val finalResp = cmd(message.replace("\r\n", "\n").replace("\n", "\r\n") + "\r\n.")
            val ok = finalResp.startsWith("250")
            runCatching { cmd("QUIT") }
            ok
        } catch (e: Exception) {
            false
        } finally {
            runCatching { socket?.close() }
        }
    }
}

private data class FeedbackCategoryItem(
    val title: String,
    val subtitle: String
)

private val FEEDBACK_CATEGORIES = listOf(
    FeedbackCategoryItem("🐛 软件 BUG", "异常闪退、功能报错、页面崩溃"),
    FeedbackCategoryItem("💡 功能优化", "交互体验改进、界面排版建议"),
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
        "应用：懒得找了 v2.2.0 | 机型：${Build.MANUFACTURER} ${Build.MODEL} | Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
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
        } else if (!isSending) {
            isSending = true
            coroutineScope.launch {
                val fullReport = buildFullReport()
                val isSuccess = withContext(Dispatchers.IO) {
                    try {
                        // 真实邮件直发：SMTP 发送到站长邮箱 307779523@qq.com
                        val mailSubject = "【懒得找了·软件反馈】${selectedCategory.title}"
                        val smtpOk = FeedbackMailer.send(mailSubject, fullReport)

                        // 本地持久化留底存证
                        val sp = context.getSharedPreferences("feedback_records", Context.MODE_PRIVATE)
                        val prev = sp.getString("history", "") ?: ""
                        sp.edit().putString("history", "$fullReport\n---\n$prev").apply()

                        smtpOk
                    } catch (e: Exception) {
                        // 离线/异常时本地存证保底
                        try {
                            val sp = context.getSharedPreferences("feedback_records", Context.MODE_PRIVATE)
                            val prev = sp.getString("history", "") ?: ""
                            sp.edit().putString("history", "$fullReport\n---\n$prev").apply()
                        } catch (_: Exception) {}
                        false
                    }
                }

                isSending = false
                if (isSuccess) {
                    Toast.makeText(context, "✅ 反馈已通过邮件发送至站长邮箱，感谢支持！", Toast.LENGTH_LONG).show()
                    onDismiss()
                } else {
                    // 邮件直发失败（未配置 SMTP 授权码或网络受限）→ 打开系统邮件客户端，收件人已预填
                    openMailComposer(context, fullReport, selectedCategory.title)
                    onDismiss()
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
                            .background(Brush.linearGradient(listOf(FlameRed, SunsetOrange))),
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

                Text(
                    text = "发送后可直接到达站长邮箱，感谢您的宝贵建议！",
                    fontSize = 10.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { sendDirectFeedback() },
                enabled = !isSending,
                colors = ButtonDefaults.buttonColors(containerColor = FlameRed),
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

/**
 * 兜底通道：SMTP 直发失败时，唤起系统邮件客户端并预填收件人与正文，
 * 确保反馈一定能够送达站长邮箱（307779523@qq.com）。
 */
private fun openMailComposer(context: Context, report: String, category: String) {
    try {
        val subject = "【懒得找了·软件反馈】$category"
        val uri = Uri.parse("mailto:${FeedbackMailer.TO_EMAIL}")
        val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
            putExtra(Intent.EXTRA_EMAIL, arrayOf(FeedbackMailer.TO_EMAIL))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, report)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        Toast.makeText(context, "已唤起邮件客户端，收件人已填好，点发送即可送达站长邮箱", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        // 未安装邮件客户端：复制到剪贴板供手动发送
        try {
            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("反馈信息", report))
            Toast.makeText(context, "未找到邮件客户端，反馈内容已复制，可粘贴到任意邮箱发送至 ${FeedbackMailer.TO_EMAIL}", Toast.LENGTH_LONG).show()
        } catch (_: Exception) {}
    }
}
