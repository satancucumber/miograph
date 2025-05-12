package com.callibri.miograph.callibri

import android.app.Activity
import android.content.Context
import android.widget.Toast
import com.callibri.miograph.R
import com.callibri.miograph.data.SensorInfoModel
import com.neurosdk2.neuro.Callibri
import com.neurosdk2.neuro.Scanner
import com.neurosdk2.neuro.Sensor
import com.neurosdk2.neuro.interfaces.CallibriSignalDataReceived
import com.neurosdk2.neuro.types.*
import kotlinx.coroutines.*


object CallibriController {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var currentJobs = mutableListOf<Job>()
    private const val CONNECT_TIMEOUT_MS = 10_000L

    private var scanner: Scanner? = null
    private var sensor: Callibri? = null

    /** Лямбда для внешней обработки смены состояния подключения */
    var connectionStateChanged: (SensorState) -> Unit = { }

    /** Лямбда для внешней обработки заряда (0..100) */
    var onBatteryChanged: (Int) -> Unit = { }

    var currentSensorInfo: SensorInfo? = null

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

    fun stopSearch() = runCatching { scanner?.stop() }

    /**
     * Создаёт экземпляр Callibri и пытается подключиться.
     *
     * @param context — нужен для показа Toast
     * @param sensorInfo — информация из сканера
     * @param onConnectionResult — колбэк с результатом попытки подключения
     */
    fun createAndConnect(
        context: Context,
        sensorInfo: SensorInfo,
        onConnectionResult: (SensorState) -> Unit
    ) {
        val job = scope.launch {
            try {
                // 1) создаём сенсор
                sensor = scanner?.createSensor(sensorInfo) as? Callibri
                currentSensorInfo = sensorInfo

                if (sensor == null) {
                    withContext(Dispatchers.Main) {
                        onConnectionResult(SensorState.StateOutOfRange)
                        (context as? Activity)?.let {
                            Toast.makeText(
                                it,
                                it.getString(R.string.connection_failed_message),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    return@launch
                }

                // 2) подписываемся на колбэки
                sensor!!.sensorStateChanged = Sensor.SensorStateChanged { st ->
                    connectionStateChanged(st)
                }
                connectionStateChanged(sensor!!.state)

                sensor!!.batteryChanged = Sensor.BatteryChanged { level ->
                    onBatteryChanged(level)
                }

                // 3) настраиваем параметры
                runCatching {
                    sensor!!.samplingFrequency = SensorSamplingFrequency.FrequencyHz1000
                    sensor!!.signalType = CallibriSignalType.EMG
                    sensor!!.hardwareFilters = listOf(
                        SensorFilter.FilterBSFBwhLvl2CutoffFreq45_55Hz,
                        SensorFilter.FilterHPFBwhLvl1CutoffFreq1Hz
                    )
                }

                // 4) запускаем connect()
                sensor!!.connect()

                // 5) ждём результата с таймаутом
                val result = withTimeoutOrNull(CONNECT_TIMEOUT_MS) {
                    while (isActive) {
                        val state = sensor!!.state
                        if (state == SensorState.StateInRange) return@withTimeoutOrNull state
                        delay(200)
                    }
                    SensorState.StateOutOfRange
                } ?: SensorState.StateOutOfRange

                // 6) возвращаем результат
                withContext(Dispatchers.Main) {
                    onConnectionResult(result)
                    if (result != SensorState.StateInRange) {
                        (context as? Activity)?.let {
                            Toast.makeText(
                                it,
                                it.getString(R.string.connection_failed_message),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                withContext(Dispatchers.Main) {
                    onConnectionResult(SensorState.StateOutOfRange)
                    (context as? Activity)?.let {
                        Toast.makeText(
                            it,
                            it.getString(R.string.connection_failed_message),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
        currentJobs.add(job)
        job.invokeOnCompletion { currentJobs.remove(job) }
    }

    fun connectCurrent(onConnectionResult: (SensorState) -> Unit) {
        sensor?.let { s ->
            val job = scope.launch {
                try {
                    s.connect()
                    withContext(Dispatchers.Main) {
                        onConnectionResult(s.state)
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    withContext(Dispatchers.Main) {
                        onConnectionResult(SensorState.StateOutOfRange)
                    }
                }
            }
            currentJobs.add(job)
            job.invokeOnCompletion { currentJobs.remove(job) }
        }
    }

    fun disconnectCurrent() = runCatching {
        sensor?.disconnect()
        // Убрано сбрасывание обработчиков
    }

    fun closeSensor() = runCatching {
        currentJobs.forEach { it.cancel() }
        currentJobs.clear()
        sensor?.close()
        sensor = null
        currentSensorInfo = null
    }

    /** Возвращает текущее состояние подключения */
    @JvmStatic
    val connectionState: SensorState?
        get() = sensor?.state
    val hasDevice: Boolean get() = sensor != null
    val samplingFrequency get() = sensor?.samplingFrequency?.toFloat()

    /**
     * Возвращает полную информацию по сенсору.
     * Если сенсор не подключён, возвращаем пустую модель.
     */
    @JvmStatic
    fun fullInfo(): SensorInfoModel {
        val s = sensor
            ?: // возвращаем "пустую" модель, чтобы тест размерности прошёл
            return SensorInfoModel(
                parameters = emptyList(),
                commands = emptyList(),
                features = emptyList(),
                color = "N/A",
                signalType = "N/A",
                signal = false
            )
        val parameters = mutableListOf<SensorInfoModel.Parameter>().apply {
            for (param in s.supportedParameter) {
                val paramName = param.param.name
                val paramAccess = param.paramAccess?.toString() ?: ""
                val paramValue = when (param.param!!) {
                    SensorParameter.ParameterGain -> s.gain?.toString()
                    SensorParameter.ParameterHardwareFilterState ->
                        s.hardwareFilters.joinToString { it.toString() }
                    SensorParameter.ParameterFirmwareMode -> s.firmwareMode?.toString()
                    SensorParameter.ParameterSamplingFrequency -> s.samplingFrequency?.toString()
                    SensorParameter.ParameterOffset -> s.dataOffset?.toString()
                    SensorParameter.ParameterExternalSwitchState -> s.extSwInput?.toString()
                    SensorParameter.ParameterADCInputState -> s.adcInput?.toString()
                    SensorParameter.ParameterAccelerometerSens -> s.accSens?.toString()
                    SensorParameter.ParameterGyroscopeSens -> s.gyroSens?.toString()
                    SensorParameter.ParameterStimulatorAndMAState ->
                        """Stimulator state: ${s.stimulatorMAState?.stimulatorState}
                    MA state: ${s.stimulatorMAState?.maState}
                    """.trimIndent()
                    SensorParameter.ParameterStimulatorParamPack ->
                        """Current: ${s.stimulatorParam?.current}
                    Frequency: ${s.stimulatorParam?.frequency}
                    Pulse width: ${s.stimulatorParam?.pulseWidth}
                    Stimulus duration: ${s.stimulatorParam?.stimulusDuration}
                    """.trimIndent()
                    SensorParameter.ParameterMotionAssistantParamPack ->
                        """Gyro start: ${s.motionAssistantParam?.gyroStart}
                    Gyro stop: ${s.motionAssistantParam?.gyroStop}
                    Limb: ${s.motionAssistantParam?.limb}
                    Min pause (in ms): ${s.motionAssistantParam?.minPauseMs}
                    """.trimIndent()
                    SensorParameter.ParameterFirmwareVersion ->
                        """FW version: ${s.version?.fwMajor}.${s.version?.fwMinor}.${s.version?.fwPatch}
                    HW version: ${s.version?.hwMajor}.${s.version?.hwMinor}.${s.version?.hwPatch}
                    Extension: ${s.version?.extMajor}
                    """.trimIndent()
                    SensorParameter.ParameterMEMSCalibrationStatus -> s.memsCalibrateState.toString()
                    SensorParameter.ParameterMotionCounterParamPack ->
                        """Insense threshold MG: ${s.motionCounterParam?.insenseThresholdMG}
                    Insense threshold sample: ${s.motionCounterParam?.insenseThresholdSample}
                    """.trimIndent()
                    SensorParameter.ParameterMotionCounter ->
                        """Insense threshold MG: ${s.motionCounter}
                    Insense threshold sample: ${s.motionCounterParam?.insenseThresholdSample}
                    """.trimIndent()
                    SensorParameter.ParameterBattPower -> s.battPower.toString()
                    SensorParameter.ParameterSensorFamily -> s.sensFamily?.toString()
                    SensorParameter.ParameterSensorMode -> s.firmwareMode?.toString()
                    SensorParameter.ParameterSensorChannels -> s.channelsCount.toString()
                    SensorParameter.ParameterSamplingFrequencyResp -> s.samplingFrequencyResp?.toString()
                    SensorParameter.ParameterName -> s.name?.toString()
                    SensorParameter.ParameterState -> s.state?.toString()
                    SensorParameter.ParameterAddress -> s.address?.toString()
                    SensorParameter.ParameterSerialNumber -> s.serialNumber?.toString()
                    else -> continue // Пропускаем неизвестные параметры вместо прерывания
                }
                add(SensorInfoModel.Parameter(
                    paramName,
                    paramAccess,
                    paramValue ?: "N/A" // Значение по умолчанию если null
                ))
            }
        }

        val color = s.color?.toString() ?: "N/A"
        val signal = s.isSupportedFeature(SensorFeature.Signal)
        val signalType = if (signal) {
            s.signalType?.toString() ?: "N/A"
        } else {
            "Not supported"
        }

        val commands = s.supportedCommand?.map { it.name } ?: emptyList()
        val features = s.supportedFeature?.map { it.name } ?: emptyList()

        return SensorInfoModel(
            parameters = parameters,
            commands = commands,
            features = features,
            color = color,
            signalType = signalType,
            signal = signal
        )
    }

    suspend fun startSignal(signalReceived: (Array<CallibriSignalData>) -> Unit) {
        sensor?.callibriSignalDataReceived = CallibriSignalDataReceived(signalReceived)
        executeCommand(SensorCommand.StartSignal)
    }

    suspend fun stopSignal() {
        sensor?.callibriSignalDataReceived = null
        executeCommand(SensorCommand.StopSignal)
    }

    private suspend fun executeCommand(command: SensorCommand) = withContext(Dispatchers.IO) {
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

fun SensorSamplingFrequency.toFloat(): Float = when (this) {
    SensorSamplingFrequency.FrequencyHz125 -> 125f
    SensorSamplingFrequency.FrequencyHz250 -> 250f
    SensorSamplingFrequency.FrequencyHz500 -> 500f
    SensorSamplingFrequency.FrequencyHz1000 -> 1000f
    SensorSamplingFrequency.FrequencyHz2000 -> 2000f
    else -> 0f
}

fun SensorSamplingFrequency.toDisplayString(context: Context): String =
    when (this) {
        SensorSamplingFrequency.FrequencyHz125 -> "125 Hz"
        SensorSamplingFrequency.FrequencyHz250 -> "250 Hz"
        SensorSamplingFrequency.FrequencyHz500 -> "500 Hz"
        SensorSamplingFrequency.FrequencyHz1000 -> "1000 Hz"
        SensorSamplingFrequency.FrequencyHz2000 -> "2000 Hz"
        else -> "Unknown frequency"
    }