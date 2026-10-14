package com.example.fitnessdiary

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class ListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecordAdapter
    private lateinit var updateButton : Button
    private lateinit var progressBar : ProgressBar
    private lateinit var emptyText : TextView
    private var refreshJob : Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val addButton = view.findViewById<Button>(R.id.listAddButton)
        val aboutButton = view.findViewById<Button>(R.id.aboutButton)
        updateButton = view.findViewById(R.id.updateButton)
        recyclerView = view.findViewById(R.id.recyclerView)
        progressBar = view.findViewById(R.id.progressBar)
        emptyText = view.findViewById(R.id.emptyText)


        adapter = RecordAdapter { exercise ->
            val args = bundleOf("recordId" to exercise.id)
            findNavController().navigate(R.id.action_list_to_details, args)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        adapter.submitList(RecordStore.records)

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

        updateButton.setOnClickListener {
            refresh()
        }
        refresh()
    }

    private fun refresh() {
        if (refreshJob?.isActive == true) return

        refreshJob = viewLifecycleOwner.lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE
            updateButton.isEnabled = false

            try {
                delay(1200.milliseconds)
                val current = RecordStore.records.toList()
                adapter.submitList(current)
                emptyText.visibility = if (current.isEmpty()) View.VISIBLE else View.GONE
            } finally {
                progressBar.visibility = View.GONE
                updateButton.isEnabled = true
            }
        }
    }
}