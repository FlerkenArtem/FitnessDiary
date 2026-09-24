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
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

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
        typeGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.typeTime -> applyVisibility()
                R.id.typeReps -> applyVisibility()
            }
        }
        weightCheck.setOnCheckedChangeListener { _, _ -> applyVisibility() }
        showButton.setOnClickListener { onShowClick() }
        clearButton.setOnClickListener { onClearClick() }
    }

    // Применить видимость полей по текущему состоянию RadioGroup/CheckBox
    private fun applyVisibility() {
        val timeMode = typeTime.isChecked

        timeInput.visibility = if (timeMode) View.VISIBLE else View.GONE
        approachesInput.visibility = if (timeMode) View.GONE else View.VISIBLE
        repetitionsInput.visibility = if (timeMode) View.GONE else View.VISIBLE
        weightCheck.visibility = if (timeMode) View.GONE else View.VISIBLE
        weightInput.visibility =
            if (!timeMode && weightCheck.isChecked) View.VISIBLE else View.GONE
    }

    /**
     * Разбирает строку времени в Duration.
     * Поддерживаются форматы: "ЧЧ:ММ:СС", "ММ:СС", "СС".
     * Возвращает null, если формат неверный.
     */
    private fun parseDuration(input: String): Duration? {
        val parts = input.trim().split(":")
        return when (parts.size) {
            // "ММ:СС"
            2 -> {
                val m = parts[0].toLongOrNull() ?: return null
                val s = parts[1].toLongOrNull() ?: return null
                if (m < 0 || s < 0 || s >= 60) null else m.minutes + s.seconds
            }
            // "ЧЧ:ММ:СС"
            3 -> {
                val h = parts[0].toLongOrNull() ?: return null
                val m = parts[1].toLongOrNull() ?: return null
                val s = parts[2].toLongOrNull() ?: return null
                if (h < 0 || m < 0 || m >= 60 || s < 0 || s >= 60) null
                else h.hours + m.minutes + s.seconds
            }
            else -> null
        }
    }

    // Обработчик кнопки "Показать"
    private fun onShowClick() {
        nameInput.error = null
        timeInput.error = null
        approachesInput.error = null
        repetitionsInput.error = null
        weightInput.error = null

        val name = nameInput.text.toString().trim()
        if (name.isEmpty()) {
            nameInput.error = getString(R.string.err_name)
            return
        }

        val exercise: Exercise = if (typeTime.isChecked) {
            val duration = parseDuration(timeInput.text.toString())
            if (duration == null || duration <= Duration.ZERO) {
                timeInput.error = getString(R.string.err_time)
                return
            }
            Exercise(1, name, duration)
        } else {
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
                val weight = weightInput.text.toString().trim().toDoubleOrNull()
                if (weight == null || weight < 0.0) {
                    weightInput.error = getString(R.string.err_weight)
                    return
                }
                Exercise(1, name, approaches, repetitions, weight)
            } else {
                Exercise(1, name, approaches, repetitions)
            }
        }

        resultText.text = exercise.toString(false)
    }

    // Обработчик кнопки "Очистить"
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