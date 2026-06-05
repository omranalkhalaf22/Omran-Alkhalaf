package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.viewmodel.FitTrackViewModel
import com.example.viewmodel.WorkoutSet

@Composable
fun WorkoutSessionView(
    viewModel: FitTrackViewModel
) {
    val activeProgram by viewModel.activeProgram.collectAsState()
    val activeExercises by viewModel.activeExercisesList.collectAsState()
    val elapsedSeconds by viewModel.workoutDurationSeconds.collectAsState()
    val doneCount by viewModel.completedExercisesCount.collectAsState()

    // Rest tracking
    val showRestBanner by viewModel.showRestBanner.collectAsState()
    val restRemaining by viewModel.restTimeRemaining.collectAsState()
    val restMax by viewModel.restTimeMax.collectAsState()
    val restIsActive by viewModel.restTimerIsActive.collectAsState()
    val restMuted by viewModel.restTimerMuted.collectAsState()
    val restFinishedAlert by viewModel.restTimeFinishedAlert.collectAsState()

    var showCancelWarningDialog by remember { mutableStateOf(false) }

    val formattedTime = remember(elapsedSeconds) {
        val hrs = elapsedSeconds / 3600
        val mins = (elapsedSeconds % 3600) / 60
        val secs = elapsedSeconds % 60
        if (hrs > 0) {
            String.format("%02d:%02d:%02d", hrs, mins, secs)
        } else {
            String.format("%02d:%02d", mins, secs)
        }
    }

    if (activeProgram == null) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Main Scroll Layout
        Column(modifier = Modifier.fillMaxSize()) {
            // STOPWATCH BANNER / TITLEBAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepBlack)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AKTİF ANTRENMAN ⚡",
                        style = MaterialTheme.typography.labelSmall.copy(color = OrangePrimary, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = activeProgram?.name ?: "Antrenman",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = LightText)
                    )
                }

                // Timer watch
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(CardBg, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        Icons.Filled.Timer,
                        contentDescription = null,
                        tint = OrangePrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formattedTime,
                        color = LightText,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
            }

            // PROGRESS SUMMARY BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tamamlanan Egzersiz: $doneCount / ${activeExercises.size}",
                    color = LightText,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = { showCancelWarningDialog = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = AccentError)
                    ) {
                        Text("İptal Et", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Button(
                        onClick = { viewModel.finishWorkoutSession() },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentSuccess, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("submit_workout_button")
                    ) {
                        Text("Antrenmanı Bitir", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            // LIST OF CURRENT ACTIVE EXERCISES
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
            ) {
                items(activeExercises) { exercise ->
                    val setsList = viewModel.currentSessionSets[exercise.id] ?: emptyList()
                    val exerciseDone = setsList.isNotEmpty() && setsList.all { it.isCompleted }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                if (exerciseDone) AccentSuccess.copy(alpha = 0.5f) else Color.Transparent,
                                RoundedCornerShape(16.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (exerciseDone) CardBg.copy(alpha = 0.9f) else CardBg
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .background(
                                                if (exerciseDone) AccentSuccess.copy(alpha = 0.15f) else OrangePrimary.copy(alpha = 0.15f),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            if (exerciseDone) Icons.Filled.CheckCircle else Icons.Filled.FitnessCenter,
                                            contentDescription = null,
                                            tint = if (exerciseDone) AccentSuccess else OrangePrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = exercise.name,
                                            fontWeight = FontWeight.Bold,
                                            color = LightText,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = "${exercise.muscleGroup} • Hedef: 3 Set × 10 Tekrar",
                                            color = MutedText,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                Button(
                                    onClick = { viewModel.addSetRow(exercise.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark, contentColor = LightText),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier.height(30.dp),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("+ Set", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Set Headers
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("SET", modifier = Modifier.width(36.dp), color = MutedText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                                Text("AĞIRLIK (kg)", modifier = Modifier.weight(1f), color = MutedText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                                Text("TEKRAR", modifier = Modifier.weight(1f), color = MutedText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                                Text("NOT / DURUM", modifier = Modifier.weight(1.5f), color = MutedText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                                Spacer(modifier = Modifier.width(44.dp)) // Complete check spacing
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Interactive Sets lists Rows
                            setsList.forEach { valSet ->
                                var weightText by remember { mutableStateOf(valSet.weight.toString()) }
                                var repsText by remember { mutableStateOf(valSet.reps.toString()) }
                                var noteText by remember { mutableStateOf(valSet.note) }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .background(
                                            if (valSet.isCompleted) AccentSuccess.copy(alpha = 0.08f) else Color.Transparent,
                                            RoundedCornerShape(8.dp)
                                        ),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Index
                                    Text(
                                        text = valSet.setIndex.toString(),
                                        fontWeight = FontWeight.Bold,
                                        color = if (valSet.isCompleted) AccentSuccess else LightText,
                                        modifier = Modifier.width(36.dp),
                                        textAlign = TextAlign.Center
                                    )

                                    // Weight entry
                                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                        TextField(
                                            value = weightText,
                                            onValueChange = {
                                                weightText = it
                                                val d = it.toDoubleOrNull() ?: valSet.weight
                                                viewModel.updateSet(exercise.id, valSet.setIndex, d, valSet.reps, valSet.note)
                                            },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            enabled = !valSet.isCompleted,
                                            colors = TextFieldDefaults.colors(
                                                focusedContainerColor = SurfaceDark,
                                                unfocusedContainerColor = SurfaceDark,
                                                focusedTextColor = LightText,
                                                unfocusedTextColor = LightText,
                                                disabledContainerColor = SurfaceDark.copy(alpha = 0.5f),
                                                disabledTextColor = MutedText
                                            ),
                                            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 13.sp),
                                            modifier = Modifier.height(42.dp).padding(horizontal = 4.dp),
                                            singleLine = true
                                        )
                                    }

                                    // Reps entry
                                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                        TextField(
                                            value = repsText,
                                            onValueChange = {
                                                repsText = it
                                                val r = it.toIntOrNull() ?: valSet.reps
                                                viewModel.updateSet(exercise.id, valSet.setIndex, valSet.weight, r, valSet.note)
                                            },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            enabled = !valSet.isCompleted,
                                            colors = TextFieldDefaults.colors(
                                                focusedContainerColor = SurfaceDark,
                                                unfocusedContainerColor = SurfaceDark,
                                                focusedTextColor = LightText,
                                                unfocusedTextColor = LightText,
                                                disabledContainerColor = SurfaceDark.copy(alpha = 0.5f),
                                                disabledTextColor = MutedText
                                            ),
                                            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 13.sp),
                                            modifier = Modifier.height(42.dp).padding(horizontal = 4.dp),
                                            singleLine = true
                                        )
                                    }

                                    // Notes entry
                                    Box(modifier = Modifier.weight(1.5f), contentAlignment = Alignment.Center) {
                                        TextField(
                                            value = noteText,
                                            onValueChange = {
                                                noteText = it
                                                viewModel.updateSet(exercise.id, valSet.setIndex, valSet.weight, valSet.reps, it)
                                            },
                                            placeholder = { Text("Not...", fontSize = 11.sp) },
                                            enabled = !valSet.isCompleted,
                                            colors = TextFieldDefaults.colors(
                                                focusedContainerColor = SurfaceDark,
                                                unfocusedContainerColor = SurfaceDark,
                                                focusedTextColor = LightText,
                                                unfocusedTextColor = LightText,
                                                disabledContainerColor = SurfaceDark.copy(alpha = 0.5f),
                                                disabledTextColor = MutedText
                                            ),
                                            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 11.sp),
                                            modifier = Modifier.height(42.dp).padding(horizontal = 4.dp),
                                            singleLine = true
                                        )
                                    }

                                    // Completed set check checkbox button
                                    Box(modifier = Modifier.width(44.dp), contentAlignment = Alignment.Center) {
                                        IconButton(
                                            onClick = { viewModel.toggleSetCompleted(exercise.id, valSet.setIndex) },
                                            modifier = Modifier.testTag("check_set_${exercise.id}_${valSet.setIndex}")
                                        ) {
                                            Icon(
                                                imageVector = if (valSet.isCompleted) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                                                contentDescription = "Set Tamamla",
                                                tint = if (valSet.isCompleted) AccentSuccess else MutedText,
                                                modifier = Modifier.size(24.dp)
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

        // --- BOTTOM REST TIMER POPUP BANNER ---
        AnimatedVisibility(
            visible = showRestBanner,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding() // Avoid system navigation pill collision
                    .testTag("rest_timer_panel"),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (restFinishedAlert) Icons.Filled.AlarmOn else Icons.Filled.SelfImprovement,
                                contentDescription = null,
                                tint = if (restFinishedAlert) AccentError else OrangePrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (restFinishedAlert) "SÜRE BİTTİ! 🚀" else "SET ARASI DİNLENME",
                                fontWeight = FontWeight.Bold,
                                color = if (restFinishedAlert) AccentError else LightText,
                                fontSize = 14.sp
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = { viewModel.toggleRestMuted() }) {
                                Icon(
                                    if (restMuted) Icons.Filled.VolumeMute else Icons.Filled.VolumeUp,
                                    contentDescription = null,
                                    tint = MutedText
                                )
                            }
                            IconButton(onClick = { viewModel.closeRestTimerBanner() }) {
                                Icon(Icons.Filled.Close, contentDescription = "Kapat", tint = MutedText)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Drawing circular countdown ring inside Canvas
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(76.dp)) {
                                val remainingPct = if (restMax > 0) restRemaining.toFloat() / restMax else 0f
                                
                                // Back dial
                                drawCircle(
                                    color = SurfaceDark,
                                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                                )
                                // Active arc
                                drawArc(
                                    color = if (restFinishedAlert) AccentError else OrangePrimary,
                                    startAngle = -90f,
                                    sweepAngle = remainingPct * 360f,
                                    useCenter = false,
                                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            // Text remaining inside ring
                            Text(
                                text = if (restFinishedAlert) "0" else restRemaining.toString(),
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = if (restFinishedAlert) AccentError else LightText
                            )
                        }

                        // Play / Pause controls
                        Column(
                            modifier = Modifier.weight(1.5f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (restIsActive) {
                                    Button(
                                        onClick = { viewModel.pauseRestTimer() },
                                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark, contentColor = LightText)
                                    ) {
                                        Icon(Icons.Filled.Pause, contentDescription = null, tint = LightText)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Duraklat", fontSize = 12.sp)
                                    }
                                } else {
                                    Button(
                                        onClick = { viewModel.resumeRestTimer() },
                                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.Black)
                                    ) {
                                        Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.Black)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Devam et", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                
                                Button(
                                    onClick = { viewModel.startRestTimer(restMax) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark, contentColor = MutedText)
                                ) {
                                    Icon(Icons.Filled.RestartAlt, contentDescription = null, tint = MutedText)
                                }
                            }

                            // Quick adjustment intervals
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                val timesList = listOf(30, 60, 90, 120, 180)
                                timesList.forEach { s ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (restMax == s) OrangePrimary.copy(alpha = 0.2f) else SurfaceDark)
                                            .clickable { viewModel.setRestTimerDuration(s) }
                                            .padding(vertical = 4.dp, horizontal = 8.dp)
                                    ) {
                                        Text("${s}s", fontSize = 10.sp, color = if (restMax == s) OrangePrimary else LightText, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- DESTRUCTIVE CANCEL WARNING DIALOG ---
        if (showCancelWarningDialog) {
            AlertDialog(
                onDismissRequest = { showCancelWarningDialog = false },
                title = { Text("Antrenmanı İptal Et?", color = AccentError, fontWeight = FontWeight.Bold) },
                containerColor = CardBg,
                text = { Text("Mevcut setleriniz ve istatistik ilerlemeleriniz silinecektir. Devam etmek istiyor musunuz?", color = LightText) },
                dismissButton = {
                    TextButton(onClick = { showCancelWarningDialog = false }) {
                        Text("Vazgeç", color = MutedText)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.cancelWorkout()
                            showCancelWarningDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentError, contentColor = Color.Black)
                    ) {
                        Text("Evet, İptal Et", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}
