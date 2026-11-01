package com.example.fitnessdiary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.fitnessdiary.data.ExerciseDao
import com.example.fitnessdiary.data.ExerciseEntity
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class DetailsFragment : Fragment() {

    private var nameInput: EditText? = null
    private var approachesInput: EditText? = null
    private var repetitionsInput: EditText? = null
    private var weightInput: EditText? = null
    private var timeInput: EditText? = null
    private var saveButton: Button? = null
    private var deleteButton: Button? = null
    private var currentEntity: ExerciseEntity? = null

    private val dao: ExerciseDao
        get() = (requireActivity().application as FitnessDiaryApplication).exerciseDao

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_details, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nameInput = view.findViewById(R.id.exercise_name)
        approachesInput = view.findViewById(R.id.exercise_approaches)
        repetitionsInput = view.findViewById(R.id.exercise_repetitions)
        weightInput = view.findViewById(R.id.exercise_weight)
        timeInput = view.findViewById(R.id.exercise_time)
        saveButton = view.findViewById(R.id.saveButton)
        deleteButton = view.findViewById(R.id.deleteButton)

        val recordId: Long = arguments?.getLong("recordId", -1L) ?: -1L

        // Загрузка из DAO
        viewLifecycleOwner.lifecycleScope.launch {
            val entity = dao.findById(recordId)
            currentEntity = entity

            if (entity == null) {
                nameInput?.setText(getString(R.string.record_not_found))
                nameInput?.isEnabled = false
                saveButton?.visibility = View.GONE
                deleteButton?.visibility = View.GONE
                approachesInput?.visibility = View.GONE
                repetitionsInput?.visibility = View.GONE
                weightInput?.visibility = View.GONE
                timeInput?.visibility = View.GONE
                return@launch
            }

            // Заполняем поля
            nameInput?.setText(entity.name)

            if (entity.approaches != null) {
                approachesInput?.setText(entity.approaches.toString())
                approachesInput?.visibility = View.VISIBLE
            } else approachesInput?.visibility = View.GONE

            if (entity.repetitions != null) {
                repetitionsInput?.setText(entity.repetitions.toString())
                repetitionsInput?.visibility = View.VISIBLE
            } else repetitionsInput?.visibility = View.GONE

            if (entity.weight != null) {
                weightInput?.setText(entity.weight.toString())
                weightInput?.visibility = View.VISIBLE
            } else weightInput?.visibility = View.GONE

            if (entity.timeSeconds != null) {
                timeInput?.setText(entity.timeSeconds.seconds.toString())
                timeInput?.visibility = View.VISIBLE
            } else timeInput?.visibility = View.GONE
        }

        saveButton?.setOnClickListener   { onSaveClick() }
        deleteButton?.setOnClickListener { onDeleteClick() }
    }

    private fun onSaveClick() {
        val current = currentEntity ?: return

        nameInput?.error = null
        approachesInput?.error = null
        repetitionsInput?.error = null
        weightInput?.error = null
        timeInput?.error = null

        val newName = nameInput?.text?.toString()?.trim().orEmpty()
        if (newName.isEmpty()) {
            nameInput?.error = getString(R.string.err_name)
            return
        }

        val newApproaches: Int? =
            if (approachesInput?.visibility == View.VISIBLE) {
                val v = approachesInput?.text?.toString()?.trim()?.toIntOrNull()
                if (v == null || v <= 0) {
                    approachesInput?.error = getString(R.string.err_approaches)
                    return
                }
                v
            } else current.approaches

        val newRepetitions: Int? =
            if (repetitionsInput?.visibility == View.VISIBLE) {
                val v = repetitionsInput?.text?.toString()?.trim()?.toIntOrNull()
                if (v == null || v <= 0) {
                    repetitionsInput?.error = getString(R.string.err_repetitions)
                    return
                }
                v
            } else current.repetitions

        val newWeight: Double? =
            if (weightInput?.visibility == View.VISIBLE) {
                val v = weightInput?.text?.toString()?.trim()?.toDoubleOrNull()
                if (v == null || v < 0.0) {
                    weightInput?.error = getString(R.string.err_weight)
                    return
                }
                v
            } else current.weight

        val newTimeSeconds: Long? =
            if (timeInput?.visibility == View.VISIBLE) {
                val duration = parseDuration(timeInput?.text?.toString().orEmpty())
                if (duration == null || duration <= Duration.ZERO) {
                    timeInput?.error = getString(R.string.err_time)
                    return
                }
                duration.inWholeSeconds
            } else current.timeSeconds

        val updated = current.copy(
            name = newName,
            approaches = newApproaches,
            repetitions = newRepetitions,
            weight = newWeight,
            timeSeconds = newTimeSeconds
        )

        viewLifecycleOwner.lifecycleScope.launch {
            saveButton?.isEnabled = false
            try {
                dao.update(updated)
                Toast.makeText(requireContext(), R.string.saved_changes, Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.save_error, e.message ?: ""),
                    Toast.LENGTH_LONG
                ).show()
                saveButton?.isEnabled = true
            }
        }
    }

    // Удаление записи.
    private fun onDeleteClick() {
        val current = currentEntity ?: return

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.confirm_delete_title)
            .setMessage(R.string.confirm_delete_message)
            .setPositiveButton(R.string.confirm_yes) { _, _ ->
                performDelete(current)
            }
            .setNegativeButton(R.string.confirm_no, null)
            .show()
    }

    private fun performDelete(entity: ExerciseEntity) {
        viewLifecycleOwner.lifecycleScope.launch {
            deleteButton?.isEnabled = false
            saveButton?.isEnabled = false
            try {
                dao.delete(entity)
                Toast.makeText(requireContext(), R.string.deleted, Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.delete_error, e.message ?: ""),
                    Toast.LENGTH_LONG
                ).show()
                deleteButton?.isEnabled = true
                saveButton?.isEnabled = true
            }
        }
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

    override fun onDestroyView() {
        super.onDestroyView()
        nameInput = null
        approachesInput = null
        repetitionsInput = null
        weightInput = null
        timeInput = null
        saveButton = null
        deleteButton = null
        currentEntity = null
    }
}