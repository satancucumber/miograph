package com.callibri.miograph

import org.junit.Test

import org.junit.Assert.*

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.callibri.miograph.utils.MinMaxArrayHelper
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Local unit tests for pure logic and ViewModels
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class MinMaxArrayHelperUnitTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var minMaxArrayHelper: MinMaxArrayHelper
    private lateinit var context: android.content.Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        minMaxArrayHelper = MinMaxArrayHelper(3)
    }

    @Test
    fun minMaxArrayHelper_basicOperations() {
        minMaxArrayHelper.addValue(5)
        minMaxArrayHelper.addValue(2)
        minMaxArrayHelper.addValue(8)
        assertEquals(2.0, (minMaxArrayHelper.min as Number).toDouble(), 0.0)
        assertEquals(8.0, (minMaxArrayHelper.max as Number).toDouble(), 0.0)
    }
}