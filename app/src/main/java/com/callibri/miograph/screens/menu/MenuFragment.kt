package com.callibri.miograph.screens.menu

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
import com.callibri.miograph.databinding.FragmentMenuBinding
import com.callibri.miograph.screens.sensorMenu.SensorListAdapter

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class MenuFragment : Fragment() {

    private var _binding: FragmentMenuBinding? = null
    private var _viewModel: MenuViewModel? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val viewModel get() = _viewModel!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        _viewModel = ViewModelProvider(this).get(MenuViewModel::class.java)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel

        binding.buttonSearch.setOnClickListener {
            findNavController().navigate(R.id.action_MenuFragment_to_SearchFragment)
        }

        val adapter = SensorListAdapter().apply {
            submitList(viewModel.connectedSensors)
            setOnItemClickListener { sensor ->
                // Сохраняем выбранный сенсор
                CallibriController.currentSensorInfo = sensor
                // Переходим в меню сенсора
                findNavController().navigate(R.id.action_MenuFragment_to_SensorMenuFragment)
            }
        }

        binding.sensorsList.adapter = adapter
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