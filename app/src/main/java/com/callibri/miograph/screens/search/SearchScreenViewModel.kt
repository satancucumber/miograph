package com.callibri.miograph.screens.search

import android.content.Context
import androidx.databinding.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.callibri.miograph.callibri.CallibriController
import com.neurosdk2.neuro.types.SensorState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SearchScreenViewModel : ViewModel() {

    var started = ObservableBoolean(false)

    private val _devices = MutableLiveData<List<DeviceListItem>>()
    val devices: LiveData<List<DeviceListItem>> get() = _devices

    private val _connectionError = MutableLiveData<Boolean>()
    val connectionError: LiveData<Boolean> get() = _connectionError

    private val _navigateToMenu = MutableLiveData<Boolean>()
    val navigateToMenu: LiveData<Boolean> get() = _navigateToMenu

    fun onSearchClicked() {
        if (started.get()) {
            CallibriController.stopSearch()
        } else {
            CallibriController.disconnectCurrent()
            CallibriController.closeSensor()

            CallibriController.startSearch { _, infos ->
                _devices.postValue(
                    infos.map { sensorInfo ->
                        DeviceListItem(
                            name = sensorInfo.name,
                            address = sensorInfo.address,
                            inProgress = false,
                            sInfo = sensorInfo
                        )
                    }
                )
            }
        }
        started.set(!started.get())
    }

    fun connectToDevice(context: Context, device: DeviceListItem) {
        val currentDevices = _devices.value ?: return
        val index = currentDevices.indexOfFirst { it.sInfo == device.sInfo }
        if (index == -1) return

        val updatedDevices = currentDevices.toMutableList().apply {
            set(index, device.copy(inProgress = true))
        }
        _devices.postValue(updatedDevices)

        viewModelScope.launch {
            try {
                val state = withContext(Dispatchers.IO) {
                    suspendCoroutine<SensorState> { cont ->
                        CallibriController.createAndConnect(context, device.sInfo) { state ->
                            cont.resume(state)
                        }
                    }
                }

                val updatedList = currentDevices.toMutableList().apply {
                    set(index, device.copy(inProgress = false))
                }
                _devices.postValue(updatedList)

                if (state == SensorState.StateInRange) {
                    _navigateToMenu.postValue(true)
                } else {
                    _connectionError.postValue(true)
                }
            } catch (e: Exception) {
                val updatedList = currentDevices.toMutableList().apply {
                    set(index, device.copy(inProgress = false))
                }
                _devices.postValue(updatedList)
                _connectionError.postValue(true)
            }
        }
    }

    fun resetConnectionError() {
        _connectionError.value = false
    }

    fun resetNavigateToMenu() {
        _navigateToMenu.value = false
    }

    fun close() {
        CallibriController.stopSearch()
    }
}

