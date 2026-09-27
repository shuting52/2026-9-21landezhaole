package com.example.ui.screens.toolbox

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.ui.theme.FlameRed
import com.example.ui.theme.JadeGreen
import kotlin.math.min

/**
 * 紧急电话工具（v1.0.4）
 *
 * - 全域服务电话逐一分类（急救救援/交通出行/政务服务/社会保障/劳动劳务/投诉举报/银行/快递/电商/外卖/通信/生活/保险/儿童救助）
 * - 每个紧急电话后都有「拨打」按钮，可一键快捷呼出
 * - 覆盖全国：支持地区选择（省 → 市/区/县），选择地区后本地热线随地区展示
 * - 每个分类自动匹配相关 icon（emoji 图标）
 * - 号码均为公开真实权威热线
 */
@Composable
fun EmergencyPhoneSection() {
    val context = LocalContext.current

    // 当前地区选择（默认全国）
    var selectedProvince by remember { mutableStateOf<String?>(null) }
    var selectedRegion by remember { mutableStateOf<RegionCity?>(null) }
    var showRegionPicker by remember { mutableStateOf(false) }
    // 当前分类
    var activeCategoryId by remember { mutableStateOf(NATIONAL_EMERGENCY_CATEGORIES.first().id) }

    fun callNumber(number: String) {
        val clean = number.filter { it.isDigit() || it == '+' }
        try {
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$clean")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                context.startActivity(intent)
            } else {
                // 无权限：打开拨号盘预填号码（同样一键可呼出）
                val dial = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$clean")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    context.startActivity(dial)
                } catch (e: Exception) {
                    Toast.makeText(context, "无法打开拨号盘", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            try {
                val dial = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$clean")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(dial)
            } catch (e2: Exception) {
                Toast.makeText(context, "无法呼出该号码", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp)) {
        // ---------- 顶部横幅 ----------
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = Color.White.copy(alpha = 0.55f),
            border = BorderStroke(1.dp, FlameRed.copy(alpha = 0.35f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.linearGradient(listOf(FlameRed, Color(0xFFFF9500)))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Call, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "紧急电话 · 覆盖全国",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "全域服务分类 · 一键快捷呼出 · 支持地区选择",
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ---------- 地区选择器 ----------
        Surface(
            onClick = { showRegionPicker = true },
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = FlameRed, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "当前地区",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = buildString {
                            append(selectedProvince ?: "全国")
                            selectedRegion?.let { append(" · ${it.name}") }
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "切换 ▾",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = FlameRed
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ---------- 分类横向选择（自动匹配 icon） ----------
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(NATIONAL_EMERGENCY_CATEGORIES) { cat ->
                val selected = cat.id == activeCategoryId
                Surface(
                    onClick = { activeCategoryId = cat.id },
                    shape = RoundedCornerShape(50),
                    color = if (selected) FlameRed.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.55f),
                    border = BorderStroke(
                        1.dp,
                        if (selected) FlameRed.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = cat.icon, fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = cat.title,
                            fontSize = 11.5.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = if (selected) FlameRed else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ---------- 号码列表 ----------
        val activeCat = NATIONAL_EMERGENCY_CATEGORIES.firstOrNull { it.id == activeCategoryId }
            ?: NATIONAL_EMERGENCY_CATEGORIES.first()
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // 地区本地热线（选择地区后展示该地区专属号码）
            if (selectedRegion != null) {
                item {
                    RegionLocalCard(
                        region = selectedRegion!!,
                        onCall = { number -> callNumber(number) }
                    )
                }
            }
            items(activeCat.numbers, key = { it.number }) { num ->
                EmergencyNumberRow(
                    name = num.name,
                    number = num.number,
                    desc = num.desc,
                    icon = activeCat.icon,
                    onCall = { callNumber(num.number) }
                )
            }
        }
    }

    // ---------- 地区选择弹窗（省 → 市/区/县） ----------
    if (showRegionPicker) {
        RegionPickerDialog(
            regions = NATIONAL_REGIONS,
            currentProvince = selectedProvince,
            onSelect = { province, city ->
                selectedProvince = province
                selectedRegion = city
                showRegionPicker = false
            },
            onClear = {
                selectedProvince = null
                selectedRegion = null
                showRegionPicker = false
            },
            onDismiss = { showRegionPicker = false }
        )
    }
}

/** 地区本地热线卡片（政务/查询/本地应急，全国通用本地接入） */
@Composable
private fun RegionLocalCard(region: RegionCity, onCall: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = FlameRed.copy(alpha = 0.08f)),
        border = BorderStroke(1.dp, FlameRed.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = "📍 ${region.name} 本地服务热线",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Black,
                color = FlameRed
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MiniCallChip(name = "本地政务服务", number = "12345", onCall = { onCall("12345") }, modifier = Modifier.weight(1f))
                MiniCallChip(name = "本地医疗急救", number = "120", onCall = { onCall("120") }, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MiniCallChip(name = "本地公安报警", number = "110", onCall = { onCall("110") }, modifier = Modifier.weight(1f))
                MiniCallChip(name = "本地查号", number = "114", onCall = { onCall("114") }, modifier = Modifier.weight(1f))
            }
        }
    }
}

/** 小号调用卡片 */
@Composable
private fun MiniCallChip(name: String, number: String, onCall: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onCall,
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, FlameRed.copy(alpha = 0.2f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = name, fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            Text(text = number, fontSize = 13.sp, fontWeight = FontWeight.Black, color = FlameRed)
        }
    }
}

/** 单个号码卡片 */
@Composable
private fun EmergencyNumberRow(
    name: String,
    number: String,
    desc: String,
    icon: String,
    onCall: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // icon 圆形徽标
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(FlameRed.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 17.sp)
            }
            Spacer(modifier = Modifier.width(10.dp))
            // 名称 + 号码
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = number,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = FlameRed
                    )
                    if (desc.isNotBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = desc,
                            fontSize = 9.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(6.dp))
            // 拨打按钮
            Button(
                onClick = onCall,
                colors = ButtonDefaults.buttonColors(containerColor = FlameRed),
                shape = RoundedCornerShape(10.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Icon(Icons.Filled.Call, contentDescription = "拨打", tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("拨打", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

/** 地区选择弹窗：省份列表 → 城市/区县列表 */
@Composable
private fun RegionPickerDialog(
    regions: List<Region>,
    currentProvince: String?,
    onSelect: (String, RegionCity) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedProvince by remember { mutableStateOf(currentProvince) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .clip(RoundedCornerShape(18.dp)),
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                // 标题栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (selectedProvince == null) "选择省份" else "选择城市 / 区县 · $selectedProvince",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row {
                        OutlinedButton(
                            onClick = onClear,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("恢复全国", fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(onClick = onDismiss, shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                            Box(modifier = Modifier.size(34.dp), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Close, contentDescription = "关闭", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                if (selectedProvince == null) {
                    // 省份列表
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(regions) { region ->
                            val pName = region.province
                            Surface(
                                onClick = { selectedProvince = pName },
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = pName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text("▸", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                } else {
                    // 城市 / 区县列表（返回省份按钮）
                    val region = regions.firstOrNull { it.province == selectedProvince }
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        item {
                            Surface(
                                onClick = { selectedProvince = null },
                                shape = RoundedCornerShape(10.dp),
                                color = Color.Transparent,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("◂ 返回省份", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = FlameRed)
                                }
                            }
                        }
                        if (region != null) {
                            items(region.cities) { city ->
                                Surface(
                                    onClick = { onSelect(region.province, city) },
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = city.name,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "区号 ${city.areaCode}",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text("拨打本地热线 ✓", fontSize = 10.5.sp, color = JadeGreen, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}