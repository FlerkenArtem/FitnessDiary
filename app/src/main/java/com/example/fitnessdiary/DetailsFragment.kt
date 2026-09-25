package com.example.fitnessdiary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class DetailsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(
        R.layout.fragment_details, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recordId: Long = arguments?.getLong("recordId", -1L) ?: -1L

        val nameView        = view.findViewById<TextView>(R.id.exercise_name)
        val timeView        = view.findViewById<TextView>(R.id.exercise_time)
        val approachesView  = view.findViewById<TextView>(R.id.exercise_approaches)
        val repetitionsView = view.findViewById<TextView>(R.id.exercise_repetitions)
        val weightView      = view.findViewById<TextView>(R.id.exercise_weight)

        val exercise = RecordStore.records.firstOrNull { it.id == recordId }

        if (exercise == null) {
            nameView.text = getString(R.string.record_not_found)
            listOf(timeView, approachesView, repetitionsView, weightView).forEach {
                it.visibility = View.GONE
            }
            return
        }

        nameView.text = exercise.name

        if (exercise.time != null) {
            timeView.text = "Время: ${exercise.time}"
            timeView.visibility = View.VISIBLE
        } else {
            timeView.visibility = View.GONE
        }

        if (exercise.approaches != null) {
            approachesView.text = "Подходы: ${exercise.approaches}"
            approachesView.visibility = View.VISIBLE
        } else {
            approachesView.visibility = View.GONE
        }

        if (exercise.repetitions != null) {
            repetitionsView.text = "Повторения: ${exercise.repetitions}"
            repetitionsView.visibility = View.VISIBLE
        } else {
            repetitionsView.visibility = View.GONE
        }

        if (exercise.weight != null) {
            weightView.text = "Вес: ${exercise.weight} кг"
            weightView.visibility = View.VISIBLE
        } else {
            weightView.visibility = View.GONE
        }
    }
}