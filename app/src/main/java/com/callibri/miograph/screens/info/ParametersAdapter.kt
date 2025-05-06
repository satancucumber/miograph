package com.callibri.miograph.screens.info

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.callibri.miograph.data.SensorInfoModel
import com.callibri.miograph.databinding.ParameterListItemBinding

class ParametersAdapter :
    ListAdapter<SensorInfoModel.Parameter, ParametersAdapter.ParameterViewHolder>(ParameterDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParameterViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ParameterListItemBinding.inflate(inflater, parent, false)
        return ParameterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ParameterViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ParameterViewHolder(private val binding: ParameterListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(parameter: SensorInfoModel.Parameter) {
            binding.parameter = parameter
            binding.executePendingBindings()
        }
    }

    private class ParameterDiffCallback : DiffUtil.ItemCallback<SensorInfoModel.Parameter>() {
        override fun areItemsTheSame(
            oldItem: SensorInfoModel.Parameter,
            newItem: SensorInfoModel.Parameter
        ): Boolean = oldItem.name == newItem.name

        override fun areContentsTheSame(
            oldItem: SensorInfoModel.Parameter,
            newItem: SensorInfoModel.Parameter
        ): Boolean = oldItem == newItem
    }
}