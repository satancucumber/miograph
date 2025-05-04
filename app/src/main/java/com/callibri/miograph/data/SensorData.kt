package com.callibri.miograph.data

data class SensorData(
    val sensorName: String,
    val sensorAddress: String,
    val timestamp: Long,
    val value: Double
)