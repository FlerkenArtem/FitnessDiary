package com.example.fitnessdiary

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.time.Duration.Companion.minutes

class MainActivity : AppCompatActivity() {

    // Поля ввода
    private lateinit var nameInput: EditText
    private lateinit var timeInput: EditText
    private lateinit var approachesInput: EditText
    private lateinit var repetitionsInput: EditText
    private lateinit var weightInput: EditText

    // Управляющие элементы
    private lateinit var typeGroup: RadioGroup
    private lateinit var typeTime: RadioButton
    private lateinit var typeReps: RadioButton
    private lateinit var weightCheck: CheckBox
    private lateinit var showButton: Button
    private lateinit var clearButton: Button
    private lateinit var resultText: TextView

    companion object {
        private const val KEY_RESULT = "result_text"
        private const val TAG = "LAB2"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        bindViews()
        setupListeners()

        applyVisibility()

        val savedResult = savedInstanceState?.getString(KEY_RESULT)
        if (!savedResult.isNullOrEmpty()) {
            resultText.text = savedResult
            Log.d(TAG, "Восстановление")
        }
    }

    // Метод обработки закрытия
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }

    // Сохранение текущих данных
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_RESULT, resultText.text.toString())
    }

    // Привязка представлений формы к полям класса
    private fun bindViews() {
        nameInput = findViewById(R.id.nameInput)
        timeInput = findViewById(R.id.timeInput)
        approachesInput = findViewById(R.id.approachesInput)
        repetitionsInput = findViewById(R.id.repetitionsInput)
        weightInput = findViewById(R.id.weightInput)

        typeGroup = findViewById(R.id.typeGroup)
        typeTime = findViewById(R.id.typeTime)
        typeReps = findViewById(R.id.typeReps)
        weightCheck = findViewById(R.id.weightCheck)
        showButton = findViewById(R.id.showButton)
        clearButton = findViewById(R.id.clearButton)
        resultText = findViewById(R.id.resultText)
    }

    // Привязка наблюдателей к их функциям
    private fun setupListeners() {
        // Переключение RadioButton меняет набор видимых полей
        typeGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.typeTime -> applyVisibility()
                R.id.typeReps -> applyVisibility()
            }
        }

        // Чекбокс веса влияет только в режиме "по подходам"
        weightCheck.setOnCheckedChangeListener { _, _ -> applyVisibility() }

        // Кнопка "Показать"
        showButton.setOnClickListener { onShowClick() }

        // Кнопка "Очистить"
        clearButton.setOnClickListener { onClearClick() }
    }

    // Применить видимость полей по текущему состоянию RadioGroup/CheckBox
    private fun applyVisibility() {
        val timeMode = typeTime.isChecked

        // Поля времени — только в режиме "по времени"
        timeInput.visibility = if (timeMode) View.VISIBLE else View.GONE

        // Поля подходов/повторений — только в режиме "по подходам"
        approachesInput.visibility = if (timeMode) View.GONE else View.VISIBLE
        repetitionsInput.visibility = if (timeMode) View.GONE else View.VISIBLE

        // Чекбокс веса — только в режиме "по подходам"
        weightCheck.visibility = if (timeMode) View.GONE else View.VISIBLE

        // Поле веса — только если чекбокс отмечен
        weightInput.visibility =
            if (!timeMode && weightCheck.isChecked) View.VISIBLE else View.GONE
    }

    private fun onShowClick() {
        // Сброс прошлых ошибок
        nameInput.error = null
        timeInput.error = null
        approachesInput.error = null
        repetitionsInput.error = null
        weightInput.error = null

        // 1. Название
        val name = nameInput.text.toString().trim()
        if (name.isEmpty()) {
            nameInput.error = getString(R.string.err_name)
            return
        }

        // 2. Ветка по выбранному RadioButton
        val exercise: Exercise = if (typeTime.isChecked) {
            // Конструктор (id, name, time)
            val minutes = timeInput.text.toString().trim().toIntOrNull()
            if (minutes == null || minutes <= 0) {
                timeInput.error = getString(R.string.err_time)
                return
            }
            Exercise(1, name, minutes.minutes)
        } else {
            // Общие поля для ветки с подходами
            val approaches = approachesInput.text.toString().trim().toIntOrNull()
            if (approaches == null || approaches <= 0) {
                approachesInput.error = getString(R.string.err_approaches)
                return
            }
            val repetitions = repetitionsInput.text.toString().trim().toIntOrNull()
            if (repetitions == null || repetitions <= 0) {
                repetitionsInput.error = getString(R.string.err_repetitions)
                return
            }

            if (weightCheck.isChecked) {
                // Конструктор (id, name, approaches, repetitions, weight)
                val weight = weightInput.text.toString().trim().toDoubleOrNull()
                if (weight == null || weight < 0.0) {
                    weightInput.error = getString(R.string.err_weight)
                    return
                }
                Exercise(1, name, approaches, repetitions, weight)
            } else {
                // Конструктор (id, name, approaches, repetitions)
                Exercise(1, name, approaches, repetitions)
            }
        }

        // 3. Вывод результата
        resultText.text = exercise.toString(false)
    }

    private fun onClearClick() {
        nameInput.error = null
        timeInput.error = null
        approachesInput.error = null
        repetitionsInput.error = null
        weightInput.error = null

        nameInput.text = null
        timeInput.text = null
        approachesInput.text = null
        repetitionsInput.text = null
        weightInput.text = null

        resultText.text = null
    }
}