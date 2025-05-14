package com.callibri.miograph

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

import com.callibri.miograph.callibri.toDisplayString
import com.callibri.miograph.screens.emg.EMGFragment
import com.callibri.miograph.screens.search.SearchScreenFragment
import com.neurosdk2.neuro.types.SensorSamplingFrequency


/**
 * Instrumented tests that run on an Android device/emulator.
 */
@RunWith(AndroidJUnit4::class)
class InstrumentedTest {
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

    @Test
    fun testEmgFragmentUIElements() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        scenario.onActivity {
            it.supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment_content_main, EMGFragment.newInstance())
                .commitNow()
        }

        Espresso.onView(ViewMatchers.withId(R.id.frequencySpinner))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.emgButton))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.exportButton))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.plot_signal))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun testSearchFragmentUIElements() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        scenario.onActivity {
            it.supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment_content_main, SearchScreenFragment())
                .commitNow()
        }

        Espresso.onView(ViewMatchers.withId(R.id.searchDevicesList))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.button_restart))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

}