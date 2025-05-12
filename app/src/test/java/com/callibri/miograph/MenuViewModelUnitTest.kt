package com.callibri.miograph

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.callibri.miograph.screens.menu.MenuViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

@ExperimentalCoroutinesApi
class MenuViewModelUnitTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var viewModel: MenuViewModel

    @Before
    fun setup() {
        viewModel = MenuViewModel()
    }

    @Test
    fun updateConnectedDevices_whenConnected_updatesList() = runTest {
        viewModel.updateConnectedDevices()
        val devices = viewModel.devices.value
        assertTrue(devices?.isEmpty() ?: false)
    }

    @Test
    fun reconnect_changesConnectionState() = runTest {
        viewModel.reconnect(mock())
        assertFalse(viewModel.connected.get())
    }
}