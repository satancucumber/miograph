package com.callibri.miograph.screens.envelope

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

    fun onStartClicked() {
        if (started.get()) {
            CallibriController.stopEnvelope()
            isSessionCompleted.set(true)
        } else {
            recordedData.clear()
            sampleIndex = 0
            startTime = System.currentTimeMillis()
            val sensorName = CallibriController.currentSensorInfo?.name ?: "Unknown"
            val sensorAddress = CallibriController.currentSensorInfo?.address ?: "Unknown"
            CallibriController.startEnvelope { envelopeDataArray ->
                val samplesList = envelopeDataArray.map { it.sample }
                samples.postValue(samplesList)

                envelopeDataArray.forEach { envelopeData ->
                    val timestamp = startTime + sampleIndex // 1 ms per sample at 1000Hz
                    recordedData.add(SensorData(sensorName, sensorAddress, timestamp, envelopeData.sample))
                    sampleIndex++
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

            // Создаем файл в папке Downloads
            val file = File(downloadsDir, fileName)

            // Записываем данные
            FileWriter(file).use { writer ->
                writer.write("Sensor Name,Address,Time (ms),Value\n")
                recordedData.forEach { data ->
                    writer.write("${data.sensorName},${data.sensorAddress},${data.timestamp},${data.value}\n")
                }
            }

            // Обновляем медиа-сканер чтобы файл сразу был виден
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

    fun close(){
        CallibriController.stopEnvelope()
    }
}