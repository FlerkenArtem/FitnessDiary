package com.example.fitnessdiary

import android.app.Application
import androidx.room.Room
import com.example.fitnessdiary.data.AppDatabase
import com.example.fitnessdiary.data.ExerciseDao

class FitnessDiaryApplication : Application() {
    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "fitness_diary.db"
        ).build()
    }

    val exerciseDao: ExerciseDao by lazy { database.exerciseDao() }
}