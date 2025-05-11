package com.callibri.miograph.screens.menu

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.callibri.miograph.R
import com.callibri.miograph.callibri.CallibriController
import com.callibri.miograph.databinding.FragmentMenuBinding
import com.neurosdk2.neuro.types.SensorState

class MenuFragment : Fragment() {

    private var _binding: FragmentMenuBinding? = null
    private var _viewModel: MenuViewModel? = null
    private val binding get() = _binding!!
    private val viewModel get() = _viewModel!!
    private lateinit var devicesListAdapter: DevicesListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        _viewModel = ViewModelProvider(this).get(MenuViewModel::class.java)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        devicesListAdapter = DevicesListAdapter(
            devices = mutableListOf(),
            onConnectClick = { viewModel.reconnect(devicesListAdapter) },
            onDisconnectClick = { viewModel.reconnect(devicesListAdapter) },
            onInfoClick = { showDeviceInfo() },
            onForgetClick = {
                CallibriController.closeSensor()
                viewModel.updateConnectedDevices()
            },
            isConnectedProvider = { viewModel.connected.get() }
        )

        binding.viewModel = viewModel

        binding.sensorsList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = devicesListAdapter
        }

        binding.buttonSearch.setOnClickListener {
            findNavController().navigate(R.id.action_MenuFragment_to_SearchFragment)
        }
        binding.buttonEnvelope.setOnClickListener {
            findNavController().navigate(R.id.action_MenuFragment_to_emgFragment)
        }

        viewModel.devices.observe(viewLifecycleOwner) { devices ->
            devicesListAdapter.updateDevices(devices)
            devicesListAdapter.notifyDataSetChanged()
        }

        viewModel.connectionError.observe(viewLifecycleOwner) { error ->
            if (error) {
                Toast.makeText(requireContext(), "Connection failed!", Toast.LENGTH_SHORT).show()
                viewModel.resetConnectionError()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateConnectedDevices()
    }

    private fun showDeviceInfo() {
        if(CallibriController.connectionState == SensorState.StateInRange) {
            findNavController().navigate(R.id.action_MenuFragment_to_infoFragment)
        } else {
            Toast.makeText(requireActivity(), "Connect to device first!", Toast.LENGTH_SHORT).show()
        }
    }
}