package com.callibri.miograph.screens.menu

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.callibri.miograph.callibri.CallibriController
import com.neurosdk2.neuro.types.SensorState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MenuViewModel : ViewModel() {
    var connected = ObservableBoolean(false)
    var hasDevice = ObservableBoolean(CallibriController.hasDevice)

    private val _devices = MutableLiveData<List<DeviceListItem>>()
    val devices: LiveData<List<DeviceListItem>> get() = _devices

    private val _connectionError = MutableLiveData<Boolean>()
    val connectionError: LiveData<Boolean> = _connectionError

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

    internal fun reconnect(devicesListAdapter: DevicesListAdapter) {
        val currentDevice = devices.value?.firstOrNull() ?: return
        currentDevice.inProgress = true
        devicesListAdapter.notifyItemChanged(0)

        if (CallibriController.connectionState == SensorState.StateInRange) {
            viewModelScope.launch {
                CallibriController.disconnectCurrent()
                withContext(Dispatchers.Main) {
                    currentDevice.inProgress = false
                    updateConnectedDevices()
                    devicesListAdapter.notifyItemChanged(0)
                }
            }
        } else {
            CallibriController.connectCurrent { state ->
                viewModelScope.launch {
                    currentDevice.inProgress = false
                    connected.set(state == SensorState.StateInRange)
                    updateConnectedDevices()
                    devicesListAdapter.notifyItemChanged(0)

                    if (state != SensorState.StateInRange) {
                        _connectionError.postValue(true)
                    }
                }
            }
        }
    }

    fun resetConnectionError() {
        _connectionError.value = false
    }
}