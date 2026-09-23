package com.example.ui.screens.toolbox

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.clonecenter.database.CloneEntity
import com.example.clonecenter.model.InstalledApp
import com.example.clonecenter.ui.CloneCenterViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 分身助手 · Work Profile 应用分身
 * - 扫描手机已安装应用
 * - 创建系统级「工作分身空间」（Work Profile）
 * - 一键将应用分身安装进分身空间，实现真正的双开隔离
 */
@Composable
fun CloneCenterScreenView(
    viewModel: CloneCenterViewModel = viewModel()
) {
    val context = LocalContext.current
    val apps by viewModel.apps.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val message by viewModel.message.collectAsState()
    val clones by viewModel.observeClones()
        .collectAsStateWithLifecycle(initialValue = emptyList())

    val supportsProfile = viewModel.supportsProfile()
    val isProfileOwner = viewModel.isProfileOwner()
    // 分身空间启用状态（周期性刷新，创建/移除后自动更新）
    var profileActive by remember { mutableStateOf(viewModel.isProfileActive()) }
    // 移除分身空间二次确认弹窗
    var showRemoveConfirm by remember { mutableStateOf(false) }
    // 已在分身空间中的应用包名集合（避免重复创建）
    var installedInProfile by remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(Unit) {
        while (true) {
            profileActive = viewModel.isProfileActive()
            installedInProfile = apps.filter { viewModel.isAppInProfile(it.packageName) }.map { it.packageName }.toSet()
            kotlinx.coroutines.delay(2000L)
        }
    }

    // 分身空间创建结果回调（v1.7.3：创建成功后自动添加待创建的分身应用）
    val provisionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val ok = result.resultCode == android.app.Activity.RESULT_OK
        viewModel.onProvisioningResult(ok)
        // 重新加载状态
        profileActive = viewModel.isProfileActive()
        viewModel.refreshApps()
    }

    // 一次性提示消息
    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.consumeMessage()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        // 顶部状态横幅
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (supportsProfile) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            } else {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                Text(
                    text = "分身助手 · 应用分身助手",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = when {
                        !supportsProfile -> "当前设备不支持应用分身（需 Android 5.0+ 且支持多用户）"
                        profileActive && isProfileOwner -> "✅ 分身空间已启用：选择应用点击「创建」即可分身"
                        profileActive -> "分身空间已创建：请先打开系统「分身空间设置」确认本机管理状态"
                        !isProfileOwner -> "已就绪：点击应用「创建」即可自动创建分身空间并分身"
                        else -> "分身空间已启用：选择应用点击「创建」即可分身"
                    },
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 操作按钮行
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.refreshApps() },
                enabled = !isScanning,
                modifier = Modifier.weight(1f)
            ) {
                if (isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text("重新扫描", fontSize = 13.sp)
            }
            Button(
                onClick = {
                    val activity = context.findActivity()
                    if (activity != null) {
                        viewModel.createProfile(activity)
                    } else {
                        Toast.makeText(context, "无法启动分身空间创建流程", Toast.LENGTH_LONG).show()
                    }
                },
                enabled = supportsProfile && !profileActive,
                modifier = Modifier.weight(1f)
            ) {
                Text(if (profileActive) "分身空间已启用" else "创建分身空间", fontSize = 13.sp)
            }
        }

        // 分身空间管理操作（启用后可设置/移除）
        if (profileActive) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.openProfileSettings() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("⚙️ 分身空间设置", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { showRemoveConfirm = true },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("🗑 移除分身空间", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "💡 移除后将删除桌面「工作」标签及分身空间内全部应用数据，请谨慎操作",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.75f)
            )
        }

        // 移除分身空间二次确认
        if (showRemoveConfirm) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showRemoveConfirm = false },
                title = { Text("移除分身空间？", fontWeight = FontWeight.Black) },
                text = {
                    Text("将删除桌面「工作」标签及其中的全部分身应用与数据，移除后需重新创建才能分身。确定继续吗？", fontSize = 13.sp)
                },
                confirmButton = {
                    androidx.compose.material3.TextButton(
                        onClick = {
                            viewModel.removeProfile()
                            showRemoveConfirm = false
                            profileActive = viewModel.isProfileActive()
                        }
                    ) {
                        Text("确定移除", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(onClick = { showRemoveConfirm = false }) {
                        Text("取消")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 已创建分身列表
        if (clones.isNotEmpty()) {
            Text(
                text = "已创建分身（${clones.size}）",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (clones.size > 2) 150.dp else (clones.size * 56).dp)
            ) {
                items(clones, key = { it.id }) { clone ->
                    CloneRow(clone)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        Spacer(modifier = Modifier.height(8.dp))

        // 应用扫描列表
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "已发现 ${apps.size} 个可启动应用",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "点击「创建」分身到工作空间",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(apps, key = { it.packageName }) { app ->
                AppRow(
                    app = app,
                    alreadyInProfile = app.packageName in installedInProfile,
                    onClick = {
                        val activity = context.findActivity()
                        if (activity != null) {
                            viewModel.cloneApp(app, activity)
                        } else {
                            Toast.makeText(context, "无法启动创建流程", Toast.LENGTH_LONG).show()
                        }
                    }
                )
            }
            if (apps.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 30.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isScanning) "正在扫描应用…" else "未发现应用，请点击「重新扫描」",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CloneRow(clone: CloneEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(
                    if (clone.state == "INSTALLED") {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    }
                )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = clone.appLabel,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = clone.packageName,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = if (clone.state == "INSTALLED") "已分身" else "已记录",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (clone.state == "INSTALLED") {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = formatTime(clone.createdAt),
            fontSize = 9.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun AppRow(
    app: InstalledApp,
    alreadyInProfile: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            bitmap = app.icon.toImageBitmap(),
            contentDescription = null,
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.label,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
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
        Spacer(modifier = Modifier.width(8.dp))
        if (alreadyInProfile) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            ) {
                Text(
                    text = "已在分身空间",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        } else {
            Button(
                onClick = onClick,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 14.dp, vertical = 6.dp
                )
            ) {
                Text("创建", fontSize = 12.sp)
            }
        }
    }
}

/** Drawable 转 Compose ImageBitmap（处理尺寸异常） */
private fun Drawable.toImageBitmap(): ImageBitmap {
    return try {
        val w = if (intrinsicWidth > 0) intrinsicWidth else 64
        val h = if (intrinsicHeight > 0) intrinsicHeight else 64
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bmp)
        setBounds(0, 0, w, h)
        draw(canvas)
        bmp.asImageBitmap()
    } catch (e: Exception) {
        Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888).asImageBitmap()
    }
}

private fun formatTime(timestamp: Long): String {
    return try {
        SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))
    } catch (e: Exception) {
        ""
    }
}

/** 从 Context 链中安全找到 Activity（Dialog 内 LocalContext 不是 Activity，直接强转会闪退） */
private tailrec fun Context.findActivity(): android.app.Activity? = when (this) {
    is android.app.Activity -> this
    is android.content.ContextWrapper -> baseContext.findActivity()
    else -> null
}
