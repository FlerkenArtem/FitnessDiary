package com.example.fitnessdiary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.fitnessdiary.ui.theme.FitnessDiaryTheme
import kotlin.time.Duration.Companion.minutes
import android.util.Log

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitnessDiaryTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
        runDemo()
    }

    fun runDemo() {
        val tag : String = "LAB1"

        val exercises = listOf<Exercise>(
            Exercise(1, "Жим штанги лежа", 4, 6, 75.0),
            Exercise(2, "Жим гантелей лежа", 4, 10, 22.0),
            Exercise(3, "Жим от груди", 4, 8, 35.0),
            Exercise(4, "Степпер", 30.minutes),
            Exercise(5, "Отжимания", 3, 20),
            Exercise(5, "Скручивания", 4, 15))

        // Вывод каждого упражнения из списка
        for (ex in exercises) {
            Log.d(tag, ex.toString(true))
        }

        // Вывод количества упражнений
        Log.d(tag, "Количество упражнений: " + exercises.size)

        // Вывод упражнений, в которых вес не пустой и больше 40 кг
        Log.d(tag, "Количество упражнений в которых вес не пустой и больше 40 кг: ")
        for (ex in exercises) {
            ex.weight?.let { weight ->
                if (weight > 40.0) {
                    Log.d(tag, ex.toString(true))
                }
            }
        }

        // Вывод упражнений, отсортированных по названию
        Log.d(tag, "Список упражнений, отсортированный по названию: ")
        val sortedByName = exercises.sortedBy { it.name }
        for (ex in sortedByName) {
            Log.d(tag, ex.toString(true))
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FitnessDiaryTheme {
        Greeting("Android")
    }
}