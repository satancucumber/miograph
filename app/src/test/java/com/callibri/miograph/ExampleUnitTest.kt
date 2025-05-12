package com.callibri.miograph

import org.junit.Test

import org.junit.Assert.*

import android.os.Looper
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.callibri.miograph.callibri.CallibriController
import com.callibri.miograph.callibri.toDisplayString
import com.callibri.miograph.callibri.toFloat
import com.callibri.miograph.screens.emg.EMGViewModel
import com.callibri.miograph.screens.info.InfoViewModel
import com.callibri.miograph.screens.search.SearchScreenViewModel
import com.neurosdk2.neuro.types.SensorSamplingFrequency
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows

/**
 * Local unit tests for pure logic and ViewModels
 */
@RunWith(RobolectricTestRunner::class)
class ExampleUnitTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var context: android.content.Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun sensorSamplingFrequency_toFloat_correct() {
        assertEquals(125f, SensorSamplingFrequency.FrequencyHz125.toFloat())
        assertEquals(250f, SensorSamplingFrequency.FrequencyHz250.toFloat())
        assertEquals(0f, SensorSamplingFrequency.FrequencyUnsupported.toFloat())
    }

    @Test
    fun sensorSamplingFrequency_toDisplayString_correct() {
        val s125 = SensorSamplingFrequency.FrequencyHz125.toDisplayString(context)
        assertTrue(s125.contains("125"))
        val unknown = SensorSamplingFrequency.FrequencyUnsupported.toDisplayString(context)
        assertTrue(unknown.isNotEmpty())
    }

    @Test
    fun minMaxArrayHelper_basicOperations() {
        val helper = com.callibri.miograph.utils.MinMaxArrayHelper(3)
        helper.addValue(5)
        helper.addValue(2)
        helper.addValue(8)
        assertEquals(2.0, (helper.min as Number).toDouble(), 0.0)
        assertEquals(8.0, (helper.max as Number).toDouble(), 0.0)
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
    fun infoViewModel_loadSensorInfo_postsFullInfo() {
        val dummyInfo = mock<com.callibri.miograph.data.SensorInfoModel>()
        mockStatic(CallibriController::class.java).use { mocked ->
            whenever(CallibriController.fullInfo()).thenReturn(dummyInfo)
            val vm = InfoViewModel()
            vm.loadSensorInfo()  // здесь используется postValue внутри
            // прогоняем main-лоупер, чтобы postValue отработал
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            assertEquals(dummyInfo, vm.sensorInfo.getOrAwaitValue())
        }
    }

    @Test
    fun searchScreenViewModel_navigation_and_error_reset() {
        val vm = SearchScreenViewModel()
        vm.resetConnectionError()
        vm.resetNavigateToMenu()
        assertFalse(vm.connectionError.value ?: false)
        assertFalse(vm.navigateToMenu.value ?: false)
    }

    // ----------------------------
    // LiveData testing helper
    // ----------------------------
    private fun <T> androidx.lifecycle.LiveData<T>.getOrAwaitValue(
        time: Long = 2, unit: java.util.concurrent.TimeUnit = java.util.concurrent.TimeUnit.SECONDS
    ): T {
        var data: T? = null
        val latch = java.util.concurrent.CountDownLatch(1)
        val observer = object : androidx.lifecycle.Observer<T> {
            override fun onChanged(value: T) {
                data = value
                latch.countDown()
                this@getOrAwaitValue.removeObserver(this)
            }
        }
        this.observeForever(observer)
        if (!latch.await(time, unit)) {
            this.removeObserver(observer)
            throw java.util.concurrent.TimeoutException("LiveData value was never set.")
        }
        @Suppress("UNCHECKED_CAST")
        return data as T
    }
}