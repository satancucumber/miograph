package com.callibri.miograph.screens.info

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.callibri.miograph.databinding.FeatureListItemBinding

class FeaturesAdapter :
    ListAdapter<String, FeaturesAdapter.FeatureViewHolder>(FeatureDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeatureViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = FeatureListItemBinding.inflate(inflater, parent, false)
        return FeatureViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FeatureViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class FeatureViewHolder(private val binding: FeatureListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(feature: String) {
            binding.feature = feature
            binding.executePendingBindings()
        }
    }

    private class FeatureDiffCallback : DiffUtil.ItemCallback<String>() {
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