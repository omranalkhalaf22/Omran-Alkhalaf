package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.ui.theme.*

@Composable
fun LevelUpOverlay(
    level: Int,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(320.dp)
                .background(Brush.verticalGradient(
                    colors = listOf(DeepBlack, CardBg)
                ), RoundedCornerShape(24.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Neon energy ring
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(OrangePrimary.copy(alpha = 0.2f), CircleShape)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(color = OrangePrimary, radius = size.width * 0.44f, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx()))
                        drawCircle(color = Color(0xFF64B5F6), radius = size.width * 0.38f, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx()))
                    }
                    Icon(
                        Icons.Filled.ElectricBolt,
                        contentDescription = null,
                        tint = OrangePrimary,
                        modifier = Modifier.size(54.dp)
                    )
                }

                Text(
                    text = "SEVİYE ATLADIN! 🎉",
                    color = OrangePrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Tebrikler! Güç katsayın arttı ve Seviye $level seviyesine ulaştın! FitTrack Pro dünyasında yeni zirveleri zorluyorsun.",
                    color = LightText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Box(
                    modifier = Modifier
                        .background(OrangePrimary.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Unvan: ${getLevelTitle(level)}",
                        color = OrangePrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Devam Et", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

private fun getLevelTitle(level: Int): String {
    return when {
        level <= 1 -> "Çaylak Sporcu"
        level <= 3 -> "Zinde Atlet"
        level <= 5 -> "Demir Bükücü"
        level <= 7 -> "Seçkin Şampiyon"
        else -> "Efsanevi Canavar"
    }
}
