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
    internal val _connected = MutableLiveData(CallibriController.connectionState == SensorState.StateInRange)
    val connected: LiveData<Boolean> = _connected

    init {
        CallibriController.connectionStateChanged = { state ->
            _connected.postValue(state == SensorState.StateInRange)
            updateConnectedDevices()
        }
    }

    var hasDevice = ObservableBoolean(CallibriController.hasDevice)

    private val _devices = MutableLiveData<List<DeviceListItem>>()
    val devices: LiveData<List<DeviceListItem>> get() = _devices

    private val _connectionError = MutableLiveData<Boolean>()
    val connectionError: LiveData<Boolean> = _connectionError

    fun updateConnectedDevices() {
        hasDevice.set(CallibriController.hasDevice)

        val devices = mutableListOf<DeviceListItem>()
        CallibriController.currentSensorInfo?.let { sensorInfo ->
            devices.add(
                DeviceListItem(
                    name = sensorInfo.name,
                    address = sensorInfo.address,
                    inProgress = false,
                    sInfo = sensorInfo,
                    isConnected = CallibriController.connectionState == SensorState.StateInRange
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
                    _connected.postValue(false)
                    currentDevice.inProgress = false
                    updateConnectedDevices()
                    devicesListAdapter.notifyItemChanged(0)
                }
            }
        } else {
            CallibriController.connectCurrent { state ->
                viewModelScope.launch {
                    _connected.postValue(state == SensorState.StateInRange)
                    currentDevice.inProgress = false
                    updateConnectedDevices()
                    devicesListAdapter.notifyItemChanged(0)
                    _connectionError.postValue(state != SensorState.StateInRange)
                }
            }
        }
    }

    fun resetConnectionError() {
        _connectionError.value = false
    }
}