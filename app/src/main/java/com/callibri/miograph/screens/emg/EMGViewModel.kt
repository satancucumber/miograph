package com.callibri.miograph.screens.emg

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.callibri.miograph.R
import com.callibri.miograph.callibri.CallibriController
import com.callibri.miograph.data.SensorData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EMGViewModel : ViewModel() {
    private var job: Job? = null
    var started = ObservableBoolean(false)
    val isSessionCompleted = ObservableBoolean(false)
    val exportStatus = MutableLiveData<String>()

    val samples: MutableLiveData<List<Double>> by lazy {
        MutableLiveData<List<Double>>()
    }

    internal var recordedData = mutableListOf<SensorData>()
    private var startTime: Long = 0
    private var sampleIndex: Int = 0

    suspend fun onStartClicked() {
        if (started.get()) {
            job?.cancel()
            CallibriController.stopSignal()
            isSessionCompleted.set(true)
        } else {
            recordedData.clear()
            val sensorName = CallibriController.currentSensorInfo?.name ?: "Unknown"
            val sensorAddress = CallibriController.currentSensorInfo?.address ?: "Unknown"
            val sampleFrequency = CallibriController.getSamplingFrequency()

            job = viewModelScope.launch {
                CallibriController.startSignal { signalDataArray ->
                    val res = arrayListOf<Double>()
                    for (sample in signalDataArray) {
                        res.addAll(sample.samples.toList())
                    }
                    samples.postValue(res)

                    val currentTime = System.currentTimeMillis()
                    val interval = (1000.0 / sampleFrequency).toLong()

                    launch(Dispatchers.Default) {
                        res.forEachIndexed { index, signalData ->
                            val timestamp = currentTime - (signalDataArray.size - index - 1) * interval
                            recordedData.add(
                                SensorData(
                                    sensorName,
                                    sensorAddress,
                                    timestamp,
                                    signalData
                                )
                            )
                        }
                    }
                }
            }
            isSessionCompleted.set(false)
        }
        started.set(!started.get())
    }

    fun exportData(context: Context) {
        if (recordedData.isEmpty()) {
            exportStatus.postValue(context.getString(R.string.no_data_to_export))
            return
        }

        try {
            val fileName = "miograph_report_${System.currentTimeMillis()}.csv"
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

            val file = File(downloadsDir, fileName)

            FileWriter(file).use { writer ->
                writer.write("Sensor Name,Address,Time,Value\n")
                recordedData.forEach { data ->
                    writer.write("${data.sensorName},${data.sensorAddress},${formatTimestamp(data.timestamp)},${data.value}\n")
                }
            }

            MediaScannerConnection.scanFile(
                context,
                arrayOf(file.absolutePath),
                arrayOf("text/csv"),
                null
            )

            exportStatus.postValue(context.getString(R.string.report_saved_to, file.absolutePath))
        } catch (e: Exception) {
            exportStatus.postValue(context.getString(R.string.export_failed, e.localizedMessage))
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    suspend fun close(){
        CallibriController.stopSignal()
    }
}