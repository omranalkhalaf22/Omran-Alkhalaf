package com.example.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.FitTrackDatabase
import com.example.data.model.*
import com.example.data.repository.FitTrackRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Internal state structure representing sets in progress
data class WorkoutSet(
    val setIndex: Int,
    var weight: Double,
    var reps: Int,
    var isCompleted: Boolean = false,
    var note: String = ""
)

data class MealLog(
    val id: Int,
    val type: String, // Kahvaltı, Öğle Yemeği, Akşam Yemeği, Atıştırmalık
    val name: String,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int,
    val timestamp: Long = System.currentTimeMillis()
)

data class FitnessGoal(
    val id: String,
    val title: String,
    val target: String,
    val progress: Float, // 0.0 to 1.0
    val isCompleted: Boolean,
    val category: String // Kardiyo, Güç, Diyet, Su, Alışkanlık
)

data class SocialPost(
    val id: Int,
    val author: String,
    val authorAvatar: String, // Yusuf, Caner, Selin, Hilal
    val activitySummary: String,
    val timestampText: String,
    val likes: Int,
    val isLiked: Boolean = false,
    val commentsCount: Int = 0
)

data class LeaderboardUser(
    val rank: Int,
    val name: String,
    val score: Int, // XP or workouts done
    val avatar: String,
    val isCurrentUser: Boolean = false
)

class FitTrackViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FitTrackRepository
    
    // UI state streams observed directly by Jetpack Compose
    val allPrograms: StateFlow<List<WorkoutProgram>>
    val allExercises: StateFlow<List<Exercise>>
    val allWorkoutLogs: StateFlow<List<WorkoutSessionLog>>
    val allWeightLogs: StateFlow<List<WeightLog>>
    val allPhotos: StateFlow<List<ProgressPhoto>>
    val allRecords: StateFlow<List<PersonalRecord>>
    val userProfile: StateFlow<UserProfile>

    // --- Active Workout State ---
    private val _activeProgram = MutableStateFlow<WorkoutProgram?>(null)
    val activeProgram: StateFlow<WorkoutProgram?> = _activeProgram.asStateFlow()

    private val _activeExercisesList = MutableStateFlow<List<Exercise>>(emptyList())
    val activeExercisesList: StateFlow<List<Exercise>> = _activeExercisesList.asStateFlow()

    private val _workoutDurationSeconds = MutableStateFlow(0)
    val workoutDurationSeconds: StateFlow<Int> = _workoutDurationSeconds.asStateFlow()

    private val _completedExercisesCount = MutableStateFlow(0)
    val completedExercisesCount: StateFlow<Int> = _completedExercisesCount.asStateFlow()

    // Sets progress per exerciseId
    val currentSessionSets = mutableStateMapOf<String, List<WorkoutSet>>()

    private var workoutTimerJob: Job? = null

    // --- Rest Timer State ---
    private val _restTimeRemaining = MutableStateFlow(0)
    val restTimeRemaining: StateFlow<Int> = _restTimeRemaining.asStateFlow()

    private val _restTimeMax = MutableStateFlow(90)
    val restTimeMax: StateFlow<Int> = _restTimeMax.asStateFlow()

    private val _restTimerIsActive = MutableStateFlow(false)
    val restTimerIsActive: StateFlow<Boolean> = _restTimerIsActive.asStateFlow()

    private val _restTimerMuted = MutableStateFlow(false)
    val restTimerMuted: StateFlow<Boolean> = _restTimerMuted.asStateFlow()

    private val _showRestBanner = MutableStateFlow(false)
    val showRestBanner: StateFlow<Boolean> = _showRestBanner.asStateFlow()

    private val _restTimeFinishedAlert = MutableStateFlow(false)
    val restTimeFinishedAlert: StateFlow<Boolean> = _restTimeFinishedAlert.asStateFlow()

    private var restTimerJob: Job? = null

    // --- PR Animation state ---
    private val _recentPrBroken = MutableStateFlow<String?>(null) // Name of exercise with PR
    val recentPrBroken: StateFlow<String?> = _recentPrBroken.asStateFlow()

    // --- Extra Ecosystem State ---
    private val _xp = MutableStateFlow(2400)
    val xp: StateFlow<Int> = _xp.asStateFlow()

    private val _waterIntake = MutableStateFlow(1250)
    val waterIntake: StateFlow<Int> = _waterIntake.asStateFlow()

    private val _waterTarget = MutableStateFlow(2500)
    val waterTarget: StateFlow<Int> = _waterTarget.asStateFlow()

    private val _stepCount = MutableStateFlow(6542)
    val stepCount: StateFlow<Int> = _stepCount.asStateFlow()

    private val _stepTarget = MutableStateFlow(10000)
    val stepTarget: StateFlow<Int> = _stepTarget.asStateFlow()

    private val _mealLogs = MutableStateFlow<List<MealLog>>(emptyList())
    val mealLogs: StateFlow<List<MealLog>> = _mealLogs.asStateFlow()

    val calorieTarget = 2600
    val proteinTarget = 150
    val carbsTarget = 300
    val fatTarget = 80

    private val _allGoals = MutableStateFlow<List<FitnessGoal>>(emptyList())
    val allGoals: StateFlow<List<FitnessGoal>> = _allGoals.asStateFlow()

    private val _socialPosts = MutableStateFlow<List<SocialPost>>(emptyList())
    val socialPosts: StateFlow<List<SocialPost>> = _socialPosts.asStateFlow()

    private val _leaderboardUsers = MutableStateFlow<List<LeaderboardUser>>(emptyList())
    val leaderboardUsers: StateFlow<List<LeaderboardUser>> = _leaderboardUsers.asStateFlow()

    // --- Muscle Group Recovery Visualizer ---
    private val _chestRecovery = MutableStateFlow(85)
    val chestRecovery = _chestRecovery.asStateFlow()

    private val _backRecovery = MutableStateFlow(62)
    val backRecovery = _backRecovery.asStateFlow()

    private val _shoulderRecovery = MutableStateFlow(95)
    val shoulderRecovery = _shoulderRecovery.asStateFlow()

    private val _legsRecovery = MutableStateFlow(40)
    val legsRecovery = _legsRecovery.asStateFlow()

    private val _coreRecovery = MutableStateFlow(75)
    val coreRecovery = _coreRecovery.asStateFlow()

    // --- Level Up Flag ---
    private val _recentlyLeveledUp = MutableStateFlow<Int?>(null)
    val recentlyLeveledUp = _recentlyLeveledUp.asStateFlow()

    // --- Video Learning System additions ---
    private val _bookmarkedExerciseIds = MutableStateFlow<Set<String>>(setOf("bench_press", "squat"))
    val bookmarkedExerciseIds: StateFlow<Set<String>> = _bookmarkedExerciseIds.asStateFlow()

    private val _customPlaylists = MutableStateFlow<Map<String, List<String>>>(
        mapOf(
            "Favori Kardiyolarım" to listOf("treadmill_run"),
            "Üst Vücut Bombası" to listOf("bench_press", "lat_pulldown")
        )
    )
    val customPlaylists: StateFlow<Map<String, List<String>>> = _customPlaylists.asStateFlow()

    private val _downloadedExerciseIds = MutableStateFlow<Map<String, String>>(emptyMap()) // exerciseId -> "1080p" etc.
    val downloadedExerciseIds: StateFlow<Map<String, String>> = _downloadedExerciseIds.asStateFlow()

    private val _beginnerModeEnabled = MutableStateFlow(true)
    val beginnerModeEnabled: StateFlow<Boolean> = _beginnerModeEnabled.asStateFlow()

    private val _advancedModeEnabled = MutableStateFlow(false)
    val advancedModeEnabled: StateFlow<Boolean> = _advancedModeEnabled.asStateFlow()

    fun toggleBookmark(exerciseId: String) {
        val currentSet = _bookmarkedExerciseIds.value
        _bookmarkedExerciseIds.value = if (currentSet.contains(exerciseId)) {
            currentSet - exerciseId
        } else {
            currentSet + exerciseId
        }
        addXp(15)
    }

    fun createPlaylist(name: String) {
        if (name.isBlank()) return
        val current = _customPlaylists.value.toMutableMap()
        if (!current.containsKey(name)) {
            current[name] = emptyList()
            _customPlaylists.value = current
            addXp(30)
        }
    }

    fun addExerciseToPlaylist(playlistName: String, exerciseId: String) {
        val current = _customPlaylists.value.toMutableMap()
        val list = current[playlistName] ?: emptyList()
        if (!list.contains(exerciseId)) {
            current[playlistName] = list + exerciseId
            _customPlaylists.value = current
            addXp(20)
        }
    }

    fun removeExerciseFromPlaylist(playlistName: String, exerciseId: String) {
        val current = _customPlaylists.value.toMutableMap()
        val list = current[playlistName] ?: emptyList()
        if (list.contains(exerciseId)) {
            current[playlistName] = list - exerciseId
            _customPlaylists.value = current
        }
    }

    fun deletePlaylist(playlistName: String) {
        val current = _customPlaylists.value.toMutableMap()
        current.remove(playlistName)
        _customPlaylists.value = current
    }

    fun saveVideoOffline(exerciseId: String, quality: String) {
        val current = _downloadedExerciseIds.value.toMutableMap()
        current[exerciseId] = quality
        _downloadedExerciseIds.value = current
        addXp(40)
    }

    fun deleteOfflineVideo(exerciseId: String) {
        val current = _downloadedExerciseIds.value.toMutableMap()
        current.remove(exerciseId)
        _downloadedExerciseIds.value = current
    }

    fun setBeginnerMode(enabled: Boolean) {
        _beginnerModeEnabled.value = enabled
        if (enabled) _advancedModeEnabled.value = false
    }

    fun setAdvancedMode(enabled: Boolean) {
        _advancedModeEnabled.value = enabled
        if (enabled) _beginnerModeEnabled.value = false
    }

    init {
        val database = FitTrackDatabase.getDatabase(application)
        repository = FitTrackRepository(database.dao)

        // Prepopulate db
        viewModelScope.launch {
            repository.seedExercisesIfEmpty()
        }

        // Connect flows
        allPrograms = repository.allPrograms
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        allExercises = repository.allExercises
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        allWorkoutLogs = repository.allWorkoutLogs
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        allWeightLogs = repository.allWeightLogs
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        allPhotos = repository.allPhotos
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        allRecords = repository.allRecords
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        userProfile = repository.userProfile
            .map { it ?: UserProfile() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())
    }

    // --- Profile Settings ---
    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.saveProfile(profile)
        }
    }

    fun setWaterTarget(target: Int) {
        _waterTarget.value = target
    }

    fun setStepTarget(target: Int) {
        _stepTarget.value = target
    }

    // --- Streak & Badges computation based on Log history ---
    val currentStreak: StateFlow<Int> = allWorkoutLogs.map { logs ->
        if (logs.isEmpty()) return@map 1 // Default start at 1 for opening the app!
        // To be secure, we can check logs per daily dates and increment
        // Let's make an elegant calculation using the timestamps
        val daysWithWorkouts = logs.map {
            val cal = java.util.Calendar.getInstance()
            cal.timeInMillis = it.date
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
            cal.set(java.util.Calendar.MINUTE, 0)
            cal.set(java.util.Calendar.SECOND, 0)
            cal.set(java.util.Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }.distinct().sortedDescending()

        if (daysWithWorkouts.isEmpty()) return@map 1

        var streak = 1
        var today = java.util.Calendar.getInstance()
        today.set(java.util.Calendar.HOUR_OF_DAY, 0)
        today.set(java.util.Calendar.MINUTE, 0)
        today.set(java.util.Calendar.SECOND, 0)
        today.set(java.util.Calendar.MILLISECOND, 0)
        val todayMs = today.timeInMillis

        // If last workout was today or yesterday, streak is active
        val firstWorkoutMs = daysWithWorkouts.first()
        val differenceDays = (todayMs - firstWorkoutMs) / (24 * 60 * 60 * 1000L)
        
        if (differenceDays <= 1) {
            streak = 1
            for (i in 0 until daysWithWorkouts.size - 1) {
                val current = daysWithWorkouts[i]
                val next = daysWithWorkouts[i + 1]
                val diff = (current - next) / (24 * 60 * 60 * 1000L)
                if (diff == 1L) {
                    streak++
                } else if (diff > 1L) {
                    break
                }
            }
        } else {
            // Streak broken but user opened app today, let's keep it at 1
            streak = 1
        }
        streak
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    // --- Program Management ---
    fun createProgram(name: String, goal: String, days: Int, exercises: List<String>) {
        viewModelScope.launch {
            val program = WorkoutProgram(
                name = name,
                goal = goal,
                daysPerWeek = days,
                exercisesString = exercises.joinToString(",")
            )
            repository.insertProgram(program)
        }
    }

    fun deleteProgram(id: Int) {
        viewModelScope.launch {
            repository.deleteProgramById(id)
        }
    }

    // --- Workout Session Loggers ---
    fun startWorkoutSession(program: WorkoutProgram) {
        _activeProgram.value = program
        _workoutDurationSeconds.value = 0
        _completedExercisesCount.value = 0
        currentSessionSets.clear()

        // Extract and load exercise objects
        viewModelScope.launch {
            val library = allExercises.first()
            val progExercises = program.exercisesString.split(",")
                .mapNotNull { id -> library.find { it.id == id.trim() } }
            _activeExercisesList.value = progExercises

            // Initialize default training sets
            progExercises.forEach { exercise ->
                currentSessionSets[exercise.id] = listOf(
                    WorkoutSet(1, 40.0, 10),
                    WorkoutSet(2, 40.0, 10),
                    WorkoutSet(3, 40.0, 10)
                )
            }
        }

        // Start duration timer
        workoutTimerJob?.cancel()
        workoutTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                _workoutDurationSeconds.value += 1
            }
        }
    }

    fun updateSet(exerciseId: String, setIndex: Int, weight: Double, reps: Int, note: String) {
        val currentSets = currentSessionSets[exerciseId] ?: return
        val updated = currentSets.map {
            if (it.setIndex == setIndex) {
                it.copy(weight = weight, reps = reps, note = note)
            } else {
                it
            }
        }
        currentSessionSets[exerciseId] = updated
    }

    fun toggleSetCompleted(exerciseId: String, setIndex: Int) {
        val currentSets = currentSessionSets[exerciseId] ?: return
        val updated = currentSets.map {
            if (it.setIndex == setIndex) {
                val state = !it.isCompleted
                if (state) {
                    // Set was completed, auto-trigger Rest Timer!
                    startRestTimer()
                }
                it.copy(isCompleted = state)
            } else {
                it
            }
        }
        currentSessionSets[exerciseId] = updated

        // Recalculate completed exercises
        val doneCount = currentSessionSets.values.count { setList ->
            setList.isNotEmpty() && setList.all { it.isCompleted }
        }
        _completedExercisesCount.value = doneCount
    }

    fun addSetRow(exerciseId: String) {
        val currentSets = currentSessionSets[exerciseId] ?: emptyList()
        val nextIdx = currentSets.size + 1
        val lastWeight = currentSets.lastOrNull()?.weight ?: 40.0
        val lastReps = currentSets.lastOrNull()?.reps ?: 10
        currentSessionSets[exerciseId] = currentSets + WorkoutSet(nextIdx, lastWeight, lastReps)
    }

    fun finishWorkoutSession() {
        val prog = _activeProgram.value ?: return
        val duration = _workoutDurationSeconds.value
        val exCount = _activeExercisesList.value.size
        
        var totalSets = 0
        var totalWeight = 0.0

        // Calculate session statistics
        currentSessionSets.forEach { (exerciseId, sets) ->
            sets.forEach { set ->
                if (set.isCompleted) {
                    totalSets++
                    totalWeight += set.weight * set.reps

                    // Check and Update Personal Records (PRs)
                    checkAndTriggerPR(exerciseId, set.weight)
                }
            }
        }

        viewModelScope.launch {
            val sessionLog = WorkoutSessionLog(
                programName = prog.name,
                durationSeconds = duration,
                completedExercisesCount = exCount,
                totalSets = totalSets,
                totalWeight = totalWeight,
                notes = "Harika antrenman! Toplam kaldırılan: $totalWeight kg"
            )
            repository.insertWorkoutLog(sessionLog)

            // Auto share workout to social activity feed!
            shareWorkoutToFeed(prog.name)

            // Add completion XP!
            addXp(150 + (totalSets * 10))

            // Decrease muscle recovery based on program target
            val lowerName = prog.name.lowercase()
            if (lowerName.contains("body") || lowerName.contains("tüm")) {
                _chestRecovery.value = (_chestRecovery.value - 30).coerceAtLeast(10)
                _backRecovery.value = (_backRecovery.value - 25).coerceAtLeast(10)
                _legsRecovery.value = (_legsRecovery.value - 35).coerceAtLeast(10)
                _shoulderRecovery.value = (_shoulderRecovery.value - 20).coerceAtLeast(10)
                _coreRecovery.value = (_coreRecovery.value - 15).coerceAtLeast(10)
            } else if (lowerName.contains("push") || lowerName.contains("göğüs")) {
                _chestRecovery.value = (_chestRecovery.value - 45).coerceAtLeast(10)
                _shoulderRecovery.value = (_shoulderRecovery.value - 35).coerceAtLeast(10)
            } else if (lowerName.contains("pull") || lowerName.contains("sırt")) {
                _backRecovery.value = (_backRecovery.value - 40).coerceAtLeast(10)
            } else if (lowerName.contains("leg") || lowerName.contains("bacak")) {
                _legsRecovery.value = (_legsRecovery.value - 55).coerceAtLeast(10)
            }

            // Update workout goal progress
            _allGoals.value = _allGoals.value.map { goal ->
                if (goal.id == "goal_workout") {
                    val currentProgress = goal.progress + 0.33f
                    goal.copy(
                        progress = currentProgress.coerceAtMost(1.0f),
                        isCompleted = currentProgress >= 1.0f
                    )
                } else {
                    goal
                }
            }

            // Terminate active view
            cancelWorkout()
        }
    }

    private fun checkAndTriggerPR(exerciseId: String, weight: Double) {
        viewModelScope.launch {
            val lib = allExercises.first()
            val exercise = lib.find { it.id == exerciseId } ?: return@launch
            val prName = exercise.name

            // Check if weight is larger than existing PR of specific exercises (Bench Press, Squat, Deadlift, Overhead Press)
            val trackedPrs = listOf("Bench Press", "Squat", "Deadlift", "Overhead Press")
            if (trackedPrs.contains(prName)) {
                val currentMax = repository.getBestRecordByExercise(prName)
                if (currentMax == null || weight > currentMax.weight) {
                    // Record broken!
                    repository.insertRecord(PersonalRecord(exerciseName = prName, weight = weight))
                    triggerPrCelebrationAlert(prName)
                }
            }
        }
    }

    fun forceRecordManualPr(exerciseName: String, weight: Double) {
        viewModelScope.launch {
            repository.insertRecord(PersonalRecord(exerciseName = exerciseName, weight = weight))
            triggerPrCelebrationAlert(exerciseName)
        }
    }

    private fun triggerPrCelebrationAlert(name: String) {
        _recentPrBroken.value = name
        viewModelScope.launch {
            delay(5000L) // Show animation/alert for 5 seconds
            _recentPrBroken.value = null
        }
    }

    fun clearPrAlert() {
        _recentPrBroken.value = null
    }

    fun cancelWorkout() {
        workoutTimerJob?.cancel()
        restTimerJob?.cancel()
        _activeProgram.value = null
        _activeExercisesList.value = emptyList()
        _workoutDurationSeconds.value = 0
        _completedExercisesCount.value = 0
        currentSessionSets.clear()
        _showRestBanner.value = false
        _restTimerIsActive.value = false
    }

    // --- Rest Timer ---
    fun setRestTimerDuration(seconds: Int) {
        _restTimeMax.value = seconds
        if (_restTimerIsActive.value) {
            startRestTimer(seconds)
        }
    }

    fun startRestTimer(seconds: Int = _restTimeMax.value) {
        _showRestBanner.value = true
        _restTimeRemaining.value = seconds
        _restTimerIsActive.value = true
        _restTimeFinishedAlert.value = false

        restTimerJob?.cancel()
        restTimerJob = viewModelScope.launch {
            while (_restTimeRemaining.value > 0) {
                delay(1000L)
                _restTimeRemaining.value -= 1
            }
            _restTimerIsActive.value = false
            _restTimeFinishedAlert.value = true
            
            // Auto hide timer banner after completion notice
            delay(8000L)
            _restTimeFinishedAlert.value = false
            _showRestBanner.value = false
        }
    }

    fun pauseRestTimer() {
        _restTimerIsActive.value = false
        restTimerJob?.cancel()
    }

    fun resumeRestTimer() {
        if (_restTimeRemaining.value > 0) {
            _restTimerIsActive.value = true
            restTimerJob?.cancel()
            restTimerJob = viewModelScope.launch {
                while (_restTimeRemaining.value > 0) {
                    delay(1000L)
                    _restTimeRemaining.value -= 1
                }
                _restTimerIsActive.value = false
                _restTimeFinishedAlert.value = true
                delay(8000L)
                _restTimeFinishedAlert.value = false
                _showRestBanner.value = false
            }
        }
    }

    fun toggleRestMuted() {
        _restTimerMuted.value = !_restTimerMuted.value
    }

    fun closeRestTimerBanner() {
        _showRestBanner.value = false
        _restTimeFinishedAlert.value = false
        _restTimerIsActive.value = false
        restTimerJob?.cancel()
    }

    // --- Weight Logging ---
    fun addWeightLog(weight: Double, notes: String, date: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            repository.insertWeightLog(WeightLog(weight = weight, timestamp = date, notes = notes))
            
            // Also update the current weight parameter in profile!
            val profile = userProfile.first()
            repository.saveProfile(profile.copy(weight = weight))
        }
    }

    fun removeWeightLog(id: Int) {
        viewModelScope.launch {
            repository.deleteWeightLogById(id)
        }
    }

    // --- Progress Photo Management ---
    fun addProgressPhoto(position: String, notes: String, mockPhotoType: String = "vector_muscle_avatar") {
        viewModelScope.launch {
            // We use standard placeholder descriptors representing front, side, back avatars for clean styling in UI!
            val photoRecord = ProgressPhoto(
                position = position,
                photoData = mockPhotoType,
                notes = notes,
                timestamp = System.currentTimeMillis()
            )
            repository.insertPhoto(photoRecord)
        }
    }

    fun deleteProgressPhoto(id: Int) {
        viewModelScope.launch {
            repository.deletePhotoById(id)
        }
    }

    // --- Ecosystem Helper Functions ---

    fun addXp(amount: Int) {
        val oldLevel = (_xp.value / 500) + 1
        _xp.value += amount
        val newLevel = (_xp.value / 500) + 1
        if (newLevel > oldLevel) {
            _recentlyLeveledUp.value = newLevel
            viewModelScope.launch {
                delay(4000L) // Show level up banner for 4s
                _recentlyLeveledUp.value = null
            }
        }
        updateLeaderboard()
    }

    fun clearLevelUpAlert() {
        _recentlyLeveledUp.value = null
    }

    fun addWater(amountMl: Int) {
        _waterIntake.value = (_waterIntake.value + amountMl).coerceAtLeast(0)
        
        // Update water goal progress
        val currentGoals = _allGoals.value
        _allGoals.value = currentGoals.map { goal ->
            if (goal.id == "goal_water") {
                val newProgress = (_waterIntake.value.toFloat() / _waterTarget.value.toFloat()).coerceAtMost(1.0f)
                goal.copy(
                    progress = newProgress,
                    isCompleted = newProgress >= 1.0f
                )
            } else {
                goal
            }
        }

        // Add 15 XP for hydration
        addXp(15)
    }

    fun resetWater() {
        _waterIntake.value = 0
        // Update water goal progress
        val currentGoals = _allGoals.value
        _allGoals.value = currentGoals.map { goal ->
            if (goal.id == "goal_water") {
                goal.copy(progress = 0f, isCompleted = false)
            } else {
                goal
            }
        }
    }

    fun addSteps(amount: Int) {
        _stepCount.value = (_stepCount.value + amount).coerceAtLeast(0)

        // Update step goal progress
        val currentGoals = _allGoals.value
        _allGoals.value = currentGoals.map { goal ->
            if (goal.id == "goal_steps") {
                val newProgress = (_stepCount.value.toFloat() / _stepTarget.value.toFloat()).coerceAtMost(1.0f)
                goal.copy(
                    progress = newProgress,
                    isCompleted = newProgress >= 1.0f
                )
            } else {
                goal
            }
        }

        // Add 5 XP per 1000 steps simulated
        addXp(amount / 50)
    }

    fun addMeal(type: String, name: String, calories: Int, protein: Int, carbs: Int, fat: Int) {
        val nextId = (_mealLogs.value.maxOfOrNull { it.id } ?: 0) + 1
        val newMeal = MealLog(nextId, type, name, calories, protein, carbs, fat)
        _mealLogs.value = _mealLogs.value + newMeal

        // Add 25 XP for recording diet
        addXp(25)
    }

    fun deleteMealLog(id: Int) {
        _mealLogs.value = _mealLogs.value.filter { it.id != id }
    }

    fun toggleGoalCompleted(goalId: String) {
        _allGoals.value = _allGoals.value.map { goal ->
            if (goal.id == goalId) {
                val nextState = !goal.isCompleted
                if (nextState) {
                    addXp(75) // Bonus XP for finishing a goal!
                }
                goal.copy(
                    isCompleted = nextState,
                    progress = if (nextState) 1.0f else 0.0f
                )
            } else {
                goal
            }
        }
    }

    fun addNewGoal(title: String, target: String, category: String) {
        val id = "goal_" + System.currentTimeMillis()
        val goal = FitnessGoal(id, title, target, 0.0f, false, category)
        _allGoals.value = _allGoals.value + goal
        addXp(20)
    }

    fun toggleLikePost(postId: Int) {
        _socialPosts.value = _socialPosts.value.map { post ->
            if (post.id == postId) {
                val state = !post.isLiked
                post.copy(
                    isLiked = state,
                    likes = if (state) post.likes + 1 else post.likes - 1
                )
            } else {
                post
            }
        }
    }

    fun shareWorkoutToFeed(workoutName: String) {
        val nextId = (_socialPosts.value.maxOfOrNull { it.id } ?: 0) + 1
        val newPost = SocialPost(
            id = nextId,
            author = "Yusuf Demir (Sen)",
            authorAvatar = "Yusuf",
            activitySummary = "Antrenmanını tamamladı: $workoutName! Toplam egzersiz hacmi artırıldı! 🏋️‍♂️🔥 Bu rüzgarı hisset!",
            timestampText = "Şimdi",
            likes = 0,
            isLiked = false,
            commentsCount = 0
        )
        _socialPosts.value = listOf(newPost) + _socialPosts.value
        addXp(50) // Sharing boost!
    }

    fun quickRestMuscle(muscleGroup: String) {
        when (muscleGroup) {
            "Göğüs" -> _chestRecovery.value = 100
            "Sırt" -> _backRecovery.value = 100
            "Omuz" -> _shoulderRecovery.value = 100
            "Bacak" -> _legsRecovery.value = 100
            "Karın" -> _coreRecovery.value = 100
        }
        addXp(10) // Recovery focus reward
    }

    fun updateLeaderboard() {
        _leaderboardUsers.value = listOf(
            LeaderboardUser(1, "Selin Kaya", 2950, "Selin"),
            LeaderboardUser(2, "Caner Öztürk", 2600, "Caner"),
            LeaderboardUser(3, "Yusuf Demir (Sen)", _xp.value, "Yusuf", isCurrentUser = true),
            LeaderboardUser(4, "Hilal Şahin", 2150, "Hilal"),
            LeaderboardUser(5, "Umut Güler", 1800, "Umut")
        ).sortedByDescending { it.score }.mapIndexed { index, user -> user.copy(rank = index + 1) }
    }
}
