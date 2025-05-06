package com.callibri.miograph.data

data class SensorInfoModel(
    val parameters: List<Parameter>,
    val commands: List<String>,
    val features: List<String>,
    val color: String,
    val signalType: String,
    val signal: Boolean
) {
    data class Parameter(
        val name: String,
        val access: String,
        val value: String
    )
}