package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.ui.HabitViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HabitViewModelTest {

    @Test
    fun habitViewModel_initialization_and_quickAdd() = runTest {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = HabitViewModel(app)

        assertNotNull(viewModel.uiState)
        assertNotNull(viewModel.habits)

        // Verify adding by name and toggling does not throw
        viewModel.addHabitByName("Popij čaj")
        viewModel.toggleHabitCompletion(habitId = 1)
        viewModel.selectFilter("MORNING")
        viewModel.selectDate("2026-08-28")
    }
}
