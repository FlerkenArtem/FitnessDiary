package com.example.fitnessdiary

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class ListFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val addButton = view.findViewById<Button>(R.id.listAddButton)
        val showFirstButton = view.findViewById<Button>(R.id.listShowFirstButton)
        val showSecondButton = view.findViewById<Button>(R.id.listShowSecondButton)
        val aboutButton = view.findViewById<Button>(R.id.aboutButton)

        addButton.setOnClickListener {
            findNavController().navigate(R.id.action_list_to_form)
        }

        showFirstButton.setOnClickListener {
            val first = RecordStore.records.firstOrNull()
            if (first == null) {
                Toast.makeText(requireContext(), R.string.no_records, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val args = bundleOf("recordId" to first.id)
            findNavController().navigate(R.id.action_list_to_details, args)
        }

        showSecondButton.setOnClickListener {
            val second = RecordStore.records.getOrNull(1)
            if (second == null) {
                Toast.makeText(requireContext(), R.string.no_records, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val args = bundleOf("recordId" to second.id)
            findNavController().navigate(R.id.action_list_to_details, args)
        }

        aboutButton.setOnClickListener {
            val intent = Intent(requireContext(), AboutActivity::class.java).apply {
                putExtra(AboutActivity.EXTRA_THEME, getString(R.string.about_theme))
            }
            startActivity(intent)
        }
    }
}