package com.example.fitnessdiary.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val date: Long = System.currentTimeMillis()

) {
    override fun toString(): String {
        // Вариант "Все" или "Без связи"
        if (id <= -1L) {
            return name
        }

        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        return "$name (${dateFormat.format(Date(date))})"
    }
}