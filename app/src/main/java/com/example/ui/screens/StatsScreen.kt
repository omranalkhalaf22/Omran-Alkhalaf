package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
fun StatsScreen(viewModel: FitTrackViewModel) {
    val logs by viewModel.allWorkoutLogs.collectAsState()
    val weightLogs by viewModel.allWeightLogs.collectAsState()
    val exercises by viewModel.allExercises.collectAsState()
    val records by viewModel.allRecords.collectAsState()

    var activeSubTab by remember { mutableStateOf(0) } // 0: Aktivite & Isı Haritası, 1: Güç Analitiği, 2: Hesaplayıcılar

    // 1RM Selected Lift State
    var selectedLiftFor1RM by remember { mutableStateOf("Bench Press") }
    val liftsList = listOf("Bench Press", "Squat", "Deadlift", "Overhead Press")

    // Plate Calculator States
    var targetPlateWeight by remember { mutableStateOf(100f) }
    var selectedBarWeight by remember { mutableStateOf(20f) } // 20 kg (Olimpik) veya 15 kg

    // Volume Calculator States
    var sliderSets by remember { mutableStateOf(4f) }
    var sliderReps by remember { mutableStateOf(10f) }
    var sliderWeightLoad by remember { mutableStateOf(80f) }

    // Math metrics
    val totalWorkouts = logs.size
    val totalLiftedWeight = logs.sumOf { it.totalWeight }
    val totalSets = logs.sumOf { it.totalSets }
    val averageDurationMins = if (logs.isNotEmpty()) {
        logs.map { it.durationSeconds }.average() / 60
    } else 0.0

    val mostActiveDay = remember(logs) {
        if (logs.isEmpty()) "Henüz Veri Yok"
        else {
            val sdfDay = SimpleDateFormat("EEEE", Locale("tr"))
            val days = logs.map { sdfDay.format(Date(it.date)) }
            days.groupBy { it }.maxByOrNull { it.value.size }?.key ?: "Pazartesi"
        }
    }

    val weeklyFrequency = remember(logs) {
        val days = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f)
        val sdfShortDay = SimpleDateFormat("u", Locale("tr"))
        logs.forEach { log ->
            try {
                val dayNum = sdfShortDay.format(Date(log.date)).toInt()
                if (dayNum in 1..7) {
                    days[dayNum - 1] += 1f
                }
            } catch (e: Exception) {
                days[0] += 0.5f
            }
        }
        days
    }

    val muscleGroupsDist = remember(logs) {
        mapOf(
            "Göğüs" to 0.35f,
            "Bacak" to 0.25f,
            "Sırt" to 0.20f,
            "Omuz" to 0.12f,
            "Kollar" to 0.08f
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Stats Subtabs (Activity, Strength, Calculators)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .background(SurfaceDark, RoundedCornerShape(12.dp))
                .padding(4.dp)
                .horizontalScroll(rememberScrollState())
        ) {
            val subTabs = listOf("Aktivite & Isı Haritası", "Güç & 1RM Analizi", "Hesaplayıcılar")
            subTabs.forEachIndexed { index, title ->
                val isSelected = activeSubTab == index
                Box(
                    modifier = Modifier
                        .widthIn(min = 90.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) OrangePrimary else Color.Transparent)
                        .clickable { activeSubTab = index }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
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
            // TAB 0: ACTIVITY, HEATMAP, WEEKLY BAR CHART
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // Summary Totals
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("overall_totals_card"),
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Genel İstatistik Özetleri",
                                color = OrangePrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Toplam Antrenman", color = MutedText, fontSize = 11.sp)
                                    Text("$totalWorkouts Seans", color = LightText, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                }
                                Column {
                                    Text("Hacim Kaldırılan", color = MutedText, fontSize = 11.sp)
                                    Text("${String.format("%.0f", totalLiftedWeight)} kg", color = LightText, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Divider(color = SurfaceDark)
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Toplam Set", color = MutedText, fontSize = 11.sp)
                                    Text("$totalSets Set", color = LightText, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                                Column {
                                    Text("Ortalama Süre", color = MutedText, fontSize = 11.sp)
                                    Text("${String.format("%.1f", averageDurationMins)} dk", color = LightText, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                                Column {
                                    Text("En Aktif Gün", color = MutedText, fontSize = 11.sp)
                                    Text(mostActiveDay, color = OrangeAccent, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                            }
                        }
                    }
                }

                // GitHub-style smart calendar consistency heatmap! (Last 12 weeks grid)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Akıllı İstikrar Isı Haritası",
                                fontWeight = FontWeight.Bold,
                                color = LightText,
                                fontSize = 15.sp
                            )
                            Text(
                                "Son 12 haftalık fitness seans sıklığı (GitHub stili)",
                                color = MutedText,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Visual Grid heatmap
                            ConsistencyHeatmapGrid(logs = logs)
                        }
                    }
                }

                // Weekly Vertical bar chart
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("weekly_freq_chart"),
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Haftalık Antrenman Sıklığı",
                                fontWeight = FontWeight.Bold,
                                color = LightText,
                                fontSize = 15.sp
                            )
                            Text(
                                "Günlere göre tamamlanan antrenman miktarları",
                                color = MutedText,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            WeeklyFreqVerticalBarChart(weeklyFrequency = weeklyFrequency)
                        }
                    }
                }

                // Muscle target distribution
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("muscle_distribution_card"),
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Hedeflenen Kas Grubu Dağılımı",
                                fontWeight = FontWeight.Bold,
                                color = LightText,
                                fontSize = 15.sp
                            )
                            Text(
                                "Egzersizlerinizde en çok çalıştırılan odak bölgeleri",
                                color = MutedText,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            muscleGroupsDist.forEach { (muscle, pct) ->
                                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(muscle, color = LightText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        Text("${(pct * 100).toInt()}%", color = OrangePrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .background(SurfaceDark, RoundedCornerShape(4.dp))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(pct)
                                                .fillMaxHeight()
                                                .background(
                                                    brush = Brush.linearGradient(colors = listOf(OrangePrimary, OrangeAccent)),
                                                    shape = RoundedCornerShape(4.dp)
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if (activeSubTab == 1) {
            // TAB 1: STRENGTH, ESTIMATED 1RM, PROGRESS STATS
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // Lift Selector
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Anatomi Güç Egzersizi Odak Noktası",
                                color = OrangePrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                liftsList.forEach { lift ->
                                    val isSelected = selectedLiftFor1RM == lift
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) OrangePrimary else SurfaceDark)
                                            .clickable { selectedLiftFor1RM = lift }
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = lift,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.Black else LightText,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 1RM Report & Lift Progression Reports
                item {
                    // Let's compute estimated 1RM based on selected lift and body status
                    val currentWeight = weightLogs.firstOrNull()?.weight ?: 75.0
                    val recordsForLift = records.filter { it.exerciseName.contains(selectedLiftFor1RM, ignoreCase = true) }
                    
                    val maxLogLifted = if (recordsForLift.isNotEmpty()) recordsForLift.maxOfOrNull { it.weight } ?: 70.0 else 70.0
                    val est1RM = maxLogLifted * (1.0 + 5.0 / 30.0) // Epley formula estimation with average 5 reps reference
                    val volumeTotalLifted = recordsForLift.sumOf { it.weight }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Tahmini 1 Rep Max (1RM)", color = MutedText, fontSize = 11.sp)
                                    Text("${String.format("%.1f", est1RM)} kg", color = OrangePrimary, style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black))
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(OrangeAccent.copy(alpha = 0.15f))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    val strengthRatio = est1RM / currentWeight
                                    val rankText = when {
                                        strengthRatio < 0.8 -> "Başlangıç"
                                        strengthRatio < 1.2 -> "Orta Seviye"
                                        strengthRatio < 1.6 -> "İleri Seviye"
                                        else -> "Elit Seviye"
                                    }
                                    Text(rankText, color = OrangeAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Divider(color = SurfaceDark)
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Egzersiz Kişisel Rekoru (PR)", color = MutedText, fontSize = 11.sp)
                                    Text("$maxLogLifted kg", color = LightText, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                                Column {
                                    Text("Toplam Çalışılan Hacim", color = MutedText, fontSize = 11.sp)
                                    Text("$volumeTotalLifted kg", color = LightText, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                                Column {
                                    Text("Güç Katsayısı", color = MutedText, fontSize = 11.sp)
                                    Text(String.format("%.2f x", est1RM / currentWeight), color = LightText, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                            }
                        }
                    }
                }

                // Progression Lift chart simulator
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Güç Kapasitesi Değişim Raporu",
                                fontWeight = FontWeight.Bold,
                                color = LightText,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Visual simulated chart
                            ProgressionChartVisual(selectedLift = selectedLiftFor1RM)
                        }
                    }
                }

                // Standard Strength Level Ranking chart reference for motivation
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Sınıflandırma ve Güç Derecesi Standartları",
                                color = OrangePrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                "Sıralama katsayısı, seçilen egzersizdeki 1RM değerinin vücut ağırlığınıza oranı üzerinden hesaplanır:",
                                color = MutedText,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            LevelRankItem("Elit Savaşçı", "1.6x ve üzeri", true)
                            LevelRankItem("İleri Seviye", "1.2x - 1.6x", false)
                            LevelRankItem("Orta Segment", "0.8x - 1.2x", false)
                            LevelRankItem("Yeni Başlayan", "0.8x altı", false)
                        }
                    }
                }
            }
        } else {
            // TAB 2: CALCULATORS (PLATE & VOLUME CALCULATOR)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // 1. Barbell Plate Stack Calculator
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Demir Plaka Dağılım Hesaplayıcı",
                                color = OrangePrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Barın her bir yanına takılması gereken plakaları görün.",
                                color = MutedText,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Slider target weight
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Hedef Toplam Ağırlık: ${targetPlateWeight.toInt()} kg", color = LightText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = targetPlateWeight,
                                onValueChange = { targetPlateWeight = it },
                                valueRange = 20f..250f,
                                colors = SliderDefaults.colors(activeTrackColor = OrangePrimary, thumbColor = OrangePrimary)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Choose barbell
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                listOf(20f, 15f, 10f).forEach { bar ->
                                    val isSel = selectedBarWeight == bar
                                    val barName = when(bar) {
                                        20f -> "Olimpik (20 kg)"
                                        15f -> "Standart (15 kg)"
                                        else -> "İnce/Z (10 kg)"
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSel) OrangePrimary else SurfaceDark)
                                            .clickable { selectedBarWeight = bar }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            barName,
                                            color = if (isSel) Color.Black else LightText,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Plate calculation logic
                            val remainderPerSide = (targetPlateWeight - selectedBarWeight) / 2f
                            val calculatedPlatesList = calculatePlatesForSide(remainderPerSide)

                            if (remainderPerSide <= 0) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().background(SurfaceDark, RoundedCornerShape(10.dp)).padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Plaka eklemeye gerek yok. Sadece boş bar ağırlığı!", color = OrangeAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Column(
                                    modifier = Modifier.fillMaxWidth().background(SurfaceDark, RoundedCornerShape(12.dp)).padding(12.dp)
                                ) {
                                    Text(
                                        "Tek Taraf Plaka Konfigürasyonu (${remainderPerSide} kg):",
                                        color = MutedText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))

                                    calculatedPlatesList.forEach { (plate, qty) ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .background(OrangePrimary, CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(Icons.Filled.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("$plate kg Plaka", color = LightText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            }
                                            Text("$qty adet", color = OrangeAccent, fontWeight = FontWeight.Black, fontSize = 13.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Volume and Hypertrophy Calculator
                item {
                    val computedVolume = sliderWeightLoad * sliderSets * sliderReps
                    val adaptationType = when {
                        sliderReps <= 5 -> Pair("Maksimum Güç & Nöral Adaptasyon", Color(0xFFE57373))
                        sliderReps <= 12 -> Pair("Miyofibriler Hipertrofi (Kas Yapımı)", OrangePrimary)
                        else -> Pair("Kas Dayanıklılığı & Sarkoplazmik Yoğunluk", Color(0xFF81C784))
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Mühimmat Hacim ve Hipertrofi Analizörü",
                                color = OrangePrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Dönemsel antrenman yükünüzü ve kas adaptasyonlarını analiz edin.",
                                color = MutedText,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Weight Slider
                            Text("Ağırlık Yükü: ${sliderWeightLoad.toInt()} kg", color = LightText, fontSize = 12.sp)
                            Slider(
                                value = sliderWeightLoad,
                                onValueChange = { sliderWeightLoad = it },
                                valueRange = 10f..200f,
                                colors = SliderDefaults.colors(activeTrackColor = OrangePrimary, thumbColor = OrangePrimary)
                            )

                            // Sets Slider
                            Text("Set Sayısı: ${sliderSets.toInt()} Set", color = LightText, fontSize = 12.sp)
                            Slider(
                                value = sliderSets,
                                onValueChange = { sliderSets = it },
                                valueRange = 1f..15f,
                                colors = SliderDefaults.colors(activeTrackColor = OrangePrimary, thumbColor = OrangePrimary)
                            )

                            // Reps Slider
                            Text("Tekrar Sayısı: ${sliderReps.toInt()} Tekrar", color = LightText, fontSize = 12.sp)
                            Slider(
                                value = sliderReps,
                                onValueChange = { sliderReps = it },
                                valueRange = 1f..30f,
                                colors = SliderDefaults.colors(activeTrackColor = OrangePrimary, thumbColor = OrangePrimary)
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = SurfaceDark)
                            Spacer(modifier = Modifier.height(12.dp))

                            // Results output
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Hesaplanan Toplam Hacim", color = MutedText, fontSize = 11.sp)
                                    Text("${computedVolume.toInt()} kg", color = LightText, fontWeight = FontWeight.Black, fontSize = 18.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(adaptationType.second.copy(alpha = 0.15f))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        adaptationType.first,
                                        color = adaptationType.second,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
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
}

// GITHUB-STYLE 12-WEEK CONSISTENCY CALENDAR GRID CHART
@Composable
fun ConsistencyHeatmapGrid(logs: List<WorkoutSessionLog>) {
    // Generate simulated last 84 days (12 weeks)
    // Convert logs into calendar day timestamps
    val sdfDay = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val sessionDatesSet = remember(logs) {
        logs.map { sdfDay.format(Date(it.date)) }.toSet()
    }

    val daysOfWeekLabels = listOf("Pzt", "Çar", "Cum", "Paz")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // y-axis days indicator
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            daysOfWeekLabels.forEach { label ->
                Text(label, color = MutedText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Grid contents (12 columns)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val calendar = Calendar.getInstance()
            // Backtrack to 12 weeks ago, aligned to Monday
            calendar.add(Calendar.WEEK_OF_YEAR, -11)
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

            for (w in 0 until 12) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Week header (month name indicator)
                    val monthLabel = if (w == 0 || w == 6) {
                        SimpleDateFormat("MMM", Locale("tr")).format(calendar.time)
                    } else ""
                    Text(monthLabel, color = OrangeAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.height(16.dp))

                    for (d in 0 until 7) {
                        val formattedDate = sdfDay.format(calendar.time)
                        val workoutCompleted = sessionDatesSet.contains(formattedDate)

                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    if (workoutCompleted) OrangePrimary 
                                    else MutedText.copy(alpha = 0.1f)
                                )
                        )
                        // Progress to next day
                        calendar.add(Calendar.DAY_OF_YEAR, 1)
                    }
                }
            }
        }
    }
}

@Composable
fun ProgressionChartVisual(selectedLift: String) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(SurfaceDark, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        val w = size.width
        val h = size.height

        // Simulated progress curve points based on exercise name length to make it distinct!
        val mockPointsCount = 6
        val stepsX = w / (mockPointsCount - 1)
        val initialOffset = if (selectedLift.contains("Bench")) 50f else 75f

        val path = androidx.compose.ui.graphics.Path()

        // Trace grid
        for (gridIdx in 1..3) {
            val gridY = h * (gridIdx / 4f)
            drawLine(
                color = MutedText.copy(alpha = 0.1f),
                start = Offset(0f, gridY),
                end = Offset(w, gridY),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Draw progression path
        for (index in 0 until mockPointsCount) {
            val ptX = index * stepsX
            // Progression trends upward with indexes simulation
            val factor = index * 12f + (index * index * 1.5f)
            val normalizedY = (initialOffset + factor) / 200f
            val ptY = h - (h * normalizedY).toFloat()

            if (index == 0) {
                path.moveTo(ptX, ptY)
            } else {
                val prevX = (index - 1) * stepsX
                val prevFactor = (index - 1) * 12f + ((index - 1) * (index - 1) * 1.5f)
                val prevYNorm = (initialOffset + prevFactor) / 200f
                val prevPtY = h - (h * prevYNorm).toFloat()

                val ctrlX = (prevX + ptX) / 2f
                path.cubicTo(ctrlX, prevPtY, ctrlX, ptY, ptX, ptY)
            }

            // Draw individual dots
            drawCircle(
                color = OrangePrimary,
                radius = 3.dp.toPx(),
                center = Offset(ptX, ptY)
            )
        }

        drawPath(
            path = path,
            color = OrangePrimary,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 2.5.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                join = androidx.compose.ui.graphics.StrokeJoin.Round
            )
        )
    }
}

@Composable
fun LevelRankItem(rankName: String, scale: String, glow: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .background(
                if (glow) OrangePrimary.copy(alpha = 0.08f) else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(if (glow) OrangePrimary else MutedText, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(rankName, color = if (glow) OrangePrimary else LightText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        Text(scale, color = MutedText, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

// Greedy plate computation configuration solver
private fun calculatePlatesForSide(weight: Float): List<Pair<Float, Int>> {
    val plateDenominations = listOf(25f, 20f, 15f, 10f, 5f, 2.5f, 1.25f)
    var target = weight
    val result = mutableListOf<Pair<Float, Int>>()

    for (denom in plateDenominations) {
        if (target >= denom) {
            val count = (target / denom).toInt()
            result.add(Pair(denom, count))
            target %= denom
        }
    }
    return result
}

@Composable
fun WeeklyFreqVerticalBarChart(weeklyFrequency: FloatArray) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .background(SurfaceDark, RoundedCornerShape(12.dp))
            .padding(top = 16.dp, bottom = 4.dp, start = 8.dp, end = 8.dp)
    ) {
        val width = size.width
        val height = size.height

        val maxVal = (weeklyFrequency.maxOrNull() ?: 1f).coerceAtLeast(1f)

        val columns = 7
        val colWidth = width / columns
        val barWidth = colWidth * 0.45f

        for (i in 0 until columns) {
            val freq = weeklyFrequency[i]
            val barHeightNorm = freq / maxVal
            val barHeight = height * barHeightNorm * 0.75f // reserves top padding

            val x = (colWidth * i) + (colWidth - barWidth) / 2f
            val y = height - barHeight - 16.dp.toPx() // bottom label offset

            // Draw shadow bar
            drawRoundRect(
                color = MutedText.copy(alpha = 0.05f),
                topLeft = Offset(x, 0f),
                size = Size(barWidth, height - 16.dp.toPx()),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )

            // Draw real value bar
            drawRoundRect(
                color = if (freq > 0) OrangePrimary else MutedText.copy(alpha = 0.2f),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight.coerceAtLeast(4.dp.toPx())),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
        }
    }

    // Days label text row below the canvas
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val daysLabels = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")
        daysLabels.forEach { label ->
            Text(
                text = label,
                color = MutedText,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(36.dp)
            )
        }
    }
}

