package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import com.example.data.remote.UpdateDialogDto
import com.example.data.remote.VersionDto
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.example.ui.components.AppRatingDialog
import com.example.ui.components.AppUpdateDialog
import com.example.ui.components.OfficialWebsiteDialog
import com.example.ui.components.ShareSoftwareDialog
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FlameRed
import com.example.ui.theme.SunsetOrange
import com.example.ui.theme.ThemePreset
import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.SupportAgent
import com.example.ui.components.FeedbackDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.text.style.TextAlign

const val OFFICIAL_QQ_GROUP_URL =
    "https://qun.qq.com/universal-share/share?ac=1&authKey=gtnBoTi8HEzXQAF9x40Y5GYQtubkWu4pGDJg7OuNQte9oz3sXiFonGqZaUXxjffu&busi_data=eyJncm91cENvZGUiOiI0MzkyMTEzNDciLCJ0b2tlbiI6IkVxeXJDb0tyVjM3Y0VIRmhZQ3M5eDg4VW5MYWU0RW4ybVlSRlBlS2ozQXRxanB5V2ZtNzNHMlRIa2ZRd0VTQnUiLCJ1aW4iOiIzMDc3Nzk1MjMifQ%3D%3D&data=QnUzn164u21Cu1dG7vAVYJqU_4hw0COArsGrrBOIc0vxu7ES6gOJcYyrpu2JgkVs-y3X0ZUGZb_nPBJsBTRccQ&svctype=4&tempid=h5_group_info"

@Composable
fun SettingsScreen(
    currentTheme: ThemePreset,
    onOpenThemeSwitcher: () -> Unit,
    cloudUpdate: UpdateDialogDto? = null,
    cloudVersion: VersionDto? = null,
    onCheckUpdate: (suspend () -> Pair<Boolean, VersionDto?>)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var activeDialogType by remember { mutableStateOf<String?>(null) }
    var isCheckingUpdate by remember { mutableStateOf(false) }

    // 云端新版本检测：云端 versionCode 大于本地时视为有新版本
    val hasNewCloudVersion = (cloudVersion?.code ?: 0) > com.example.BuildConfig.VERSION_CODE

    // 自动检测：进入设置页无需手动点击，自动获取云端仓库最新版本状态并实时刷新
    LaunchedEffect(Unit) {
        onCheckUpdate?.invoke()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("settings_screen")
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Brush.linearGradient(listOf(FlameRed, SunsetOrange))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "系统与服务设置",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "官方社群 · 主题外观 · 协议条款与关于",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Section 1: Appearance & Community
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(14.dp),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)) {
                // Theme Switcher
                SettingsClickableItem(
                    title = "主题切换",
                    icon = Icons.Filled.Palette,
                    iconColor = currentTheme.primaryColor,
                    onClick = onOpenThemeSwitcher
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Contact Author (联系作者)
                SettingsClickableItem(
                    title = "联系作者",
                    icon = Icons.Filled.SupportAgent,
                    iconColor = FlameRed,
                    onClick = { activeDialogType = "contact_author" }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // 软件反馈
                SettingsClickableItem(
                    title = "软件反馈",
                    icon = Icons.Filled.BugReport,
                    iconColor = SunsetOrange,
                    onClick = { activeDialogType = "feedback_bug" }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // 官方交流群 (已恢复)
                SettingsClickableItem(
                    title = "官方交流群",
                    icon = Icons.Filled.Group,
                    iconColor = Color(0xFF1976D2),
                    onClick = { openQqGroup(context) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // 官方网站 (待定中)
                SettingsClickableItem(
                    title = "官方网站",
                    icon = Icons.Filled.Language,
                    iconColor = Color(0xFF00897B),
                    onClick = { activeDialogType = "official_website" }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // 应用评分 (Uiverse 5星好评)
                SettingsClickableItem(
                    title = "应用评分",
                    icon = Icons.Filled.Star,
                    iconColor = Color(0xFFFFC73A),
                    onClick = { activeDialogType = "rating" }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // 分享软件 (分享到微信、QQ及其他第三方平台)
                SettingsClickableItem(
                    title = "分享软件",
                    icon = Icons.Filled.Share,
                    iconColor = Color(0xFF2E7D32),
                    onClick = { activeDialogType = "share_software" }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section 2: Policies and About Us
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(14.dp),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)) {
                // 检查更新（云端自动检测，有新版本时展示提示并一键直达更新下载）
                SettingsClickableItem(
                    title = when {
                        isCheckingUpdate -> "正在检测云端新版本…"
                        hasNewCloudVersion -> "发现新版本 v${cloudVersion?.name ?: ""}"
                        else -> "检查更新"
                    },
                    subtitle = when {
                        isCheckingUpdate -> "连接云端同步中…"
                        hasNewCloudVersion -> "当前版本 v${com.example.BuildConfig.VERSION_NAME} → 有新版本可更新，点击查看下载"
                        else -> "当前版本 v${com.example.BuildConfig.VERSION_NAME} (code:${com.example.BuildConfig.VERSION_CODE})"
                    },
                    icon = Icons.Filled.RocketLaunch,
                    iconColor = if (hasNewCloudVersion) Color(0xFF34C759) else Color(0xFF6C63FF),
                    onClick = {
                        if (isCheckingUpdate) return@SettingsClickableItem
                        if (onCheckUpdate != null) {
                            coroutineScope.launch {
                                isCheckingUpdate = true
                                Toast.makeText(context, "正在连接云端仓库检测最新版本…", Toast.LENGTH_SHORT).show()
                                val (hasNew, ver) = onCheckUpdate()
                                isCheckingUpdate = false
                                if (hasNew) {
                                    activeDialogType = "update"
                                } else {
                                    Toast.makeText(context, "当前已是最新版本 (v${ver?.name ?: com.example.BuildConfig.VERSION_NAME})！", Toast.LENGTH_SHORT).show()
                                    activeDialogType = "update"
                                }
                            }
                        } else {
                            activeDialogType = "update"
                        }
                    }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                SettingsClickableItem(
                    title = "关于我们",
                    icon = Icons.Filled.Info,
                    iconColor = SunsetOrange,
                    onClick = { activeDialogType = "about" }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                SettingsClickableItem(
                    title = "用户协议",
                    icon = Icons.Filled.Policy,
                    iconColor = Color(0xFF1976D2),
                    onClick = { activeDialogType = "terms" }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                SettingsClickableItem(
                    title = "隐私政策",
                    icon = Icons.Filled.Lock,
                    iconColor = Color(0xFF388E3C),
                    onClick = { activeDialogType = "privacy" }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                SettingsClickableItem(
                    title = "儿童隐私政策",
                    icon = Icons.Filled.ChildCare,
                    iconColor = Color(0xFFE91E63),
                    onClick = { activeDialogType = "child_privacy" }
                )
            }
        }

        Spacer(modifier = Modifier.height(26.dp))

        // Footer Brand Info with Typewriter Effect
        com.example.ui.components.TypewriterFooter()
    }

    // Modal Dialogs for Policies & About Us
    when (activeDialogType) {
        "feedback_bug" -> {
            FeedbackDialog(onDismiss = { activeDialogType = null })
        }
        "update" -> {
            AppUpdateDialog(
                onDismiss = { activeDialogType = null },
                versionName = "v${cloudVersion?.name ?: "2.0.0"}",
                onUpdateFinished = {
                    Toast.makeText(context, "已成功升级至最新版本 v${cloudVersion?.name ?: "2.0.0"}！", Toast.LENGTH_SHORT).show()
                },
                update = cloudUpdate,
                apkUrl = cloudVersion?.apkUrl?.ifBlank { null },
                forceUpdate = cloudVersion?.force == true
            )
        }
        "official_website" -> {
            OfficialWebsiteDialog(onDismiss = { activeDialogType = null })
        }
        "rating" -> {
            AppRatingDialog(onDismiss = { activeDialogType = null })
        }
        "share_software" -> {
            ShareSoftwareDialog(onDismiss = { activeDialogType = null })
        }
        "about" -> {
            AlertDialog(
                onDismissRequest = { activeDialogType = null },
                title = { Text("关于「懒得找了」", fontWeight = FontWeight.Black) },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text(
                            text = "版本：v2.2.0\n\n" +
                                    "「懒得找了」致力于打造一个纯净、聚合、高效的资源与工具导航平台。让大家不用再到处求资源、不用忍受满屏广告垃圾，一键直达互联网精品！\n\n" +
                                    "✨ 我们的初心与承诺：\n" +
                                    "· 纯净体验：无任何强制广告流，启动极速，开箱即用\n" +
                                    "· 隐私安全：核心浏览与收藏数据皆存放于设备本地加密空间，不上传个人隐私\n" +
                                    "· 开放生态：支持作者自主发布分享优质工具，与广大互联网爱好者共同成长\n\n" +
                                    "感谢大家一路以来的支持与反馈，我们会持续迭代，把更多真正实用的宝藏工具带给每一位朋友。",
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = { activeDialogType = null }) {
                        Text("知道了")
                    }
                }
            )
        }
        "terms" -> {
            AlertDialog(
                onDismissRequest = { activeDialogType = null },
                title = { Text("用户服务协议", fontWeight = FontWeight.Black) },
                text = {
                    Column(
                        modifier = Modifier
                            .height(380.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "【引言与总则】\n" +
                                    "欢迎您使用「懒得找了」应用及相关服务！在您使用本软件前，请务必审慎阅读、充分理解本协议各条款内容。当您开始使用本应用，即视为您已充分理解并同意接受本协议的全部约定。\n\n" +
                                    "第一条：服务内容与形式\n" +
                                    "1.1 本应用为一款综合型资源索引与效率工具集合体，提供聚合搜索、分类直达、实用工具箱、作者资源分享及本地文件管理等功能。\n" +
                                    "1.2 本应用坚持免强制注册登录机制，绝大多数功能均可本地离线或免密无感畅享。\n\n" +
                                    "第二条：知识产权与免责声明\n" +
                                    "2.1 本应用所收录展示的第三方网站、产品链接及商标标识，其知识产权均归各原始权利人所有。本应用仅提供超链接导航服务，不对第三方站点的真实性、有效性、安全性承担连带保证责任。\n" +
                                    "2.2 当您点击导航直达跳转至外部第三方网站时，请严格遵守外部网站的使用协议并提高安全防范意识，注意保护个人财产与账号密码安全。\n\n" +
                                    "第三条：用户行为与自主发布规范\n" +
                                    "3.1 用户使用本应用（包括作者上传软件与Skill技能模块）时，必须遵守中华人民共和国相关法律法规，不得利用本应用从事任何违法违规行为。\n" +
                                    "3.2 严禁上传含有病毒、木马、恶意扣费、侵犯他人隐私或侵犯知识产权的文件及链接。平台有权对违规内容立即执行下架、清除或限制访问。\n\n" +
                                    "第四条：免责与不可抗力\n" +
                                    "4.1 因互联网网络波动、通信运营商故障、黑客攻击、系统维护或不可抗力导致的服务中断或延迟，平台将在第一时间内全力修复，但不承担由此引起的间接损失。\n\n" +
                                    "第五条：协议修改与终止\n" +
                                    "5.1 平台有权根据法律法规变化或业务运营需要对本协议进行修订，修改后的协议将在应用内及时公布更新。",
                            fontSize = 12.5.sp,
                            lineHeight = 19.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = { activeDialogType = null }) { Text("我已阅读并同意") }
                }
            )
        }
        "privacy" -> {
            AlertDialog(
                onDismissRequest = { activeDialogType = null },
                title = { Text("隐私政策与数据保护准则", fontWeight = FontWeight.Black) },
                text = {
                    Column(
                        modifier = Modifier
                            .height(380.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "【引言与承诺】\n" +
                                    "「懒得找了」深知个人信息安全对您的重要性。我们始终恪守“最小必要”、“存储本地化”、“安全透明”原则，致力于为您提供无干扰、无追踪的纯净数字环境。\n\n" +
                                    "第一条：我们收集与处理的信息\n" +
                                    "1.1 本地偏好数据：您的主题选择、深浅色模式、每日使用习惯记录完全保存在您本地手机的 SQLite / Room 沙盒中，绝不上报云端服务器。\n" +
                                    "1.2 收藏夹与浏览记录：您收藏的网站或历史点击记录均属于设备本地私有数据，不经过任何后台远程统计，您可以随时在应用内一键清除。\n" +
                                    "1.3 IP实时监控定位信息：主界面顶部展示的 IP 归属地数据，仅通过公开接口（如 myip.ipip.net）请求当前客户端的出网 IP 及大致城市，仅用于客户端本地呈现网络健康状况，服务器端不予持久化存储或归档。\n\n" +
                                    "第二条：权限调用与使用声明\n" +
                                    "2.1 网络访问权限（INTERNET）：仅用于加载导航列表、网络健康监测以及打开外部链接。\n" +
                                    "2.2 文档/文件选择器：当您使用「本地上传APK」或「本地上传Skill技能包（ZIP/MD）」时，系统仅调用 Android 系统原生的文件选择器（Storage Access Framework），我们仅读取您主动授权选中的单个文件元信息，不会扫描或遍历您的私有相册与手机存储。\n\n" +
                                    "第三条：第三方服务与SDK声明\n" +
                                    "3.1 本应用不接入任何弹窗广告SDK、个性化广告追踪SDK或后台常驻定位SDK，杜绝隐私泄露风险。\n\n" +
                                    "第四条：用户权利与自主控制\n" +
                                    "4.1 随时删除：您可以随时在设置或各个模块中删除您添加的本地资源与自定义代码主题，数据即刻在本地被永久擦除。\n\n" +
                                    "第五条：政策更新与联系方式\n" +
                                    "5.1 我们会根据业务发展更新隐私政策，重大变更将通过应用内公告方式予以公示。",
                            fontSize = 12.5.sp,
                            lineHeight = 19.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = { activeDialogType = null }) { Text("我已充分理解") }
                }
            )
        }
        "child_privacy" -> {
            AlertDialog(
                onDismissRequest = { activeDialogType = null },
                title = { Text("儿童及未成年人隐私守护政策", fontWeight = FontWeight.Black) },
                text = {
                    Column(
                        modifier = Modifier
                            .height(380.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "【未成年人特殊关怀声明】\n" +
                                    "「懒得找了」高度重视并积极履行对未成年人及不满十四周岁儿童的个人信息安全保护义务。本政策旨在说明我们如何守护青少年的健康用网与隐私安全。\n\n" +
                                    "第一条：监护人指导与协同责任\n" +
                                    "1.1 若您为未满十四周岁的儿童或未成年人，在使用本软件前，请务必请您的父母或其他法定监护人仔细阅读并理解本政策，并在监护人的指导与同意下使用本应用。\n" +
                                    "1.2 监护人应当协助未成年人树立正确的网络价值观与安全防范意识，监督其网络活动，合理规划使用设备时长。\n\n" +
                                    "第二条：严格的儿童信息零收集原则\n" +
                                    "2.1 本应用坚持无账户、无实名绑定的纯净架构，我们绝不会主动索取、收集、保存、出售或向任何第三方披露不满十四周岁儿童的姓名、身份证号、人脸特征、住址或联系方式。\n" +
                                    "2.2 本应用禁止利用任何算法对儿童行为进行商业化画像、消费倾向分析或精准营销推送。\n\n" +
                                    "第三条：绿色内容与安全防护机制\n" +
                                    "3.1 内容过滤：我们持续审核与筛查收录的导航内容，杜绝涉黄、暴恐、不良低俗、诱导打赏或网络赌博等危害身心健康的有害信息。\n" +
                                    "3.2 防沉迷与健康提示：倡导青少年劳逸结合，避免过度用眼，建立健康作息。\n\n" +
                                    "第四条：监护人权利与快速响应救济通道\n" +
                                    "4.1 若监护人发现未成年人在未经许可的情况下上传了可能涉及个人隐私的内容，可通过官方社群渠道联系我们，我们将在核实后第一时间进行下架与删除处理。",
                            fontSize = 12.5.sp,
                            lineHeight = 19.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = { activeDialogType = null }) { Text("知晓并遵守") }
                }
            )
        }
        "contact_author" -> {
            ContactAuthorDialog(
                context = context,
                onDismiss = { activeDialogType = null },
                onOpenFeedback = { activeDialogType = "feedback_bug" }
            )
        }
    }
}

@Composable
private fun SettingsClickableItem(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = if (subtitle.isNullOrBlank()) 14.dp else 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            if (!subtitle.isNullOrBlank()) {
                Text(
                    subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(14.dp)
        )
    }
}

fun openQqGroup(context: Context, groupUrl: String = OFFICIAL_QQ_GROUP_URL, groupUin: String = "439211347") {
    // 1. Copy group number to clipboard
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("官方QQ群号", groupUin))

    // 2. Try launching QQ app directly via card intent
    var launched = false
    try {
        val qqIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("mqqapi://card/show_pslcard?src_type=internal&version=1&uin=$groupUin&card_type=group&source=qrcode")
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(qqIntent)
        launched = true
        Toast.makeText(context, "已复制群号($groupUin)，正在唤起QQ加群...", Toast.LENGTH_SHORT).show()
    } catch (_: Exception) {
    }

    if (!launched) {
        try {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(groupUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
            Toast.makeText(context, "已复制群号($groupUin)，正在打开官方群...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "已复制官方QQ群号: $groupUin，请在QQ中搜索加入", Toast.LENGTH_LONG).show()
        }
    }
}

private fun openUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "无法打开链接: $url", Toast.LENGTH_SHORT).show()
    }
}

private fun copyText(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
    Toast.makeText(context, "已复制 $label 到剪贴板", Toast.LENGTH_SHORT).show()
}

@Composable
private fun ContactAuthorDialog(
    context: Context,
    onDismiss: () -> Unit,
    onOpenFeedback: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("支付宝", "QQ", "微信")

    fun openAlipay() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("alipays://platformapi/startapp?saId=10000007")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val launchIntent = context.packageManager.getLaunchIntentForPackage("com.eg.android.AlipayGphone")
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                } else {
                    Toast.makeText(context, "请先保存或截屏二维码，在支付宝中扫码投喂", Toast.LENGTH_LONG).show()
                }
            } catch (ex: Exception) {
                Toast.makeText(context, "请截屏二维码，打开支付宝扫一扫", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun openQq() {
        try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage("com.tencent.mobileqq")
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
            } else {
                Toast.makeText(context, "未检测到QQ，请先截屏二维码在QQ中扫一扫添加好友", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "打开QQ失败，请截屏后扫一扫", Toast.LENGTH_SHORT).show()
        }
    }

    fun openWeChat() {
        try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage("com.tencent.mm")
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
            } else {
                Toast.makeText(context, "未检测到微信，请先截屏二维码在微信中扫一扫添加好友", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "打开微信失败，请截屏后扫一扫", Toast.LENGTH_SHORT).show()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Brush.linearGradient(listOf(FlameRed, SunsetOrange))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.SupportAgent,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("联系作者", fontWeight = FontWeight.Black, fontSize = 16.sp)
                    Text("扫码支持或添加好友交流", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    tabs.forEachIndexed { index, tabTitle ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = tabTitle,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == index) FlameRed else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedTab) {
                    0 -> {
                        // 支付宝
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF1677FF).copy(alpha = 0.3f)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(220.dp)
                                        .padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.img_contact_alipay),
                                        contentDescription = "支付宝扫码",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "支付宝扫码",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(14.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { openAlipay() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1677FF)),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Text("唤醒支付宝", fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        copyText(context, "官方交流QQ群", "439211347")
                                    },
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("复制QQ群号", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                    1 -> {
                        // QQ
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF1976D2).copy(alpha = 0.3f)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(220.dp)
                                        .padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.img_contact_qq),
                                        contentDescription = "QQ扫码",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "QQ扫码",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(14.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { openQq() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Text("唤醒QQ", fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        copyText(context, "官方交流QQ群", "439211347")
                                    },
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("复制QQ群号", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                    2 -> {
                        // 微信
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF07C160).copy(alpha = 0.3f)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(220.dp)
                                        .padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.img_contact_wechat),
                                        contentDescription = "微信扫码",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "微信扫码",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(14.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { openWeChat() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF07C160)),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Text("唤醒微信", fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        copyText(context, "官方交流QQ群", "439211347")
                                    },
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("复制QQ群号", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onOpenFeedback()
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.BugReport, contentDescription = null, modifier = Modifier.size(14.dp), tint = FlameRed)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("软件反馈", fontSize = 12.sp, color = FlameRed)
                }
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = FlameRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("关闭")
                }
            }
        }
    )
}
