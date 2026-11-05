package com.example.fitnessdiary

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.room.withTransaction
import com.example.fitnessdiary.data.ExerciseDao
import com.example.fitnessdiary.data.ExerciseEntity
import com.example.fitnessdiary.data.WorkoutDao
import com.example.fitnessdiary.data.WorkoutEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class FormFragment : Fragment() {

    private val exerciseDao: ExerciseDao
        get() = (requireActivity().application as FitnessDiaryApplication).exerciseDao
    private val workoutDao: WorkoutDao
        get() = (requireActivity().application as FitnessDiaryApplication).workoutDao
    private val database
        get() = (requireActivity().application as FitnessDiaryApplication).database

    // Поля упражнения
    private lateinit var nameInput: EditText
    private lateinit var timeInput: EditText
    private lateinit var approachesInput: EditText
    private lateinit var repetitionsInput: EditText
    private lateinit var weightInput: EditText
    private lateinit var typeGroup: RadioGroup
    private lateinit var typeTime: RadioButton
    private lateinit var typeReps: RadioButton
    private lateinit var weightCheck: CheckBox
    private lateinit var addButton: Button
    private lateinit var clearButton: Button

    // Поля тренировки
    private lateinit var workoutNameInput: EditText
    private lateinit var createWorkoutButton: Button
    private lateinit var createWorkoutWithExerciseButton: Button
    private lateinit var workoutSpinner: Spinner
    private lateinit var workoutDateText: TextView
    private lateinit var pickDateButton: Button

    private var selectedWorkoutDate: Long = System.currentTimeMillis()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_form, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setupListeners()
        setupWorkoutSpinner()
        updateDateText()
        applyVisibility()
    }

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

        workoutNameInput = view.findViewById(R.id.workoutNameInput)
        createWorkoutButton = view.findViewById(R.id.createWorkoutButton)
        createWorkoutWithExerciseButton = view.findViewById(R.id.createWorkoutWithExerciseButton)
        workoutSpinner = view.findViewById(R.id.workoutSpinner)
        workoutDateText = view.findViewById(R.id.workoutDateText)
        pickDateButton = view.findViewById(R.id.pickDateButton)
    }

    private fun setupListeners() {
        typeGroup.setOnCheckedChangeListener { _, _ -> applyVisibility() }
        weightCheck.setOnCheckedChangeListener { _, _ -> applyVisibility() }
        addButton.setOnClickListener { onAddClick() }
        clearButton.setOnClickListener { onClearClick() }
        createWorkoutButton.setOnClickListener { onCreateWorkoutClick() }
        createWorkoutWithExerciseButton.setOnClickListener { onCreateWorkoutWithExerciseClick() }
        pickDateButton.setOnClickListener { showDatePicker() }
    }

    private fun applyVisibility() {
        val timeMode = typeTime.isChecked
        timeInput.visibility = if (timeMode) View.VISIBLE else View.GONE
        approachesInput.visibility = if (timeMode) View.GONE else View.VISIBLE
        repetitionsInput.visibility = if (timeMode) View.GONE else View.VISIBLE
        weightCheck.visibility = if (timeMode) View.GONE else View.VISIBLE
        weightInput.visibility =
            if (!timeMode && weightCheck.isChecked) View.VISIBLE else View.GONE
    }

    private fun setupWorkoutSpinner() {
        updateSpinner(emptyList())
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                workoutDao.getAllWorkouts().collect { workouts -> updateSpinner(workouts) }
            }
        }
    }

    private fun updateSpinner(workouts: List<WorkoutEntity>) {
        val selectedId = (workoutSpinner.selectedItem as? WorkoutEntity)?.id ?: -1L
        val noLink = WorkoutEntity(id = -1L, name = getString(R.string.workout_no_link))
        val items = listOf(noLink) + workouts
        val adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item, items
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        workoutSpinner.adapter = adapter
        val newIndex = items.indexOfFirst { it.id == selectedId }
        workoutSpinner.setSelection(if (newIndex >= 0) newIndex else 0)
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance().apply { timeInMillis = selectedWorkoutDate }
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val c = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                selectedWorkoutDate = c.timeInMillis
                updateDateText()
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).apply { setTitle(getString(R.string.date_picker_title)) }.show()
    }

    private fun updateDateText() {
        val fmt = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        workoutDateText.text = getString(R.string.workout_date_label, fmt.format(Date(selectedWorkoutDate)))
    }

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

    private fun onCreateWorkoutClick() {
        val name = workoutNameInput.text.toString().trim()
        if (name.isEmpty()) {
            workoutNameInput.error = getString(R.string.workout_name_empty)
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            createWorkoutButton.isEnabled = false
            try {
                workoutDao.insert(WorkoutEntity(name = name, date = selectedWorkoutDate))
                workoutNameInput.text.clear()
                selectedWorkoutDate = System.currentTimeMillis()
                updateDateText()
                Toast.makeText(requireContext(), R.string.workout_created, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                createWorkoutButton.isEnabled = true
            }
        }
    }

    // Создание тренировки и первого упражнения одной транзакцией.
    private fun onCreateWorkoutWithExerciseClick() {
        // Валидация тренировки
        val workoutName = workoutNameInput.text.toString().trim()
        if (workoutName.isEmpty()) {
            workoutNameInput.error = getString(R.string.workout_name_empty)
            return
        }

        // Валидация упражнения
        nameInput.error = null
        timeInput.error = null
        approachesInput.error = null
        repetitionsInput.error = null
        weightInput.error = null

        val exerciseName = nameInput.text.toString().trim()
        if (exerciseName.isEmpty()) {
            nameInput.error = getString(R.string.err_name)
            return
        }

        val exerciseEntity: ExerciseEntity = if (typeTime.isChecked) {
            val duration = parseDuration(timeInput.text.toString())
            if (duration == null || duration <= Duration.ZERO) {
                timeInput.error = getString(R.string.err_time)
                return
            }
            ExerciseEntity(name = exerciseName, timeSeconds = duration.inWholeSeconds)
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
                ExerciseEntity(
                    name = exerciseName,
                    approaches = approaches,
                    repetitions = repetitions,
                    weight = weight
                )
            } else {
                ExerciseEntity(
                    name = exerciseName,
                    approaches = approaches,
                    repetitions = repetitions
                )
            }
        }

        // Транзакция
        viewLifecycleOwner.lifecycleScope.launch {
            createWorkoutWithExerciseButton.isEnabled = false
            addButton.isEnabled = false
            try {
                database.withTransaction {
                    val workoutId = workoutDao.insert(
                        WorkoutEntity(name = workoutName, date = selectedWorkoutDate)
                    )
                    exerciseDao.insert(exerciseEntity.copy(workoutId = workoutId))
                }
                Toast.makeText(
                    requireContext(),
                    R.string.workout_with_exercise_created,
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().popBackStack()
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.transaction_error, e.message ?: ""),
                    Toast.LENGTH_LONG
                ).show()
                createWorkoutWithExerciseButton.isEnabled = true
                addButton.isEnabled = true
            }
        }
    }

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

        val selectedWorkout = workoutSpinner.selectedItem as? WorkoutEntity
        val workoutId = if (selectedWorkout?.id == -1L) null else selectedWorkout?.id

        val entity: ExerciseEntity = if (typeTime.isChecked) {
            val duration = parseDuration(timeInput.text.toString())
            if (duration == null || duration <= Duration.ZERO) {
                timeInput.error = getString(R.string.err_time)
                return
            }
            ExerciseEntity(
                name = name,
                timeSeconds = duration.inWholeSeconds,
                workoutId = workoutId
            )
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
                ExerciseEntity(
                    name = name,
                    approaches = approaches,
                    repetitions = repetitions,
                    weight = weight,
                    workoutId = workoutId
                )
            } else {
                ExerciseEntity(
                    name = name,
                    approaches = approaches,
                    repetitions = repetitions,
                    workoutId = workoutId
                )
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            addButton.isEnabled = false
            try {
                exerciseDao.insert(entity)
                Toast.makeText(requireContext(), R.string.record_added, Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка сохранения: ${e.message}", Toast.LENGTH_LONG).show()
                addButton.isEnabled = true
            }
        }
    }

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

        workoutNameInput.text.clear()
        workoutSpinner.setSelection(0)
        selectedWorkoutDate = System.currentTimeMillis()
        updateDateText()

        applyVisibility()
    }
}