package com.callibri.miograph.screens.menu

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.callibri.miograph.callibri.CallibriController
import com.neurosdk2.neuro.types.SensorState

class MenuViewModel : ViewModel() {
    var connected = ObservableBoolean(false)
    var hasDevice = ObservableBoolean(CallibriController.hasDevice)

    private val _devices = MutableLiveData<List<DeviceListItem>>()
    val devices: LiveData<List<DeviceListItem>> get() = _devices

    fun updateConnectedDevices() {
        connected.set(CallibriController.connectionState == SensorState.StateInRange)
        hasDevice.set(CallibriController.hasDevice)

        val devices = mutableListOf<DeviceListItem>()
        CallibriController.currentSensorInfo?.let { sensorInfo ->
            devices.add(
                DeviceListItem(
                    name = sensorInfo.name,
                    address = sensorInfo.address,
                    inProgress = false,
                    sInfo = sensorInfo
                )
            )
        }
        _devices.postValue(devices)
    }
}