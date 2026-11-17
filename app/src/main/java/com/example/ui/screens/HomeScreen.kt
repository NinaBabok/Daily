package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HabitItem
import com.example.ui.HabitUiState
import com.example.ui.components.AddEditHabitDialog
import com.example.ui.components.AdMobBanner
import com.example.ui.components.DailyReminderDialog
import com.example.ui.components.FeedbackDialog
import com.example.ui.components.DateSelectorStrip
import com.example.ui.components.HabitCard
import com.example.ui.components.HabitDetailsDialog
import com.example.ui.components.HabitProgressSummary
import com.example.ui.components.HabitStatsDialog
import com.example.ui.components.MonetizationDialog
import com.example.ui.theme.StreakOrange
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HabitUiState,
    onDateSelected: (String) -> Unit,
    onFilterSelected: (String) -> Unit,
    onToggleHabit: (Long) -> Unit,
    onOpenAddHabit: () -> Unit,
    onOpenEditHabit: (com.example.data.model.HabitEntity) -> Unit,
    onCloseAddEdit: () -> Unit,
    onSaveHabit: (String, String, String, String, String, String) -> Unit,
    onDeleteHabit: (Long) -> Unit,
    onSelectHabitForDetails: (HabitItem?) -> Unit,
    onQuickAddHabit: (String) -> Unit = {},
    onUnlockAdFree: () -> Unit = {},
    onResetAdFree: () -> Unit = {},
    onToggleTheme: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showStatsDialog by remember { mutableStateOf(false) }
    var showMonetizationDialog by remember { mutableStateOf(false) }
    var showReminderDialog by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    val todayString = LocalDate.now().toString()
    val isViewingToday = uiState.selectedDate == todayString

    val completedCount = uiState.habitItems.count { it.isCompletedToday }
    val totalCount = uiState.habitItems.size

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🔥", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Daily",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                if (uiState.isAdFree) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(StreakOrange)
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "PRO",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = androidx.compose.ui.graphics.Color.White
                                        )
                                    }
                                }
                            }
                            Text(
                                text = if (isViewingToday) "Danas" else uiState.selectedDate,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Free Light / Dark Mode Toggle
                    IconButton(
                        onClick = onToggleTheme,
                        modifier = Modifier.testTag("toggle_theme_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.isDarkTheme) Icons.Default.WbSunny else Icons.Default.DarkMode,
                            contentDescription = if (uiState.isDarkTheme) "Prebaci na svijetlu temu" else "Prebaci na tamnu temu",
                            tint = if (uiState.isDarkTheme) StreakOrange else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Daily Reminder / Notification Settings Button
                    IconButton(
                        onClick = { showReminderDialog = true },
                        modifier = Modifier.testTag("open_reminder_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Dnevni podsjetnik",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Stats Button
                    IconButton(
                        onClick = { showStatsDialog = true },
                        modifier = Modifier.testTag("open_stats_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Equalizer,
                            contentDescription = "Statistika",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Feedback & Help Button
                    IconButton(
                        onClick = { showFeedbackDialog = true },
                        modifier = Modifier.testTag("open_feedback_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Feedback,
                            contentDescription = "Povratne informacije i prijava grešaka",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // PRO / Monetization Button
                    IconButton(
                        onClick = { showMonetizationDialog = true },
                        modifier = Modifier.testTag("open_pro_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "PRO Verzija / Ukloni reklame",
                            tint = if (uiState.isAdFree) StreakOrange else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            if (!uiState.isAdFree) {
                AdMobBanner(
                    isAdFree = uiState.isAdFree,
                    onRemoveAdsClick = { showMonetizationDialog = true }
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onOpenAddHabit,
                icon = { Icon(Icons.Default.Add, contentDescription = "Dodaj") },
                text = { Text("Nova Navika", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.testTag("add_habit_fab")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Horizontal Date Selector Strip
            item {
                DateSelectorStrip(
                    days = uiState.dayProgressList,
                    selectedDate = uiState.selectedDate,
                    onDateSelected = onDateSelected,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            // Progress Summary Card
            item {
                HabitProgressSummary(
                    completedCount = completedCount,
                    totalCount = totalCount,
                    isViewingToday = isViewingToday,
                    stats = uiState.stats
                )
            }

            // Time Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf(
                        "ALL" to "Sve",
                        "MORNING" to "🌅 Jutro",
                        "AFTERNOON" to "☀️ Dan",
                        "EVENING" to "🌙 Večer"
                    )

                    filters.forEach { (key, label) ->
                        val isSelected = uiState.selectedFilter == key
                        FilterChip(
                            selected = isSelected,
                            onClick = { onFilterSelected(key) },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            border = null,
                            modifier = Modifier.testTag("filter_chip_$key")
                        )
                    }
                }
            }

            // Habits List
            if (uiState.filteredHabitItems.isEmpty()) {
                item {
                    EmptyHabitState(
                        filter = uiState.selectedFilter,
                        totalHabitsCount = uiState.habitItems.size,
                        onAddClick = onOpenAddHabit
                    )
                }
            } else {
                items(
                    items = uiState.filteredHabitItems,
                    key = { it.habit.id }
                ) { habitItem ->
                    HabitCard(
                        habitItem = habitItem,
                        onToggle = { onToggleHabit(habitItem.habit.id) },
                        onCardClick = { onSelectHabitForDetails(habitItem) },
                        onEdit = { onOpenEditHabit(habitItem.habit) },
                        onDelete = { onDeleteHabit(habitItem.habit.id) }
                    )
                }
            }
        }
    }

    // Add / Edit Habit Dialog
    if (uiState.isAddEditDialogOpen) {
        AddEditHabitDialog(
            habitToEdit = uiState.habitToEdit,
            onDismiss = onCloseAddEdit,
            onSave = onSaveHabit
        )
    }

    // Habit Details Dialog
    uiState.selectedHabitForDetails?.let { habitItem ->
        HabitDetailsDialog(
            habitItem = habitItem,
            onDismiss = { onSelectHabitForDetails(null) },
            onEdit = { onOpenEditHabit(habitItem.habit) },
            onDelete = { onDeleteHabit(habitItem.habit.id) }
        )
    }

    // Overall Stats Dialog
    if (showStatsDialog) {
        HabitStatsDialog(
            stats = uiState.stats,
            days = uiState.dayProgressList,
            habits = uiState.habitItems,
            onDismiss = { showStatsDialog = false }
        )
    }

    // Daily Reminder / Notifications Dialog
    if (showReminderDialog) {
        DailyReminderDialog(
            onDismiss = { showReminderDialog = false }
        )
    }

    // Feedback & Bug Reporting Dialog
    if (showFeedbackDialog) {
        FeedbackDialog(
            onDismiss = { showFeedbackDialog = false }
        )
    }

    // Monetization / Remove Ads Dialog
    if (showMonetizationDialog) {
        MonetizationDialog(
            isAdFree = uiState.isAdFree,
            onDismiss = { showMonetizationDialog = false },
            onUnlockLifetime = {
                onUnlockAdFree()
            },
            onResetAds = {
                onResetAdFree()
            }
        )
    }
}

@Composable
private fun EmptyHabitState(
    filter: String,
    totalHabitsCount: Int,
    onAddClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .testTag("empty_habit_state"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (totalHabitsCount == 0) "🌱" else "🔍",
                    fontSize = 32.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (totalHabitsCount == 0) "Još nemaš dodanih navika" else "Nema navika za odabrani filter",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (totalHabitsCount == 0) {
                    "Stvori svoju prvu zdravu naviku i počni graditi pobjednički niz već danas!"
                } else {
                    "Odaberi drugi filter ili dodaj novu naviku za ovaj dio dana."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            if (totalHabitsCount == 0) {
                Spacer(modifier = Modifier.height(18.dp))
                ExtendedFloatingActionButton(
                    onClick = onAddClick,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Dodaj prvu naviku") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("empty_state_add_button")
                )
            }
        }
    }
}
