package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TimeOfDay(val label: String, val emoji: String) {
    ALL_DAY("Bilo kada", "⏰"),
    MORNING("Jutro", "🌅"),
    AFTERNOON("Dan", "☀️"),
    EVENING("Večer", "🌙")
}

enum class HabitFrequency(val label: String) {
    DAILY("Svaki dan"),
    WEEKDAYS("Radnim danima"),
    WEEKENDS("Vikendom")
}

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val iconEmoji: String = "✨",
    val colorHex: String = "#10B981", // Emerald default
    val timeOfDay: String = TimeOfDay.ALL_DAY.name,
    val frequency: String = HabitFrequency.DAILY.name,
    val createdAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false
)
