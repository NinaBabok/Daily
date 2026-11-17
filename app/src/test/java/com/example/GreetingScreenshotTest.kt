package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.DayProgress
import com.example.data.model.HabitEntity
import com.example.data.model.HabitFrequency
import com.example.data.model.HabitItem
import com.example.data.model.OverallStats
import com.example.data.model.TimeOfDay
import com.example.ui.HabitUiState
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val today = LocalDate.now().toString()
    val mockHabits = listOf(
        HabitItem(
            habit = HabitEntity(id = 1, title = "Popij 2L vode", iconEmoji = "💧", colorHex = "#0284C7"),
            isCompletedToday = true,
            currentStreak = 4,
            bestStreak = 7,
            totalCompletions = 14,
            completedDates = setOf(today)
        ),
        HabitItem(
            habit = HabitEntity(id = 2, title = "Jutarnja šetnja", iconEmoji = "🏃", colorHex = "#10B981", timeOfDay = TimeOfDay.MORNING.name),
            isCompletedToday = false,
            currentStreak = 2,
            bestStreak = 5,
            totalCompletions = 10,
            completedDates = emptySet()
        )
    )

    val mockState = HabitUiState(
        selectedDate = today,
        habitItems = mockHabits,
        filteredHabitItems = mockHabits,
        dayProgressList = listOf(
            DayProgress(today, "Danas", 27, isToday = true, isSelected = true, completionRatio = 0.5f, completedCount = 1, totalCount = 2)
        ),
        stats = OverallStats(2, 1, 24, 0.5f, 7, "Popij 2L vode")
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        HomeScreen(
            uiState = mockState,
            onDateSelected = {},
            onFilterSelected = {},
            onToggleHabit = {},
            onOpenAddHabit = {},
            onOpenEditHabit = {},
            onCloseAddEdit = {},
            onSaveHabit = { _, _, _, _, _, _ -> },
            onDeleteHabit = {},
            onSelectHabitForDetails = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
