package com.callibri.miograph.callibri

import android.content.Context
import com.callibri.miograph.R
import com.callibri.miograph.data.SensorInfoModel
import com.neurosdk2.neuro.Callibri
import com.neurosdk2.neuro.Scanner
import com.neurosdk2.neuro.Sensor
import com.neurosdk2.neuro.interfaces.CallibriEnvelopeDataReceived
import com.neurosdk2.neuro.interfaces.CallibriSignalDataReceived
import com.neurosdk2.neuro.types.*
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.thread
import kotlinx.coroutines.*

object CallibriController {

    var scanner: Scanner? = null

    fun startSearch(sensorsChanged: (Scanner, List<SensorInfo>) -> Unit) {
        try {
            if (scanner == null) {
                scanner = Scanner(SensorFamily.SensorLECallibri, SensorFamily.SensorLEKolibri)
            } else {
                sensorsChanged(scanner!!, scanner!!.sensors)
            }
            scanner?.sensorsChanged = Scanner.ScannerCallback(sensorsChanged)
            scanner?.start()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun stopSearch() {
        try {
            scanner?.stop()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private var sensor: Callibri? = null

    var connectionStateChanged: (SensorState) -> Unit = { }
    var batteryChanged: (Int) -> Unit = { }
    var currentSensorInfo: SensorInfo? = null

    fun createAndConnect(sensorInfo: SensorInfo, onConnectionResult: (SensorState) -> Unit) {
        thread {
            try {
                sensor = scanner?.createSensor(sensorInfo) as Callibri
                currentSensorInfo = sensorInfo

                if (sensor != null) {
                    sensor?.sensorStateChanged = Sensor.SensorStateChanged(connectionStateChanged)
                    sensor?.batteryChanged = Sensor.BatteryChanged(batteryChanged)

                    try {
                        sensor?.samplingFrequency = SensorSamplingFrequency.FrequencyHz1000
                        sensor?.signalType = CallibriSignalType.EMG
                        sensor?.hardwareFilters = listOf(SensorFilter.FilterBSFBwhLvl2CutoffFreq45_55Hz,
                            SensorFilter.FilterHPFBwhLvl1CutoffFreq1Hz);
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }

                    connectionStateChanged(SensorState.StateInRange)
                    onConnectionResult(SensorState.StateInRange)
                } else {
                    onConnectionResult(SensorState.StateOutOfRange)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }

        }
    }

    fun connectCurrent(onConnectionResult: (SensorState) -> Unit) {
        if (sensor?.state == SensorState.StateOutOfRange) {
            thread {
                try {
                    sensor?.connect()
                    sensor?.sensorStateChanged = Sensor.SensorStateChanged(connectionStateChanged)
                    sensor?.batteryChanged = Sensor.BatteryChanged(batteryChanged)
                    onConnectionResult(sensor!!.state)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }
    }

    fun disconnectCurrent() {
        try {
            if (sensor?.state == SensorState.StateInRange)
                sensor?.disconnect()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

    }

    fun closeSensor() {
        try {
            sensor?.close()
            sensor = null
            currentSensorInfo = null
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    val connectionState get() = sensor?.state

    val hasDevice: Boolean
        get() {
            return sensor != null
        }

    val samplingFrequency get() = sensor?.samplingFrequency?.toFloat()

    fun fullInfo(): SensorInfoModel {
        val parameters = mutableListOf<SensorInfoModel.Parameter>().apply {
            for (param in sensor!!.supportedParameter) {
                val paramName = param.param.name
                val paramAccess = param.paramAccess?.toString() ?: ""
                val paramValue = when (param.param!!) {
                    SensorParameter.ParameterGain -> sensor?.gain?.toString()
                    SensorParameter.ParameterHardwareFilterState ->
                        sensor?.hardwareFilters?.joinToString { it.toString() }
                    SensorParameter.ParameterFirmwareMode -> sensor?.firmwareMode?.toString()
                    SensorParameter.ParameterSamplingFrequency -> sensor?.samplingFrequency?.toString()
                    SensorParameter.ParameterOffset -> sensor?.dataOffset?.toString()
                    SensorParameter.ParameterExternalSwitchState -> sensor?.extSwInput?.toString()
                    SensorParameter.ParameterADCInputState -> sensor?.adcInput?.toString()
                    SensorParameter.ParameterAccelerometerSens -> sensor?.accSens?.toString()
                    SensorParameter.ParameterGyroscopeSens -> sensor?.gyroSens?.toString()
                    SensorParameter.ParameterStimulatorAndMAState ->
                        """Stimulator state: ${sensor?.stimulatorMAState?.stimulatorState}
                    MA state: ${sensor?.stimulatorMAState?.maState}
                    """.trimIndent()
                    SensorParameter.ParameterStimulatorParamPack ->
                        """Current: ${sensor?.stimulatorParam?.current}
                    Frequency: ${sensor?.stimulatorParam?.frequency}
                    Pulse width: ${sensor?.stimulatorParam?.pulseWidth}
                    Stimulus duration: ${sensor?.stimulatorParam?.stimulusDuration}
                    """.trimIndent()
                    SensorParameter.ParameterMotionAssistantParamPack ->
                        """Gyro start: ${sensor?.motionAssistantParam?.gyroStart}
                    Gyro stop: ${sensor?.motionAssistantParam?.gyroStop}
                    Limb: ${sensor?.motionAssistantParam?.limb}
                    Min pause (in ms): ${sensor?.motionAssistantParam?.minPauseMs}
                    """.trimIndent()
                    SensorParameter.ParameterFirmwareVersion ->
                        """FW version: ${sensor?.version?.fwMajor}.${sensor?.version?.fwMinor}.${sensor?.version?.fwPatch}
                    HW version: ${sensor?.version?.hwMajor}.${sensor?.version?.hwMinor}.${sensor?.version?.hwPatch}
                    Extension: ${sensor?.version?.extMajor}
                    """.trimIndent()
                    SensorParameter.ParameterMEMSCalibrationStatus -> sensor?.memsCalibrateState?.toString()
                    SensorParameter.ParameterMotionCounterParamPack ->
                        """Insense threshold MG: ${sensor?.motionCounterParam?.insenseThresholdMG}
                    Insense threshold sample: ${sensor?.motionCounterParam?.insenseThresholdSample}
                    """.trimIndent()
                    SensorParameter.ParameterMotionCounter ->
                        """Insense threshold MG: ${sensor?.motionCounter}
                    Insense threshold sample: ${sensor?.motionCounterParam?.insenseThresholdSample}
                    """.trimIndent()
                    SensorParameter.ParameterBattPower -> sensor?.battPower?.toString()
                    SensorParameter.ParameterSensorFamily -> sensor?.sensFamily?.toString()
                    SensorParameter.ParameterSensorMode -> sensor?.firmwareMode?.toString()
                    SensorParameter.ParameterSensorChannels -> sensor?.channelsCount?.toString()
                    SensorParameter.ParameterSamplingFrequencyResp -> sensor?.samplingFrequencyResp?.toString()
                    SensorParameter.ParameterName -> sensor?.name?.toString()
                    SensorParameter.ParameterState -> sensor?.state?.toString()
                    SensorParameter.ParameterAddress -> sensor?.address?.toString()
                    SensorParameter.ParameterSerialNumber -> sensor?.serialNumber?.toString()
                    else -> continue // Пропускаем неизвестные параметры вместо прерывания
                }
                add(SensorInfoModel.Parameter(
                    paramName,
                    paramAccess,
                    paramValue ?: "N/A" // Значение по умолчанию если null
                ))
            }
        }

        val color = sensor?.color?.toString() ?: "N/A"
        val signal = sensor?.isSupportedFeature(SensorFeature.Signal) == true
        val signalType = if (signal) {
            sensor?.signalType?.toString() ?: "N/A"
        } else {
            "Not supported"
        }

        val commands = sensor?.supportedCommand?.map { it.name } ?: emptyList()
        val features = sensor?.supportedFeature?.map { it.name } ?: emptyList()

        return SensorInfoModel(
            parameters = parameters,
            commands = commands,
            features = features,
            color = color,
            signalType = signalType,
            signal = signal
        )
    }

    fun startEnvelope(envelopeReceived: (Array<CallibriEnvelopeData>) -> Unit) {
        sensor?.callibriEnvelopeDataReceived = CallibriEnvelopeDataReceived(envelopeReceived)
        executeCommand(SensorCommand.StartEnvelope)
    }

    fun stopEnvelope() {
        sensor?.callibriEnvelopeDataReceived = null

        executeCommand(SensorCommand.StopEnvelope)
    }

    fun startSignal(signalReceived: (Array<CallibriSignalData>) -> Unit) {
        sensor?.callibriSignalDataReceived = CallibriSignalDataReceived(signalReceived)
        executeCommand(SensorCommand.StartSignal)
    }

    fun stopSignal() {
        sensor?.callibriSignalDataReceived = null
        executeCommand(SensorCommand.StopSignal)
    }

    @OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
    private fun executeCommand(command: SensorCommand) =
        runBlocking(newSingleThreadContext("dedicatedThread")) {
            try {
                if (sensor?.isSupportedCommand(command) == true) {
                    sensor?.execCommand(command)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

    fun getSamplingFrequency(): Float {
        return sensor?.samplingFrequency?.toFloat() ?: 0f
    }

    fun setSamplingFrequency(frequency: SensorSamplingFrequency) {
        try {
            sensor?.samplingFrequency = frequency
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}

fun SensorSamplingFrequency.toFloat(): Float {
    return when (this) {
        SensorSamplingFrequency.FrequencyHz125 -> 125F
        SensorSamplingFrequency.FrequencyHz250 -> 250F
        SensorSamplingFrequency.FrequencyHz500 -> 500F
        SensorSamplingFrequency.FrequencyHz1000 -> 1000F
        SensorSamplingFrequency.FrequencyHz2000 -> 2000F
        SensorSamplingFrequency.FrequencyUnsupported -> 0.0f
        else -> 0.0f
    }
}

fun SensorSamplingFrequency.toDisplayString(context: Context): String {
    return when (this) {
        SensorSamplingFrequency.FrequencyHz125 -> context.getString(R.string.frequency_hz_125)
        SensorSamplingFrequency.FrequencyHz250 -> context.getString(R.string.frequency_hz_250)
        SensorSamplingFrequency.FrequencyHz500 -> context.getString(R.string.frequency_hz_500)
        SensorSamplingFrequency.FrequencyHz1000 -> context.getString(R.string.frequency_hz_1000)
        SensorSamplingFrequency.FrequencyHz2000 -> context.getString(R.string.frequency_hz_2000)
        else -> context.getString(R.string.unknown_frequency)
    }
}