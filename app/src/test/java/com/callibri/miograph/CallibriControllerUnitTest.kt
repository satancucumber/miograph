package com.callibri.miograph

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.callibri.miograph.callibri.CallibriController
import com.callibri.miograph.callibri.toDisplayString
import com.callibri.miograph.callibri.toFloat
import com.neurosdk2.neuro.types.SensorSamplingFrequency
import com.neurosdk2.neuro.types.SensorState
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class CallibriControllerUnitTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        CallibriController.closeSensor()
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
    fun hasDevice_initiallyFalse() {
        assertFalse(CallibriController.hasDevice)
    }

    @Test
    fun fullInfo_withoutSensor_returnsEmptyModel() {
        val model = CallibriController.fullInfo()
        assertTrue(model.parameters.isEmpty())
        assertTrue(model.commands.isEmpty())
        assertTrue(model.features.isEmpty())
        assertEquals("N/A", model.color)
        assertEquals("N/A", model.signalType)
        assertFalse(model.signal)
    }

    @Test
    fun getSamplingFrequency_withoutSensor_returnsZero() {
        assertEquals(0f, CallibriController.getSamplingFrequency(), 0f)
    }

    @Test
    fun setSamplingFrequency_withoutSensor_noExceptionThrown() {
        // Should not throw
        CallibriController.setSamplingFrequency(SensorSamplingFrequency.FrequencyHz500)
    }

    @Test
    fun connectionState_withoutSensor_returnsNull() {
        assertNull(CallibriController.connectionState)
    }

    @Test
    fun closeSensor_afterClose_clearsState() {
        // No sensor; should still be false
        CallibriController.closeSensor()
        assertFalse(CallibriController.hasDevice)
        assertNull(CallibriController.connectionState)
    }

    @Test
    fun startSearch_and_stopSearch_noException() {
        // Simply exercise methods; actual scanner behavior mocked by SDK
        assertNotNull(CallibriController.startSearch { _, _ -> })
        // stopSearch returns Result; ensure no exception
        CallibriController.stopSearch().onFailure { fail("stopSearch threw exception: \$it") }
    }

    @Test
    fun disconnectCurrent_withoutSensor_noException() {
        CallibriController.disconnectCurrent().onFailure { fail("disconnectCurrent threw exception: \$it") }
    }

    @Test
    fun createAndConnect_withNullScanner_reportsOutOfRange() = runBlocking {
        val resultHolder = mutableListOf<SensorState>()
        CallibriController.createAndConnect(context, mock(), onConnectionResult = { resultHolder.add(it) })
        Thread.sleep(100)
        assertTrue(resultHolder.contains(SensorState.StateOutOfRange))
    }
}

