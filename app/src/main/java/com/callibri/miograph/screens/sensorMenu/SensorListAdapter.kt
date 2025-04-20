package com.callibri.miograph.screens.sensorMenu

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.neurosdk2.neuro.types.SensorInfo
import com.callibri.miograph.databinding.ItemSensorBinding

class SensorListAdapter :
    ListAdapter<SensorInfo, SensorListAdapter.SensorViewHolder>(DiffCallback()) {

    private var onItemClickListener: ((SensorInfo) -> Unit)? = null

    class SensorViewHolder(
        private val binding: ItemSensorBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(sensor: SensorInfo) {
            binding.sensor = sensor
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SensorViewHolder {
        val binding = ItemSensorBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SensorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SensorViewHolder, position: Int) {
        val sensor = getItem(position)
        holder.bind(sensor)
        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(sensor)
        }
    }

    fun setOnItemClickListener(listener: (SensorInfo) -> Unit) {
        onItemClickListener = listener
    }

    class DiffCallback : DiffUtil.ItemCallback<SensorInfo>() {
        override fun areItemsTheSame(oldItem: SensorInfo, newItem: SensorInfo): Boolean {
            return oldItem.address == newItem.address
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: SensorInfo, newItem: SensorInfo): Boolean {
            return oldItem == newItem
        }
    }
}