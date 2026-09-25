package com.example.fitnessdiary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class FormFragment : Fragment() {

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
    private lateinit var addButton: Button
    private lateinit var clearButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_form,
            container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindViews(view)
        setupListeners()
        applyVisibility()
    }

    // Привязка представлений формы к полям класса
    private fun bindViews(view: View) {
        nameInput = view.findViewById(R.id.nameInput)
        timeInput = view.findViewById(R.id.timeInput)
        approachesInput = view.findViewById(R.id.approachesInput)
        repetitionsInput = view.findViewById(R.id.repetitionsInput)
        weightInput = view.findViewById(R.id.weightInput)

        typeGroup = view.findViewById(R.id.typeGroup)
        typeTime = view.findViewById(R.id.typeTime)
        typeReps = view.findViewById(R.id.typeReps)
        weightCheck = view.findViewById(R.id.weightCheck)
        addButton = view.findViewById(R.id.addButton)
        clearButton = view.findViewById(R.id.clearButton)
    }

    // Привязка наблюдателей к их функциям
    private fun setupListeners() {
        typeGroup.setOnCheckedChangeListener { _, _ -> applyVisibility() }
        weightCheck.setOnCheckedChangeListener { _, _ -> applyVisibility() }
        addButton.setOnClickListener { onAddClick() }
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
     * Поддерживаются форматы: "ЧЧ:ММ:СС", "ММ:СС".
     * Возвращает null, если формат неверный.
     */
    private fun parseDuration(input: String): Duration? {
        val parts = input.trim().split(":")
        return when (parts.size) {
            2 -> {
                val m = parts[0].toLongOrNull() ?: return null
                val s = parts[1].toLongOrNull() ?: return null
                if (m < 0 || s < 0 || s >= 60) null else m.minutes + s.seconds
            }
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

    // Обработчик кнопки "Добавить"
    private fun onAddClick() {
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
            Exercise(name, duration)
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
                Exercise(name, approaches, repetitions, weight)
            } else {
                Exercise(name, approaches, repetitions)
            }
        }

        RecordStore.add(exercise)
        Toast.makeText(requireContext(), R.string.record_added, Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

    // Обработчик кнопки "Очистить"
    private fun onClearClick() {
        nameInput.error = null
        timeInput.error = null
        approachesInput.error = null
        repetitionsInput.error = null
        weightInput.error = null

        nameInput.text.clear()
        timeInput.text.clear()
        approachesInput.text.clear()
        repetitionsInput.text.clear()
        weightInput.text.clear()

        weightCheck.isChecked = false
        typeTime.isChecked = true

        applyVisibility()
    }
}