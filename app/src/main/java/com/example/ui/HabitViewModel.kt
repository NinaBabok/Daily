package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.DayProgress
import com.example.data.model.HabitCompletionEntity
import com.example.data.model.HabitEntity
import com.example.data.model.HabitItem
import com.example.data.model.OverallStats
import com.example.data.repository.HabitRepository
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

data class HabitUiState(
    val selectedDate: String = LocalDate.now().toString(),
    val selectedFilter: String = "ALL", // ALL, MORNING, AFTERNOON, EVENING
    val habitItems: List<HabitItem> = emptyList(),
    val filteredHabitItems: List<HabitItem> = emptyList(),
    val dayProgressList: List<DayProgress> = emptyList(),
    val stats: OverallStats = OverallStats(0, 0, 0, 0f, 0, null),
    val isAddEditDialogOpen: Boolean = false,
    val habitToEdit: HabitEntity? = null,
    val selectedHabitForDetails: HabitItem? = null,
    val isAdFree: Boolean = false,
    val isDarkTheme: Boolean = false
)

private data class DataState(
    val habits: List<HabitEntity>,
    val completions: List<HabitCompletionEntity>
)

class HabitViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HabitRepository
    private val selectedDateFlow = MutableStateFlow(LocalDate.now().toString())
    private val selectedFilterFlow = MutableStateFlow("ALL")
    private val isAddEditDialogOpenFlow = MutableStateFlow(false)
    private val habitToEditFlow = MutableStateFlow<HabitEntity?>(null)
    private val selectedHabitForDetailsFlow = MutableStateFlow<HabitItem?>(null)

    private val sharedPrefs = application.getSharedPreferences("dnevne_navike_prefs", Context.MODE_PRIVATE)
    private val isAdFreeFlow = MutableStateFlow(sharedPrefs.getBoolean("key_is_ad_free", false))
    private val isDarkThemeFlow = MutableStateFlow(sharedPrefs.getBoolean("key_is_dark_theme", false))

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = HabitRepository(database.habitDao())
    }

    private val dialogStateFlow = combine(
        isAddEditDialogOpenFlow,
        habitToEditFlow,
        selectedHabitForDetailsFlow,
        combine(isAdFreeFlow, isDarkThemeFlow) { adFree, darkTheme -> Pair(adFree, darkTheme) }
    ) { isOpen, toEdit, details, (adFree, darkTheme) ->
        DialogState(isOpen, toEdit, details, adFree, darkTheme)
    }

    private data class DialogState(
        val isOpen: Boolean,
        val habitToEdit: HabitEntity?,
        val selectedForDetails: HabitItem?,
        val isAdFree: Boolean,
        val isDarkTheme: Boolean
    )

    private val dataStateFlow = combine(
        repository.allHabits,
        repository.allCompletions
    ) { habits, completions ->
        DataState(habits, completions)
    }

    val uiState: StateFlow<HabitUiState> = combine(
        selectedDateFlow,
        selectedFilterFlow,
        dataStateFlow,
        dialogStateFlow
    ) { date, filter, data, dialog ->
        val habits = data.habits
        val completions = data.completions
        val completionsByHabit = completions.groupBy { it.habitId }

        val habitItems = habits.map { habit ->
            val habitCompletions = completionsByHabit[habit.id] ?: emptyList()
            val dateSet = habitCompletions.map { it.dateString }.toSet()
            val isCompleted = dateSet.contains(date)
            val (currentStreak, bestStreak) = calculateStreaks(dateSet, date)

            HabitItem(
                habit = habit,
                isCompletedToday = isCompleted,
                currentStreak = currentStreak,
                bestStreak = bestStreak,
                totalCompletions = dateSet.size,
                completedDates = dateSet
            )
        }

        val filteredItems = when (filter) {
            "MORNING" -> habitItems.filter { it.habit.timeOfDay == "MORNING" }
            "AFTERNOON" -> habitItems.filter { it.habit.timeOfDay == "AFTERNOON" }
            "EVENING" -> habitItems.filter { it.habit.timeOfDay == "EVENING" }
            else -> habitItems
        }

        val dayProgressList = generateDayProgressList(date, habits, completions)

        val todayString = LocalDate.now().toString()
        val todayHabitItems = habits.map { habit ->
            val habitCompletions = completionsByHabit[habit.id] ?: emptyList()
            habitCompletions.any { it.dateString == todayString }
        }
        val todayCompletedCount = todayHabitItems.count { it }
        val totalActive = habits.size
        val completionRate = if (totalActive > 0) todayCompletedCount.toFloat() / totalActive.toFloat() else 0f

        val bestOverallStreak = habitItems.maxOfOrNull { it.bestStreak } ?: 0
        val habitWithLongestStreak = habitItems
            .filter { it.currentStreak > 0 }
            .maxByOrNull { it.currentStreak }
            ?.habit
            ?.title

        val stats = OverallStats(
            totalActiveHabits = totalActive,
            todayCompletedHabits = todayCompletedCount,
            totalCompletionsAllTime = completions.size,
            overallCompletionRate = completionRate,
            bestOverallStreak = bestOverallStreak,
            activeHabitWithLongestStreak = habitWithLongestStreak
        )

        val updatedDetailsHabit = dialog.selectedForDetails?.let { currentDetails ->
            habitItems.find { it.habit.id == currentDetails.habit.id }
        }

        HabitUiState(
            selectedDate = date,
            selectedFilter = filter,
            habitItems = habitItems,
            filteredHabitItems = filteredItems,
            dayProgressList = dayProgressList,
            stats = stats,
            isAddEditDialogOpen = dialog.isOpen,
            habitToEdit = dialog.habitToEdit,
            selectedHabitForDetails = updatedDetailsHabit,
            isAdFree = dialog.isAdFree,
            isDarkTheme = dialog.isDarkTheme
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HabitUiState()
    )

    val habits: StateFlow<List<HabitItem>> = repository.allHabits
        .combine(repository.allCompletions) { habits, completions ->
            val completionsByHabit = completions.groupBy { it.habitId }
            val todayStr = LocalDate.now().toString()
            habits.map { habit ->
                val habitCompletions = completionsByHabit[habit.id] ?: emptyList()
                val dateSet = habitCompletions.map { it.dateString }.toSet()
                val (currentStreak, bestStreak) = calculateStreaks(dateSet)
                HabitItem(
                    habit = habit,
                    isCompletedToday = dateSet.contains(todayStr),
                    currentStreak = currentStreak,
                    bestStreak = bestStreak,
                    totalCompletions = dateSet.size,
                    completedDates = dateSet
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectDate(dateString: String) {
        selectedDateFlow.value = dateString
    }

    fun selectFilter(filter: String) {
        selectedFilterFlow.value = filter
    }

    fun toggleHabitCompletion(habitId: Long) {
        val currentDate = selectedDateFlow.value
        viewModelScope.launch {
            repository.toggleHabitCompletion(habitId, currentDate)
        }
    }

    fun openAddHabitDialog() {
        habitToEditFlow.value = null
        isAddEditDialogOpenFlow.value = true
    }

    fun openEditHabitDialog(habit: HabitEntity) {
        habitToEditFlow.value = habit
        isAddEditDialogOpenFlow.value = true
    }

    fun closeAddEditDialog() {
        isAddEditDialogOpenFlow.value = false
        habitToEditFlow.value = null
    }

    fun saveHabit(
        title: String,
        description: String,
        iconEmoji: String,
        colorHex: String,
        timeOfDay: String,
        frequency: String
    ) {
        viewModelScope.launch {
            val existing = habitToEditFlow.value
            if (existing != null) {
                repository.updateHabit(
                    existing.copy(
                        title = title.trim(),
                        description = description.trim(),
                        iconEmoji = iconEmoji,
                        colorHex = colorHex,
                        timeOfDay = timeOfDay,
                        frequency = frequency
                    )
                )
            } else {
                repository.insertHabit(
                    HabitEntity(
                        title = title.trim(),
                        description = description.trim(),
                        iconEmoji = iconEmoji,
                        colorHex = colorHex,
                        timeOfDay = timeOfDay,
                        frequency = frequency
                    )
                )
            }
            closeAddEditDialog()
        }
    }

    fun addHabitByName(habitName: String) {
        val trimmed = habitName.trim()
        if (trimmed.isEmpty()) return

        // Pick an emoji based on habit name keywords
        val lower = trimmed.lowercase()
        val emoji = when {
            lower.contains("vod") || lower.contains("water") || lower.contains("čaj") || lower.contains("pij") -> "💧"
            lower.contains("trč") || lower.contains("hod") || lower.contains("šet") || lower.contains("run") || lower.contains("korak") -> "🏃"
            lower.contains("knjig") || lower.contains("čita") || lower.contains("read") || lower.contains("uči") -> "📖"
            lower.contains("vježb") || lower.contains("trening") || lower.contains("fit") || lower.contains("gym") -> "💪"
            lower.contains("spav") || lower.contains("san") || lower.contains("sleep") || lower.contains("krevet") -> "🌙"
            lower.contains("medit") || lower.contains("dah") || lower.contains("mir") || lower.contains("mind") -> "🧘"
            lower.contains("jabuk") || lower.contains("hran") || lower.contains("voć") || lower.contains("diet") -> "🍎"
            lower.contains("financ") || lower.contains("novac") || lower.contains("šted") -> "💰"
            else -> "🎯"
        }

        viewModelScope.launch {
            repository.insertHabit(
                HabitEntity(
                    title = trimmed,
                    description = "",
                    iconEmoji = emoji,
                    colorHex = "#4F46E5",
                    timeOfDay = "ANYTIME",
                    frequency = "DAILY"
                )
            )
        }
    }

    fun deleteHabit(habitId: Long) {
        viewModelScope.launch {
            repository.deleteHabit(habitId)
            if (selectedHabitForDetailsFlow.value?.habit?.id == habitId) {
                selectedHabitForDetailsFlow.value = null
            }
        }
    }

    fun selectHabitForDetails(habitItem: HabitItem?) {
        selectedHabitForDetailsFlow.value = habitItem
    }

    private fun generateDayProgressList(
        selectedDate: String,
        habits: List<HabitEntity>,
        completions: List<HabitCompletionEntity>
    ): List<DayProgress> {
        val today = LocalDate.now()
        val totalHabitsCount = habits.size
        val completionsByDate = completions.groupBy { it.dateString }

        val hrLocale = Locale("hr", "HR")

        // 10 days before today + today + 3 days ahead
        val days = (-10..3).map { offset ->
            val date = today.plusDays(offset.toLong())
            val dateStr = date.toString()
            val dayCompletions = completionsByDate[dateStr] ?: emptyList()
            val completedCount = dayCompletions.size
            val ratio = if (totalHabitsCount > 0) (completedCount.toFloat() / totalHabitsCount.toFloat()).coerceIn(0f, 1f) else 0f

            val dayLabel = when {
                date == today -> "Danas"
                date == today.minusDays(1) -> "Jučer"
                date == today.plusDays(1) -> "Sutra"
                else -> date.dayOfWeek.getDisplayName(TextStyle.SHORT, hrLocale).replaceFirstChar { it.uppercase() }
            }

            DayProgress(
                dateString = dateStr,
                dayOfWeekLabel = dayLabel,
                dayOfMonth = date.dayOfMonth,
                isToday = date == today,
                isSelected = dateStr == selectedDate,
                completionRatio = ratio,
                completedCount = completedCount,
                totalCount = totalHabitsCount
            )
        }
        return days
    }

    private fun calculateStreaks(completedDates: Set<String>, referenceDateStr: String? = null): Pair<Int, Int> {
        if (completedDates.isEmpty()) return Pair(0, 0)
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        val parsedDates = completedDates.mapNotNull {
            try { LocalDate.parse(it, formatter) } catch (e: Exception) { null }
        }.toSet()

        if (parsedDates.isEmpty()) return Pair(0, 0)

        val today = LocalDate.now()
        val refDate = referenceDateStr?.let {
            try { LocalDate.parse(it, formatter) } catch (e: Exception) { today }
        } ?: today

        var currentStreak = 0

        if (refDate == today) {
            // When inspecting today:
            // 1. If today is completed, streak counts backwards consecutively starting from today.
            // 2. If today is not completed yet, streak shows the unbroken run ending yesterday.
            val startCheck = when {
                parsedDates.contains(today) -> today
                parsedDates.contains(today.minusDays(1)) -> today.minusDays(1)
                else -> null
            }
            if (startCheck != null) {
                var curr: LocalDate = startCheck
                while (parsedDates.contains(curr)) {
                    currentStreak++
                    curr = curr.minusDays(1)
                }
            }
        } else {
            // When inspecting a specific day (e.g. yesterday, 2 days ago):
            // The streak as of that day counts consecutively backwards from that day IF that day was completed.
            if (parsedDates.contains(refDate)) {
                var curr = refDate
                while (parsedDates.contains(curr)) {
                    currentStreak++
                    curr = curr.minusDays(1)
                }
            } else {
                currentStreak = 0
            }
        }

        val sortedAscending = parsedDates.sorted()
        var bestStreak = 0
        var tempStreak = 0
        var prevDate: LocalDate? = null

        for (date in sortedAscending) {
            if (prevDate == null) {
                tempStreak = 1
            } else if (date == prevDate.plusDays(1)) {
                tempStreak++
            } else if (date == prevDate) {
                // duplicate
            } else {
                tempStreak = 1
            }
            if (tempStreak > bestStreak) {
                bestStreak = tempStreak
            }
            prevDate = date
        }

        return Pair(currentStreak, maxOf(bestStreak, currentStreak))
    }

    fun unlockAdFree() {
        setAdFree(true)
    }

    fun setAdFree(adFree: Boolean) {
        sharedPrefs.edit().putBoolean("key_is_ad_free", adFree).apply()
        isAdFreeFlow.value = adFree
    }

    fun toggleDarkTheme() {
        setDarkTheme(!isDarkThemeFlow.value)
    }

    fun setDarkTheme(isDark: Boolean) {
        sharedPrefs.edit().putBoolean("key_is_dark_theme", isDark).apply()
        isDarkThemeFlow.value = isDark
    }
}
