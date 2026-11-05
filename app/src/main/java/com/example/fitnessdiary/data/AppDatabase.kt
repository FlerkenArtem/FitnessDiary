package com.example.fitnessdiary.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        ExerciseEntity::class,
        WorkoutEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `workouts` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `date` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    "ALTER TABLE `exercises` ADD COLUMN `workoutId` INTEGER DEFAULT NULL"
                )

                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS `index_exercises_workoutId` 
                    ON `exercises` (`workoutId`)
                    """.trimIndent()
                )
            }
        }
    }
}