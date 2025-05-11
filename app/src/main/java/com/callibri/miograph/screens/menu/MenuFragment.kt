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
import kotlin.concurrent.thread

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

        // Инициализация адаптера с добавлением ViewModel и обработчика Info
        devicesListAdapter = DevicesListAdapter(
            devices = mutableListOf(),
            onConnectClick = { reconnect() },
            onDisconnectClick = { reconnect() },
            onInfoClick = { showDeviceInfo() },
            onForgetClick = {
                CallibriController.closeSensor()
                viewModel.updateConnectedDevices()
            },
            isConnectedProvider = { viewModel.connected.get() }
        )

        binding.viewModel = viewModel

        // Настройка RecyclerView
        binding.sensorsList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = devicesListAdapter
        }

        // Кнопки
        binding.buttonSearch.setOnClickListener {
            findNavController().navigate(R.id.action_MenuFragment_to_SearchFragment)
        }
        binding.buttonEnvelope.setOnClickListener {
            findNavController().navigate(R.id.action_MenuFragment_to_emgFragment)
        }

        // Наблюдение за списком устройств
        viewModel.devices.observe(viewLifecycleOwner) { devices ->
            devicesListAdapter.updateDevices(devices)
            devicesListAdapter.notifyDataSetChanged()
        }
    }

    private fun reconnect() {
        // Получаем текущее устройство из ViewModel
        val currentDevice = viewModel.devices.value?.firstOrNull() ?: return

        // Начало процесса
        currentDevice.inProgress = true
        devicesListAdapter.notifyItemChanged(0) // Обновляем первую позицию

        if (CallibriController.connectionState == SensorState.StateInRange) {
            // Процесс отключения
            thread {
                CallibriController.disconnectCurrent()
                activity?.runOnUiThread {
                    currentDevice.inProgress = false
                    viewModel.updateConnectedDevices()
                    devicesListAdapter.notifyItemChanged(0)
                }
            }
        } else {
            // Процесс подключения
            CallibriController.connectCurrent(onConnectionResult = { state ->
                activity?.runOnUiThread {
                    currentDevice.inProgress = false
                    viewModel.connected.set(state == SensorState.StateInRange)
                    viewModel.updateConnectedDevices()
                    devicesListAdapter.notifyItemChanged(0)

                    if (state != SensorState.StateInRange) {
                        Toast.makeText(
                            requireContext(),
                            "Connection failed!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })
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
        if(CallibriController.connectionState == SensorState.StateInRange){
            findNavController().navigate(R.id.action_MenuFragment_to_infoFragment)
        }
        else {
            Toast.makeText(requireActivity(), "Connect to device first!", Toast.LENGTH_SHORT).show()
        }
    }
}