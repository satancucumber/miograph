package com.callibri.miograph.screens.emg

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.callibri.miograph.R
import com.callibri.miograph.callibri.CallibriController
import com.callibri.miograph.callibri.toFloat
import com.callibri.miograph.utils.PlotHolder
import com.callibri.miograph.databinding.FragmentEmgBinding
import com.neurosdk2.neuro.types.SensorSamplingFrequency
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EMGFragment : Fragment() {

    companion object {
        fun newInstance() = EMGFragment()
    }

    private var _binding: FragmentEmgBinding? = null
    private var _viewModel: EMGViewModel? = null

    private val binding get() = _binding!!
    private val viewModel get() = _viewModel!!

    private var plot: PlotHolder? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmgBinding.inflate(inflater, container, false)
        _viewModel = ViewModelProvider(this)[EMGViewModel::class.java]

        binding.viewModel = viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Настройка Spinner
        val frequencies = listOf(
            SensorSamplingFrequency.FrequencyHz125,
            SensorSamplingFrequency.FrequencyHz250,
            SensorSamplingFrequency.FrequencyHz500,
            SensorSamplingFrequency.FrequencyHz1000,
            SensorSamplingFrequency.FrequencyHz2000
        )

        val spinner = binding.frequencySpinner
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.sampling_frequencies,
            android.R.layout.simple_spinner_item
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinner.adapter = adapter

        // Установка начальной частоты
        val currentFrequency = CallibriController.getSamplingFrequency()
        val initialPosition = frequencies.indexOfFirst { it.toFloat() == currentFrequency }
        spinner.setSelection(initialPosition.coerceAtLeast(0))

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                val selectedFrequency = frequencies[pos]
                CallibriController.setSamplingFrequency(selectedFrequency)
                plot?.stopRender()
                plot?.startRender(selectedFrequency.toFloat(), PlotHolder.ZoomVal.V_AUTO_M_S2, 5.0f)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.emgButton.setOnClickListener {
            viewModel.onStartClicked()
        }
        binding.exportButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                createFile()
            } else {
                viewModel.exportData(requireContext())
            }
        }

        plot = PlotHolder(binding.plotSignal)
        CallibriController.samplingFrequency?.let { plot?.startRender(it, PlotHolder.ZoomVal.V_AUTO_M_S2, 5.0f) }

        val samplesObserver = Observer<List<Double>> { newSamples ->
            plot?.addData(newSamples)
        }
        viewModel.samples.observe(requireActivity(), samplesObserver)

        viewModel.exportStatus.observe(viewLifecycleOwner) { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        viewModel.close()
    }

    private val CREATE_FILE_REQUEST_CODE = 42

    private fun createFile() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/csv"
            putExtra(Intent.EXTRA_TITLE, "miograph_report_${System.currentTimeMillis()}.csv")
        }
        startActivityForResult(intent, CREATE_FILE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CREATE_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                context?.contentResolver?.openOutputStream(uri)?.use { os ->
                    BufferedWriter(OutputStreamWriter(os)).use { writer ->
                        writer.write("Sensor Name,Address,Time,Value\n")
                        viewModel.recordedData.forEach { data ->
                            writer.write("${data.sensorName},${data.sensorAddress},${formatTimestamp(data.timestamp)},${data.value}\n")
                        }
                    }
                    Toast.makeText(context, R.string.report_saved_to, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}