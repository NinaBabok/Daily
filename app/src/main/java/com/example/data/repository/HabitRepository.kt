package com.example.data.repository

import com.example.data.local.HabitDao
import com.example.data.model.HabitCompletionEntity
import com.example.data.model.HabitEntity
import com.example.data.model.HabitItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HabitRepository(private val habitDao: HabitDao) {

    val allHabits: Flow<List<HabitEntity>> = habitDao.getAllActiveHabits()
    val allCompletions: Flow<List<HabitCompletionEntity>> = habitDao.getAllCompletions()

    /**
     * Combines habits and completions for a given selected date to produce full HabitItem objects.
     */
    fun getHabitItemsForDate(selectedDate: String): Flow<List<HabitItem>> {
        return combine(habitDao.getAllActiveHabits(), habitDao.getAllCompletions()) { habits, completions ->
            val completionsByHabit = completions.groupBy { it.habitId }

            habits.map { habit ->
                val habitCompletions = completionsByHabit[habit.id] ?: emptyList()
                val dateSet = habitCompletions.map { it.dateString }.toSet()
                val isCompleted = dateSet.contains(selectedDate)

                val (currentStreak, bestStreak) = calculateStreaks(dateSet)

                HabitItem(
                    habit = habit,
                    isCompletedToday = isCompleted,
                    currentStreak = currentStreak,
                    bestStreak = bestStreak,
                    totalCompletions = dateSet.size,
                    completedDates = dateSet
                )
            }
        }
    }

    suspend fun toggleHabitCompletion(habitId: Long, dateString: String) {
        val isCompleted = habitDao.isHabitCompleted(habitId, dateString) > 0
        if (isCompleted) {
            habitDao.deleteCompletion(habitId, dateString)
        } else {
            habitDao.insertCompletion(
                HabitCompletionEntity(
                    habitId = habitId,
                    dateString = dateString
                )
            )
        }
    }

    suspend fun insertHabit(habit: HabitEntity): Long {
        return habitDao.insertHabit(habit)
    }

    suspend fun updateHabit(habit: HabitEntity) {
        habitDao.updateHabit(habit)
    }

    suspend fun deleteHabit(habitId: Long) {
        habitDao.deleteHabitById(habitId)
    }

    private fun calculateStreaks(completedDates: Set<String>): Pair<Int, Int> {
        if (completedDates.isEmpty()) return Pair(0, 0)

        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        val parsedDates = completedDates.mapNotNull {
            try {
                LocalDate.parse(it, formatter)
            } catch (e: Exception) {
                null
            }
        }.sortedDescending()

        if (parsedDates.isEmpty()) return Pair(0, 0)

        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        // Calculate current streak
        var currentStreak = 0
        val initialCheckDate = if (parsedDates.contains(today)) today else if (parsedDates.contains(yesterday)) yesterday else null

        if (initialCheckDate != null) {
            var currDate: LocalDate = initialCheckDate
            while (parsedDates.contains(currDate)) {
                currentStreak++
                currDate = currDate.minusDays(1)
            }
        }

        // Calculate best streak historically
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
                // duplicate date, ignore
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
}
