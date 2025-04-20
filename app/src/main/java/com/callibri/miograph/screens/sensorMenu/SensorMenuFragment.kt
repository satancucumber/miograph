package com.callibri.miograph.screens.sensorMenu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.neurosdk2.neuro.types.SensorState
import com.callibri.miograph.R
import com.callibri.miograph.callibri.CallibriController
import com.callibri.miograph.databinding.FragmentSensorMenuBinding

class SensorMenuFragment : Fragment() {
    private var _binding: FragmentSensorMenuBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: SensorMenuViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSensorMenuBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[SensorMenuViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel

        binding.buttonInfo.setOnClickListener {
            if(CallibriController.connectionState == SensorState.StateInRange){
                findNavController().navigate(R.id.action_SensorMenuFragment_to_infoFragment)
            }
            else {
                Toast.makeText(requireActivity(), "Connect to device first!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonEnvelope.setOnClickListener {
            findNavController().navigate(R.id.action_SensorMenuFragment_to_envelopeFragment)
        }

        binding.buttonCurrentReconect.setOnClickListener {
            if(CallibriController.hasDevice) viewModel.reconnect()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()

        viewModel.updateDeviceInfo()
    }
}