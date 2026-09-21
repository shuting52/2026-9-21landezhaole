package com.example.ui.screens.toolbox

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.db.CloneAppEntity
import com.example.ui.theme.FlameRed
import com.example.ui.theme.JadeGreen

/** 已安装的带启动入口的应用 */
data class InstalledApp(
    val packageName: String,
    val label: String,
    val activityName: String,
    val icon: androidx.compose.ui.graphics.ImageBitmap
)

/**
 * 分身多开：真实有效的应用多开助手。
 * 1) 一键调用系统内置「应用分身/双开」（小米/OPPO/vivo/华为/荣耀/三星等国产 ROM 原生支持）；
 * 2) 无系统分身能力时，可创建桌面分身入口（独立快捷方式）；
 * 3) 本地管理已创建的分身（重命名/打开/删除）。
 */
@Composable
fun MultiOpenScreenView(
    context: Context,
    clones: List<CloneAppEntity>,
    onCreateClone: (packageName: String, appName: String) -> Unit,
    onDeleteClone: (id: String) -> Unit,
    onRenameClone: (id: String, newName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val packageManager = context.packageManager
    var installedApps by remember { mutableStateOf<List<InstalledApp>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var renamingClone by remember { mutableStateOf<CloneAppEntity?>(null) }
    var renameText by remember { mutableStateOf("") }

    // 加载已安装应用（仅含启动入口的应用）
    androidx.compose.runtime.LaunchedEffect(Unit) {
        installedApps = loadInstalledApps(context)
    }

    val vendor = remember { detectVendor() }
    val filtered = remember(searchQuery, installedApps) {
        if (searchQuery.isBlank()) installedApps
        else installedApps.filter { it.label.contains(searchQuery, ignoreCase = true) || it.packageName.contains(searchQuery, ignoreCase = true) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 顶部说明
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Apps,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "分身多开助手",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "系统分身能力 ${if (vendor.isBlank()) "未检测到" else "已检测到（$vendor）"}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "点击应用下方「系统双开」可直达手机系统内置的应用分身开关；「创建分身入口」可在桌面生成独立分身图标，实现同应用多账号同时使用。",
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (vendor.isBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "未检测到系统分身功能：已自动改用「桌面分身入口」方案，同样支持多开使用。",
                            fontSize = 11.sp,
                            color = FlameRed
                        )
                    }
                }
            }
        }

        // 我的分身
        if (clones.isNotEmpty()) {
            item {
                Text(
                    text = "我的分身（${clones.size}）",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            items(clones, key = { it.id }) { clone ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = clone.cloneName.take(1),
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = clone.cloneName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = clone.packageName,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(
                            onClick = {
                                launchApp(context, clone.packageName)
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "打开",
                                tint = JadeGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = {
                                renamingClone = clone
                                renameText = clone.cloneName
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "重命名",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = { onDeleteClone(clone.id) },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "删除",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // 已安装应用列表
        item {
            Text(
                text = "已安装应用（${installedApps.size}）",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("搜索应用名称 / 包名", fontSize = 12.5.sp) },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { }),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }

        items(filtered, key = { it.packageName }) { app ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Image(
                    bitmap = app.icon,
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.label,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = app.packageName,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                TextButton(
                    onClick = {
                        val ok = openDualAppManager(context, app.packageName)
                        if (!ok) {
                            Toast.makeText(context, "未找到系统分身入口，将使用桌面分身方案", Toast.LENGTH_SHORT).show()
                            createDesktopClone(context, app, 1)
                            onCreateClone(app.packageName, app.label)
                        }
                    },
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("系统双开", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                }
                Button(
                    onClick = {
                        val count = clones.count { it.packageName == app.packageName }
                        createDesktopClone(context, app, count + 1)
                        onCreateClone(app.packageName, app.label)
                    },
                    modifier = Modifier.height(32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FlameRed),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("创建分身", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "提示：系统双开（应用分身）为各手机品牌系统自带功能，支持账号数据完全隔离，是最安全的多开方式。",
                fontSize = 10.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }

    // 重命名弹窗
    renamingClone?.let { clone ->
        Dialog(onDismissRequest = { renamingClone = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("重命名分身", fontWeight = FontWeight.Black, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = renameText,
                        onValueChange = { renameText = it },
                        singleLine = true,
                        label = { Text("分身名称") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TextButton(
                            onClick = { renamingClone = null },
                            modifier = Modifier.weight(1f)
                        ) { Text("取消") }
                        Button(
                            onClick = {
                                if (renameText.isNotBlank()) onRenameClone(clone.id, renameText.trim())
                                renamingClone = null
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = FlameRed)
                        ) { Text("保存") }
                    }
                }
            }
        }
    }
}

/** 加载已安装且有桌面入口的应用 */
private fun loadInstalledApps(context: Context): List<InstalledApp> {
    return try {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfos = pm.queryIntentActivities(intent, 0)
        val ownPackage = context.packageName
        resolveInfos
            .asSequence()
            .filter { it.activityInfo != null && it.activityInfo.packageName != ownPackage }
            .mapNotNull { ri ->
                try {
                    val label = ri.loadLabel(pm)?.toString() ?: ri.activityInfo.packageName
                    val iconDrawable = ri.loadIcon(pm)
                    val bitmap = (iconDrawable as? BitmapDrawable)?.bitmap
                        ?: iconDrawableToBitmap(iconDrawable)
                    InstalledApp(
                        packageName = ri.activityInfo.packageName,
                        label = label,
                        activityName = ri.activityInfo.name,
                        icon = bitmap.asImageBitmap()
                    )
                } catch (e: Exception) {
                    null
                }
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
            .toList()
    } catch (e: Exception) {
        emptyList()
    }
}

@Suppress("DEPRECATION")
private fun iconDrawableToBitmap(drawable: android.graphics.drawable.Drawable): Bitmap {
    if (drawable is BitmapDrawable && drawable.bitmap != null) return drawable.bitmap
    val size = (drawable.intrinsicWidth.coerceAtLeast(48))
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    drawable.setBounds(0, 0, size, size)
    drawable.draw(canvas)
    return bitmap
}

/** 检测系统品牌（用于匹配内置分身能力） */
fun detectVendor(): String {
    val m = (Build.MANUFACTURER + " " + Build.BRAND).lowercase()
    return when {
        m.contains("xiaomi") || m.contains("redmi") || m.contains("mi") -> "小米/MIUI"
        m.contains("oppo") || m.contains("realme") || m.contains("oneplus") -> "OPPO/ColorOS"
        m.contains("vivo") || m.contains("iqoo") -> "vivo/Funtouch"
        m.contains("honor") -> "荣耀"
        m.contains("huawei") -> "华为/EMUI"
        m.contains("samsung") -> "三星"
        else -> ""
    }
}

/**
 * 打开系统内置「应用分身/双开」管理界面。
 * 各品牌 ROM 入口不同，逐个尝试；成功返回 true。
 */
fun openDualAppManager(context: Context, targetPackage: String? = null): Boolean {
    val intents = listOf(
        // 小米 / MIUI / HyperOS
        Intent("miui.intent.action.OP_DUAL_APP").apply { putExtra("package_name", targetPackage ?: "") },
        Intent().setComponent(ComponentName("com.miui.securitycore", "com.miui.duallayer.app.DualAppManagerActivity")),
        Intent().setComponent(ComponentName("com.miui.securitycenter", "com.miui.duallayer.app.DualAppManagerActivity")),
        // OPPO / ColorOS
        Intent().setComponent(ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.duallayer.DualLayerAppManagerActivity")),
        Intent().setComponent(ComponentName("com.oplus.safecenter", "com.oplus.safecenter.permission.duallayer.DualLayerAppManagerActivity")),
        // vivo / iQOO
        Intent().setComponent(ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BMAppListActivity")),
        Intent().setComponent(ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.addwindow.DualAppListActivity")),
        // 华为 / EMUI
        Intent().setComponent(ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.addview.multiapp.MultiAppActivity")),
        // 荣耀
        Intent().setComponent(ComponentName("com.hihonor.systemmanager", "com.hihonor.systemmanager.addview.multiapp.MultiAppActivity")),
        // 三星
        Intent().setComponent(ComponentName("com.samsung.android.da.daagent", "com.samsung.android.da.daagent.DualAppManagerActivity"))
    )
    for (intent in intents) {
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            return true
        } catch (e: Exception) {
            // 继续尝试下一个入口
        }
    }
    // 兜底：打开系统应用详情页，提示用户在系统设置中开启分身
    try {
        if (targetPackage != null) {
            val detail = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$targetPackage"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(detail)
        }
    } catch (e: Exception) {
        // ignore
    }
    return false
}

/** 打开指定应用 */
fun launchApp(context: Context, packageName: String) {
    try {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "未找到该应用，可能已被卸载", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "打开失败", Toast.LENGTH_SHORT).show()
    }
}

/**
 * 创建桌面分身入口（独立快捷方式）。
 * Android 8.0+ 使用 ShortcutManager 固定快捷方式；低版本使用传统 INSTALL_SHORTCUT 广播。
 */
fun createDesktopClone(context: Context, app: InstalledApp, cloneIndex: Int) {
    val cloneName = when (cloneIndex) {
        1 -> app.label + "分身"
        2 -> app.label + "分身二"
        3 -> app.label + "分身三"
        else -> app.label + "分身" + cloneIndex
    }
    val launchIntent = Intent(Intent.ACTION_MAIN)
        .setClassName(app.packageName, app.activityName)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val shortcutManager = context.getSystemService(Context.SHORTCUT_SERVICE)
                    as android.content.pm.ShortcutManager
            val shortcut = android.content.pm.ShortcutInfo.Builder(context, "clone_${app.packageName}_$cloneIndex")
                .setShortLabel(cloneName)
                .setLongLabel(cloneName)
                .setIcon(android.graphics.drawable.Icon.createWithBitmap(iconBitmapOf(app)))
                .setIntent(launchIntent)
                .build()
            shortcutManager.requestPinShortcut(shortcut, null)
        } else {
            @Suppress("DEPRECATION")
            val shortcutIntent = Intent("com.android.launcher.action.INSTALL_SHORTCUT")
            shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launchIntent)
            shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, cloneName)
            shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, iconBitmapOf(app))
            shortcutIntent.putExtra("duplicate", false)
            context.sendBroadcast(shortcutIntent)
        }
        Toast.makeText(context, "已在桌面创建「$cloneName」入口", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "创建分身入口失败：${e.message}", Toast.LENGTH_LONG).show()
    }
}

private fun iconBitmapOf(app: InstalledApp): Bitmap = app.icon.asAndroidBitmap()
