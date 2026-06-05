package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FitTrackDao {

    // --- WORKOUT PROGRAMS ---
    @Query("SELECT * FROM workout_programs ORDER BY id DESC")
    fun getAllPrograms(): Flow<List<WorkoutProgram>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgram(program: WorkoutProgram): Long

    @Query("DELETE FROM workout_programs WHERE id = :id")
    suspend fun deleteProgramById(id: Int)


    // --- EXERCISE LIBRARY ---
    @Query("SELECT * FROM exercise_library")
    fun getAllExercises(): Flow<List<Exercise>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<Exercise>)


    // --- WORKOUT SESSION LOGS ---
    @Query("SELECT * FROM workout_session_logs ORDER BY date DESC")
    fun getAllWorkoutLogs(): Flow<List<WorkoutSessionLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutLog(log: WorkoutSessionLog): Long


    // --- WEIGHT LOGS ---
    @Query("SELECT * FROM weight_logs ORDER BY timestamp DESC")
    fun getAllWeightLogs(): Flow<List<WeightLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightLog(log: WeightLog): Long

    @Query("DELETE FROM weight_logs WHERE id = :id")
    suspend fun deleteWeightLogById(id: Int)


    // --- PROGRESS PHOTOS ---
    @Query("SELECT * FROM progress_photos ORDER BY timestamp DESC")
    fun getAllPhotos(): Flow<List<ProgressPhoto>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: ProgressPhoto): Long

    @Query("DELETE FROM progress_photos WHERE id = :id")
    suspend fun deletePhotoById(id: Int)


    // --- PERSONAL RECORDS ---
    @Query("SELECT * FROM personal_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<PersonalRecord>>

    @Query("SELECT * FROM personal_records WHERE exerciseName = :name ORDER BY weight DESC LIMIT 1")
    suspend fun getBestRecordByExercise(name: String): PersonalRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: PersonalRecord): Long


    // --- USER PROFILE ---
    @Query("SELECT * FROM user_profiles WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfile): Long
}
