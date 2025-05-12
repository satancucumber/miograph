package com.callibri.miograph

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.callibri.miograph.screens.emg.EMGViewModel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class EMGViewModelUnitTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: EMGViewModel

    @Before
    fun setup() {
        viewModel = EMGViewModel()
    }

    @Test
    fun emgViewModel_onStartClicked_and_exportData() = runBlocking {
        val vm = EMGViewModel()
        assertFalse(vm.started.get())
        vm.onStartClicked()
        assertTrue(vm.started.get())
        assertFalse(vm.isSessionCompleted.get())
        vm.onStartClicked()
        assertFalse(vm.started.get())
        assertTrue(vm.isSessionCompleted.get())
    }

    @Test
    fun onStartClicked_togglesState() = runTest {
        assertFalse(viewModel.started.get())

        viewModel.onStartClicked()
        assertTrue(viewModel.started.get())

        viewModel.onStartClicked()
        assertFalse(viewModel.started.get())
    }

    @Test
    fun exportData_withEmptyData_showsError() {
        val mockContext = mock(Context::class.java)
        // Настраиваем мок для возврата нужной строки при запросе R.string.no_data_to_export
        `when`(mockContext.getString(R.string.no_data_to_export)).thenReturn("No data to export")

        viewModel.exportData(mockContext)

        val message = viewModel.exportStatus.value
        assertTrue("Сообщение должно содержать 'No data'", message?.contains("No data") == true)
    }
}