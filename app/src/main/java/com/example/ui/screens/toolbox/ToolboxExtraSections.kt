package com.example.ui.screens.toolbox

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import com.example.ui.theme.FlameRed
import com.example.ui.theme.JadeGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SunsetOrange
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * 极客百宝箱子 · 扩展工具实现（v1.7.8）
 *
 * 全部工具纯本地运算：纯 Kotlin / 单脚本 / 不依赖 API、不闪退
 *
 * 已新增：
 * - RANDOM_NUMBER  随机数 / 色子
 * - BMI_CALC       BMI 计算
 * - PASSWORD_GEN   密码生成
 * - QRCODE_TEXT    文本二维码
 * - COLOR_CARD     调色卡 / 取色
 */

/* ============================================================
 * 1) 随机数 / 色子
 * ============================================================ */
@Composable
fun RandomNumberScreenView() {
    val context = LocalContext.current
    var minText by remember { mutableStateOf("1") }
    var maxText by remember { mutableStateOf("100") }
    var countText by remember { mutableStateOf("1") }
    var results by remember { mutableStateOf(listOf<Int>()) }
    var rolling by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun roll() {
        scope.launch {
            rolling = true
            repeat(8) { delay(80) }
            val mn = minText.toIntOrNull() ?: 1
            val mx = maxText.toIntOrNull() ?: 100
            val n = countText.toIntOrNull()?.coerceIn(1, 100) ?: 1
            if (mn > mx) {
                Toast.makeText(context, "最小值不能大于最大值", Toast.LENGTH_SHORT).show()
            } else {
                results = List(n) { Random.nextInt(mn, mx + 1) }
            }
            rolling = false
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.6f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.8f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🎲 随机数 / 色子", fontSize = 16.sp, fontWeight = FontWeight.Black)
                    Text("指定区间随机整数 · 批量多抽 · 抽奖抽签必备",
                        fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = minText, onValueChange = { minText = it.filter { c -> c.isDigit() } },
                    label = { Text("最小值") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = maxText, onValueChange = { maxText = it.filter { c -> c.isDigit() } },
                    label = { Text("最大值") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            OutlinedTextField(
                value = countText,
                onValueChange = { countText = it.filter { c -> c.isDigit() }.take(3) },
                label = { Text("抽几个（1-100）") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
        // 快捷：1~6 色子
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                listOf("1-6", "1-10", "1-100", "1-1000").forEach { range ->
                    Surface(
                        onClick = {
                            val parts = range.split("-")
                            minText = parts[0]; maxText = parts[1]; countText = "1"
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.7f),
                        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(range, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 6.dp).fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            }
        }
        item {
            Button(
                onClick = { roll() },
                enabled = !rolling,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SunsetOrange)
            ) {
                Text(if (rolling) "摇色子中…" else "🎲 开抽", fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color.White)
            }
        }
        if (results.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = FlameRed.copy(alpha = 0.08f)),
                    border = BorderStroke(1.dp, FlameRed.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🍀 结果", fontSize = 13.sp, fontWeight = FontWeight.Black, color = FlameRed, modifier = Modifier.weight(1f))
                            Surface(
                                onClick = {
                                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    cm.setPrimaryClip(ClipData.newPlainText("随机数", results.joinToString(", ")))
                                    Toast.makeText(context, "已复制结果", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(6.dp),
                                color = Color.White.copy(alpha = 0.7f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("复制", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        // 大数字高亮
                        results.forEach { v ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = "$v",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ============================================================
 * 2) BMI 计算器
 * ============================================================ */
@Composable
fun BmiCalculatorScreenView() {
    val context = LocalContext.current
    var heightText by remember { mutableStateOf("170") }
    var weightText by remember { mutableStateOf("65") }
    var bmi by remember { mutableStateOf<Float?>(null) }

    fun calc() {
        val h = (heightText.toFloatOrNull() ?: 0f) / 100f
        val w = weightText.toFloatOrNull() ?: 0f
        if (h <= 0f || w <= 0f) {
            Toast.makeText(context, "请输入有效的身高体重", Toast.LENGTH_SHORT).show()
            bmi = null
            return
        }
        bmi = w / (h * h)
    }

    // 自动计算（输入变化时）
    LaunchedEffect(heightText, weightText) {
        kotlinx.coroutines.delay(300)
        calc()
    }

    val (label, color) = when (val v = bmi ?: 0f) {
        0f -> "请输入身高体重" to Color.Gray
        in 0f..18.4f -> "偏瘦 · 多吃点" to JadeGreen
        in 18.5f..23.9f -> "正常范围 · 继续保持" to FlameRed
        in 24f..27.9f -> "偏胖 · 注意饮食运动" to SunsetOrange
        else -> "肥胖 · 建议咨询医生" to FlameRed
    }.let {
        if (bmi == null) "请输入身高体重" to Color.Gray
        else when (bmi!!) {
            in 0f..18.4f -> "偏瘦 · 多吃点" to JadeGreen
            in 18.5f..23.9f -> "正常范围 · 继续保持" to FlameRed
            in 24f..27.9f -> "偏胖 · 注意饮食运动" to SunsetOrange
            else -> "肥胖 · 建议咨询医生" to FlameRed
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.6f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.8f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("⚖️ BMI 计算器", fontSize = 16.sp, fontWeight = FontWeight.Black)
                    Text("身高体重 · 一键得出 BMI 值 · 中国成人标准范围",
                        fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = heightText, onValueChange = { heightText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("身高 (cm)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = weightText, onValueChange = { weightText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("体重 (kg)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        if (bmi != null) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
                    border = BorderStroke(1.5.dp, color.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("你的 BMI", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "%.1f".format(bmi),
                            fontSize = 56.sp,
                            fontWeight = FontWeight.Black,
                            color = color
                        )
                        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
                    }
                }
            }
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.6f)),
                    border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("📊 中国成人 BMI 参考", fontSize = 12.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        BmiRangeRow("< 18.5", "偏瘦", JadeGreen)
                        BmiRangeRow("18.5 ~ 23.9", "正常范围", FlameRed)
                        BmiRangeRow("24 ~ 27.9", "偏胖", SunsetOrange)
                        BmiRangeRow("≥ 28", "肥胖", FlameRed)
                    }
                }
            }
        }
    }
}

@Composable
private fun BmiRangeRow(range: String, label: String, color: Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = CircleShape, color = color, modifier = Modifier.size(8.dp)) {}
        Spacer(modifier = Modifier.width(6.dp))
        Text(range, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(110.dp))
        Text(label, fontSize = 12.sp, color = color, fontWeight = FontWeight.Bold)
    }
}

/* ============================================================
 * 3) 密码生成器（v1.7.8）：纯本地，安全可控
 * ============================================================ */
@Composable
fun PasswordGeneratorScreenView() {
    val context = LocalContext.current
    var length by remember { mutableIntStateOf(16) }
    var count by remember { mutableIntStateOf(5) }
    var includeUpper by remember { mutableStateOf(true) }
    var includeLower by remember { mutableStateOf(true) }
    var includeDigit by remember { mutableStateOf(true) }
    var includeSymbol by remember { mutableStateOf(false) }
    var passwords by remember { mutableStateOf(listOf<String>()) }

    val upperChars = "ABCDEFGHJKLMNPQRSTUVWXYZ"
    val lowerChars = "abcdefghjkmnpqrstuvwxyz"
    val digitChars = "23456789"
    val symbolChars = "!@#%^&*()-_=+[]{}"

    fun generate() {
        val pool = buildString {
            if (includeUpper) append(upperChars)
            if (includeLower) append(lowerChars)
            if (includeDigit) append(digitChars)
            if (includeSymbol) append(symbolChars)
        }
        if (pool.isEmpty()) {
            Toast.makeText(context, "请至少勾选一种字符类型", Toast.LENGTH_SHORT).show()
            return
        }
        val n = count.coerceIn(1, 20)
        val out = mutableListOf<String>()
        repeat(n) {
            val sb = StringBuilder()
            // 至少保证每类至少有一个字符（如果用户选了该类型）
            if (includeUpper) sb.append(upperChars.random())
            if (includeLower) sb.append(lowerChars.random())
            if (includeDigit) sb.append(digitChars.random())
            if (includeSymbol) sb.append(symbolChars.random())
            while (sb.length < length) sb.append(pool.random())
            // 打乱
            out.add(sb.toString().toList().shuffled().joinToString(""))
        }
        passwords = out
    }

    LaunchedEffect(Unit) { if (passwords.isEmpty()) generate() }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.6f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.8f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🔐 密码生成器", fontSize = 16.sp, fontWeight = FontWeight.Black)
                    Text("本地生成 · 安全可控 · 长度 6-64 · 不上传任何服务器",
                        fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("长度 $length", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(80.dp))
                listOf(8, 12, 16, 20, 24, 32).forEach { len ->
                    FilterChip(
                        selected = length == len,
                        onClick = { length = len },
                        label = { Text("$len", fontSize = 11.sp) },
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            }
        }
        item {
            OutlinedTextField(
                value = length.toString(),
                onValueChange = { txt ->
                    val v = txt.toIntOrNull()?.coerceIn(6, 64)
                    if (v != null) length = v
                },
                label = { Text("自定义长度 (6-64)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("生成几个：", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                listOf(1, 3, 5, 10, 20).forEach { n ->
                    FilterChip(
                        selected = count == n,
                        onClick = { count = n },
                        label = { Text("$n", fontSize = 11.sp) },
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                ToggleCell("大写 A-Z", includeUpper) { includeUpper = it }
                ToggleCell("小写 a-z", includeLower) { includeLower = it }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                ToggleCell("数字 0-9", includeDigit) { includeDigit = it }
                ToggleCell("符号 !@#", includeSymbol) { includeSymbol = it }
            }
        }
        item {
            Button(
                onClick = { generate() },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("重新生成", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.White)
            }
        }
        if (passwords.isNotEmpty()) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔑 结果（${passwords.size} 个）", fontSize = 13.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                    Surface(
                        onClick = {
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cm.setPrimaryClip(ClipData.newPlainText("密码", passwords.joinToString("\n")))
                            Toast.makeText(context, "已复制全部密码", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = NeonPurple.copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.4f))
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("一键复制", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonPurple)
                        }
                    }
                }
            }
            items(passwords) { pwd ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.7f)),
                    border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth().clickable {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("密码", pwd))
                        Toast.makeText(context, "已复制", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(pwd, fontSize = 14.sp, fontWeight = FontWeight.Black, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, modifier = Modifier.weight(1f))
                        Icon(Icons.Filled.ContentCopy, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.ToggleCell(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Surface(
        onClick = { onChange(!checked) },
        shape = RoundedCornerShape(10.dp),
        color = if (checked) JadeGreen.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.55f),
        border = BorderStroke(1.dp, if (checked) JadeGreen else Color.Gray.copy(alpha = 0.3f)),
        modifier = Modifier.weight(1f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(if (checked) JadeGreen else Color.White.copy(alpha = 0.7f))
                    .border(1.dp, if (checked) JadeGreen else Color.Gray, CircleShape)
            ) {
                if (checked) Text("✓", fontSize = 9.sp, color = Color.White, modifier = Modifier.padding(start = 2.dp))
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

/* ============================================================
 * 4) 文本二维码（v1.7.8）：纯本地，使用 zxing
 * ============================================================ */
// v1.7.8：本文件内统一使用 Compose Color（android.graphics.Color 仅用于 Bitmap.setPixel） * ============================================================ */
@Composable
fun QrCodeTextScreenView() {
    val context = LocalContext.current
    // v1.7.9：默认不再填任何地址（避免泄露/误解），留空由用户输入
    var text by remember { mutableStateOf("") }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    val scope = rememberCoroutineScope()

    fun generate() {
        scope.launch {
            if (text.isBlank()) {
                bitmap = null
                return@launch
            }
            val size = 600
            val matrix = try {
                QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, size, size)
            } catch (e: Exception) {
                null
            } ?: return@launch
            val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            for (x in 0 until size) for (y in 0 until size) {
                bmp.setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
            bitmap = bmp
        }
    }
    LaunchedEffect(text) { generate() }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.6f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.8f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("📱 文本二维码", fontSize = 16.sp, fontWeight = FontWeight.Black)
                    Text("文本 / 网址 / Wi-Fi 名片 · 全部本地生成 · 不上传任何内容",
                        fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it.take(500) },
                label = { Text("输入文本 / 网址") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                listOf("https://", "Hello!", "WIFI:T:WPA;S:账号;P:密码;;", "TEL:13800138000").forEach { tpl ->
                    Surface(
                        onClick = { text = tpl },
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.65f),
                        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            tpl.take(12),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(vertical = 6.dp).fillMaxWidth()
                        )
                    }
                }
            }
        }
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap!!.asImageBitmap(),
                            contentDescription = "二维码",
                            modifier = Modifier.size(280.dp)
                        )
                    } else {
                        Text("输入文本自动生成二维码", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("二维码文本", text))
                        Toast.makeText(context, "已复制文本", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = JadeGreen)
                ) { Text("复制文本", color = Color.White, fontWeight = FontWeight.Bold) }
                Button(
                    onClick = {
                        // 保存到相册
                        val bmp = bitmap ?: run {
                            Toast.makeText(context, "没有可保存的二维码", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        try {
                            val resolver = context.contentResolver
                            val values = android.content.ContentValues().apply {
                                put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, "lzdz_qr_${System.currentTimeMillis()}.png")
                                put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/png")
                                if (android.os.Build.VERSION.SDK_INT >= 29) {
                                    put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, android.os.Environment.DIRECTORY_PICTURES + "/LzdzQR")
                                }
                            }
                            val uri = resolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                            if (uri != null) {
                                resolver.openOutputStream(uri).use { os ->
                                    if (os != null) bmp.compress(Bitmap.CompressFormat.PNG, 100, os)
                                }
                                Toast.makeText(context, "已保存到相册 LzdzQR 目录", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "保存失败", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "保存失败：${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                ) { Text("保存到相册", color = Color.White, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

/* ============================================================
 * 5) 调色卡 / 取色（v1.7.8）：HEX/RGB/HSL 互转
 * ============================================================ */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorCardScreenView() {
    val context = LocalContext.current
    var hexInput by remember { mutableStateOf("#6366F1") }
    var parsed by remember { mutableStateOf(parseHex(hexInput)) }
    var hslInput by remember { mutableStateOf("") }

    fun refresh(newHex: String) {
        hexInput = newHex
        parsed = parseHex(newHex)
        hslInput = ""
    }

    val presets = listOf(
        "#6366F1", "#EC4899", "#10B981", "#F59E0B", "#EF4444",
        "#0EA5E9", "#A855F7", "#F97316", "#14B8A6", "#84CC16",
        "#F43F5E", "#06B6D4", "#FB7185", "#22D3EE", "#000000", "#FFFFFF"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.6f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.8f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🎨 调色卡与取色", fontSize = 16.sp, fontWeight = FontWeight.Black)
                    Text("HEX / RGB / HSL 互转 · 预设配色 · 一键复制",
                        fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(parsed.color)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = parsed.hex.uppercase(),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = if (parsed.isLight) Color.Black else Color.White
                    )
                    Text(
                        text = "RGB(${parsed.r}, ${parsed.g}, ${parsed.b})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (parsed.isLight) Color.Black else Color.White
                    )
                    Text(
                        text = "HSL(${parsed.h.roundToInt()}, ${(parsed.s * 100).roundToInt()}%, ${(parsed.l * 100).roundToInt()}%)",
                        fontSize = 12.sp,
                        color = if (parsed.isLight) Color.Black.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }
        item {
            OutlinedTextField(
                value = hexInput,
                onValueChange = { refresh(it) },
                label = { Text("HEX 颜色 (如 #6366F1)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Text("预设色（点击取色）", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            androidx.compose.foundation.layout.FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                presets.forEach { hex ->
                    Surface(
                        onClick = { refresh(hex) },
                        shape = RoundedCornerShape(8.dp),
                        color = androidx.compose.ui.graphics.Color(parseHex(hex).color),
                        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f)),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            if (parseHex(hex).hex.equals(hexInput, ignoreCase = true)) {
                                Text("✓", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("HEX", parsed.hex))
                        Toast.makeText(context, "已复制 ${parsed.hex.uppercase()}", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = JadeGreen)
                ) { Text("复制 HEX", color = Color.White, fontWeight = FontWeight.Bold) }
                Button(
                    onClick = {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("RGB", "rgb(${parsed.r}, ${parsed.g}, ${parsed.b})"))
                        Toast.makeText(context, "已复制 RGB", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = FlameRed)
                ) { Text("复制 RGB", color = Color.White, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

private data class ParsedColor(val hex: String, val color: Long, val r: Int, val g: Int, val b: Int, val h: Float, val s: Float, val l: Float, val isLight: Boolean)

private fun parseHex(input: String): ParsedColor {
    val s = input.trim().removePrefix("#").let {
        if (it.length == 3) it.map { c -> "$c$c" }.joinToString("") else it
    }
    return try {
        val v = java.lang.Long.parseLong(s, 16)
        val r = ((v shr 16) and 0xFF).toInt()
        val g = ((v shr 8) and 0xFF).toInt()
        val b = (v and 0xFF).toInt()
        val (h, sl, l) = rgbToHsl(r, g, b)
        val isLight = (0.299 * r + 0.587 * g + 0.114 * b) > 160
        ParsedColor("#${s.take(6).uppercase()}", v and 0xFFFFFFL or 0xFF000000L, r, g, b, h, sl, l, isLight)
    } catch (_: Exception) {
        ParsedColor("#000000", 0xFF000000, 0, 0, 0, 0f, 0f, 0f, false)
    }
}

/** RGB to HSL (h ∈ [0, 360), s/l ∈ [0, 1]) */
private fun rgbToHsl(r: Int, g: Int, b: Int): Triple<Float, Float, Float> {
    val rf = r / 255f
    val gf = g / 255f
    val bf = b / 255f
    val max = maxOf(rf, gf, bf)
    val min = minOf(rf, gf, bf)
    val l = (max + min) / 2f
    var h = 0f
    var s = 0f
    if (max != min) {
        val d = max - min
        s = if (l > 0.5f) d / (2f - max - min) else d / (max + min)
        h = when (max) {
            rf -> (gf - bf) / d + (if (gf < bf) 6f else 0f)
            gf -> (bf - rf) / d + 2f
            else -> (rf - gf) / d + 4f
        }
        h /= 6f
    }
    return Triple(h * 360f, s, l)
}

/* ============================================================
 * 5) 单位换算器（v1.7.9 新增）：长度/重量/温度/面积/体积
 * ============================================================ */
@Composable
fun UnitConverterScreenView() {
    val context = LocalContext.current
    val types = listOf("长度", "重量", "温度", "面积", "体积")
    var typeIdx by remember { mutableStateOf(0) }
    var fromVal by remember { mutableStateOf("1") }
    var fromIdx by remember { mutableStateOf(0) }
    var toIdx by remember { mutableStateOf(1) }

    fun units(): List<Pair<String, Double>> = when (types[typeIdx]) {
        "长度" -> listOf("毫米" to 0.001, "厘米" to 0.01, "米" to 1.0, "千米" to 1000.0,
            "英寸" to 0.0254, "英尺" to 0.3048, "码" to 0.9144, "英里" to 1609.344)
        "重量" -> listOf("毫克" to 0.000001, "克" to 0.001, "千克" to 1.0, "吨" to 1000.0,
            "盎司" to 0.0283495, "磅" to 0.453592)
        "温度" -> listOf("摄氏度" to -1.0, "华氏度" to -2.0, "开尔文" to -3.0)
        "面积" -> listOf("平方厘米" to 0.0001, "平方米" to 1.0, "公顷" to 10000.0,
            "平方千米" to 1000000.0, "亩" to 666.6667, "英亩" to 4046.856)
        else -> listOf("毫升" to 0.001, "升" to 1.0, "立方米" to 1000.0,
            "立方厘米" to 0.000001, "加仑(美)" to 3.78541, "品脱(英)" to 0.568261)
    }

    fun convert(v: Double, from: Double, to: Double): Double {
        if (types[typeIdx] != "温度") return v * from / to
        val celsius = when (from) {
            -2.0 -> (v - 32.0) * 5.0 / 9.0
            -3.0 -> v - 273.15
            else -> v
        }
        return when (to) {
            -2.0 -> celsius * 9.0 / 5.0 + 32.0
            -3.0 -> celsius + 273.15
            else -> celsius
        }
    }

    LazyColumn(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.6f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.8f)), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Text("📏 单位换算器", fontSize = 16.sp, fontWeight = FontWeight.Black)
                    Text("长度/重量/温度/面积/体积 · 本地离线换算", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                types.forEachIndexed { i, t ->
                    Surface(
                        onClick = { typeIdx = i; fromIdx = 0; toIdx = 1 },
                        shape = RoundedCornerShape(20), color = if (i == typeIdx) Color(0xFF6366F1) else Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.weight(1f)
                    ) { Text(t, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.padding(vertical = 8.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                }
            }
        }
        item {
            OutlinedTextField(value = fromVal, onValueChange = { fromVal = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("数值") }, modifier = Modifier.fillMaxWidth())
        }
        item {
            val us = units()
            val fLabel = if (fromIdx < us.size) us[fromIdx].first else us[0].first
            val tLabel = if (toIdx < us.size) us[toIdx].first else us[1].first
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = fLabel, onValueChange = {}, readOnly = true, label = { Text("从") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = tLabel, onValueChange = {}, readOnly = true, label = { Text("到") }, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                androidx.compose.material3.Button(onClick = { if (fromIdx < us.size - 1) fromIdx++ }, modifier = Modifier.weight(1f)) { Text("从▲") }
                androidx.compose.material3.Button(onClick = { if (fromIdx > 0) fromIdx-- }, modifier = Modifier.weight(1f)) { Text("从▼") }
                androidx.compose.material3.Button(onClick = { if (toIdx < us.size - 1) toIdx++ }, modifier = Modifier.weight(1f)) { Text("到▲") }
                androidx.compose.material3.Button(onClick = { if (toIdx > 0) toIdx-- }, modifier = Modifier.weight(1f)) { Text("到▼") }
            }
        }
        item {
            val us = units()
            val result = try {
                val v = fromVal.toDoubleOrNull() ?: 0.0
                val f = if (fromIdx < us.size) us[fromIdx].second else 1.0
                val t = if (toIdx < us.size) us[toIdx].second else 1.0
                convert(v, f, t)
            } catch (e: Exception) { 0.0 }
            Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Text("= $result ${if (toIdx < us.size) us[toIdx].first else ""}", fontSize = 18.sp, fontWeight = FontWeight.Black)
                    Text("（${if (fromIdx < us.size) us[fromIdx].first else ""} → ${if (toIdx < us.size) us[toIdx].first else ""}）", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

/* ============================================================
 * 6) 进制转换器（v1.7.9 新增）：2/8/10/16 互转
 * ============================================================ */
@Composable
fun BaseConverterScreenView() {
    val context = LocalContext.current
    val radices = listOf(2 to "二进制", 8 to "八进制", 10 to "十进制", 16 to "十六进制")
    var input by remember { mutableStateOf("255") }
    var fromRadix by remember { mutableStateOf(10) }
    fun toValue(s: String, radix: Int): Long? = try { s.trim().toLong(radix) } catch (e: Exception) { null }

    LazyColumn(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.6f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.8f)), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Text("🔢 进制转换", fontSize = 16.sp, fontWeight = FontWeight.Black)
                    Text("2/8/10/16 进制互转 · 输入合法字符自动计算", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                radices.forEach { (r, name) ->
                    Surface(onClick = { fromRadix = r }, shape = RoundedCornerShape(20),
                        color = if (r == fromRadix) Color(0xFF6366F1) else Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.weight(1f)) {
                        Text(name, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.padding(vertical = 8.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        item {
            OutlinedTextField(value = input, onValueChange = { input = it.take(40) },
                label = { Text("输入 ${radices.first { r -> r.first == fromRadix }.second} 数值") }, modifier = Modifier.fillMaxWidth())
        }
        item {
            val value = toValue(input, fromRadix)
            if (value == null) {
                Text("❌ 输入包含该进制下的非法字符", color = Color(0xFFEF4444), fontSize = 13.sp)
            } else {
                radices.forEach { (r, name) ->
                    val s = when (r) {
                        2 -> java.lang.Long.toBinaryString(value)
                        8 -> java.lang.Long.toOctalString(value)
                        10 -> value.toString()
                        else -> java.lang.Long.toHexString(value).uppercase()
                    }
                    Surface(color = if (r == fromRadix) Color(0xFFE8F5E9) else Color.White.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                        Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("$name", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            Text(s, fontWeight = FontWeight.Black, fontSize = 15.sp)
                            IconButton(onClick = {
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cm.setPrimaryClip(ClipData.newPlainText("进制结果", s))
                                Toast.makeText(context, "已复制 $s", Toast.LENGTH_SHORT).show()
                            }) { Icon(Icons.Filled.ContentCopy, contentDescription = "复制", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp)) }
                        }
                    }
                }
            }
        }
    }
}


/* ============================================================
 * 7) 日期计算器（v1.7.9 新增）：日期差 / 加减天数 / 星期
 * ============================================================ */
@Composable
fun DateCalcScreenView() {
    val context = LocalContext.current
    var date1 by remember { mutableStateOf("2026-09-01") }
    var date2 by remember { mutableStateOf("2026-09-25") }
    var addDays by remember { mutableStateOf("30") }
    var baseDate by remember { mutableStateOf("2026-09-01") }
    val fmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.CHINA)

    fun parse(s: String): java.util.Calendar? = try {
        val d = fmt.parse(s) ?: return null
        java.util.Calendar.getInstance().apply { time = d }
    } catch (e: Exception) { null }

    val days = parse(date1)?.let { a -> parse(date2)?.let { b ->
        (b.timeInMillis - a.timeInMillis) / 86400000L
    } }

    LazyColumn(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.6f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.8f)), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Text("📅 日期计算", fontSize = 16.sp, fontWeight = FontWeight.Black)
                    Text("两个日期相差几天 · 日期加减天数 · 星期查询", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            OutlinedTextField(value = date1, onValueChange = { date1 = it }, label = { Text("日期一 (yyyy-MM-dd)") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(value = date2, onValueChange = { date2 = it }, label = { Text("日期二 (yyyy-MM-dd)") }, modifier = Modifier.fillMaxWidth())
        }
        item {
            Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Text("相差：${days ?: "--"} 天", fontSize = 18.sp, fontWeight = FontWeight.Black)
                    val week = arrayOf("日", "一", "二", "三", "四", "五", "六")
                    val w1 = parse(date1)?.get(java.util.Calendar.DAY_OF_WEEK)
                    val w2 = parse(date2)?.get(java.util.Calendar.DAY_OF_WEEK)
                    Text("$date1 星期${w1?.let { week[it - 1] } ?: "?"} · $date2 星期${w2?.let { week[it - 1] } ?: "?"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            OutlinedTextField(value = baseDate, onValueChange = { baseDate = it }, label = { Text("基准日期") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(value = addDays, onValueChange = { addDays = it.filter { c -> c.isDigit() || c == '-' } }, label = { Text("加减天数 (可负)") }, modifier = Modifier.fillMaxWidth())
        }
        item {
            val result = try {
                val c = parse(baseDate)
                if (c != null) { c.add(java.util.Calendar.DAY_OF_YEAR, addDays.toInt()); fmt.format(c.time) } else null
            } catch (e: Exception) { null }
            Surface(color = Color(0xFFFFF8E1), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Text("$baseDate ${if (addDays.startsWith("-")) "" else "+"}${addDays} 天 = $result", modifier = Modifier.padding(14.dp), fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/* ============================================================
 * 8) 简易计算器（v1.7.9 新增）：四则运算（含括号）求值
 * ============================================================ */
@Composable
fun CalculatorScreenView() {
    val context = LocalContext.current
    var expr by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }

    fun evalExpr(s: String): Double {
        return evalExprImpl(s)
    }

    fun calc(): String {
        if (expr.isBlank()) return ""
        return try {
            val v = evalExpr(expr.replace("×", "*").replace("÷", "/"))
            val asLong = v.toLong()
            if (v == asLong.toDouble() && kotlin.math.abs(v) < 1e15) asLong.toString() else v.toString()
        } catch (e: Exception) { "错误" }
    }

    val buttons = listOf(
        listOf("7", "8", "9", "÷"), listOf("4", "5", "6", "×"),
        listOf("1", "2", "3", "-"), listOf("C", "0", ".", "+"), listOf("(", ")", "=", "⌫")
    )

    LazyColumn(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.6f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.8f)), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Text("🧮 简易计算器", fontSize = 16.sp, fontWeight = FontWeight.Black)
                    Text("四则运算 + 括号 · 本地计算 · 支持小数", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            Surface(color = Color(0xFFF3F4F6), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Text(if (expr.isBlank()) "0" else expr, fontSize = 20.sp, fontWeight = FontWeight.Black,
                        textAlign = androidx.compose.ui.text.style.TextAlign.End, modifier = Modifier.fillMaxWidth())
                    Text("= ${if (result.isBlank()) "…" else result}", fontSize = 16.sp, color = Color(0xFF6366F1),
                        textAlign = androidx.compose.ui.text.style.TextAlign.End, modifier = Modifier.fillMaxWidth())
                }
            }
        }
        buttons.forEach { row ->
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    row.forEach { b ->
                        Surface(
                            onClick = {
                                when (b) {
                                    "C" -> { expr = ""; result = "" }
                                    "⌫" -> { expr = expr.dropLast(1) }
                                    "=" -> { result = calc() }
                                    else -> { expr += b; result = calc() }
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = if (b == "=") Color(0xFF6366F1) else Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(b, textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(vertical = 12.dp), fontSize = 16.sp,
                                fontWeight = FontWeight.Black, color = if (b == "=") Color.White else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    }
}

/* ============================================================
 * 9) Emoji 表情库（v1.7.9 新增）：常用表情一键复制
 * ============================================================ */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun EmojiPickerScreenView() {
    val context = LocalContext.current
    val groups = listOf(
        "😀😁😂🤣😃😄😅😆😉😊😋😎😍🥰😘😜🤪😝🤑🤗🤔🤨🤯😳🥺😢😭😤😡🤬😱😴🤤😷🤒🤕🤢🤮🥶🥵🤠🤡🤥😇",
        "❤️🧡💛💚💙💜🖤🤍🤎💔💕💞💓💗💖💘💝💟",
        "👍👎👊✊🤛🤜🤝👏🙌🙏💪✌️🤞🖖🤙👌🤘🫶🖐️🤲",
        "🐶🐱🐭🐹🐰🦊🐻🐼🐨🐯🦁🐮🐷🐸🐵🐔🐧🐦🐤🦆🦅🦉🦋🐝🐢🐍🐙🦀🐠🐬🐳🦄🐴🦓🐘🐪🦒🦘",
        "🍎🍐🍊🍋🍌🍉🍇🍓🫐🍈🍒🍑🥭🍍🥥🥝🍅🍆🥑🥦🥒🌽🥕🥔🍞🥐🥖🧀🥚🍳🥞🍔🍟🍕🌭🥪🌮🌯🍣🍤🍜🍲🍰🎂🍦🍩🍪☕🍵",
        "⚽🏀🏈⚾🎾🏐🏉🎱🏓🏸🥊🥋⛳🏹🎣🥇🥈🥉🏆🏅🎽🚴🚵🏇",
        "🚗🚕🚙🚌🚎🏎️🚓🚑🚒🚐🚚🚛🚜🚲🛵🏍️✈️🚀🛸🚁⛵🚢🚂🚄🚇🚉",
        "🏠🏡🏢🏣🏥🏦🏪🏫🏬🏭🏯🏰💒🏘️🗽🗼⛲🎡🎢🎠🌋🗻🏕️🏖️🏝️🌄🌅🌇🌆🌃🌉🌁",
        "🎉🎊🎁🎈🎆🎇✨🎃🎄🎅🤶🦌🎀🎗️🎟️🎬🎤🎧🎷🎸🎹🎺🥁🎨🏆",
        "🔥💧☀️🌙⭐🌟💫⚡❄️🌈☁️🌤️🌧️🌩️🌪️🌊🌋🌲🌴🌹🌷🌻🌸💐"
    )
    LazyColumn(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.6f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.8f)), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Text("😀 Emoji 表情库", fontSize = 16.sp, fontWeight = FontWeight.Black)
                    Text("点击任意表情一键复制 · 分 10 大类 · 聊天斗图不发愁", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        groups.forEach { group ->
            item {
                val emojis = group.toCharArray().joinToString("") { it.toString() }
                val list = emojis.split("").filter { it.isNotEmpty() && it.isNotBlank() }
                Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.55f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(10.dp)) {
                        androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            list.forEach { e ->
                                Surface(onClick = {
                                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    cm.setPrimaryClip(ClipData.newPlainText("emoji", e))
                                    Toast.makeText(context, "已复制 $e", Toast.LENGTH_SHORT).show()
                                }, shape = RoundedCornerShape(8.dp), color = Color.White.copy(alpha = 0.7f)) {
                                    Text(e, fontSize = 20.sp, modifier = Modifier.padding(6.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/* 计算器表达式求值（顶层实现：用状态类 + 显式递归，成员函数天然支持互相引用） */
private class ExprParser(val src: String) {
    var i = 0
    fun peek(): Char = if (i < src.length) src[i] else '\u0000'
    fun skipWs() { while (i < src.length && src[i].isWhitespace()) i++ }
    fun parseNumber(): Double {
        skipWs(); val start = i
        while (i < src.length && (src[i].isDigit() || src[i] == '.')) i++
        return src.substring(start, i).toDouble()
    }
    fun parsePrimary(): Double {
        skipWs()
        if (peek() == '(') { i++; val v = parseAdd(); skipWs(); if (peek() == ')') i++; return v }
        return parseNumber()
    }
    fun parseMul(): Double {
        var v = parsePrimary(); skipWs()
        while (peek() == '*' || peek() == '/') { val op = peek(); i++; val r = parsePrimary(); v = if (op == '*') v * r else v / r; skipWs() }
        return v
    }
    fun parseAdd(): Double {
        var v = parseMul(); skipWs()
        while (peek() == '+' || peek() == '-') { val op = peek(); i++; val r = parseMul(); v = if (op == '+') v + r else v - r; skipWs() }
        return v
    }
    fun parse(): Double { return parseAdd() }
}

private fun evalExprImpl(s: String): Double = ExprParser(s).parse()
