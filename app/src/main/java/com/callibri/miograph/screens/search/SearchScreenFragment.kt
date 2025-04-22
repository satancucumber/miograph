package com.callibri.miograph.screens.search

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.neurosdk2.helpers.PermissionHelper
import com.neurosdk2.neuro.types.SensorState
import com.callibri.miograph.R
import com.callibri.miograph.callibri.CallibriController
import com.callibri.miograph.databinding.FragmentSearchScreenBinding

class SearchScreenFragment : Fragment() {

    private var _binding: FragmentSearchScreenBinding? = null
    private var _viewModel: SearchScreenViewModel? = null
    private val binding get() = _binding!!
    private val viewModel get() = _viewModel!!

    private lateinit var devicesListAdapter: DevicesListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchScreenBinding.inflate(inflater, container, false)
        _viewModel = ViewModelProvider(this).get(SearchScreenViewModel::class.java)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupButtonListeners()
    }

    private fun setupRecyclerView() {
        devicesListAdapter = DevicesListAdapter(
            devices = mutableListOf(),
            onConnectClick = { device -> handleConnect(device)}
        )

        binding.searchDevicesList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = devicesListAdapter
        }
    }

    private fun setupObservers() {
        viewModel.sensors.observe(viewLifecycleOwner) { sensors ->
            val deviceList = sensors.map { sensorInfo ->
                DeviceListItem(
                    name = sensorInfo.name,
                    address = sensorInfo.address,
                    inProgress = false,
                    sInfo = sensorInfo
                )
            }
            devicesListAdapter.updateDevices(deviceList)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setupButtonListeners() {
        binding.buttonRestart.setOnClickListener {
            if (!PermissionHelper.HasAllPermissions(requireContext())) {
                PermissionHelper.RequestPermissions(requireContext()) {
                        _, _, _ -> viewModel.onSearchClicked()
                }
            } else {
                viewModel.onSearchClicked()
            }
        }
    }

    private fun handleConnect(device: DeviceListItem) {
        device.inProgress = true
        devicesListAdapter.notifyItemChanged(devicesListAdapter.devices.indexOf(device))

        CallibriController.createAndConnect(device.sInfo) { state ->
            activity?.runOnUiThread {
                device.inProgress = false
                devicesListAdapter.notifyItemChanged(devicesListAdapter.devices.indexOf(device))

                when (state) {
                    SensorState.StateInRange -> {
                        findNavController().popBackStack(R.id.MenuFragment, false)
                    }
                    else -> {
                        Toast.makeText(
                            requireContext(),
                            "Device connection failed!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        viewModel.close()
    }
}