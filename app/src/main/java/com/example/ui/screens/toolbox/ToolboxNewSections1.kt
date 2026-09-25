package com.example.ui.screens.toolbox

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

/**
 * 极客百宝箱 · 新增小工具集（v1.8.1 第一部分：文本 / 编码 / 数字转换）
 *
 * 全部工具纯本地计算、防闪退：所有输入都做安全解析与 try-catch 兜底。
 */

/* ============================================================
 * 通用小组件
 * ============================================================ */
@Composable
fun NewToolHeader(title: String, icon: ImageVector, desc: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.60f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.70f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Black, fontSize = 16.sp)
                Text(desc, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun copyText(context: Context, label: String, text: String) {
    if (text.isBlank()) return
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText(label, text))
    Toast.makeText(context, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
}

/** 结果显示卡片（带复制按钮） */
@Composable
fun NewResultCard(result: String, context: Context, label: String = "结果") {
    if (result.isBlank()) return
    Spacer(modifier = Modifier.height(10.dp))
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(result, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
            IconButton(onClick = { copyText(context, label, result) }) {
                Icon(Icons.Filled.ContentCopy, contentDescription = "复制", modifier = Modifier.size(18.dp))
            }
        }
    }
}

/* ============================================================
 * 1) 文本反转
 * ============================================================ */
@Composable
fun ReverseTextScreenView() {
    val context = LocalContext.current
    var input by remember { mutableStateOf("") }
    var out by remember { mutableStateOf("") }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp)) {
        item {
            NewToolHeader("文本反转", Icons.Filled.TextFields, "将输入文本逐字倒序，支持中文与多行")
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = input, onValueChange = { input = it }, label = { Text("输入文本") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            Spacer(Modifier.height(8.dp))
            Button(onClick = { out = input.reversed() }, modifier = Modifier.fillMaxWidth()) { Text("反转") }
            NewResultCard(out, context)
        }
    }
}

/* ============================================================
 * 2) 大小写转换
 * ============================================================ */
@Composable
fun CaseConvertScreenView() {
    val context = LocalContext.current
    var input by remember { mutableStateOf("") }
    var out by remember { mutableStateOf("") }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp)) {
        item {
            NewToolHeader("大小写转换", Icons.Filled.Spellcheck, "全大写 / 全小写 / 首字母大写一键转换")
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = input, onValueChange = { input = it }, label = { Text("输入英文文本") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            Spacer(Modifier.height(8.dp))
            Row {
                Button(onClick = { out = input.uppercase() }, modifier = Modifier.weight(1f)) { Text("全大写") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { out = input.lowercase() }, modifier = Modifier.weight(1f)) { Text("全小写") }
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                out = input.split(" ").joinToString(" ") { w -> w.replaceFirstChar { it.uppercase() } }
            }, modifier = Modifier.fillMaxWidth()) { Text("单词首字母大写") }
            NewResultCard(out, context)
        }
    }
}

/* ============================================================
 * 3) 行去重去空
 * ============================================================ */
@Composable
fun LineDedupeScreenView() {
    val context = LocalContext.current
    var input by remember { mutableStateOf("") }
    var out by remember { mutableStateOf("") }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp)) {
        item {
            NewToolHeader("行去重去空", Icons.Filled.Sort, "按行去除重复内容与空白行")
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = input, onValueChange = { input = it }, label = { Text("每行一条内容") }, modifier = Modifier.fillMaxWidth(), minLines = 5)
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                out = input.lines().map { it.trim() }.filter { it.isNotEmpty() }.distinct().joinToString("\n")
            }, modifier = Modifier.fillMaxWidth()) { Text("去重去空") }
            NewResultCard(out, context)
        }
    }
}

/* ============================================================
 * 4) 行排序
 * ============================================================ */
@Composable
fun LineSortScreenView() {
    val context = LocalContext.current
    var input by remember { mutableStateOf("") }
    var out by remember { mutableStateOf("") }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp)) {
        item {
            NewToolHeader("行排序", Icons.Filled.Sort, "按字母 / 数字升序排列多行内容")
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = input, onValueChange = { input = it }, label = { Text("每行一条内容") }, modifier = Modifier.fillMaxWidth(), minLines = 5)
            Spacer(Modifier.height(8.dp))
            Row {
                Button(onClick = { out = input.lines().filter { it.isNotBlank() }.sorted().joinToString("\n") }, modifier = Modifier.weight(1f)) { Text("升序") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { out = input.lines().filter { it.isNotBlank() }.sortedDescending().joinToString("\n") }, modifier = Modifier.weight(1f)) { Text("降序") }
            }
            NewResultCard(out, context)
        }
    }
}

/* ============================================================
 * 5) JSON 格式化
 * ============================================================ */
@Composable
fun JsonFormatScreenView() {
    val context = LocalContext.current
    var input by remember { mutableStateOf("") }
    var out by remember { mutableStateOf("") }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp)) {
        item {
            NewToolHeader("JSON 格式化", Icons.Filled.DataObject, "压缩/美化 JSON，验证合法性（依赖 Android 内置 org.json）")
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = input, onValueChange = { input = it }, label = { Text("粘贴 JSON") }, modifier = Modifier.fillMaxWidth(), minLines = 5)
            Spacer(Modifier.height(8.dp))
            Row {
                Button(onClick = {
                    out = try {
                        org.json.JSONObject(input).toString(2)
                    } catch (e1: Exception) {
                        try { org.json.JSONArray(input).toString(2) } catch (e2: Exception) { "JSON 解析失败：请检查格式" }
                    }
                }, modifier = Modifier.weight(1f)) { Text("美化") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = {
                    out = try {
                        org.json.JSONObject(input).toString()
                    } catch (e1: Exception) {
                        try { org.json.JSONArray(input).toString() } catch (e2: Exception) { "JSON 解析失败：请检查格式" }
                    }
                }, modifier = Modifier.weight(1f)) { Text("压缩") }
            }
            NewResultCard(out, context)
        }
    }
}

/* ============================================================
 * 6) HTML 实体转义
 * ============================================================ */
@Composable
fun HtmlEscapeScreenView() {
    val context = LocalContext.current
    var input by remember { mutableStateOf("") }
    var out by remember { mutableStateOf("") }
    fun esc(s: String) = s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;")
    fun unesc(s: String) = s.replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replace("&#39;", "'").replace("&amp;", "&")
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp)) {
        item {
            NewToolHeader("HTML 实体转义", Icons.Filled.Code, "把 < > & \" 转义为 HTML 实体或反向还原")
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = input, onValueChange = { input = it }, label = { Text("输入文本 / HTML") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            Spacer(Modifier.height(8.dp))
            Row {
                Button(onClick = { out = esc(input) }, modifier = Modifier.weight(1f)) { Text("转义") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { out = unesc(input) }, modifier = Modifier.weight(1f)) { Text("反转义") }
            }
            NewResultCard(out, context)
        }
    }
}

/* ============================================================
 * 7) Unicode / \\u 编解码
 * ============================================================ */
@Composable
fun UnicodeCodecScreenView() {
    val context = LocalContext.current
    var input by remember { mutableStateOf("") }
    var out by remember { mutableStateOf("") }
    fun toUnicode(s: String): String {
        val sb = StringBuilder()
        for (ch in s) sb.append(if (ch.code > 127) "\\u%04x".format(ch.code) else ch.toString())
        return sb.toString()
    }
    fun fromUnicode(s: String): String {
        val sb = StringBuilder()
        var i = 0
        while (i < s.length) {
            if (s[i] == '\\' && i + 5 < s.length && s[i + 1] == 'u') {
                try {
                    val code = s.substring(i + 2, i + 6).toInt(16)
                    sb.append(code.toChar())
                    i += 6
                } catch (e: Exception) { sb.append(s[i]); i++ }
            } else { sb.append(s[i]); i++ }
        }
        return sb.toString()
    }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp)) {
        item {
            NewToolHeader("Unicode 编解码", Icons.Filled.Tag, "文本与 \\uXXXX 转义互相转换，也支持 Emoji")
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = input, onValueChange = { input = it }, label = { Text("输入文本或 \\u 序列") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            Spacer(Modifier.height(8.dp))
            Row {
                Button(onClick = { out = try { toUnicode(input) } catch (e: Exception) { "转换失败" } }, modifier = Modifier.weight(1f)) { Text("编码") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { out = try { fromUnicode(input) } catch (e: Exception) { "解码失败" } }, modifier = Modifier.weight(1f)) { Text("解码") }
            }
            NewResultCard(out, context)
        }
    }
}

/* ============================================================
 * 8) 摩斯电码
 * ============================================================ */
@Composable
fun MorseCodeScreenView() {
    val context = LocalContext.current
    var input by remember { mutableStateOf("") }
    var out by remember { mutableStateOf("") }
    val map = mapOf(
        'a' to ".-", 'b' to "-...", 'c' to "-.-.", 'd' to "-..", 'e' to ".", 'f' to "..-.", 'g' to "--.",
        'h' to "....", 'i' to "..", 'j' to ".---", 'k' to "-.-", 'l' to ".-..", 'm' to "--", 'n' to "-.",
        'o' to "---", 'p' to ".--.", 'q' to "--.-", 'r' to ".-.", 's' to "...", 't' to "-", 'u' to "..-",
        'v' to "...-", 'w' to ".--", 'x' to "-..-", 'y' to "-.--", 'z' to "--..",
        '0' to "-----", '1' to ".----", '2' to "..---", '3' to "...--", '4' to "....-", '5' to ".....",
        '6' to "-....", '7' to "--...", '8' to "---..", '9' to "----."
    )
    val rev = map.entries.associate { (k, v) -> v to k }
    fun encode(s: String) = s.lowercase().map { map[it] }.filterNotNull().joinToString(" ")
    fun decode(s: String): String {
        val parts = s.trim().split(Regex("\\s+"))
        val sb = StringBuilder()
        for (p in parts) sb.append(rev[p] ?: '?')
        return sb.toString()
    }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp)) {
        item {
            NewToolHeader("摩斯电码", Icons.Filled.Key, "字母数字与摩斯码互相转换（空格分隔）")
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = input, onValueChange = { input = it }, label = { Text("输入文字或摩斯码") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            Spacer(Modifier.height(8.dp))
            Row {
                Button(onClick = { out = try { encode(input) } catch (e: Exception) { "转换失败" } }, modifier = Modifier.weight(1f)) { Text("加密") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { out = try { decode(input) } catch (e: Exception) { "解密失败" } }, modifier = Modifier.weight(1f)) { Text("解密") }
            }
            NewResultCard(out, context)
        }
    }
}

/* ============================================================
 * 9) 凯撒密码
 * ============================================================ */
@Composable
fun CaesarCipherScreenView() {
    val context = LocalContext.current
    var input by remember { mutableStateOf("") }
    var shiftText by remember { mutableStateOf("3") }
    var out by remember { mutableStateOf("") }
    fun shift(s: String, n: Int): String {
        val sb = StringBuilder()
        for (ch in s) {
            when {
                ch in 'a'..'z' -> sb.append('a' + ((ch - 'a' + n) % 26 + 26) % 26)
                ch in 'A'..'Z' -> sb.append('A' + ((ch - 'A' + n) % 26 + 26) % 26)
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp)) {
        item {
            NewToolHeader("凯撒密码", Icons.Filled.Lock, "英文字母按位移加密/解密，经典古典密码")
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = input, onValueChange = { input = it }, label = { Text("输入英文文本") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = shiftText, onValueChange = { shiftText = it.filter { c -> c.isDigit() || c == '-' } }, label = { Text("位移量（如 3）") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Row {
                Button(onClick = {
                    val n = shiftText.toIntOrNull() ?: 3
                    out = try { shift(input, n) } catch (e: Exception) { "转换失败" }
                }, modifier = Modifier.weight(1f)) { Text("加密") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = {
                    val n = shiftText.toIntOrNull() ?: 3
                    out = try { shift(input, -n) } catch (e: Exception) { "转换失败" }
                }, modifier = Modifier.weight(1f)) { Text("解密") }
            }
            NewResultCard(out, context)
        }
    }
}

/* ============================================================
 * 10) 数字转中文大写
 * ============================================================ */
@Composable
fun NumToCnScreenView() {
    val context = LocalContext.current
    var input by remember { mutableStateOf("") }
    var out by remember { mutableStateOf("") }
    fun numToCn(num: Long): String {
        if (num == 0L) return "零"
        val digits = arrayOf("零", "一", "二", "三", "四", "五", "六", "七", "八", "九")
        val units = arrayOf("", "十", "百", "千", "万", "亿")
        val s = num.toString()
        val n = s.length
        val sb = StringBuilder()
        var zero = false
        for (i in 0 until n) {
            val d = s[i] - '0'
            val pos = n - i - 1
            if (d == 0) zero = true
            else {
                if (zero) { sb.append("零"); zero = false }
                sb.append(digits[d]).append(units[pos])
            }
        }
        return sb.toString()
    }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp)) {
        item {
            NewToolHeader("数字转中文", Icons.Filled.Numbers, "阿拉伯数字转中文读法（支持到亿）")
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = input, onValueChange = { input = it.filter { c -> c.isDigit() } }, label = { Text("输入整数（如 1024）") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                out = try { numToCn(input.toLong()) } catch (e: Exception) { "请输入 0-9 亿范围内的整数" }
            }, modifier = Modifier.fillMaxWidth()) { Text("转换") }
            NewResultCard(out, context)
        }
    }
}

/* ============================================================
 * 11) 金额大写
 * ============================================================ */
@Composable
fun MoneyUpperScreenView() {
    val context = LocalContext.current
    var input by remember { mutableStateOf("") }
    var out by remember { mutableStateOf("") }
    fun moneyUpper(amount: String): String {
        val v = amount.toBigDecimalOrNull() ?: return "请输入有效金额"
        if (v.signum() < 0) return "暂不支持负数"
        val big = v.movePointRight(2).setScale(0, RoundingMode.HALF_UP).toLong()
        if (big == 0L) return "零元整"
        if (big > 99999999999999L) return "金额过大（最大 9999 亿）"
        val up = arrayOf("零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖")
        val unit = arrayOf("", "拾", "佰", "仟")
        val section = arrayOf("", "万", "亿", "兆")
        var num = big
        val sb = StringBuilder()
        var secPos = 0
        while (num > 0) {
            val sec = (num % 10000).toInt()
            if (sec != 0) {
                var tmp = StringBuilder()
                var zero = false
                for (i in 3 downTo 0) {
                    val d = (sec / Math.pow(10.0, i.toDouble()).toInt()) % 10
                    if (d == 0) {
                        if (tmp.isNotEmpty() && !zero) { tmp.append("零"); zero = true }
                    } else {
                        tmp.append(up[d]).append(unit[i]); zero = false
                    }
                }
                sb.insert(0, tmp.toString() + section[secPos])
            } else if (sb.isNotEmpty() && !sb.startsWith("零")) {
                sb.insert(0, "零")
            }
            num /= 10000; secPos++
        }
        sb.append("元")
        // 角分（统一转 Int，避免 Long/Int 比较编译错误）
        val jiao = ((big % 100) / 10).toInt()
        val fen = (big % 10).toInt()
        if (jiao == 0 && fen == 0) sb.append("整")
        else {
            if (jiao != 0) sb.append(up[jiao]).append("角")
            else if (sb.isNotEmpty()) sb.append("零")
            if (fen != 0) sb.append(up[fen]).append("分")
        }
        return sb.toString()
    }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp)) {
        item {
            NewToolHeader("金额大写", Icons.Filled.Payment, "人民币数字转大写（元角分）")
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = input, onValueChange = { input = it }, label = { Text("输入金额（如 1234.56）") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Button(onClick = { out = try { moneyUpper(input) } catch (e: Exception) { "转换失败：请检查输入" } }, modifier = Modifier.fillMaxWidth()) { Text("转换大写") }
            NewResultCard(out, context)
        }
    }
}

/* ============================================================
 * 12) RGB / HEX 颜色互转
 * ============================================================ */
@Composable
fun RgbHexScreenView() {
    val context = LocalContext.current
    var hex by remember { mutableStateOf("#6366F1") }
    var rgb by remember { mutableStateOf("99,102,241") }
    var out by remember { mutableStateOf("") }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp)) {
        item {
            NewToolHeader("RGB/HEX 互转", Icons.Filled.Palette, "HEX 色值解析为 RGB，或 RGB 转 HEX")
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = hex, onValueChange = { hex = it }, label = { Text("HEX 色值（如 #6366F1）") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                out = try {
                    val h = hex.trim().removePrefix("#")
                    if (h.length != 6) "请输入 6 位 HEX（如 6366F1）"
                    else {
                        val r = h.substring(0, 2).toInt(16)
                        val g = h.substring(2, 4).toInt(16)
                        val b = h.substring(4, 6).toInt(16)
                        "R=$r  G=$g  B=$b"
                    }
                } catch (e: Exception) { "解析失败：请检查 HEX 格式" }
            }, modifier = Modifier.fillMaxWidth()) { Text("HEX → RGB") }
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(value = rgb, onValueChange = { rgb = it }, label = { Text("RGB 输入（如 99,102,241）") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                out = try {
                    val parts = rgb.split(",", "，").map { it.trim().toInt() }
                    if (parts.size != 3 || parts.any { it !in 0..255 }) "RGB 分量需为 0-255 的三个数"
                    else "#%02X%02X%02X".format(parts[0], parts[1], parts[2])
                } catch (e: Exception) { "解析失败：请输入 0-255 的三个整数，逗号分隔" }
            }, modifier = Modifier.fillMaxWidth()) { Text("RGB → HEX") }
            NewResultCard(out, context)
        }
    }
}

/* ============================================================
 * 13) 文件大小换算
 * ============================================================ */
@Composable
fun FileSizeScreenView() {
    val context = LocalContext.current
    var input by remember { mutableStateOf("") }
    var out by remember { mutableStateOf("") }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp)) {
        item {
            NewToolHeader("文件大小换算", Icons.Filled.Storage, "MB/GB/TB 互转与人性化可读大小")
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = input, onValueChange = { input = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("输入数字") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Row {
                Button(onClick = {
                    val v = input.toDoubleOrNull() ?: 0.0
                    out = "GB: %.4f\nMB: %.4f\nKB: %.4f\nB: %.0f".format(v / 1024.0 / 1024 / 1024, v / 1024.0 / 1024, v / 1024.0, v)
                }, modifier = Modifier.weight(1f)) { Text("B→单位") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = {
                    val v = input.toDoubleOrNull() ?: 0.0
                    out = "TB: %.4f\nGB: %.4f\nMB: %.4f\nKB: %.4f".format(v / 1024.0 / 1024 / 1024 / 1024, v / 1024.0 / 1024 / 1024, v / 1024.0 / 1024, v / 1024.0)
                }, modifier = Modifier.weight(1f)) { Text("GB→单位") }
            }
            NewResultCard(out, context)
        }
    }
}