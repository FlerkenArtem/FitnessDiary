package com.example.fitnessdiary

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
import kotlinx.coroutines.launch

class ListFragment : Fragment() {

    private var recyclerView: RecyclerView? = null
    private var adapter: RecordAdapter? = null
    private lateinit var emptyText: TextView

    private val dao: ExerciseDao
        get() = (requireActivity().application as FitnessDiaryApplication).exerciseDao

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.recyclerView)
        val addButton = view.findViewById<Button>(R.id.listAddButton)
        val aboutButton = view.findViewById<Button>(R.id.aboutButton)
        emptyText = view.findViewById(R.id.emptyText)

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
            val intent = Intent(requireContext(), AboutActivity::class.java).apply {
                putExtra(AboutActivity.EXTRA_THEME, getString(R.string.about_theme))
            }
            startActivity(intent)
        }

        // Наблюдение за Flow из DAO
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                dao.getAll().collect { entities ->
                    val records = entities.map { it.toExercise() }
                    adapter?.submitList(records)
                    emptyText.visibility =
                        if (records.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recyclerView?.adapter = null
        recyclerView = null
        adapter = null
    }
}