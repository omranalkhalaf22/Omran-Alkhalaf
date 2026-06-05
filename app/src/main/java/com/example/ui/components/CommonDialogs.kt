package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun AddWeightDialog(
    onDismiss: () -> Unit,
    onSave: (weight: Double, notes: String) -> Unit
) {
    var weightInput by remember { mutableStateOf("") }
    var noteInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Kilo Verisi Ekle",
                color = OrangePrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        containerColor = CardBg,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Mevcut vücut ağırlığınızı girin. Grafik ve profil verileriniz otomatik olarak güncellenecektir.",
                    color = MutedText,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                TextField(
                    value = weightInput,
                    onValueChange = { weightInput = it },
                    label = { Text("Ağırlık (kg / lb)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("weight_input_field"),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SurfaceDark,
                        unfocusedContainerColor = SurfaceDark,
                        focusedTextColor = LightText,
                        unfocusedTextColor = LightText
                    )
                )

                TextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    label = { Text("Kısa Not (Örn: Aç karnına)") },
                    modifier = Modifier.fillMaxWidth().testTag("weight_note_field"),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SurfaceDark,
                        unfocusedContainerColor = SurfaceDark,
                        focusedTextColor = LightText,
                        unfocusedTextColor = LightText
                    )
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Vazgeç", color = MutedText)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val w = weightInput.toDoubleOrNull()
                    if (w != null && w > 0.0) {
                        onSave(w, noteInput)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.Black),
                modifier = Modifier.testTag("save_weight_button")
            ) {
                Text("Kaydet", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun AddPhotoDialog(
    onDismiss: () -> Unit,
    onSave: (position: String, notes: String, avatarType: String) -> Unit
) {
    var positionSelected by remember { mutableStateOf("Ön") }
    var notesSelected by remember { mutableStateOf("") }
    var avatarSelected by remember { mutableStateOf("fit_body") }

    val positions = listOf("Ön", "Yan", "Arka")
    val avatars = listOf("fit_body", "toned_body", "muscular_body")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "İlerleme Fotoğrafı Ekle",
                color = OrangePrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        containerColor = CardBg,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Gözlem açınızı ve gelişim düzeyinizi belirleyerek günlük fiziksel form takibinizi kaydedin.",
                    color = MutedText,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                // Position segments
                Text("Açı Seçi:", color = LightText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceDark, RoundedCornerShape(10.dp))
                        .padding(3.dp)
                ) {
                    positions.forEach { pos ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (positionSelected == pos) OrangePrimary else Color.Transparent)
                                .clickable { positionSelected = pos }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = pos,
                                color = if (positionSelected == pos) Color.Black else LightText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // Silhouette progression types
                Text("Görsel Form Düzeyi (Temsili):", color = LightText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val avatarLabels = mapOf(
                        "fit_body" to "Yağsız / Atletik",
                        "toned_body" to "Sıkı / Parçalı",
                        "muscular_body" to "Hacimli / Güçlü"
                    )

                    avatars.forEach { avt ->
                        val isSelected = avatarSelected == avt
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) OrangePrimary.copy(alpha = 0.15f) else SurfaceDark)
                                .clickable { avatarSelected = avt }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = avatarLabels[avt] ?: "",
                                fontSize = 10.sp,
                                color = if (isSelected) OrangePrimary else MutedText,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                TextField(
                    value = notesSelected,
                    onValueChange = { notesSelected = it },
                    label = { Text("Formunuz hakkında notlar...") },
                    modifier = Modifier.fillMaxWidth().testTag("photo_notes_field"),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SurfaceDark,
                        unfocusedContainerColor = SurfaceDark,
                        focusedTextColor = LightText,
                        unfocusedTextColor = LightText
                    )
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Vazgeç", color = MutedText)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(positionSelected, notesSelected, avatarSelected)
                },
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.Black),
                modifier = Modifier.testTag("save_photo_button")
            ) {
                Text("Kaydet", fontWeight = FontWeight.Bold)
            }
        }
    )
}
