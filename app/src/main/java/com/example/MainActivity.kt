package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.HabitViewModel
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: HabitViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize reminder notification channel
        com.example.util.ReminderManager.createNotificationChannel(this)

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            MyApplicationTheme(darkTheme = uiState.isDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    HomeScreen(
                        uiState = uiState,
                        onDateSelected = viewModel::selectDate,
                        onFilterSelected = viewModel::selectFilter,
                        onToggleHabit = viewModel::toggleHabitCompletion,
                        onOpenAddHabit = viewModel::openAddHabitDialog,
                        onOpenEditHabit = viewModel::openEditHabitDialog,
                        onCloseAddEdit = viewModel::closeAddEditDialog,
                        onSaveHabit = viewModel::saveHabit,
                        onDeleteHabit = viewModel::deleteHabit,
                        onSelectHabitForDetails = viewModel::selectHabitForDetails,
                        onQuickAddHabit = viewModel::addHabitByName,
                        onUnlockAdFree = viewModel::unlockAdFree,
                        onResetAdFree = { viewModel.setAdFree(false) },
                        onToggleTheme = viewModel::toggleDarkTheme
                    )
                }
            }
        }
    }
}
