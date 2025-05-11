package com.callibri.miograph.screens.menu

import com.neurosdk2.neuro.types.SensorInfo

data class DeviceListItem(
    val name: String,
    val address: String,
    var inProgress: Boolean,
    val sInfo: SensorInfo,
    val isConnected: Boolean = false
)