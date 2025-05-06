package com.callibri.miograph.screens.info

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.callibri.miograph.R
import com.callibri.miograph.databinding.FragmentInfoBinding

class InfoFragment : Fragment() {
    private var _binding: FragmentInfoBinding? = null
    private val binding get() = _binding!!
    private lateinit var parametersAdapter: ParametersAdapter
    private lateinit var commandsAdapter: CommandsAdapter
    private lateinit var featuresAdapter: FeaturesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_info, container, false)
        binding.viewModel = ViewModelProvider(this)[InfoViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
        binding.viewModel?.loadSensorInfo()
    }

    private fun setupRecyclerView() {
        parametersAdapter = ParametersAdapter()
        binding.parametersList.apply {
            adapter = parametersAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        commandsAdapter = CommandsAdapter()
        binding.commandsList.apply {
            adapter = commandsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        featuresAdapter = FeaturesAdapter()
        binding.featuresList.apply {
            adapter = featuresAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupObservers() {
        binding.viewModel?.sensorInfo?.observe(viewLifecycleOwner) { sensorInfo ->
            sensorInfo?.parameters?.let { params ->
                parametersAdapter.submitList(params)
            }
        }

        binding.viewModel?.sensorInfo?.observe(viewLifecycleOwner) { sensorInfo ->
            sensorInfo?.commands?.let { commands ->
                commandsAdapter.submitList(commands)
            }
        }

        binding.viewModel?.sensorInfo?.observe(viewLifecycleOwner) { sensorInfo ->
            sensorInfo?.features?.let { features ->
                featuresAdapter.submitList(features)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}