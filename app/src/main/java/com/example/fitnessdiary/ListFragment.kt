package com.example.fitnessdiary

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnessdiary.data.ExerciseDao
import com.example.fitnessdiary.data.WorkoutDao
import com.example.fitnessdiary.data.WorkoutEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ListFragment : Fragment() {

    private var recyclerView: RecyclerView? = null
    private var adapter: RecordAdapter? = null
    private lateinit var emptyText: TextView
    private lateinit var countText: TextView
    private lateinit var filterSpinner: Spinner
    private var observeJob: Job? = null

    private val exerciseDao: ExerciseDao
        get() = (requireActivity().application as FitnessDiaryApplication).exerciseDao
    private val workoutDao: WorkoutDao
        get() = (requireActivity().application as FitnessDiaryApplication).workoutDao

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(
        R.layout.fragment_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.recyclerView)
            ?: error("RecyclerView с id=recyclerView не найден")
        val addButton = view.findViewById<Button>(R.id.listAddButton)
        val aboutButton = view.findViewById<Button>(R.id.aboutButton)

        emptyText = view.findViewById(R.id.emptyText)
        countText = view.findViewById(R.id.countText)
        filterSpinner = view.findViewById(R.id.filterSpinner)

        val a = RecordAdapter { exercise ->
            val args = bundleOf("recordId" to exercise.id)
            findNavController().navigate(R.id.action_list_to_details, args)
        }
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = a
        recyclerView = rv
        adapter = a

        addButton.setOnClickListener {
            findNavController().navigate(R.id.action_list_to_form)
        }
        aboutButton.setOnClickListener {
            val intent = Intent(requireContext(),
                AboutActivity::class.java).apply {
                putExtra(AboutActivity.EXTRA_THEME,
                    getString(R.string.about_theme))
            }
            startActivity(intent)
        }

        setupFilterSpinner()
    }

    // Spinner фильтра
    private fun setupFilterSpinner() {
        // Начальное состояние - только Все
        bindSpinnerItems(emptyList())

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                workoutDao.getAllWorkouts().collect { workouts ->
                    bindSpinnerItems(workouts)
                }
            }
        }

        filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                val selected = filterSpinner.selectedItem as? WorkoutEntity
                val workoutId = if (selected == null || selected.id == -1L) null else selected.id
                startObserving(workoutId)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun bindSpinnerItems(workouts: List<WorkoutEntity>) {
        val selectedId = (filterSpinner.selectedItem as? WorkoutEntity)?.id ?: -1L
        val allItem = WorkoutEntity(id = -1L, name = getString(R.string.workout_filter_all))
        val items = listOf(allItem) + workouts

        val adapterSpinner = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            items
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        filterSpinner.adapter = adapterSpinner

        val newIndex = items.indexOfFirst { it.id == selectedId }
        if (newIndex >= 0) filterSpinner.setSelection(newIndex)
    }

    // Наблюдение за упражнениями
    private fun startObserving(workoutId: Long?) {
        observeJob?.cancel()

        observeJob = viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                val flow = if (workoutId == null) {
                    exerciseDao.getAll()
                } else {
                    exerciseDao.getByWorkoutId(workoutId)
                }
                flow.collect { entities ->
                    val records = entities.map { it.toExercise() }
                    adapter?.submitList(records)
                    countText.text = getString(R.string.record_count, records.size)
                    emptyText.visibility =
                        if (records.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        observeJob?.cancel()
        observeJob = null
        recyclerView?.adapter = null
        recyclerView = null
        adapter = null
    }
}