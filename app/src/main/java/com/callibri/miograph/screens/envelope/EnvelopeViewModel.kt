package com.callibri.miograph.screens.envelope

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.callibri.miograph.R
import com.callibri.miograph.callibri.CallibriController
import com.callibri.miograph.data.SensorData
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.ceil
import kotlin.math.round

class EnvelopeViewModel : ViewModel() {
    var started = ObservableBoolean(false)
    val isSessionCompleted = ObservableBoolean(false)
    val exportStatus = MutableLiveData<String>()

    val samples: MutableLiveData<List<Double>> by lazy {
        MutableLiveData<List<Double>>()
    }

    internal var recordedData = mutableListOf<SensorData>()
    private var startTime: Long = 0
    private var sampleIndex: Int = 0

    fun onStartClicked(sampleFrequency: Float) {
        if (started.get()) {
            CallibriController.stopEnvelope()
            isSessionCompleted.set(true)
        } else {
            recordedData.clear()
            sampleIndex = 0 // Сброс счётчика
            startTime = System.currentTimeMillis()
            val sensorName = CallibriController.currentSensorInfo?.name ?: "Unknown"
            val sensorAddress = CallibriController.currentSensorInfo?.address ?: "Unknown"
            CallibriController.startEnvelope { envelopeDataArray ->
                val samplesList = envelopeDataArray.map { it.sample }
                samples.postValue(samplesList)
                envelopeDataArray.forEach { envelopeData ->
                    val timestamp = startTime + sampleIndex // 1 мс на семпл
                    recordedData.add(SensorData(sensorName, sensorAddress, timestamp, envelopeData.sample))
                    sampleIndex += ceil(1000 / sampleFrequency).toInt()
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

    fun close(){
        CallibriController.stopEnvelope()
    }
}