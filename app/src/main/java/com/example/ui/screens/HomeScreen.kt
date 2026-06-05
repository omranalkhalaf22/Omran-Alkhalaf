package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.viewmodel.MealLog
import com.example.viewmodel.FitnessGoal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: FitTrackViewModel,
    onNavigateToTab: (Int) -> Unit,
    onStartWorkout: (WorkoutProgram) -> Unit,
    onOpenAddWeightDialog: () -> Unit,
    onOpenAddPhotoDialog: () -> Unit
) {
    val programs by viewModel.allPrograms.collectAsState()
    val weightLogs by viewModel.allWeightLogs.collectAsState()
    val records by viewModel.allRecords.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    val streak by viewModel.currentStreak.collectAsState()
    val logs by viewModel.allWorkoutLogs.collectAsState()

    // Dynamic ecosystem states
    val currentXp by viewModel.xp.collectAsState()
    val waterIntake by viewModel.waterIntake.collectAsState()
    val waterTarget by viewModel.waterTarget.collectAsState()
    val stepCount by viewModel.stepCount.collectAsState()
    val stepTarget by viewModel.stepTarget.collectAsState()
    val mealLogs by viewModel.mealLogs.collectAsState()
    val allGoals by viewModel.allGoals.collectAsState()
    val socialPosts by viewModel.socialPosts.collectAsState()
    val leaderboardUsers by viewModel.leaderboardUsers.collectAsState()

    // Recovery states
    val chestRecovery by viewModel.chestRecovery.collectAsState()
    val backRecovery by viewModel.backRecovery.collectAsState()
    val shoulderRecovery by viewModel.shoulderRecovery.collectAsState()
    val legsRecovery by viewModel.legsRecovery.collectAsState()
    val coreRecovery by viewModel.coreRecovery.collectAsState()

    var showAddMealDialog by remember { mutableStateOf(false) }
    var activeMainSectionTab by remember { mutableStateOf(0) } // 0: Genel, 1: Hedefler, 2: Sosyal Akış & Sıralama

    val completedThisWeek = remember(logs) {
        val oneWeekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
        logs.count { it.date >= oneWeekAgo }
    }

    val latestWeight = remember(weightLogs) {
        weightLogs.firstOrNull()?.weight ?: profile.weight
    }

    val latestPrs = remember(records) {
        val tracked = listOf("Bench Press", "Squat", "Deadlift", "Overhead Press")
        tracked.map { name ->
            records.filter { it.exerciseName == name }.maxByOrNull { it.weight }
                ?: PersonalRecord(exerciseName = name, weight = 0.0)
        }
    }

    // Calories calculation
    val caloriesConsumed = remember(mealLogs) { mealLogs.sumOf { it.calories } }
    val proteinConsumed = remember(mealLogs) { mealLogs.sumOf { it.protein } }
    val carbsConsumed = remember(mealLogs) { mealLogs.sumOf { it.carbs } }
    val fatConsumed = remember(mealLogs) { mealLogs.sumOf { it.fat } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
    ) {
        // Welcoming & Level XP Bar header
        item {
            val currentLevel = (currentXp / 500) + 1
            val xpInLevel = currentXp % 500
            val pctXp = xpInLevel.toFloat() / 500f
            val levelTitle = when {
                currentLevel <= 1 -> "Çaylak Sporcu"
                currentLevel <= 3 -> "Zinde Atlet"
                currentLevel <= 5 -> "Demir Bükücü"
                currentLevel <= 7 -> "Seçkin Şampiyon"
                else -> "Efsanevi Canavar"
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBg, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Hoş Geldin, ${profile.name} 👋",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = LightText
                            )
                        )
                        Text(
                            text = "Seviye $currentLevel • $levelTitle",
                            color = OrangePrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(OrangePrimary.copy(alpha = 0.15f), CircleShape)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "$currentXp XP",
                            color = OrangePrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // XP Progress Bar
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Sonraki Seviye için İlerleme", color = MutedText, fontSize = 11.sp)
                        Text("$xpInLevel / 500 XP", color = LightText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(SurfaceDark, CircleShape)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(pctXp)
                                .fillMaxHeight()
                                .background(
                                    brush = Brush.linearGradient(colors = listOf(OrangePrimary, OrangeAccent)),
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
        }

        // Quick Actions Row
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Hızlı İşlemler",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = OrangePrimary,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (programs.isNotEmpty()) {
                                onStartWorkout(programs.first())
                            } else {
                                onNavigateToTab(1) // Tab 1 is Workouts Screen
                            }
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(54.dp)
                            .testTag("action_start_workout"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrangePrimary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Yarışa Başla!", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Button(
                        onClick = { showAddMealDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .testTag("action_log_meal"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CardBg,
                            contentColor = LightText
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = ButtonDefaults.outlinedButtonBorder
                    ) {
                        Icon(Icons.Filled.Restaurant, contentDescription = null, tint = OrangePrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Yemek Yat", fontSize = 11.sp)
                    }

                    Button(
                        onClick = onOpenAddWeightDialog,
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .testTag("action_add_weight"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CardBg,
                            contentColor = LightText
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = ButtonDefaults.outlinedButtonBorder
                    ) {
                        Icon(Icons.Filled.Scale, contentDescription = null, tint = OrangePrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Kilo", fontSize = 11.sp)
                    }
                }
            }
        }

        // Section Tabs
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark, RoundedCornerShape(10.dp))
                    .padding(3.dp)
            ) {
                val secTabs = listOf("Gözetleme Paneli", "Günlük Hedefler", "Sosyal Akış & Sıralama")
                secTabs.forEachIndexed { index, title ->
                    val isSel = activeMainSectionTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) OrangePrimary else Color.Transparent)
                            .clickable { activeMainSectionTab = index }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSel) Color.Black else MutedText,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        if (activeMainSectionTab == 0) {
            // MAIN GRID ECOSYSTEM TAB
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Aktivite & Tüketim Paneli",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = OrangePrimary,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        )
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        maxItemsInEachRow = 2
                    ) {
                        // Card 1: Günlük Seri
                        StatsCard(
                            modifier = Modifier.weight(1f),
                            title = "Günlük Seri",
                            value = "$streak Gün 🔥",
                            description = "Streak serisini bozma!",
                            icon = Icons.Filled.FlashOn,
                            accentColor = OrangePrimary
                        )

                        // Card 2: Bu haftaki antrenmanlar
                        StatsCard(
                            modifier = Modifier.weight(1f),
                            title = "Haftalık Isınma",
                            value = "$completedThisWeek Antrenman",
                            description = "Kalan seans: ${3 - completedThisWeek}",
                            icon = Icons.Filled.EmojiEvents,
                            accentColor = AccentSuccess
                        )

                        // Card 3: Son kilo
                        val changeString = if (weightLogs.size >= 2) {
                            val diff = weightLogs[0].weight - weightLogs[1].weight
                            val sign = if (diff >= 0) "+" else ""
                            "$sign${String.format("%.1f", diff)} kg"
                        } else "Değişim Yok"

                        StatsCard(
                            modifier = Modifier.weight(1f),
                            title = "Son Kilo Kaydı",
                            value = "${String.format("%.1f", latestWeight)} ${if (profile.isKg) "kg" else "lbs"}",
                            description = "Değişim: $changeString",
                            icon = Icons.Filled.MonitorWeight,
                            accentColor = OrangeAccent
                        )

                        // Card 4: Sıradaki Güç
                        val upcoming = if (programs.isNotEmpty()) programs.first().name else "Yeni Program Yapılmadı"
                        StatsCard(
                            modifier = Modifier.weight(1f),
                            title = "Sıradaki Güç",
                            value = upcoming,
                            description = if (programs.isNotEmpty()) "${programs.first().goal}" else "Savaşçı sensin!",
                            icon = Icons.Filled.FitnessCenter,
                            accentColor = Color(0xFF64B5F6)
                        )
                    }
                }
            }

            // INTERACTIVE COMPONENT 1: WATER TRACKER
            item {
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(0xFF2196F3).copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.WaterDrop, contentDescription = null, tint = Color(0xFF2196F3), modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Hidrasyon & Su Tüketimi", fontWeight = FontWeight.Bold, color = LightText, fontSize = 14.sp)
                                    Text("Hedefe kalan: ${(waterTarget - waterIntake).coerceAtLeast(0)} ml", color = MutedText, fontSize = 11.sp)
                                }
                            }
                            Text(
                                "$waterIntake / $waterTarget ml",
                                color = Color(0xFF2196F3),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Progress Indicator
                        val pctWater = (waterIntake.toFloat() / waterTarget.toFloat()).coerceAtMost(1f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .background(SurfaceDark, CircleShape)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(pctWater)
                                    .fillMaxHeight()
                                    .background(Color(0xFF2196F3), CircleShape)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.addWater(250) },
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark, contentColor = LightText),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).height(38.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("+250 ml", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { viewModel.addWater(500) },
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark, contentColor = LightText),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).height(38.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("+500 ml", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { viewModel.resetWater() },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentError.copy(alpha = 0.15f), contentColor = AccentError),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).height(38.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Sıfırla", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // INTERACTIVE COMPONENT 2: STEP TRACKER
            item {
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(0xFFFF9800).copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.DirectionsWalk, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Günlük Kardiyo Adımları", fontWeight = FontWeight.Bold, color = LightText, fontSize = 14.sp)
                                    Text("Kalori Yakımı: ${(stepCount * 0.04).toInt()} kcal", color = MutedText, fontSize = 11.sp)
                                }
                            }
                            Text(
                                "$stepCount / $stepTarget Adım",
                                color = Color(0xFFFF9800),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Progress bar
                        val pctSteps = (stepCount.toFloat() / stepTarget.toFloat()).coerceAtMost(1f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .background(SurfaceDark, CircleShape)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(pctSteps)
                                    .fillMaxHeight()
                                    .background(Color(0xFFFF9800), CircleShape)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Action button to simulate stepping
                        Button(
                            onClick = { viewModel.addSteps(1500) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800).copy(alpha = 0.12f), contentColor = Color(0xFFFF9800)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(38.dp)
                        ) {
                            Icon(Icons.Filled.DirectionsRun, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Simüle Et (+1500 Adım)", fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            // INTERACTIVE COMPONENT 3: NUTRITION kcal & list
            item {
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(0xFF4CAF50).copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Restaurant, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Kalori & Beslenme", fontWeight = FontWeight.Bold, color = LightText, fontSize = 14.sp)
                                    Text("Ekleme adeti: ${mealLogs.size}", color = MutedText, fontSize = 11.sp)
                                }
                            }
                            Text(
                                "$caloriesConsumed / ${viewModel.calorieTarget} kcal",
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Progress bars for macros
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            MacroBarItem(modifier = Modifier.weight(1f), label = "Prot", value = proteinConsumed, target = viewModel.proteinTarget, color = OrangePrimary)
                            MacroBarItem(modifier = Modifier.weight(1f), label = "Karb", value = carbsConsumed, target = viewModel.carbsTarget, color = Color(0xFF64B5F6))
                            MacroBarItem(modifier = Modifier.weight(1f), label = "Yağ", value = fatConsumed, target = viewModel.fatTarget, color = Color(0xFFFFD54F))
                        }

                        // Food listing for today
                        if (mealLogs.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Divider(color = SurfaceDark)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Bugün Kaydedilenler:", color = MutedText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            mealLogs.forEach { ml ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .background(SurfaceDark, RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(ml.name, color = LightText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("${ml.type} • P:${ml.protein}g K:${ml.carbs}g Y:${ml.fat}g", color = MutedText, fontSize = 10.sp)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("${ml.calories} kcal", color = Color(0xFF4CAF50), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        IconButton(onClick = { viewModel.deleteMealLog(ml.id) }, modifier = Modifier.size(20.dp)) {
                                            Icon(Icons.Filled.Close, contentDescription = "Sil", tint = AccentError, modifier = Modifier.size(12.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // INTERACTIVE COMPONENT 4: MUSCLE RECOVERY SCORE
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.SelfImprovement, contentDescription = null, tint = OrangePrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Kas Grupları Yorgunluk Analizi", fontWeight = FontWeight.Bold, color = LightText, fontSize = 14.sp)
                        }
                        Text("Seans bittikten sonra kas hasarını takip et. Hızlı dinlenme sağla.", color = MutedText, fontSize = 11.sp)

                        Spacer(modifier = Modifier.height(14.dp))

                        val muscles = listOf(
                            Triple("Göğüs", chestRecovery, OrangePrimary),
                            Triple("Sırt", backRecovery, Color(0xFF64B5F6)),
                            Triple("Omuz", shoulderRecovery, Color(0xFFFFD54F)),
                            Triple("Bacak", legsRecovery, Color(0xFFFF5252)),
                            Triple("Karın", coreRecovery, Color(0xFF4CAF50))
                        )

                        muscles.forEach { (name, score, color) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(color, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(name, color = LightText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("($score% Yenilendi)", color = if (score > 70) AccentSuccess else OrangeAccent, fontSize = 11.sp)
                                }
                                Box(modifier = Modifier.width(100.dp).height(6.dp).background(SurfaceDark, CircleShape)) {
                                    Box(modifier = Modifier.fillMaxWidth(score.toFloat() / 100f).fillMaxHeight().background(color, CircleShape))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { viewModel.quickRestMuscle(name) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark, contentColor = OrangePrimary),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.height(26.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp)
                                ) {
                                    Text("Dinlen", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // INTERACTIVE COMPONENT 5: DYNAMIC WORKOUT CALENDAR WORKOUT DAYS
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = OrangePrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Antrenman Takvimi", fontWeight = FontWeight.Bold, color = LightText, fontSize = 14.sp)
                        }
                        Text("Bu ay tamamlanan çalışma günleri vurgulanmaktadır.", color = MutedText, fontSize = 11.sp)

                        Spacer(modifier = Modifier.height(12.dp))

                        // Render neat grid of calendar
                        val daysWithWorkouts = remember(logs) {
                            logs.map {
                                val cal = Calendar.getInstance()
                                cal.timeInMillis = it.date
                                cal.get(Calendar.DAY_OF_MONTH)
                            }.toSet()
                        }

                        val calendar = Calendar.getInstance()
                        val currentMonthName = SimpleDateFormat("MMMM yyyy", java.util.Locale("tr")).format(Date())
                        
                        Text(currentMonthName.replaceFirstChar { it.uppercase() }, color = OrangeAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Grid headers
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            val headers = listOf("Pt", "Sa", "Çr", "Pr", "Cu", "Ct", "Pz")
                            headers.forEach { h ->
                                Text(h, color = MutedText, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))

                        // Hardcode sample compact June days layout (30 days) starting Monday
                        val daysMatrix = listOf(
                            listOf(1, 2, 3, 4, 5, 6, 7),
                            listOf(8, 9, 10, 11, 12, 13, 14),
                            listOf(15, 16, 17, 18, 19, 20, 21),
                            listOf(22, 23, 24, 25, 26, 27, 28),
                            listOf(29, 30, 0, 0, 0, 0, 0)
                        )

                        daysMatrix.forEach { week ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                week.forEach { dayNum ->
                                    if (dayNum == 0) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    } else {
                                        val working = daysWithWorkouts.contains(dayNum) || (dayNum == 5 && logs.isEmpty()) // Seed day 5 as completed highlight
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .padding(2.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (working) OrangePrimary else SurfaceDark
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = dayNum.toString(),
                                                color = if (working) Color.Black else LightText,
                                                fontWeight = if (working) FontWeight.Black else FontWeight.Normal,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Regular personal records card we had initially
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("prs_history_card"),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = OrangePrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Kişisel Rekorlar (PR)",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = LightText
                                    )
                                )
                            }
                            IconButton(onClick = { onNavigateToTab(2) }) { // Go to progress Weight logs list!
                                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MutedText)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        latestPrs.forEach { record ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(SurfaceDark, shape = RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(OrangePrimary.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Filled.KeyboardDoubleArrowUp,
                                            contentDescription = null,
                                            tint = OrangePrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = record.exerciseName,
                                        fontWeight = FontWeight.SemiBold,
                                        color = LightText,
                                        fontSize = 14.sp
                                    )
                                }
                                Text(
                                    text = "${record.weight} ${if (profile.isKg) "kg" else "lbs"}",
                                    fontWeight = FontWeight.Bold,
                                    color = OrangeAccent,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        } else if (activeMainSectionTab == 1) {
            // HEDEFLER TAB (Active goal progression details)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Özel Sporcu Hedefleri", color = OrangePrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Button(
                        onClick = { viewModel.addNewGoal("Kardiyo Koşu Seansı", "10 KM • Tempolu", "Kardiyo") },
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary.copy(alpha = 0.2f), contentColor = OrangePrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(30.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Hedef Ekle", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (allGoals.isEmpty()) {
                item {
                    Text("Hedef listesi boş.", color = MutedText, fontSize = 13.sp, modifier = Modifier.padding(16.dp))
                }
            } else {
                items(allGoals) { g ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = g.isCompleted,
                                onCheckedChange = { viewModel.toggleGoalCompleted(g.id) },
                                colors = CheckboxDefaults.colors(checkedColor = OrangePrimary)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    g.title,
                                    color = if (g.isCompleted) MutedText else LightText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    textDecoration = if (g.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                                )
                                Text("Açıklama / Hedef Değer: ${g.target}", color = MutedText, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                // Tiny process meter
                                Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(SurfaceDark, CircleShape)) {
                                    Box(modifier = Modifier.fillMaxWidth(g.progress).fillMaxHeight().background(OrangeAccent, CircleShape))
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .background(OrangePrimary.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(g.category, color = OrangePrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            // SOSYAL FEED & SIRALAMA LEADERBOARD TAB
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Leaderboard, null, tint = OrangePrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Haftalık Şampiyonlar Skor Tablosu", color = OrangePrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            // Leaderboard rendering
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        leaderboardUsers.forEach { user ->
                            val isMe = user.isCurrentUser
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (isMe) OrangePrimary.copy(alpha = 0.15f) else SurfaceDark, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "#${user.rank}",
                                        color = if (user.rank == 1) Color(0xFFFFD54F) else OrangePrimary,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        modifier = Modifier.width(26.dp)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(OrangeAccent.copy(alpha = 0.15f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(user.avatar.take(1), color = OrangeAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = user.name,
                                        color = if (isMe) OrangePrimary else LightText,
                                        fontWeight = if (isMe) FontWeight.Black else FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                                Text("${user.score} XP", color = LightText, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Stream, null, tint = OrangePrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sporcu Sosyal Akışı (Feed)", color = OrangePrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            if (socialPosts.isEmpty()) {
                item {
                    Text("Akış kaydı bulunmamaktadır.", color = MutedText)
                }
            } else {
                items(socialPosts) { post ->
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(OrangePrimary.copy(alpha = 0.15f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(post.authorAvatar.take(1), color = OrangePrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(post.author, color = LightText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(post.timestampText, color = MutedText, fontSize = 10.sp)
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .background(OrangePrimary.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("Egzersiz", color = OrangePrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(post.activitySummary, color = LightText, fontSize = 13.sp, lineHeight = 18.sp)
                            Spacer(modifier = Modifier.height(14.dp))
                            Divider(color = SurfaceDark)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { viewModel.toggleLikePost(post.id) }
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (post.isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                        contentDescription = null,
                                        tint = if (post.isLiked) AccentError else MutedText,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("${post.likes} Beğeni", color = if (post.isLiked) AccentError else LightText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Comment, null, tint = MutedText, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${post.commentsCount} Yorum", color = MutedText, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // DIALOG POPUP FOR ADDING MEALS NUTRITION
    if (showAddMealDialog) {
        var customFoodName by remember { mutableStateOf("") }
        var customCalories by remember { mutableStateOf("") }
        var customProtein by remember { mutableStateOf("") }
        var customCarbs by remember { mutableStateOf("") }
        var customFat by remember { mutableStateOf("") }
        var selectedMealType by remember { mutableStateOf("Öğle Yemeği") }

        AlertDialog(
            onDismissRequest = { showAddMealDialog = false },
            title = { Text("Beslenme & Öğün Günlüğü", color = OrangePrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            containerColor = CardBg,
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Hızlı Sporcu Menüsü Ekle:", color = LightText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    
                    val quickMeals = listOf(
                        Triple("Tavuk Göğsü & Pirinç Pilavı", 620, Triple(45, 75, 8)),
                        Triple("Muzlu Fıstıklı Protein Shake", 290, Triple(26, 32, 3)),
                        Triple("Yumurta Beyazı Omlet (4)", 180, Triple(24, 2, 8)),
                        Triple("Fırın Bonfile & Brokoli", 490, Triple(38, 15, 14))
                    )

                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        quickMeals.forEach { (name, kcal, macros) ->
                            Box(
                                modifier = Modifier
                                    .background(SurfaceDark, RoundedCornerShape(10.dp))
                                    .border(1.dp, OrangePrimary.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                    .clickable {
                                        viewModel.addMeal(selectedMealType, name, kcal, macros.first, macros.second, macros.third)
                                        showAddMealDialog = false
                                    }
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text(name, color = LightText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("$kcal kcal • ${macros.first}g Protein", color = OrangeAccent, fontSize = 10.sp)
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = SurfaceDark)

                    Text("Öğün Türü Seçin:", color = LightText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth().background(SurfaceDark, RoundedCornerShape(8.dp)).padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val types = listOf("Kahvaltı", "Öğle", "Akşam", "Meyve / Atıştırmalık")
                        types.forEach { t ->
                            val isSel = (t == "Meyve / Atıştırmalık" && selectedMealType == "Atıştırmalık") ||
                                    (t == "Kahvaltı" && selectedMealType == "Kahvaltı") ||
                                    (t == "Öğle" && selectedMealType == "Öğle Yemeği") ||
                                    (t == "Akşam" && selectedMealType == "Akşam Yemeği")
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) OrangePrimary else Color.Transparent)
                                    .clickable {
                                        selectedMealType = when(t) {
                                            "Öğle" -> "Öğle Yemeği"
                                            "Akşam" -> "Akşam Yemeği"
                                            "Meyve / Atıştırmalık" -> "Atıştırmalık"
                                            else -> t
                                        }
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(t, color = if (isSel) Color.Black else MutedText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Text("Manuel Öğün Girişi:", color = LightText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = customFoodName,
                        onValueChange = { customFoodName = it },
                        label = { Text("Yemek / Öge Adı", color = MutedText, fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = SurfaceDark,
                            focusedLabelColor = OrangePrimary,
                            unfocusedLabelColor = MutedText
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = customCalories,
                            onValueChange = { customCalories = it },
                            label = { Text("Kcal", color = MutedText, fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary, unfocusedBorderColor = SurfaceDark),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = customProtein,
                            onValueChange = { customProtein = it },
                            label = { Text("Prot (g)", color = MutedText, fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary, unfocusedBorderColor = SurfaceDark),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = customCarbs,
                            onValueChange = { customCarbs = it },
                            label = { Text("Karb (g)", color = MutedText, fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary, unfocusedBorderColor = SurfaceDark),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = customFat,
                            onValueChange = { customFat = it },
                            label = { Text("Yağ (g)", color = MutedText, fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrangePrimary, unfocusedBorderColor = SurfaceDark),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customFoodName.isNotEmpty()) {
                            val kcal = customCalories.toIntOrNull() ?: 0
                            val prot = customProtein.toIntOrNull() ?: 0
                            val carbs = customCarbs.toIntOrNull() ?: 0
                            val fat = customFat.toIntOrNull() ?: 0
                            viewModel.addMeal(selectedMealType, customFoodName, kcal, prot, carbs, fat)
                        }
                        showAddMealDialog = false
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
fun MacroBarItem(
    modifier: Modifier = Modifier,
    label: String,
    value: Int,
    target: Int,
    color: Color
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = MutedText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text("$value / $target g", color = LightText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        val pct = (value.toFloat() / target.toFloat()).coerceAtMost(1f)
        Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(SurfaceDark, CircleShape)) {
            Box(modifier = Modifier.fillMaxWidth(pct).fillMaxHeight().background(color, CircleShape))
        }
    }
}

@Composable
fun StatsCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    description: String,
    icon: ImageVector,
    accentColor: Color
) {
    Card(
        modifier = modifier.height(130.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = MutedText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Column {
                Text(
                    text = value,
                    color = LightText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    color = MutedText,
                    fontSize = 10.sp,
                    lineHeight = 12.sp
                )
            }
        }
    }
}
