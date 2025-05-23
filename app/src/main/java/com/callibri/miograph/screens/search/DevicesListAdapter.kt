package com.callibri.miograph.screens.search

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.callibri.miograph.R
import com.callibri.miograph.databinding.DeviceListItemSearchBinding

class DevicesListAdapter(
    internal var devices: MutableList<DeviceListItem>,
    private val onConnectClick: (DeviceListItem) -> Unit
) : RecyclerView.Adapter<DevicesListAdapter.ViewHolder>() {

    // Делаем ViewHolder inner классом чтобы иметь доступ к свойствам адаптера
    inner class ViewHolder(val binding: DeviceListItemSearchBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            // Добавляем обработчик клика на иконку меню
            binding.overflowMenu.setOnClickListener { view ->
                showPopupMenu(view)
            }
        }

        private fun showPopupMenu(view: View) {
            val popup = PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.device_menu_connect, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                when(item.itemId) {
                    R.id.action_connect -> onConnectClick(devices[adapterPosition])
                }
                true
            }
            popup.show()
        }

        fun bind(device: DeviceListItem) {
            binding.deviceListItem = device
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Убираем lateinit binding и создаем его непосредственно для каждого ViewHolder
        val binding = DeviceListItemSearchBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(devices[position])
    }

    override fun getItemCount() = devices.size

    // Метод для обновления списка устройств
    @SuppressLint("NotifyDataSetChanged")
    fun updateDevices(newDevices: List<DeviceListItem>) {
        devices.clear()
        devices.addAll(newDevices)
        notifyDataSetChanged()
    }
}