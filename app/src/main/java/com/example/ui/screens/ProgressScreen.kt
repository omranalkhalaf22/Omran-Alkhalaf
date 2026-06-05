package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.viewmodel.FitTrackViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProgressScreen(
    viewModel: FitTrackViewModel,
    onOpenAddWeightDialog: () -> Unit,
    onOpenAddPhotoDialog: () -> Unit
) {
    val weightLogs by viewModel.allWeightLogs.collectAsState()
    val rawPhotos by viewModel.allPhotos.collectAsState()
    val profile by viewModel.userProfile.collectAsState()

    var activeSubTab by remember { mutableStateOf(0) } // 0: Kilo, 1: Galeri, 2: Vücut, 3: Zaman Tüneli
    var selectedPhotoForView by remember { mutableStateOf<ProgressPhoto?>(null) }

    // Before/After state comparison
    var beforePhoto by remember { mutableStateOf<ProgressPhoto?>(null) }
    var afterPhoto by remember { mutableStateOf<ProgressPhoto?>(null) }
    var showComparisonSheet by remember { mutableStateOf(false) }

    // --- Body Comp Sliders States ---
    val currentWeightFromLogs = weightLogs.firstOrNull()?.weight ?: profile.weight
    var sliderWeight by remember(currentWeightFromLogs) { mutableStateOf(currentWeightFromLogs.toFloat()) }
    var sliderWaist by remember { mutableStateOf(84f) }
    var sliderFatPct by remember { mutableStateOf(15f) }

    // --- Spotify Wrapped style state ---
    var showWrappedModal by remember { mutableStateOf(false) }
    var wrappedSlideIndex by remember { mutableStateOf(0) }

    val sdfShort = remember { SimpleDateFormat("dd MMM", Locale("tr")) }

    // Dynamically update Before/After choices when photos load
    LaunchedEffect(rawPhotos) {
        if (rawPhotos.size >= 2) {
            beforePhoto = rawPhotos.lastOrNull()
            afterPhoto = rawPhotos.firstOrNull()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Sub-tabs Row with horizontal scroll to ensure no truncation on small devices
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .background(SurfaceDark, RoundedCornerShape(12.dp))
                .padding(4.dp)
                .horizontalScroll(rememberScrollState())
        ) {
            val subTabs = listOf("Kilo Analizi", "Foto Galeri", "Vücut Laboratuvarı", "Zaman Tüneli")
            subTabs.forEachIndexed { index, title ->
                val isSelected = activeSubTab == index
                Box(
                    modifier = Modifier
                        .widthIn(min = 80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) OrangePrimary else Color.Transparent)
                        .clickable { activeSubTab = index }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.Black else MutedText,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
            }
        }

        if (activeSubTab == 0) {
            // KİLO TAKİBİ TAB
            val sortedLogs = remember(weightLogs) { weightLogs.sortedBy { it.timestamp } }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // Header
                item {
                    val currentWeight = weightLogs.firstOrNull()?.weight ?: profile.weight
                    val initialWeight = weightLogs.lastOrNull()?.weight ?: profile.weight
                    val totalDiff = currentWeight - initialWeight
                    val diffSign = if (totalDiff >= 0) "+" else ""

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Güncel Durum Değerleri", color = MutedText, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = String.format("%.1f", currentWeight),
                                        style = MaterialTheme.typography.displayMedium.copy(
                                            fontWeight = FontWeight.Black,
                                            color = LightText
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (profile.isKg) "kg" else "lbs",
                                        color = OrangePrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (totalDiff <= 0) AccentSuccess.copy(alpha = 0.2f) else AccentError.copy(alpha = 0.2f))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "$diffSign${String.format("%.1f", totalDiff)} ${if (profile.isKg) "kg" else "lbs"}",
                                        color = if (totalDiff <= 0) AccentSuccess else AccentError,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                MetricSummaryItem("Başlangıç", "${String.format("%.1f", initialWeight)} ${if (profile.isKg) "kg" else "lbs"}")
                                MetricSummaryItem("Aylık Hedef", "Yağ Yakımı")
                                MetricSummaryItem("Kayıt Adedi", "${weightLogs.size} logs")
                            }
                        }
                    }
                }

                // LINE GRAPH VIEW
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("weight_chart_card"),
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Kilo Değişim Trendi",
                                    fontWeight = FontWeight.Bold,
                                    color = LightText,
                                    fontSize = 15.sp
                                )
                                Text(
                                    "Haftalık Grafiği",
                                    color = OrangePrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            if (sortedLogs.size < 2) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .background(SurfaceDark, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "İlerleme grafiğinin çizilmesi için en az iki farklı kilo kaydı ekleyin.",
                                        color = MutedText,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(16.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                // Draw beautiful premium line chart inside Canvas!
                                WeightLineChart(sortedLogs = sortedLogs)
                            }
                        }
                    }
                }

                // Logs list and Add action button
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Kilo Kayıt Tarihçesi",
                            color = OrangePrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            letterSpacing = 0.5.sp
                        )
                        Button(
                            onClick = onOpenAddWeightDialog,
                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary.copy(alpha = 0.15f), contentColor = OrangePrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Yeni Ekle", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (weightLogs.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Henüz kaydedilmiş kilo verisi yok.", color = MutedText, fontSize = 13.sp)
                        }
                    }
                } else {
                    items(weightLogs) { valLog ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CardBg, RoundedCornerShape(12.dp))
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${valLog.weight} ${if (profile.isKg) "kg" else "lbs"}",
                                    fontWeight = FontWeight.Bold,
                                    color = LightText,
                                    fontSize = 16.sp
                                )
                                if (valLog.notes.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(valLog.notes, color = MutedText, fontSize = 12.sp)
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = sdfShort.format(Date(valLog.timestamp)),
                                    color = MutedText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { viewModel.removeWeightLog(valLog.id) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Filled.Close, contentDescription = "Sil", tint = AccentError, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        } else if (activeSubTab == 1) {
            // İLERLEME FOTOĞRAFLARI TAB
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // Before/After comparison widget
                if (rawPhotos.size >= 2) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CardBg),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Önce / Sonra Karşılaştırması",
                                    fontWeight = FontWeight.Bold,
                                    color = LightText,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Before Card
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(SurfaceDark, RoundedCornerShape(12.dp))
                                            .padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("ÖNCE", color = OrangePrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        AvatarMuscleSilhouette(modifier = Modifier.size(54.dp), bodyType = beforePhoto?.photoData)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            beforePhoto?.position ?: "Bilinmeyen",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            color = LightText
                                        )
                                        Text(
                                            if (beforePhoto != null) sdfShort.format(Date(beforePhoto!!.timestamp)) else "-",
                                            fontSize = 11.sp,
                                            color = MutedText
                                        )
                                    }

                                    // After Card
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(SurfaceDark, RoundedCornerShape(12.dp))
                                            .padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("SONRA", color = AccentSuccess, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        AvatarMuscleSilhouette(modifier = Modifier.size(54.dp), bodyType = afterPhoto?.photoData, glow = true)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            afterPhoto?.position ?: "Bilinmeyen",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            color = LightText
                                        )
                                        Text(
                                            if (afterPhoto != null) sdfShort.format(Date(afterPhoto!!.timestamp)) else "-",
                                            fontSize = 11.sp,
                                            color = MutedText
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { showComparisonSheet = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.Black),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Filled.Compare, contentDescription = null, tint = Color.Black)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Fotoğrafları Karşılaştır", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Add photo action
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Kaydedilen İlerleme Fotoğrafları",
                            color = OrangePrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            letterSpacing = 0.5.sp
                        )
                        Button(
                            onClick = onOpenAddPhotoDialog,
                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary.copy(alpha = 0.15f), contentColor = OrangePrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Filled.AddAPhoto, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Foto Ekle", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (rawPhotos.isEmpty()) {
                    item {
                        EmptyStateView(
                            iconVector = Icons.Filled.PhotoLibrary,
                            text = "Henüz ilerleme fotoğrafı yüklenmedi.\nÖn, Yan, Arka formunuzu belirleyip ekleyin!",
                            buttonText = "Fotoğraf Ekle",
                            onAction = onOpenAddPhotoDialog
                        )
                    }
                } else {
                    item {
                        // Multi column grid of photos in flow or grid
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            maxItemsInEachRow = 2
                        ) {
                            rawPhotos.forEach { photo ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(CardBg, RoundedCornerShape(12.dp))
                                        .border(1.dp, SurfaceDark, RoundedCornerShape(12.dp))
                                        .clickable { selectedPhotoForView = photo }
                                        .padding(12.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(OrangePrimary.copy(alpha = 0.15f))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(photo.position, color = OrangePrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                            IconButton(
                                                onClick = { viewModel.deleteProgressPhoto(photo.id) },
                                                modifier = Modifier.size(20.dp)
                                            ) {
                                                Icon(Icons.Filled.Delete, contentDescription = "Sil", tint = AccentError, modifier = Modifier.size(14.dp))
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        AvatarMuscleSilhouette(modifier = Modifier.size(68.dp), bodyType = photo.photoData)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        Text(
                                            sdfShort.format(Date(photo.timestamp)),
                                            color = LightText,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp
                                        )
                                        if (photo.notes.isNotEmpty()) {
                                            Text(
                                                photo.notes,
                                                color = MutedText,
                                                fontSize = 11.sp,
                                                maxLines = 1,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if (activeSubTab == 2) {
            // VÜCUT ANALİZ LABORATUVARI
            val heightM = profile.height / 100.0
            val bmi = sliderWeight / (heightM * heightM)
            val fatMass = sliderWeight * (sliderFatPct / 100.0)
            val leanMass = sliderWeight - fatMass
            
            // FFMI calculation formula
            val ffmi = (leanMass) / (heightM * heightM)
            val normalizedFfmi = ffmi + 6.1 * (1.8 - heightM)
            
            val waistToHeight = sliderWaist / profile.height

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // Interactive controllers card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Kompozisyon Test Parametreleri",
                                color = OrangePrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Weight Slider
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Ağırlık: ${String.format("%.1f", sliderWeight)} kg", color = LightText, fontSize = 12.sp)
                                Text("Boy: ${profile.height} cm", color = MutedText, fontSize = 12.sp)
                            }
                            Slider(
                                value = sliderWeight,
                                onValueChange = { sliderWeight = it },
                                valueRange = 40f..150f,
                                colors = SliderDefaults.colors(activeTrackColor = OrangePrimary, thumbColor = OrangePrimary)
                            )

                            // Fat percentage slider
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Yağ Oranı: ${String.format("%.1f", sliderFatPct)}%", color = LightText, fontSize = 12.sp)
                                val fatStatus = when {
                                    sliderFatPct < 8 -> "Esansiyel/Çok Düşük"
                                    sliderFatPct < 15 -> "Atletik"
                                    sliderFatPct < 21 -> "Zinde"
                                    else -> "Yüksek"
                                }
                                Text(fatStatus, color = OrangeAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = sliderFatPct,
                                onValueChange = { sliderFatPct = it },
                                valueRange = 3f..45f,
                                colors = SliderDefaults.colors(activeTrackColor = OrangePrimary, thumbColor = OrangePrimary)
                            )

                            // Waist circumference
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Bel Çevresi: ${String.format("%.1f", sliderWaist)} cm", color = LightText, fontSize = 12.sp)
                            }
                            Slider(
                                value = sliderWaist,
                                onValueChange = { sliderWaist = it },
                                valueRange = 50f..140f,
                                colors = SliderDefaults.colors(activeTrackColor = OrangePrimary, thumbColor = OrangePrimary)
                            )
                        }
                    }
                }

                // Analytics report cards
                item {
                    Text(
                        "Laboratuvar Veri Çıktıları",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = OrangePrimary,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        )
                    )
                }

                item {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        maxItemsInEachRow = 2
                    ) {
                        // Card 1: BMI
                        val bmiRating = when {
                            bmi < 18.5 -> "Düşük Kilolu"
                            bmi < 25.0 -> "Normal"
                            bmi < 30.0 -> "Kilolu"
                            else -> "Obez"
                        }
                        StatsCard(
                            modifier = Modifier.weight(1f),
                            title = "Vücut Kitle Endeksi (BMI)",
                            value = String.format("%.1f", bmi),
                            description = "Durum: $bmiRating",
                            icon = Icons.Filled.HealthAndSafety,
                            accentColor = Color(0xFF64B5F6)
                        )

                        // Card 2: FFMI
                        val ffmiRating = when {
                            normalizedFfmi < 18 -> "Ortalama Altı"
                            normalizedFfmi < 20 -> "Ortalama"
                            normalizedFfmi < 22 -> "Mükemmel"
                            normalizedFfmi < 25 -> "Üst Seviye"
                            else -> "Doğal Limit Ötesi"
                        }
                        StatsCard(
                            modifier = Modifier.weight(1f),
                            title = "Kas Kütle Endeksi (FFMI)",
                            value = String.format("%.1f", normalizedFfmi),
                            description = "Potansiyel: $ffmiRating",
                            icon = Icons.Filled.FitnessCenter,
                            accentColor = OrangePrimary
                        )

                        // Card 3: Lean Body Mass
                        StatsCard(
                            modifier = Modifier.weight(1f),
                            title = "Yağsız Kas Kütlesi",
                            value = "${String.format("%.1f", leanMass)} kg",
                            description = "Saf lif ağırlığı",
                            icon = Icons.Filled.AccessibilityNew,
                            accentColor = AccentSuccess
                        )

                        // Card 4: Fat Mass
                        StatsCard(
                            modifier = Modifier.weight(1f),
                            title = "Toplam Yağ Deposu",
                            value = "${String.format("%.1f", fatMass)} kg",
                            description = "Yağ dokusu ağırlığı",
                            icon = Icons.Filled.Waves,
                            accentColor = AccentError
                        )

                        // Card 5: Bel-Boy Oranı
                        val whtRating = when {
                            waistToHeight < 0.43 -> "Çok İnce"
                            waistToHeight < 0.53 -> "Sağlıklı"
                            waistToHeight < 0.58 -> "Kilo Riski"
                            else -> "Yüksek Risk"
                        }
                        StatsCard(
                            modifier = Modifier.weight(1f),
                            title = "Bel-Boy Oranı",
                            value = String.format("%.2f", waistToHeight),
                            description = "Sağlık: $whtRating",
                            icon = Icons.Filled.QueryStats,
                            accentColor = OrangeAccent
                        )
                    }
                }
            }
        } else if (activeSubTab == 3) {
            // ZAMAN TÜNELİ & WRAPPED
            val highestRecord = if (viewModel.allRecords.value.isNotEmpty()) viewModel.allRecords.value.maxOfOrNull { it.weight } ?: 0.0 else 0.0

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // Spotify Wrapped Button
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                wrappedSlideIndex = 0
                                showWrappedModal = true
                            },
                        colors = CardDefaults.cardColors(containerColor = DeepBlack),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.5.dp, Brush.horizontalGradient(colors = listOf(OrangePrimary, OrangeAccent)))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "FITTRACK WRAPPED 2026",
                                    color = OrangePrimary,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Yıllık istatistiklerini Spotify Wrapped tarzında animasyonlu hikaye kartlarıyla gör!",
                                    color = LightText,
                                    fontSize = 11.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(OrangePrimary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = "Başlat", tint = Color.Black, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }

                item {
                    Text(
                        "Kariyer Kilometre Taşları Zaman Tüneli",
                        color = OrangePrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                // Vertical timeline view
                item {
                    TimelineNode(
                        title = "Savaş Arenasına İlk Adım",
                        date = "01 Ocak 2026",
                        desc = "FitTrack Pro operating system'de ilk egzersizin tamamlandığı gün.",
                        isCompleted = true,
                        icon = Icons.Filled.Flag
                    )
                }
                item {
                    TimelineNode(
                        title = "Kırılmaz İrade (Streak)",
                        date = "15 Şubat 2026",
                        desc = "Egzersiz disiplini aksatılmadan sürdürülüyor.",
                        isCompleted = true,
                        icon = Icons.Filled.Whatshot
                    )
                }
                item {
                    TimelineNode(
                        title = "Gezegen Değişimi (Form)",
                        date = "22 Nisan 2026",
                        desc = "Vücut ağırlığı stabilizasyon seviyelerine ulaştı.",
                        isCompleted = true,
                        icon = Icons.Filled.Scale
                    )
                }
                item {
                    TimelineNode(
                        title = "Yerçekimine Karşı (PR Müzesi)",
                        date = "01 Mayıs 2026",
                        desc = "Zirve kaldırış rekoru kırıldı! $highestRecord kg ile PR müzesine eklendi.",
                        isCompleted = highestRecord > 0,
                        icon = Icons.Filled.EmojiEvents
                    )
                }
            }
        }
    }

    // --- FITTRACK WRAPPED INTERACTIVE BACKYARD MODAL ---
    if (showWrappedModal) {
        val totalWorkouts = viewModel.allWorkoutLogs.value.size
        val totalMinutes = totalWorkouts * 45
        val recordsList = viewModel.allRecords.value
        val maxLift = if (recordsList.isNotEmpty()) recordsList.maxOfOrNull { it.weight } ?: 90.0 else 90.0

        val wrappedSlides = listOf(
            Triple(
                "SAVAŞ RAPORU",
                "Kayıtlı toplam seanslarınla sezonu domine ettin!\n\n2026 yılında toplam $totalWorkouts antrenman seansını başarıyla bitirdin!\n\nBu tam $totalMinutes dakikalık sarsılmaz bir motivasyon anlamına geliyor. Harikasın!",
                Icons.Filled.FitnessCenter
            ),
            Triple(
                "DURMAKSIZIN",
                "Kaslarını çalıştırmak ve hedeflerine yürümek senin işindi.\n\nEgzersiz zincirini hiç kırmadın ve disiplin abidesi oldun!",
                Icons.Filled.Whatshot
            ),
            Triple(
                "ZİRVE PERFORMANS",
                "Bu sene demir yığınlarına tam anlamıyla hükmettin!\n\nEn büyük gövde gösterin Squat/Deadlift/Bench rekorlarındaki o rekor ağırlık olan $maxLift kg kaldırmandı!\n\nSınırları zorlamaya ve gelişmeye hazır ol!",
                Icons.Filled.EmojiEvents
            )
        )

        val currentSlide = wrappedSlides[wrappedSlideIndex]

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.96f))
                .padding(24.dp)
                .clickable(enabled = false) {} // block clicks
        ) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                // Header indicators
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        wrappedSlides.forEachIndexed { idx, _ ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .background(
                                        if (idx <= wrappedSlideIndex) OrangePrimary else MutedText.copy(alpha = 0.3f),
                                        CircleShape
                                    )
                            )
                        }
                    }
                    Text(
                        "FITTRACK PRO WRAPPED 2026",
                        color = MutedText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }

                // Body text slide
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(OrangePrimary.copy(alpha = 0.12f), CircleShape)
                            .border(2.dp, OrangePrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(currentSlide.third, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(48.dp))
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = currentSlide.first,
                        color = OrangePrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        letterSpacing = 1.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = currentSlide.second,
                        color = LightText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                // Footer controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { showWrappedModal = false }) {
                        Text("Kapat", color = MutedText, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (wrappedSlideIndex < wrappedSlides.size - 1) {
                                wrappedSlideIndex++
                            } else {
                                showWrappedModal = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.Black)
                    ) {
                        Text(
                            if (wrappedSlideIndex < wrappedSlides.size - 1) "İlerle ->" else "Bitir",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // SLIDEOUT EXPAND VIEW OF PHOTO DETAIL
    selectedPhotoForView?.let { photo ->
        AlertDialog(
            onDismissRequest = { selectedPhotoForView = null },
            title = {
                Text("${photo.position} Görünümü - Form Detayı", color = OrangePrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            containerColor = CardBg,
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AvatarMuscleSilhouette(modifier = Modifier.size(150.dp), bodyType = photo.photoData, glow = true)
                    Text("Tarih: " + SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("tr")).format(Date(photo.timestamp)), color = LightText, fontSize = 13.sp)
                    if (photo.notes.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceDark, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text("Not: ${photo.notes}", color = MutedText, fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedPhotoForView = null },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.Black)
                ) {
                    Text("Kapat", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // BEFORE AFTER SELECTOR MODAL COMPARISON
    if (showComparisonSheet) {
        AlertDialog(
            onDismissRequest = { showComparisonSheet = false },
            title = { Text("Fotoğraf Karşılaştır", color = OrangePrimary, fontWeight = FontWeight.Bold) },
            containerColor = CardBg,
            text = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Karşılaştırılacak Fotoğrafları Seçin:", color = LightText, fontSize = 13.sp)
                    
                    Text("Soldaki (Önce):", color = OrangeAccent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Box(modifier = Modifier.height(100.dp)) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(rawPhotos) { p ->
                                val isSelected = beforePhoto?.id == p.id
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) OrangePrimary.copy(alpha = 0.2f) else SurfaceDark)
                                        .border(2.dp, if (isSelected) OrangePrimary else Color.Transparent, RoundedCornerShape(8.dp))
                                        .clickable { beforePhoto = p }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        AvatarMuscleSilhouette(modifier = Modifier.size(36.dp), bodyType = p.photoData)
                                        Text("${p.position} - ${sdfShort.format(Date(p.timestamp))}", fontSize = 10.sp, color = LightText)
                                    }
                                }
                            }
                        }
                    }

                    Text("Sağdaki (Sonra):", color = AccentSuccess, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Box(modifier = Modifier.height(100.dp)) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(rawPhotos) { p ->
                                val isSelected = afterPhoto?.id == p.id
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) AccentSuccess.copy(alpha = 0.2f) else SurfaceDark)
                                        .border(2.dp, if (isSelected) AccentSuccess else Color.Transparent, RoundedCornerShape(8.dp))
                                        .clickable { afterPhoto = p }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        AvatarMuscleSilhouette(modifier = Modifier.size(36.dp), bodyType = p.photoData)
                                        Text("${p.position} - ${sdfShort.format(Date(p.timestamp))}", fontSize = 10.sp, color = LightText)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showComparisonSheet = false },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.Black)
                ) {
                    Text("Tamam", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun MetricSummaryItem(label: String, value: String) {
    Column {
        Text(label, color = MutedText, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, color = LightText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

// PREMIUM LINE CHART COMPONENT DRAWING
@Composable
fun WeightLineChart(sortedLogs: List<WeightLog>) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(SurfaceDark, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        val width = size.width
        val height = size.height

        val weights = sortedLogs.map { it.weight }
        val minWeight = (weights.minOrNull() ?: 50.0) - 1.0
        val maxWeight = (weights.maxOrNull() ?: 100.0) + 1.0
        val weightRange = maxWeight - minWeight

        val xPointsCount = sortedLogs.size
        val xSteps = width / (xPointsCount - 1).coerceAtLeast(1)

        val strokePath = Path()
        val fillPath = Path()

        val sdf = SimpleDateFormat("dd/MM", Locale("tr"))

        // Grid lines drawing (Glowing grid)
        val gridLinesCount = 4
        for (i in 0..gridLinesCount) {
            val y = height - (height / gridLinesCount) * i
            drawLine(
                color = MutedText.copy(alpha = 0.1f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        val pointCoordinates = mutableListOf<Offset>()

        sortedLogs.forEachIndexed { idx, item ->
            val x = xSteps * idx
            val yNormalized = (item.weight - minWeight) / weightRange
            // Subtract from height as 0,0 is top-left in canvas coordinates
            val y = height - (height * yNormalized).toFloat()

            val pt = Offset(x, y)
            pointCoordinates.add(pt)

            if (idx == 0) {
                strokePath.moveTo(x, y)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, y)
            } else {
                // Curved control points for elegant line transition
                val prevPt = pointCoordinates[idx - 1]
                val controlX = (prevPt.x + x) / 2f
                strokePath.cubicTo(controlX, prevPt.y, controlX, y, x, y)
                fillPath.cubicTo(controlX, prevPt.y, controlX, y, x, y)
            }

            if (idx == sortedLogs.size - 1) {
                fillPath.lineTo(x, height)
                fillPath.close()
            }
        }

        // Draw glowing translucent gradient fill below the path
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    OrangePrimary.copy(alpha = 0.35f),
                    OrangePrimary.copy(alpha = 0.00f)
                )
            )
        )

        // Draw primary neon-orange path stroke
        drawPath(
            path = strokePath,
            color = OrangePrimary,
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Draw points and values
        pointCoordinates.forEachIndexed { idx, point ->
            // Draw outer glow ring
            drawCircle(
                color = OrangeAccent.copy(alpha = 0.4f),
                radius = 8.dp.toPx(),
                center = point
            )
            // Draw inner node dot
            drawCircle(
                color = OrangePrimary,
                radius = 4.dp.toPx(),
                center = point
            )

            // Let's only list labels for end nodes to avoid clutter
            if (idx == 0 || idx == sortedLogs.size - 1) {
                // Just let the dashboard show labels or logs show exact numbers
            }
        }
    }
}

// CUSTOM MOCK AVATARS FOR BODY MODEL SILHOUETTES
@Composable
fun AvatarMuscleSilhouette(
    modifier: Modifier = Modifier,
    bodyType: String? = "vector_muscle_avatar",
    glow: Boolean = false
) {
    Box(
        modifier = modifier
            .background(
                if (glow) OrangePrimary.copy(alpha = 0.08f) else SurfaceDark,
                CircleShape
            )
            .border(
                1.5.dp,
                if (glow) OrangePrimary else MutedText.copy(alpha = 0.3f),
                CircleShape
            )
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Draw elegant canvas schematic of muscle groups Front, Side, Back, representing fitness progress
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val primaryColor = if (glow) OrangePrimary else MutedText

            // Draw athletic wireframe silhouette
            // Head
            drawCircle(color = primaryColor, radius = w * 0.12f, center = Offset(w * 0.5f, h * 0.2f))
            // Neck & Shoulders
            val shoulderY = h * 0.35f
            drawLine(
                color = primaryColor,
                start = Offset(w * 0.3f, shoulderY),
                end = Offset(w * 0.7f, shoulderY),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
            // Spine / Torso outline
            drawLine(
                color = primaryColor,
                start = Offset(w * 0.5f, h * 0.2f),
                end = Offset(w * 0.5f, h * 0.65f),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )
            // Chest / Core plate (depending on Front or back)
            drawCircle(
                color = primaryColor.copy(alpha = 0.3f),
                radius = w * 0.16f,
                center = Offset(w * 0.5f, h * 0.48f)
            )
            // Arms
            drawLine(
                color = primaryColor,
                start = Offset(w * 0.3f, shoulderY),
                end = Offset(w * 0.25f, h * 0.58f),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = primaryColor,
                start = Offset(w * 0.7f, shoulderY),
                end = Offset(w * 0.75f, h * 0.58f),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
            // Legs
            drawLine(
                color = primaryColor,
                start = Offset(w * 0.42f, h * 0.65f),
                end = Offset(w * 0.36f, h * 0.9f),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = primaryColor,
                start = Offset(w * 0.58f, h * 0.65f),
                end = Offset(w * 0.64f, h * 0.9f),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun TimelineNode(
    title: String,
    date: String,
    desc: String,
    isCompleted: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        if (isCompleted) OrangePrimary.copy(alpha = 0.15f) else SurfaceDark,
                        CircleShape
                    )
                    .border(
                        1.5.dp,
                        if (isCompleted) OrangePrimary else MutedText.copy(alpha = 0.3f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (isCompleted) OrangePrimary else MutedText,
                    modifier = Modifier.size(18.dp)
                )
            }
            // Vertical timeline trace line
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(44.dp)
                    .background(if (isCompleted) OrangePrimary.copy(alpha = 0.2f) else MutedText.copy(alpha = 0.1f))
            )
        }

        Column(modifier = Modifier.padding(top = 4.dp).weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, fontWeight = FontWeight.Bold, color = LightText, fontSize = 13.sp)
                Text(date, color = OrangeAccent, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(desc, color = MutedText, fontSize = 11.sp, lineHeight = 15.sp)
        }
    }
}

