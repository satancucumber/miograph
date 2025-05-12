package com.callibri.miograph

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.callibri.miograph.screens.search.SearchScreenViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SearchScreenUnitTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var context: android.content.Context
    private lateinit var viewModel: SearchScreenViewModel

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        viewModel = SearchScreenViewModel()
    }

    @Test
    fun searchScreenViewModel_navigation_and_error_reset() {
        viewModel.resetConnectionError()
        viewModel.resetNavigateToMenu()
        assertFalse(viewModel.connectionError.value ?: false)
        assertFalse(viewModel.navigateToMenu.value ?: false)
    }
}