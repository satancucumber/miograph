package com.callibri.miograph

import android.os.Looper
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.callibri.miograph.callibri.CallibriController
import com.callibri.miograph.data.SensorInfoModel
import com.callibri.miograph.screens.info.InfoViewModel
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class InfoViewModelUnitTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var viewModel: InfoViewModel
    private val mockObserver = mock(Observer::class.java) as Observer<SensorInfoModel>

    @Before
    fun setup() {
        viewModel = InfoViewModel()
        viewModel.sensorInfo.observeForever(mockObserver)
    }

    @Test
    fun infoViewModel_loadSensorInfo_postsFullInfo() {
        val dummyInfo = org.mockito.kotlin.mock<SensorInfoModel>()
        mockStatic(CallibriController::class.java).use { mocked ->
            whenever(CallibriController.fullInfo()).thenReturn(dummyInfo)
            viewModel.loadSensorInfo()
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            assertEquals(dummyInfo, viewModel.sensorInfo.getOrAwaitValue())
        }
    }

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