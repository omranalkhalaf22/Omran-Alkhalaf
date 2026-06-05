package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun PrCelebrationOverlay(
    exerciseName: String,
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
                // Glowing badge circle
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(OrangePrimary.copy(alpha = 0.2f), CircleShape)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Draw cool dynamic orange energy sparks on canvas
                        drawCircle(color = OrangePrimary, radius = size.width * 0.42f, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()))
                        drawCircle(color = OrangeAccent, radius = size.width * 0.35f, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx()))
                    }
                    Icon(
                        Icons.Filled.EmojiEvents,
                        contentDescription = null,
                        tint = OrangePrimary,
                        modifier = Modifier.size(54.dp)
                    )
                }

                Text(
                    text = "YENİ KİŞİSEL REKOR!",
                    color = OrangePrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "$exerciseName egzersizinde limitlerini sıfırladın ve yeni zirveye ulaştın!",
                    color = LightText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 20.sp
                )

                Text(
                    text = "🔥 Gelişmeye Devam Et!",
                    color = OrangeAccent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Tebrikler!", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}
