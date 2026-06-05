package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_programs")
data class WorkoutProgram(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val goal: String, // Kas kazanımı, Güç artışı, Yağ yakımı, Genel fitness
    val daysPerWeek: Int,
    val exercisesString: String // Comma separated exercise IDs, e.g., "bench_press,squat"
)

@Entity(tableName = "exercise_library")
data class Exercise(
    @PrimaryKey val id: String,
    val name: String,
    val muscleGroup: String, // Göğüs, Sırt, Omuz, Kol, Bacak, Karın, Kardiyo
    val description: String,
    val difficulty: String, // Kolay, Orta, Zor
    val howTo: String,
    val tips: String,
    val commonMistakes: String
)

@Entity(tableName = "workout_session_logs")
data class WorkoutSessionLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val programName: String,
    val date: Long = System.currentTimeMillis(),
    val durationSeconds: Int,
    val completedExercisesCount: Int,
    val totalSets: Int,
    val totalWeight: Double,
    val notes: String = ""
)

@Entity(tableName = "weight_logs")
data class WeightLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val weight: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(tableName = "progress_photos")
data class ProgressPhoto(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val position: String, // Ön, Yan, Arka
    val photoData: String, // Base64 or local identifier representing progress photo
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(tableName = "personal_records")
data class PersonalRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val exerciseName: String, // Bench Press, Squat, Deadlift, Overhead Press
    val weight: Double,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "Sporcu",
    val height: Double = 175.0,
    val weight: Double = 75.0,
    val age: Int = 25,
    val goal: String = "Kas Kazanımı",
    val isKg: Boolean = true,
    val isDarkTheme: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val isOnboarded: Boolean = false
)
