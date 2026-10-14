package com.example.fitnessdiary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class RecordAdapter(
    private val onItemClick: (Exercise) -> Unit) :
    ListAdapter<Exercise, RecordAdapter.RecordViewHolder>(DIFF_CALLBACK) {
    class RecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameView: TextView = itemView.findViewById(R.id.itemName)
        val approachesView: TextView = itemView.findViewById(R.id.itemApproaches)
        val repetitionsView: TextView = itemView.findViewById(R.id.itemRepetitions)
        val weightView: TextView = itemView.findViewById(R.id.itemWeight)
        val timeView: TextView = itemView.findViewById(R.id.itemTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_record, parent, false)
        return RecordViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        val exercise = getItem(position)
        val ctx = holder.itemView.context

        holder.nameView.text = exercise.name

        if (exercise.approaches != null) {
            holder.approachesView.text =
                ctx.getString(R.string.item_approaches, exercise.approaches)
            holder.approachesView.visibility = View.VISIBLE
        } else {
            holder.approachesView.visibility = View.GONE
        }

        if (exercise.repetitions != null) {
            holder.repetitionsView.text =
                ctx.getString(R.string.item_repetitions, exercise.repetitions)
            holder.repetitionsView.visibility = View.VISIBLE
        } else {
            holder.repetitionsView.visibility = View.GONE
        }

        if (exercise.weight != null) {
            holder.weightView.text =
                ctx.getString(R.string.item_weight, exercise.weight.toString())
            holder.weightView.visibility = View.VISIBLE
        } else {
            holder.weightView.visibility = View.GONE
        }

        if (exercise.time != null) {
            holder.timeView.text =
                ctx.getString(R.string.item_time, exercise.time.toString())
            holder.timeView.visibility = View.VISIBLE
        } else {
            holder.timeView.visibility = View.GONE
        }

        holder.itemView.setOnClickListener { onItemClick(exercise) }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Exercise>() {
            override fun areItemsTheSame(oldItem: Exercise, newItem: Exercise): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Exercise, newItem: Exercise): Boolean =
                oldItem.name == newItem.name &&
                oldItem.approaches == newItem.approaches &&
                oldItem.repetitions == newItem.repetitions &&
                oldItem.weight == newItem.weight &&
                oldItem.time == newItem.time
        }
    }
}