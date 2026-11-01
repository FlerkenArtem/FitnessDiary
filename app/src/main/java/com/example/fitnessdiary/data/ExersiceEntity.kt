package com.example.fitnessdiary.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.fitnessdiary.Exercise
import kotlin.time.Duration.Companion.seconds

@Entity(tableName = "exercises")
data class ExerciseEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val approaches: Int? = null,
    val repetitions: Int? = null,
    val weight: Double? = null,
    val timeSeconds: Long? = null
) {
    fun toExercise(): Exercise = Exercise(
        id = id,
        name = name,
        approaches = approaches,
        repetitions = repetitions,
        weight = weight,
        time = timeSeconds?.seconds
    )
}