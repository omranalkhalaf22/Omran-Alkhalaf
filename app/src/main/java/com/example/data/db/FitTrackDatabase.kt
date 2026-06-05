package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.FitTrackDao
import com.example.data.model.*

@Database(
    entities = [
        WorkoutProgram::class,
        Exercise::class,
        WorkoutSessionLog::class,
        WeightLog::class,
        ProgressPhoto::class,
        PersonalRecord::class,
        UserProfile::class
    ],
    version = 2,
    exportSchema = false
)
abstract class FitTrackDatabase : RoomDatabase() {
    abstract val dao: FitTrackDao

    companion object {
        @Volatile
        private var INSTANCE: FitTrackDatabase? = null

        fun getDatabase(context: Context): FitTrackDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FitTrackDatabase::class.java,
                    "fittrack_pro_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
