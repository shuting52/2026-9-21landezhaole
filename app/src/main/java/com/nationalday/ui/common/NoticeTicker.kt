package com.nationalday.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 跑马灯通知栏
 * 精准文件路径: app/src/main/java/com/nationalday/ui/common/NoticeTicker.kt
 */
@Composable
fun NationalDayNoticeTicker(
    notice: String = "热烈庆祝中华人民共和国成立75周年！祝全国开发者节日快乐！",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF7F1D1D))
            .border(1.dp, Color(0xFFFFD700), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("📢", fontSize = 16.sp, modifier = Modifier.padding(end = 8.dp))
        Text(notice, color = Color(0xFFFFD700), fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}