package com.callibri.miograph.screens.menu

import android.widget.Toast
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import com.neurosdk2.neuro.types.SensorInfo
import com.neurosdk2.neuro.types.SensorState
import com.callibri.miograph.R
import com.callibri.miograph.callibri.CallibriController

class MenuViewModel : ViewModel() {
    var connected = ObservableBoolean(false)
    var hasDevice = ObservableBoolean(CallibriController.hasDevice)
    val connectedSensors = ObservableArrayList<SensorInfo>()
    val sensorCount = ObservableInt(0)

    fun updateDeviceInfo() {
        connected.set(CallibriController.connectionState == SensorState.StateInRange)
        hasDevice.set(CallibriController.hasDevice)
        updateConnectedSensors()
    }

    private fun updateConnectedSensors() {
        connectedSensors.clear()
        CallibriController.currentSensorInfo?.let {
            connectedSensors.add(it)
        }
        sensorCount.set(connectedSensors.size)
    }
}