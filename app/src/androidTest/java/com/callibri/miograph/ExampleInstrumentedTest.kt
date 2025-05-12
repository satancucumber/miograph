package com.callibri.miograph

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

import com.callibri.miograph.callibri.toDisplayString
import com.neurosdk2.neuro.types.SensorSamplingFrequency
import org.junit.Assert.*


/**
 * Instrumented tests that run on an Android device/emulator.
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.callibri.miograph", appContext.packageName)
    }

    @Test
    fun frequencyDisplay_instrumented() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val disp = SensorSamplingFrequency.FrequencyHz500.toDisplayString(appContext)
        assertTrue(disp.contains("500"))
    }
}