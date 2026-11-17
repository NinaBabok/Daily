package com.example.data.model

data class HabitItem(
    val habit: HabitEntity,
    val isCompletedToday: Boolean,
    val currentStreak: Int,
    val bestStreak: Int,
    val totalCompletions: Int,
    val completedDates: Set<String>
)

data class DayProgress(
    val dateString: String,
    val dayOfWeekLabel: String,
    val dayOfMonth: Int,
    val isToday: Boolean,
    val isSelected: Boolean,
    val completionRatio: Float, // 0f .. 1f
    val completedCount: Int,
    val totalCount: Int
)

data class OverallStats(
    val totalActiveHabits: Int,
    val todayCompletedHabits: Int,
    val totalCompletionsAllTime: Int,
    val overallCompletionRate: Float,
    val bestOverallStreak: Int,
    val activeHabitWithLongestStreak: String?
)
