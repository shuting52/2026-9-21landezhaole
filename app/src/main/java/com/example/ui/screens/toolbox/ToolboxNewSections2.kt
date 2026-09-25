package com.example.ui.screens.toolbox

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
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.pow

/**
 * 极客百宝箱 · 新增小工具集（v1.8.1 第二部分：计算 / 生活 / 趣味）
 *
 * 全部本地纯计算、防闪退：输入安全解析 + try-catch 兜底。
 */

/* ============================================================
 * 14) 百分比计算器
 * ============================================================ */
@Composable
fun PercentCalcScreenView() {
    val context = LocalContext.current
    var a by remember { mutableStateOf("") }
    var b by remember { mutableStateOf("") }
    var out by remember { mutableStateOf("") }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp)) {
        item {
            NewToolHeader("百分比计算", Icons.Filled.Percent, "A 占 B 的百分比、A 的 B% 是多少")
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = a, onValueChange = { a = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("数值 A") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = b, onValueChange = { b = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("数值 B（或百分比）") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Row {
                Button(onClick = {
                    val x = a.toDoubleOrNull(); val y = b.toDoubleOrNull()
                    out = if (x != null && y != null && y != 0.0)
                        "A 占 B 的 %.2f%%".format(x / y * 100)
                    else "请输入有效数字（B 不能为 0）"
                }, modifier = Modifier.weight(1f)) { Text("占比%") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = {
                    val x = a.toDoubleOrNull(); val y = b.toDoubleOrNull()
                    out = if (x != null && y != null) "A 的 B% = %.2f".format(x * y / 100)
                    else "请输入有效数字"
                }, modifier = Modifier.weight(1f)) { Text("求A×B%") }
            }
            NewResultCard(out, context)
        }
    }
}

/* ============================================================
 * 15) 折扣计算器
 * ============================================================ */
@Composable
fun DiscountCalcScreenView() {
    val context = LocalContext.current
    var price by remember { mutableStateOf("") }
    var discount by remember { mutableStateOf("") }
    var out by remember { mutableStateOf("") }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp)) {
        item {
            NewToolHeader("折扣计算", Icons.Filled.LocalOffer, "原价与折扣算折后价/节省金额")
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = price, onValueChange = { price = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("原价（元）") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = discount, onValueChange = { discount = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("折扣（如 8.5 = 85折）") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                val p = price.toDoubleOrNull(); val d = discount.toDoubleOrNull()
                out = if (p != null && d != null && d in 0.0..10.0)
                    "折后价：%.2f 元\n节省：%.2f 元".format(p * d / 10, p - p * d / 10)
                else "请输入有效数字（折扣 0-10）"
            }, modifier = Modifier.fillMaxWidth()) { Text("计算") }
            NewResultCard(out, context)
        }
    }
}

/* ============================================================
 * 16) 房贷计算器（等额本息 / 等额本金）
 * ============================================================ */
@Composable
fun MortgageCalcScreenView() {
    val context = LocalContext.current
    var principal by remember { mutableStateOf("") }
    var years by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("") }
    var out by remember { mutableStateOf("") }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp)) {
        item {
            NewToolHeader("房贷计算器", Icons.Filled.Balance, "等额本息 / 等额本金月供试算")
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = principal, onValueChange = { principal = it.filter { c -> c.isDigit() } }, label = { Text("贷款总额（万元）") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = years, onValueChange = { years = it.filter { c -> c.isDigit() } }, label = { Text("贷款年限（年）") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = rate, onValueChange = { rate = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("年利率%（如 3.9）") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                val p = (principal.toDoubleOrNull() ?: 0.0) * 10000
                val n = (years.toDoubleOrNull() ?: 0.0)
                val r = (rate.toDoubleOrNull() ?: 0.0) / 100 / 12
                if (p <= 0 || n <= 0) out = "请输入有效的贷款金额与年限"
                else {
                    val months = (n * 12).toInt()
                    try {
                        if (r == 0.0) {
                            out = "等额本息/本金月供：%.2f 元\n总利息：0 元".format(p / months)
                        } else {
                            val mi = r * (1 + r).pow(months) / ((1 + r).pow(months) - 1)
                            val monthly = p * mi
                            val total = monthly * months
                            // 等额本金
                            val first = p / months + p * r
                            val last = p / months + (p - p * (months - 1) / months) * r
                            out = "等额本息：每月 %.2f 元\n总还款 %.2f 元，总利息 %.2f 元\n\n等额本金：首月 %.2f 元\n末月 %.2f 元\n总利息约 %.2f 元".format(
                                monthly, total, total - p, first, last, (p * r * (months + 1) / 2))
                        }
                    } catch (e: Exception) { out = "计算失败：请检查输入" }
                }
            }, modifier = Modifier.fillMaxWidth()) { Text("计算月供") }
            NewResultCard(out, context)
        }
    }
}

/* ============================================================
 * 17) 复利计算器
 * ============================================================ */
@Composable
fun CompoundInterestScreenView() {
    val context = LocalContext.current
    var principal by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("") }
    var years by remember { mutableStateOf("") }
    var out by remember { mutableStateOf("") }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp)) {
        item {
            NewToolHeader("复利计算器", Icons.Filled.AttachMoney, "本金按年复利滚存终值计算")
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = principal, onValueChange = { principal = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("本金（元）") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = rate, onValueChange = { rate = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("年利率%（如 5）") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = years, onValueChange = { years = it.filter { c -> c.isDigit() } }, label = { Text("年数") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                val p = principal.toDoubleOrNull() ?: 0.0
                val r = (rate.toDoubleOrNull() ?: 0.0) / 100
                val n = years.toIntOrNull() ?: 0
                out = if (p > 0 && n > 0)
                    "终值：%.2f 元\n利息收益：%.2f 元".format(p * (1 + r).pow(n), p * (1 + r).pow(n) - p)
                else "请输入有效的本金与年数"
            }, modifier = Modifier.fillMaxWidth()) { Text("计算终值") }
            NewResultCard(out, context)
        }
    }
}

/* ============================================================
 * 18) BMR 基础代谢
 * ============================================================ */
@Composable
fun BmrCalcScreenView() {
    val context = LocalContext.current
    var sex by remember { mutableStateOf("男") }
    var age by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var out by remember { mutableStateOf("") }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp)) {
        item {
            NewToolHeader("BMR 基础代谢", Icons.Filled.Favorite, "Mifflin-St Jeor 公式估算每日基础消耗")
            Spacer(Modifier.height(10.dp))
            Row {
                Button(onClick = { sex = "男" }, modifier = Modifier.weight(1f)) { Text(if (sex == "男") "✓ 男" else "男") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { sex = "女" }, modifier = Modifier.weight(1f)) { Text(if (sex == "女") "✓ 女" else "女") }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = age, onValueChange = { age = it.filter { c -> c.isDigit() } }, label = { Text("年龄") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = height, onValueChange = { height = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("身高 cm") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = weight, onValueChange = { weight = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("体重 kg") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                val a = age.toIntOrNull() ?: 0
                val h = height.toDoubleOrNull() ?: 0.0
                val w = weight.toDoubleOrNull() ?: 0.0
                if (a in 1..120 && h > 50 && w > 10) {
                    val bmr = if (sex == "男") 10 * w + 6.25 * h - 5 * a + 5 else 10 * w + 6.25 * h - 5 * a - 161
                    out = "BMR：%.0f 千卡/天\n\n活动消耗估算：\n久坐：%.0f 千卡\n轻度：%.0f 千卡\n中度：%.0f 千卡\n重度：%.0f 千卡".format(
                        bmr, bmr * 1.2, bmr * 1.375, bmr * 1.55, bmr * 1.725)
                } else out = "请输入有效数值"
            }, modifier = Modifier.fillMaxWidth()) { Text("计算") }
            NewResultCard(out, context)
        }
    }
}

/* ============================================================
 * 19) 标准体重
 * ============================================================ */
@Composable
fun IdealWeightScreenView() {
    val context = LocalContext.current
    var sex by remember { mutableStateOf("男") }
    var height by remember { mutableStateOf("") }
    var out by remember { mutableStateOf("") }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp)) {
        item {
            NewToolHeader("标准体重", Icons.Filled.MonitorWeight, "按身高性别计算标准体重与波动范围")
            Spacer(Modifier.height(10.dp))
            Row {
                Button(onClick = { sex = "男" }, modifier = Modifier.weight(1f)) { Text(if (sex == "男") "✓ 男" else "男") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { sex = "女" }, modifier = Modifier.weight(1f)) { Text(if (sex == "女") "✓ 女" else "女") }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = height, onValueChange = { height = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("身高 cm") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                val h = height.toDoubleOrNull() ?: 0.0
                if (h > 50) {
                    val ideal = if (sex == "男") (h - 80) * 0.7 else (h - 70) * 0.6
                    out = "标准体重：%.1f kg\n正常范围：%.1f ~ %.1f kg".format(ideal, ideal * 0.9, ideal * 1.1)
                } else out = "请输入有效身高"
            }, modifier = Modifier.fillMaxWidth()) { Text("计算") }
            NewResultCard(out, context)
        }
    }
}

/* ============================================================
 * 20) 生肖查询
 * ============================================================ */
@Composable
fun ZodiacScreenView() {
    val context = LocalContext.current
    var year by remember { mutableStateOf("") }
    var out by remember { mutableStateOf("") }
    val zodiac = listOf("鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪")
    val branch = listOf("子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥")
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp)) {
        item {
            NewToolHeader("生肖查询", Icons.Filled.Pets, "出生年份查生肖与天干地支")
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = year, onValueChange = { year = it.filter { c -> c.isDigit() } }, label = { Text("出生年份（如 1990）") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                val y = year.toIntOrNull()
                if (y != null && y in 1900..2100) {
                    val idxZ = ((y - 4) % 12 + 12) % 12
                    val idxB = ((y - 4) % 12 + 12) % 12
                    val gan = listOf("甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸")[(y - 4) % 10]
                    out = "生肖：${zodiac[idxZ]}\n地支：${branch[idxB]}\n干支：$gan${branch[idxB]} 年\n年龄（2026 年）：${2026 - y} 岁"
                } else out = "请输入 1900-2100 之间的年份"
            }, modifier = Modifier.fillMaxWidth()) { Text("查询") }
            NewResultCard(out, context)
        }
    }
}

/* ============================================================
 * 21) 日期差计算
 * ============================================================ */
@Composable
fun DateDiffScreenView() {
    val context = LocalContext.current
    var d1 by remember { mutableStateOf("") }
    var d2 by remember { mutableStateOf("") }
    var out by remember { mutableStateOf("") }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp)) {
        item {
            NewToolHeader("日期差计算", Icons.Filled.DateRange, "两个日期相差多少天（yyyy-MM-dd）")
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = d1, onValueChange = { d1 = it }, label = { Text("日期一（如 2025-01-01）") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = d2, onValueChange = { d2 = it }, label = { Text("日期二") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                try {
                    val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    fmt.isLenient = false
                    val a = fmt.parse(d1.trim()) ?: throw Exception()
                    val b = fmt.parse(d2.trim()) ?: throw Exception()
                    val diff = (b.time - a.time) / (1000 * 60 * 60 * 24)
                    out = "相差：${kotlin.math.abs(diff)} 天\n${if (diff >= 0) "日期二晚于日期一 ${diff} 天" else "日期二早于日期一 ${-diff} 天"}"
                } catch (e: Exception) { out = "格式错误：请使用 yyyy-MM-dd" }
            }, modifier = Modifier.fillMaxWidth()) { Text("计算天数") }
            NewResultCard(out, context)
        }
    }
}

/* ============================================================
 * 22) 时间差计算
 * ============================================================ */
@Composable
fun TimeDiffScreenView() {
    val context = LocalContext.current
    var h1 by remember { mutableStateOf("") }
    var m1 by remember { mutableStateOf("") }
    var h2 by remember { mutableStateOf("") }
    var m2 by remember { mutableStateOf("") }
    var out by remember { mutableStateOf("") }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp)) {
        item {
            NewToolHeader("时间差计算", Icons.Filled.Timer, "计算两个时刻的间隔（24 小时制）")
            Spacer(Modifier.height(10.dp))
            Row {
                OutlinedTextField(value = h1, onValueChange = { h1 = it.filter { c -> c.isDigit() } }, label = { Text("开始 时") }, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(value = m1, onValueChange = { m1 = it.filter { c -> c.isDigit() } }, label = { Text("开始 分") }, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Row {
                OutlinedTextField(value = h2, onValueChange = { h2 = it.filter { c -> c.isDigit() } }, label = { Text("结束 时") }, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(value = m2, onValueChange = { m2 = it.filter { c -> c.isDigit() } }, label = { Text("结束 分") }, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                val ha = h1.toIntOrNull() ?: -1; val ma = m1.toIntOrNull() ?: -1
                val hb = h2.toIntOrNull() ?: -1; val mb = m2.toIntOrNull() ?: -1
                if (ha in 0..23 && ma in 0..59 && hb in 0..23 && mb in 0..59) {
                    val t1 = ha * 60 + ma; val t2 = hb * 60 + mb
                    var d = t2 - t1
                    if (d < 0) d += 24 * 60
                    out = "间隔：${d / 60} 小时 ${d % 60} 分\n（共 ${d} 分钟）"
                } else out = "请输入合法的 0-23 时、0-59 分"
            }, modifier = Modifier.fillMaxWidth()) { Text("计算间隔") }
            NewResultCard(out, context)
        }
    }
}

/* ============================================================
 * 23) 抽奖转盘 / 随机点名
 * ============================================================ */
@Composable
fun LotteryScreenView() {
    val context = LocalContext.current
    var items by remember { mutableStateOf("") }
    var out by remember { mutableStateOf("") }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp)) {
        item {
            NewToolHeader("随机点名 / 抽奖", Icons.Filled.Casino, "录入名单随机抽取（每行一个）")
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = items, onValueChange = { items = it }, label = { Text("每行一个选项") }, modifier = Modifier.fillMaxWidth(), minLines = 5)
            Spacer(Modifier.height(8.dp))
            Row {
                Button(onClick = {
                    val list = items.lines().map { it.trim() }.filter { it.isNotEmpty() }
                    out = if (list.isEmpty()) "请先输入选项"
                    else "抽中：🎉 ${list[ThreadLocalRandom.current().nextInt(list.size)]}"
                }, modifier = Modifier.weight(1f)) { Text("抽 1 个") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = {
                    val list = items.lines().map { it.trim() }.filter { it.isNotEmpty() }
                    out = if (list.size < 2) "至少需要 2 个选项"
                    else "随机排序：\n" + list.shuffled().joinToString("\n")
                }, modifier = Modifier.weight(1f)) { Text("随机排序") }
            }
            NewResultCard(out, context)
        }
    }
}

/* ============================================================
 * 24) 石头剪刀布
 * ============================================================ */
@Composable
fun RpsScreenView() {
    val context = LocalContext.current
    var out by remember { mutableStateOf("点击下方出拳开始游戏") }
    val rps = listOf("✊ 石头", "✌️ 剪刀", "🖐 布")
    fun play(mine: Int) {
        val ai = ThreadLocalRandom.current().nextInt(3)
        val result = when {
            mine == ai -> "平局"
            (mine == 0 && ai == 1) || (mine == 1 && ai == 2) || (mine == 2 && ai == 0) -> "你赢了 🎉"
            else -> "你输了 😅"
        }
        out = "你出：${rps[mine]}　对手：${rps[ai]}\n结果：$result"
    }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp)) {
        item {
            NewToolHeader("石头剪刀布", Icons.Filled.SportsEsports, "与电脑对战猜拳")
            Spacer(Modifier.height(10.dp))
            Text(out, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(14.dp))
            Row {
                Button(onClick = { play(0) }, modifier = Modifier.weight(1f)) { Text("✊\n石头", fontSize = 12.sp) }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { play(1) }, modifier = Modifier.weight(1f)) { Text("✌️\n剪刀", fontSize = 12.sp) }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { play(2) }, modifier = Modifier.weight(1f)) { Text("🖐\n布", fontSize = 12.sp) }
            }
        }
    }
}

/* ============================================================
 * 25) 猜数字游戏
 * ============================================================ */
@Composable
fun GuessNumberScreenView() {
    val context = LocalContext.current
    var target by remember { mutableStateOf(ThreadLocalRandom.current().nextInt(1, 101)) }
    var guess by remember { mutableStateOf("") }
    var out by remember { mutableStateOf("已生成 1-100 的随机数，开始猜吧！") }
    var count by remember { mutableStateOf(0) }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp)) {
        item {
            NewToolHeader("猜数字游戏", Icons.Filled.Casino, "1-100 之间猜数字，越猜越近")
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = guess, onValueChange = { guess = it.filter { c -> c.isDigit() } }, label = { Text("输入 1-100") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Row {
                Button(onClick = {
                    val g = guess.toIntOrNull()
                    if (g == null) { out = "请输入数字"; return@Button }
                    count++
                    out = when {
                        g < target -> "第 $count 次：$g 小了，再大一点 ↑"
                        g > target -> "第 $count 次：$g 大了，再小一点 ↓"
                        else -> "🎉 恭喜！第 $count 次猜中 $target！"
                    }
                }, modifier = Modifier.weight(1f)) { Text("猜！") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = {
                    target = ThreadLocalRandom.current().nextInt(1, 101); count = 0
                    out = "已重置，重新开始猜吧！"
                    guess = ""
                }, modifier = Modifier.weight(1f)) { Text("重开") }
            }
            Spacer(Modifier.height(8.dp))
            Text(out, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

/* ============================================================
 * 26) 密码强度检测
 * ============================================================ */
@Composable
fun PasswordStrengthScreenView() {
    val context = LocalContext.current
    var input by remember { mutableStateOf("") }
    var out by remember { mutableStateOf("") }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp)) {
        item {
            NewToolHeader("密码强度检测", Icons.Filled.Security, "评估密码复杂度并给出建议")
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = input, onValueChange = { input = it }, label = { Text("输入密码") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                val s = input
                if (s.isEmpty()) { out = "请输入密码"; return@Button }
                var score = 0
                if (s.length >= 8) score++
                if (s.length >= 12) score++
                if (s.any { it.isLowerCase() } && s.any { it.isUpperCase() }) score++
                if (s.any { it.isDigit() }) score++
                if (s.any { !it.isLetterOrDigit() }) score++
                val level = when {
                    score >= 5 -> "💪 非常强"
                    score == 4 -> "👍 较强"
                    score == 3 -> "⚠️ 中等"
                    else -> "🔒 较弱"
                }
                out = "长度：${s.length}\n复杂度评分：$score/5\n强度：$level\n建议：${if (score < 4) "混合大小写字母+数字+符号，长度至少 8 位" else "保持口令私密，定期更换"}"
            }, modifier = Modifier.fillMaxWidth()) { Text("检测") }
            NewResultCard(out, context)
        }
    }
}

/* ============================================================
 * 27) 手机号校验
 * ============================================================ */
@Composable
fun PhoneValidateScreenView() {
    val context = LocalContext.current
    var input by remember { mutableStateOf("") }
    var out by remember { mutableStateOf("") }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp)) {
        item {
            NewToolHeader("手机号校验", Icons.Filled.Phone, "校验中国大陆手机号格式并识别运营商")
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = input, onValueChange = { input = it.filter { c -> c.isDigit() } }, label = { Text("输入 11 位手机号") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                val s = input
                when {
                    s.length != 11 || !s.startsWith("1") -> out = "❌ 不是有效的 11 位大陆手机号"
                    !s.matches(Regex("^1[3-9]\\d{9}$")) -> out = "❌ 号段格式不正确"
                    else -> {
                        val op = when (s.substring(0, 3)) {
                            "130", "131", "132", "133", "134", "135", "136", "137", "138", "139", "145", "146", "147", "148", "149", "150", "151", "152", "153", "155", "156", "157", "158", "159", "162", "165", "166", "167", "170", "171", "172", "173", "174", "175", "176", "177", "178", "180", "181", "182", "183", "184", "185", "186", "187", "188", "189", "190", "191", "193", "195", "196", "197", "198", "199" -> "中国移动"
                            "155", "156", "166", "185", "186", "188", "176", "175", "170", "171", "1349" -> "中国联通"
                            else -> "中国联通/移动/电信（号段识别仅供参考）"
                        }
                        out = "✅ 格式有效\n号码：$s\n运营商：$op"
                    }
                }
            }, modifier = Modifier.fillMaxWidth()) { Text("校验") }
            NewResultCard(out, context)
        }
    }
}

/* ============================================================
 * 28) 身份证解析
 * ============================================================ */
@Composable
fun IdCardScreenView() {
    val context = LocalContext.current
    var input by remember { mutableStateOf("") }
    var out by remember { mutableStateOf("") }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp)) {
        item {
            NewToolHeader("身份证解析", Icons.Filled.Badge, "18 位身份证号提取生日/性别/地区（本地运算）")
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = input, onValueChange = { input = it.filter { c -> c.isDigit() || c == 'x' || c == 'X' } }, label = { Text("输入 18 位身份证号") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                val s = input.uppercase()
                if (s.length != 18) { out = "请输入 18 位身份证号"; return@Button }
                try {
                    val birth = "${s.substring(6, 10)}-${s.substring(10, 12)}-${s.substring(12, 14)}"
                    val sexBit = s[16].toString().toInt()
                    val sex = if (sexBit % 2 == 1) "男" else "女"
                    out = "出生日期：$birth\n性别：$sex\n地区码：${s.substring(0, 6)}\n校验位：${s[17]}\n（地区名称可另行查询号段表）"
                } catch (e: Exception) { out = "解析失败：请检查输入" }
            }, modifier = Modifier.fillMaxWidth()) { Text("解析") }
            NewResultCard(out, context)
        }
    }
}

/* ============================================================
 * 29) 银行卡 Luhn 校验
 * ============================================================ */
@Composable
fun LuhnCheckScreenView() {
    val context = LocalContext.current
    var input by remember { mutableStateOf("") }
    var out by remember { mutableStateOf("") }
    fun luhn(num: String): Boolean {
        var sum = 0
        var alt = false
        for (i in num.length - 1 downTo 0) {
            var d = num[i] - '0'
            if (alt) { d *= 2; if (d > 9) d -= 9 }
            sum += d
            alt = !alt
        }
        return sum % 10 == 0
    }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp)) {
        item {
            NewToolHeader("银行卡校验", Icons.Filled.CreditCard, "Luhn 算法校验银行卡/信用卡号")
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = input, onValueChange = { input = it.filter { c -> c.isDigit() } }, label = { Text("输入卡号（纯数字）") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                val s = input.trim()
                out = when {
                    s.length < 13 -> "卡号过短（13-19 位）"
                    luhn(s) -> "✅ 校验通过（Luhn 算法）"
                    else -> "❌ 校验未通过，请检查卡号"
                }
            }, modifier = Modifier.fillMaxWidth()) { Text("校验") }
            NewResultCard(out, context)
        }
    }
}

/* ============================================================
 * 30) 文本分段拆分
 * ============================================================ */
@Composable
fun TextSplitScreenView() {
    val context = LocalContext.current
    var input by remember { mutableStateOf("") }
    var sep by remember { mutableStateOf(",") }
    var out by remember { mutableStateOf("") }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp)) {
        item {
            NewToolHeader("文本拆分", Icons.Filled.Verified, "按分隔符拆分文本，并统计分段数量")
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = input, onValueChange = { input = it }, label = { Text("输入文本") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = sep, onValueChange = { sep = if (it.isEmpty()) " " else it }, label = { Text("分隔符（如 , 或空格）") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                val parts = input.split(sep).map { it.trim() }.filter { it.isNotEmpty() }
                out = if (parts.isEmpty()) "无内容" else "共 ${parts.size} 段：\n" + parts.joinToString("\n")
            }, modifier = Modifier.fillMaxWidth()) { Text("拆分") }
            NewResultCard(out, context)
        }
    }
}