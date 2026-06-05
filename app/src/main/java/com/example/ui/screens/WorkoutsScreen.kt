package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.viewmodel.FitTrackViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorkoutsScreen(
    viewModel: FitTrackViewModel,
    onStartWorkout: (WorkoutProgram) -> Unit
) {
    val programs by viewModel.allPrograms.collectAsState()
    val exercises by viewModel.allExercises.collectAsState()
    
    // Video Learning System state flows
    val bookmarkedExerciseIds by viewModel.bookmarkedExerciseIds.collectAsState()
    val customPlaylists by viewModel.customPlaylists.collectAsState()
    val downloadedExerciseIds by viewModel.downloadedExerciseIds.collectAsState()
    
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var activeTab by remember { mutableStateOf(0) } // 0: Programlar, 1: Egzersizler

    // Filter states
    var searchKeyword by remember { mutableStateOf("") }
    var selectedGoalFilter by remember { mutableStateOf("Hepsi") }
    var selectedMuscleFilter by remember { mutableStateOf("Hepsi") }

    // Dialog sheets
    var showCreateProgramWizard by remember { mutableStateOf(false) }
    var selectedExerciseForDetail by remember { mutableStateOf<Exercise?>(null) }
    var showAddCustomExerciseDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Tab Layout headers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .background(SurfaceDark, RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            val tabs = listOf("Programlarım", "Egzersiz Kütüphanesi")
            tabs.forEachIndexed { index, text ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (activeTab == index) OrangePrimary else Color.Transparent)
                        .clickable { activeTab = index }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = text,
                        fontWeight = if (activeTab == index) FontWeight.Bold else FontWeight.Medium,
                        color = if (activeTab == index) Color.Black else MutedText,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Search Input & Action row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = searchKeyword,
                onValueChange = { searchKeyword = it },
                placeholder = { Text("Arama yapın...", color = MutedText, fontSize = 14.sp) },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .clip(RoundedCornerShape(10.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = CardBg,
                    unfocusedContainerColor = CardBg,
                    focusedIndicatorColor = OrangePrimary,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = LightText,
                    unfocusedTextColor = LightText
                ),
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = OrangePrimary) },
                singleLine = true
            )

            if (activeTab == 0) {
                Box(
                    modifier = Modifier
                        .height(52.dp)
                        .background(OrangePrimary.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                        .border(1.dp, OrangePrimary.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .clickable { showCreateProgramWizard = true }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = OrangePrimary)
                        Text("Yeni", color = OrangePrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .height(52.dp)
                        .background(OrangePrimary.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                        .border(1.dp, OrangePrimary.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .clickable { showAddCustomExerciseDialog = true }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Filled.PostAdd, contentDescription = null, tint = OrangePrimary)
                        Text("+Egzersiz", color = OrangePrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // FILTER CHIPS ROW
        if (activeTab == 0) {
            val goals = listOf("Hepsi", "Kas kazanımı", "Güç artışı", "Yağ yakımı", "Genel fitness")
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(goals) { goal ->
                    val isSelected = selectedGoalFilter.lowercase() == goal.lowercase()
                    AssistChip(
                        onClick = { selectedGoalFilter = goal },
                        label = { Text(goal, color = if (isSelected) Color.Black else LightText) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (isSelected) OrangeAccent else CardBg
                        ),
                        border = AssistChipDefaults.assistChipBorder(enabled = !isSelected, borderColor = MutedText.copy(alpha = 0.3f))
                    )
                }
            }
        } else {
            val premiumCategories = listOf("Hepsi", "Favori Listem ⭐️", "Çevrimdışı İndirilenler 📥") + 
                                    customPlaylists.keys.toList() + 
                                    listOf("Göğüs", "Sırt", "Omuz", "Kol", "Bacak", "Karın", "Kardiyo")

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(premiumCategories) { group ->
                    val isSelected = selectedMuscleFilter == group
                    AssistChip(
                        onClick = { selectedMuscleFilter = group },
                        label = { Text(group, color = if (isSelected) Color.Black else LightText) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (isSelected) OrangeAccent else CardBg
                        ),
                        border = AssistChipDefaults.assistChipBorder(enabled = !isSelected, borderColor = MutedText.copy(alpha = 0.3f))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // CONTENT SECTION
        if (activeTab == 0) {
            // Programs list
            val filteredPrograms = programs.filter {
                (selectedGoalFilter == "Hepsi" || it.goal.lowercase() == selectedGoalFilter.lowercase()) &&
                (searchKeyword.isEmpty() || it.name.lowercase().contains(searchKeyword.lowercase()))
            }

            if (filteredPrograms.isEmpty()) {
                EmptyStateView(
                    iconVector = Icons.Filled.FitnessCenter,
                    text = "Aradığınız kriterde program şablonu bulunamadı.\nKendinize özel yeni bir program oluşturun!",
                    buttonText = "Yeni Program Oluştur",
                    onAction = { showCreateProgramWizard = true }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredPrograms) { program ->
                        ProgramCard(
                            program = program,
                            exercisesCount = program.exercisesString.split(",").filter { it.isNotEmpty() }.size,
                            onStart = { onStartWorkout(program) },
                            onDelete = { viewModel.deleteProgram(program.id) }
                        )
                    }
                }
            }
        } else {
            // Exercises list with unified search, bookmarks, playlists, and category filters
            val filteredExercises = exercises.filter { exercise ->
                val matchesKeyword = searchKeyword.isEmpty() || exercise.name.lowercase().contains(searchKeyword.lowercase())
                val matchesFilter = when {
                    selectedMuscleFilter == "Hepsi" -> true
                    selectedMuscleFilter == "Favori Listem ⭐️" -> bookmarkedExerciseIds.contains(exercise.id)
                    selectedMuscleFilter == "Çevrimdışı İndirilenler 📥" -> downloadedExerciseIds.containsKey(exercise.id)
                    customPlaylists.containsKey(selectedMuscleFilter) -> {
                        val playlistExercises = customPlaylists[selectedMuscleFilter] ?: emptyList()
                        playlistExercises.contains(exercise.id)
                    }
                    else -> exercise.muscleGroup.lowercase() == selectedMuscleFilter.lowercase()
                }
                matchesKeyword && matchesFilter
            }

            if (filteredExercises.isEmpty()) {
                EmptyStateView(
                    iconVector = Icons.Filled.SearchOff,
                    text = "Egzersiz kütüphanesinde eşleşen video eğitimi bulunamadı.",
                    buttonText = "Özel Egzersiz Ekle",
                    onAction = { showAddCustomExerciseDialog = true }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredExercises) { exercise ->
                        ExerciseListItem(
                            exercise = exercise,
                            isFavorite = bookmarkedExerciseIds.contains(exercise.id),
                            isDownloaded = downloadedExerciseIds.containsKey(exercise.id),
                            onDetailClick = { selectedExerciseForDetail = exercise }
                        )
                    }
                }
            }
        }
    }

    // CREATE PROGRAM DIALOG/SHEET
    if (showCreateProgramWizard) {
        CreateProgramWizardDialog(
            exercisesList = exercises,
            onDismiss = { showCreateProgramWizard = false },
            onSave = { name, goal, days, selectedExs ->
                viewModel.createProgram(name, goal, days, selectedExs)
                showCreateProgramWizard = false
            }
        )
    }

    // DETAIL VIEW FOR EXERCISE DRAWER
    selectedExerciseForDetail?.let { exercise ->
        ExerciseVideoDetailView(
            exercise = exercise,
            viewModel = viewModel,
            onDismiss = { selectedExerciseForDetail = null }
        )
    }

    // ADD CUSTOM EXERCISE DIALOG
    if (showAddCustomExerciseDialog) {
        AddCustomExerciseDialog(
            onDismiss = { showAddCustomExerciseDialog = false },
            onSave = { name, muscle, desc, difficulty, how, tips, mistakes ->
                // Save custom exercise in the database programmatically
                val custom = Exercise(
                    id = name.lowercase().replace(" ", "_"),
                    name = name,
                    muscleGroup = muscle,
                    description = desc,
                    difficulty = difficulty,
                    howTo = how,
                    tips = tips,
                    commonMistakes = mistakes
                )
                // Accessing background repository insertion
                scope.launch {
                    val db = com.example.data.db.FitTrackDatabase.getDatabase(context)
                    db.dao.insertExercises(listOf(custom))
                    showAddCustomExerciseDialog = false
                }
            }
        )
    }
}

@Composable
fun EmptyStateView(
    iconVector: androidx.compose.ui.graphics.vector.ImageVector = Icons.Filled.FitnessCenter,
    text: String,
    buttonText: String? = null,
    onAction: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            iconVector,
            contentDescription = null,
            tint = MutedText.copy(alpha = 0.5f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = text,
            color = MutedText,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        if (buttonText != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.Black)
            ) {
                Text(buttonText, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ProgramCard(
    program: WorkoutProgram,
    exercisesCount: Int,
    onStart: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("program_card_${program.id}"),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = program.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = LightText
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text(program.goal, fontSize = 11.sp, color = OrangePrimary) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = OrangePrimary.copy(alpha = 0.1f)
                            ),
                            border = BorderStroke(0.dp, Color.Transparent)
                        )
                        Text(
                            text = "Haftada ${program.daysPerWeek} Gün",
                            color = MutedText,
                            fontSize = 12.sp
                        )
                    }
                }

                Row {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = "Sil", tint = AccentError.copy(alpha = 0.7f))
                    }
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = null,
                            tint = MutedText
                        )
                    }
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Divider(color = SurfaceDark)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Egzersizler ($exercisesCount adet):",
                        style = MaterialTheme.typography.labelMedium.copy(color = OrangeAccent, fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    // List formatted exercises
                    val split = program.exercisesString.split(",").filter { it.isNotEmpty() }
                    split.forEachIndexed { index, rawId ->
                        val prettyName = rawId.replace("_", " ").replaceFirstChar { it.uppercase() }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${index + 1}. $prettyName", color = LightText, fontSize = 13.sp)
                            Text("3 S × 10 T", color = MutedText, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("program_start_btn_${program.id}"),
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.Black),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Filled.FitnessCenter, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Antrenmana Başla", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun ExerciseListItem(
    exercise: Exercise,
    isFavorite: Boolean = false,
    isDownloaded: Boolean = false,
    onDetailClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onDetailClick() }
            .testTag("exercise_item_${exercise.id}"),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Premium play circle overlay
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(OrangePrimary.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = "Videoyu İzle",
                        tint = OrangePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = exercise.name,
                            fontWeight = FontWeight.Bold,
                            color = LightText,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isFavorite) {
                            Icon(
                                Icons.Filled.Bookmark,
                                contentDescription = "Kaydedildi",
                                tint = OrangePrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        if (isDownloaded) {
                            Icon(
                                Icons.Filled.CloudDone,
                                contentDescription = "Çevrimdışı Aktif",
                                tint = AccentSuccess,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Text(
                        text = "${exercise.muscleGroup} • Zorluk: ${exercise.difficulty} • 1080p HD",
                        color = MutedText,
                        fontSize = 12.sp
                    )
                }
            }

            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MutedText)
        }
    }
}

@Composable
fun CreateProgramWizardDialog(
    exercisesList: List<Exercise>,
    onDismiss: () -> Unit,
    onSave: (String, String, Int, List<String>) -> Unit
) {
    var step by remember { mutableStateOf(1) }
    var programName by remember { mutableStateOf("") }
    var selectedGoal by remember { mutableStateOf("Kas kazanımı") }
    var daysCount by remember { mutableStateOf("3") }
    val selectedExerciseIds = remember { mutableStateListOf<String>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Yeni Program Sihirbazı (Adım $step/4)",
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
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (step) {
                    1 -> {
                        Text("Adım 1: Program Adı", color = LightText, fontWeight = FontWeight.SemiBold)
                        TextField(
                            value = programName,
                            onValueChange = { programName = it },
                            placeholder = { Text("Örn: Hypertrophy Split", color = MutedText) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = SurfaceDark,
                                unfocusedContainerColor = SurfaceDark,
                                focusedTextColor = LightText,
                                unfocusedTextColor = LightText
                            )
                        )
                    }
                    2 -> {
                        Text("Adım 2: Hedef Seçimi", color = LightText, fontWeight = FontWeight.SemiBold)
                        val targets = listOf("Kas kazanımı", "Güç artışı", "Yağ yakımı", "Genel fitness")
                        targets.forEach { target ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedGoal = target }
                                    .background(
                                        if (selectedGoal == target) OrangePrimary.copy(alpha = 0.15f) else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedGoal == target,
                                    onClick = { selectedGoal = target },
                                    colors = RadioButtonDefaults.colors(selectedColor = OrangePrimary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(target, color = LightText)
                            }
                        }
                    }
                    3 -> {
                        Text("Adım 3: Haftalık Gün Sayısı", color = LightText, fontWeight = FontWeight.SemiBold)
                        TextField(
                            value = daysCount,
                            onValueChange = { daysCount = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = SurfaceDark,
                                unfocusedContainerColor = SurfaceDark,
                                focusedTextColor = LightText,
                                unfocusedTextColor = LightText
                            )
                        )
                    }
                    4 -> {
                        Text("Adım 4: Egzersiz Seçimi", color = LightText, fontWeight = FontWeight.SemiBold)
                        Text("Seçilen: ${selectedExerciseIds.size} egzersiz", color = OrangeAccent, fontSize = 12.sp)
                        Box(modifier = Modifier.height(200.dp)) {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(exercisesList) { ex ->
                                    val isChecked = selectedExerciseIds.contains(ex.id)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                if (isChecked) selectedExerciseIds.remove(ex.id)
                                                else selectedExerciseIds.add(ex.id)
                                            }
                                            .background(SurfaceDark, RoundedCornerShape(8.dp))
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(ex.name, color = LightText, fontSize = 13.sp)
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = {
                                                if (isChecked) selectedExerciseIds.remove(ex.id)
                                                else selectedExerciseIds.add(ex.id)
                                            },
                                            colors = CheckboxDefaults.colors(checkedColor = OrangePrimary)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = {
                if (step > 1) step-- else onDismiss()
            }) {
                Text(if (step > 1) "Geri" else "Kapat", color = MutedText)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (step < 4) {
                        step++
                    } else {
                        if (programName.isNotEmpty() && selectedExerciseIds.isNotEmpty()) {
                            onSave(programName, selectedGoal, daysCount.toIntOrNull() ?: 3, selectedExerciseIds.toList())
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.Black)
            ) {
                Text(if (step < 4) "İleri" else "Kaydet", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun ExerciseDetailBottomSheet(
    exercise: Exercise,
    onDismiss: () -> Unit
) {
    // Beautiful detail sheet using dialog style representing descriptions
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(exercise.name, color = LightText, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(
                    text = "${exercise.muscleGroup} • Zorluk: ${exercise.difficulty}",
                    color = OrangePrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        containerColor = CardBg,
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 350.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text(
                        text = "Açıklama",
                        style = MaterialTheme.typography.labelLarge.copy(color = OrangeAccent, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(exercise.description, color = LightText, fontSize = 14.sp)
                }

                item {
                    Text(
                        text = "Nasıl Yapılır?",
                        style = MaterialTheme.typography.labelLarge.copy(color = OrangeAccent, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(exercise.howTo, color = LightText, fontSize = 14.sp)
                }

                item {
                    Text(
                        text = "💡 Profesyonel İpuçları",
                        style = MaterialTheme.typography.labelLarge.copy(color = AccentSuccess, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(exercise.tips, color = LightText, fontSize = 14.sp)
                }

                item {
                    Text(
                        text = "⚠️ Yaygın Hatalar",
                        style = MaterialTheme.typography.labelLarge.copy(color = AccentError, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(exercise.commonMistakes, color = LightText, fontSize = 14.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.Black)
            ) {
                Text("Anladım", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun AddCustomExerciseDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, muscle: String, desc: String, difficulty: String, how: String, tips: String, mistakes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var muscle by remember { mutableStateOf("Göğüs") }
    val muscles = listOf("Göğüs", "Sırt", "Omuz", "Kol", "Bacak", "Karın", "Kardiyo")
    var desc by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf("Orta") }
    val difficulties = listOf("Kolay", "Orta", "Zor")
    var howTo by remember { mutableStateOf("") }
    var tips by remember { mutableStateOf("") }
    var mistakes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Egzersiz Ekle", color = OrangePrimary, fontWeight = FontWeight.Bold) },
        containerColor = CardBg,
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Egzersiz Adı") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(focusedContainerColor = SurfaceDark, unfocusedContainerColor = SurfaceDark, focusedTextColor = LightText)
                    )
                }
                item {
                    Text("Kas Grubu:", color = LightText, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        muscles.take(4).forEach { m ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (muscle == m) OrangePrimary else SurfaceDark)
                                    .clickable { muscle = m }
                                    .padding(vertical = 4.dp, horizontal = 8.dp)
                            ) {
                                Text(m, fontSize = 11.sp, color = if (muscle == m) Color.Black else LightText)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        muscles.drop(4).forEach { m ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (muscle == m) OrangePrimary else SurfaceDark)
                                    .clickable { muscle = m }
                                    .padding(vertical = 4.dp, horizontal = 8.dp)
                            ) {
                                Text(m, fontSize = 11.sp, color = if (muscle == m) Color.Black else LightText)
                            }
                        }
                    }
                }
                item {
                    Text("Zorluk Seviyesi:", color = LightText, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        difficulties.forEach { d ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (difficulty == d) OrangePrimary else SurfaceDark)
                                    .clickable { difficulty = d }
                                    .padding(vertical = 4.dp, horizontal = 12.dp)
                            ) {
                                Text(d, fontSize = 11.sp, color = if (difficulty == d) Color.Black else LightText)
                            }
                        }
                    }
                }
                item {
                    TextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Kısa Açıklama") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(focusedContainerColor = SurfaceDark, unfocusedContainerColor = SurfaceDark, focusedTextColor = LightText)
                    )
                }
                item {
                    TextField(
                        value = howTo,
                        onValueChange = { howTo = it },
                        label = { Text("Nasıl Yapılır?") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(focusedContainerColor = SurfaceDark, unfocusedContainerColor = SurfaceDark, focusedTextColor = LightText)
                    )
                }
                item {
                    TextField(
                        value = tips,
                        onValueChange = { tips = it },
                        label = { Text("İpuçları") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(focusedContainerColor = SurfaceDark, unfocusedContainerColor = SurfaceDark, focusedTextColor = LightText)
                    )
                }
                item {
                    TextField(
                        value = mistakes,
                        onValueChange = { mistakes = it },
                        label = { Text("Yaygın Hatalar") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(focusedContainerColor = SurfaceDark, unfocusedContainerColor = SurfaceDark, focusedTextColor = LightText)
                    )
                }
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
                    if (name.isNotEmpty()) {
                        onSave(name, muscle, desc, difficulty, howTo, tips, mistakes)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.Black)
            ) {
                Text("Kaydet", fontWeight = FontWeight.Bold)
            }
        }
    )
}
