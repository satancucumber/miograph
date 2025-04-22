package com.callibri.miograph.screens.menu

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.callibri.miograph.R
import com.callibri.miograph.databinding.DeviceListItemMenuBinding

class DevicesListAdapter(
    internal var devices: MutableList<DeviceListItem>,
    private val onConnectClick: (DeviceListItem) -> Unit,
    private val onDisconnectClick: (DeviceListItem) -> Unit,
    private val onInfoClick: (DeviceListItem) -> Unit,
    private val isConnectedProvider: () -> Boolean
) : RecyclerView.Adapter<DevicesListAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: DeviceListItemMenuBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.overflowMenu.setOnClickListener { view ->
                showPopupMenu(view)
            }
        }

        private fun showPopupMenu(view: View) {
            val popup = PopupMenu(view.context, view)
            val menuRes = if (isConnectedProvider()) {
                R.menu.device_menu_disconnect_info
            } else {
                R.menu.device_menu_connect
            }
            popup.menuInflater.inflate(menuRes, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                when(item.itemId) {
                    R.id.action_connect -> onConnectClick(devices[adapterPosition])
                    R.id.action_disconnect -> onDisconnectClick(devices[adapterPosition])
                    R.id.action_info -> onInfoClick(devices[adapterPosition])
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
        val binding = DeviceListItemMenuBinding.inflate(
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