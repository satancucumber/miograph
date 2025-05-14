package com.callibri.miograph.screens.info

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.callibri.miograph.callibri.CallibriController
import com.callibri.miograph.data.SensorInfoModel

class InfoViewModel : ViewModel() {
    private val _sensorInfo = MutableLiveData<SensorInfoModel>()
    val sensorInfo: LiveData<SensorInfoModel> = _sensorInfo

    fun loadSensorInfo() {
        _sensorInfo.postValue(CallibriController.fullInfo())
    }

    fun setSensorInfo(info: SensorInfoModel) {
        _sensorInfo.postValue(info)
    }
}