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
import com.callibri.miograph.R
import com.callibri.miograph.databinding.FragmentSearchScreenBinding

class SearchScreenFragment : Fragment() {

    private var _binding: FragmentSearchScreenBinding? = null
    lateinit var _viewModel: SearchScreenViewModel
    private val binding get() = _binding!!
    private val viewModel get() = _viewModel!!

    private lateinit var devicesListAdapter: DevicesListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchScreenBinding.inflate(inflater, container, false)
        _viewModel = ViewModelProvider(this)[SearchScreenViewModel::class.java]
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
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
            onConnectClick = { device -> viewModel.connectToDevice(requireContext(), device) }
        )

        binding.searchDevicesList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = devicesListAdapter
        }
    }

    private fun setupObservers() {
        viewModel.devices.observe(viewLifecycleOwner) { devices ->
            devicesListAdapter.updateDevices(devices)
        }

        viewModel.navigateToMenu.observe(viewLifecycleOwner) { navigate ->
            if (navigate) {
                findNavController().popBackStack(R.id.MenuFragment, false)
                viewModel.resetNavigateToMenu()
            }
        }

        viewModel.connectionError.observe(viewLifecycleOwner) { error ->
            if (error) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.connection_failed_message),
                    Toast.LENGTH_SHORT
                ).show()
                viewModel.resetConnectionError()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setupButtonListeners() {
        binding.buttonRestart.setOnClickListener {
            if (!PermissionHelper.HasAllPermissions(requireContext())) {
                PermissionHelper.RequestPermissions(requireContext()) { _, _, _ ->
                    viewModel.onSearchClicked()
                }
            } else {
                viewModel.onSearchClicked()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        viewModel.close()
    }
}