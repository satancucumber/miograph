package com.callibri.miograph.screens.info

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.callibri.miograph.databinding.CommandListItemBinding

class CommandsAdapter :
    ListAdapter<String, CommandsAdapter.CommandViewHolder>(CommandDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommandViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CommandListItemBinding.inflate(inflater, parent, false)
        return CommandViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommandViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CommandViewHolder(private val binding: CommandListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(command: String) {
            binding.command = command
            binding.executePendingBindings()
        }
    }

    private class CommandDiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(
            oldItem: String,
            newItem: String
        ): Boolean = oldItem == newItem

        override fun areContentsTheSame(
            oldItem: String,
            newItem: String
        ): Boolean = oldItem == newItem
    }
}