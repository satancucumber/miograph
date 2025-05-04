package com.callibri.miograph.screens.envelope

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import com.callibri.miograph.R
import com.callibri.miograph.callibri.CallibriController
import com.callibri.miograph.databinding.FragmentEnvelopeBinding
import com.callibri.miograph.utils.PlotHolder
import java.io.BufferedWriter
import java.io.OutputStreamWriter

class EnvelopeFragment : Fragment() {

    companion object {
        fun newInstance() = EnvelopeFragment()
    }

    private var _binding: FragmentEnvelopeBinding? = null
    private var _viewModel: EnvelopeViewModel? = null

    private val binding get() = _binding!!
    private val viewModel get() = _viewModel!!

    private var plot: PlotHolder? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEnvelopeBinding.inflate(inflater, container, false)
        _viewModel = ViewModelProvider(this)[EnvelopeViewModel::class.java]

        binding.viewModel = viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.envelopeButton.setOnClickListener { viewModel.onStartClicked() }
        binding.exportButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                createFile()
            } else {
                viewModel.exportData(requireContext())
            }
        }

        plot = PlotHolder(binding.plotSignal)
        plot?.startRender(40.0f, PlotHolder.ZoomVal.V_AUTO_M_S2, 5.0f)

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
                        writer.write("Sensor Name,Address,Time (ms),Value\n")
                        viewModel.recordedData.forEach { data ->
                            writer.write("${data.sensorName},${data.sensorAddress},${data.timestamp},${data.value}\n")
                        }
                    }
                    Toast.makeText(context, R.string.report_saved_to, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

}