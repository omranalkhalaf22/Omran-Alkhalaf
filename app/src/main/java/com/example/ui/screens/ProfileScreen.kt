package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.viewmodel.FitTrackViewModel

@Composable
fun ProfileScreen(viewModel: FitTrackViewModel) {
    val profile by viewModel.userProfile.collectAsState()

    var showEditProfileDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
    ) {
        // PROFILE BIOMETRICS CARD (Premium glassmorphic avatar layout)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_id_card"),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar icon with orange glow background
                    Box(
                        modifier = Modifier
                            .size(84.dp)
                            .background(OrangePrimary.copy(alpha = 0.15f), CircleShape)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.AccountCircle,
                            contentDescription = "Hesap",
                            tint = OrangePrimary,
                            modifier = Modifier.size(76.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = profile.name,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = LightText
                        )
                    )

                    Text(
                        text = "${profile.goal} • ${profile.age} Yaşında",
                        color = OrangeAccent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = SurfaceDark)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Row showing stats heights & weights
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BiometricsInfoItem("Boy", "${profile.height.toInt()} cm")
                        Box(modifier = Modifier.width(1.dp).height(24.dp).background(SurfaceDark))
                        BiometricsInfoItem("Ağırlık", "${profile.weight} ${if (profile.isKg) "kg" else "lb"}")
                        Box(modifier = Modifier.width(1.dp).height(24.dp).background(SurfaceDark))
                        BiometricsInfoItem("Vücut Yapısı", "Atletik")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showEditProfileDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Profili Düzenle", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // PREFERENCES SETTINGS GROUPS
        item {
            Text(
                "Uygulama Tercihleri",
                color = OrangePrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBg, RoundedCornerShape(16.dp))
                    .padding(vertical = 8.dp)
            ) {
                // Metric Unit toggle
                SettingsToggleRow(
                    title = "Metrik Ölçü Birimi (kg/cm)",
                    description = "Ağırlıklar kilogram (kg), boy santimetre (cm) cinsinden hesaplanır.",
                    icon = Icons.Filled.Scale,
                    checked = profile.isKg,
                    onCheckedChange = { state ->
                        viewModel.updateProfile(profile.copy(isKg = state))
                    }
                )

                Divider(color = SurfaceDark, modifier = Modifier.padding(horizontal = 16.dp))

                // Default Dark theme selector
                SettingsToggleRow(
                    title = "Premium Koyu Tema",
                    description = "FitTrack Pro'nun gece sporu dostu loş arayüzünü her zaman aktif tutar.",
                    icon = Icons.Filled.DarkMode,
                    checked = profile.isDarkTheme,
                    onCheckedChange = { state ->
                        viewModel.updateProfile(profile.copy(isDarkTheme = state))
                    }
                )

                Divider(color = SurfaceDark, modifier = Modifier.padding(horizontal = 16.dp))

                // Notifications Setup
                SettingsToggleRow(
                    title = "Hatırlatıcı Bildirimler",
                    description = "Günlük giriş, seri bozulma uyarısı ve set dinlenme bitiş alarmlarını tetikler.",
                    icon = Icons.Filled.NotificationsActive,
                    checked = profile.notificationsEnabled,
                    onCheckedChange = { state ->
                        viewModel.updateProfile(profile.copy(notificationsEnabled = state))
                    }
                )
            }
        }

        // HEAP DETAILS INFO SECTIONS (Account Settings simulated)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBg, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(
                    "Uygulama Sürümü",
                    fontWeight = FontWeight.Bold,
                    color = LightText,
                    fontSize = 14.sp
                )
                Text(
                    "FitTrack Pro v1.0.4 (Production Ready)",
                    color = MutedText,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = SurfaceDark)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Destek ve İletişim", color = LightText, fontSize = 13.sp)
                    Text("fittrack-support@aistudio.com", color = OrangeAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // EDIT PROFILE REGISTRATION SHEET DIALOG
    if (showEditProfileDialog) {
        var tempName by remember { mutableStateOf(profile.name) }
        var tempHeight by remember { mutableStateOf(profile.height.toString()) }
        var tempWeight by remember { mutableStateOf(profile.weight.toString()) }
        var tempAge by remember { mutableStateOf(profile.age.toString()) }
        var tempGoal by remember { mutableStateOf(profile.goal) }

        val goalsList = listOf("Kas kazanımı", "Güç artışı", "Yağ yakımı", "Genel fitness")

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Profili Düzenle", color = OrangePrimary, fontWeight = FontWeight.Bold) },
            containerColor = CardBg,
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        TextField(
                            value = tempName,
                            onValueChange = { tempName = it },
                            label = { Text("Ad Soyad") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = SurfaceDark,
                                unfocusedContainerColor = SurfaceDark,
                                focusedTextColor = LightText,
                                unfocusedTextColor = LightText
                            )
                        )
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextField(
                                value = tempHeight,
                                onValueChange = { tempHeight = it },
                                label = { Text("Boy (cm)") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = TextFieldDefaults.colors(focusedContainerColor = SurfaceDark, unfocusedContainerColor = SurfaceDark, focusedTextColor = LightText)
                            )
                            TextField(
                                value = tempWeight,
                                onValueChange = { tempWeight = it },
                                label = { Text("Kilo (${if (profile.isKg) "kg" else "lb"})") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                colors = TextFieldDefaults.colors(focusedContainerColor = SurfaceDark, unfocusedContainerColor = SurfaceDark, focusedTextColor = LightText)
                            )
                        }
                    }

                    item {
                        TextField(
                            value = tempAge,
                            onValueChange = { tempAge = it },
                            label = { Text("Yaş") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = TextFieldDefaults.colors(focusedContainerColor = SurfaceDark, unfocusedContainerColor = SurfaceDark, focusedTextColor = LightText)
                        )
                    }

                    item {
                        Text("Ana Hedef:", color = LightText, fontSize = 12.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            goalsList.take(2).forEach { g ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (tempGoal == g) OrangePrimary else SurfaceDark)
                                        .clickable { tempGoal = g }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(g, fontSize = 11.sp, color = if (tempGoal == g) Color.Black else LightText, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            goalsList.drop(2).forEach { g ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (tempGoal == g) OrangePrimary else SurfaceDark)
                                        .clickable { tempGoal = g }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(g, fontSize = 11.sp, color = if (tempGoal == g) Color.Black else LightText, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Vazgeç", color = MutedText)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsedH = tempHeight.toDoubleOrNull() ?: profile.height
                        val parsedW = tempWeight.toDoubleOrNull() ?: profile.weight
                        val parsedA = tempAge.toIntOrNull() ?: profile.age
                        
                        if (tempName.isNotEmpty()) {
                            viewModel.updateProfile(
                                profile.copy(
                                    name = tempName,
                                    height = parsedH,
                                    weight = parsedW,
                                    age = parsedA,
                                    goal = tempGoal
                                )
                            )
                            showEditProfileDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.Black)
                ) {
                    Text("Kaydet", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun BiometricsInfoItem(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, color = MutedText, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, color = LightText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun SettingsToggleRow(
    title: String,
    description: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(OrangePrimary.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = LightText,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                color = MutedText,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = OrangePrimary,
                uncheckedThumbColor = MutedText,
                uncheckedTrackColor = SurfaceDark
            )
        )
    }
}
