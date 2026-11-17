package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.HabitEntity
import com.example.data.model.HabitFrequency
import com.example.data.model.TimeOfDay

data class HabitTemplate(
    val title: String,
    val description: String,
    val iconEmoji: String,
    val colorHex: String,
    val timeOfDay: String
)

val PRESET_TEMPLATES = listOf(
    HabitTemplate("Popij 2L vode", "Održi hidrataciju kroz dan", "💧", "#0284C7", TimeOfDay.ALL_DAY.name),
    HabitTemplate("10.000 koraka", "Dnevna aktivnost i kretanje", "🏃", "#10B981", TimeOfDay.AFTERNOON.name),
    HabitTemplate("Čitanje 15 min", "Jedno poglavlje omiljene knjige", "📖", "#F59E0B", TimeOfDay.EVENING.name),
    HabitTemplate("Meditacija 10 min", "Smirenje uma i duboko disanje", "🧘", "#8B5CF6", TimeOfDay.MORNING.name),
    HabitTemplate("Zdrav doručak", "Hranjiv obrok za dobar početak", "🥗", "#F43F5E", TimeOfDay.MORNING.name),
    HabitTemplate("Rano na spavanje", "U krevetu prije 23:00", "💤", "#6366F1", TimeOfDay.EVENING.name),
    HabitTemplate("Istezanje / Yoga", "Razgibavanje mišića", "🌿", "#14B8A6", TimeOfDay.MORNING.name),
    HabitTemplate("Učenje novog", "20 min jezika ili vještine", "🧠", "#EC4899", TimeOfDay.AFTERNOON.name)
)

val EMOJI_OPTIONS = listOf(
    "💧", "🏃", "📖", "🧘", "🥗", "💤", "🌿", "🧠", "🍎", "☕", "🚴", "🚶", "🎯", "✍️", "🧹", "💊", "💪", "☀️"
)

val COLOR_OPTIONS = listOf(
    "#0284C7", // Blue
    "#10B981", // Emerald
    "#F59E0B", // Amber
    "#8B5CF6", // Purple
    "#F43F5E", // Rose
    "#14B8A6", // Teal
    "#6366F1", // Indigo
    "#EC4899"  // Pink
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditHabitDialog(
    habitToEdit: HabitEntity?,
    onDismiss: () -> Unit,
    onSave: (title: String, description: String, iconEmoji: String, colorHex: String, timeOfDay: String, frequency: String) -> Unit
) {
    var title by remember { mutableStateOf(habitToEdit?.title ?: "") }
    var description by remember { mutableStateOf(habitToEdit?.description ?: "") }
    var selectedEmoji by remember { mutableStateOf(habitToEdit?.iconEmoji ?: "💧") }
    var selectedColorHex by remember { mutableStateOf(habitToEdit?.colorHex ?: "#10B981") }
    var selectedTimeOfDay by remember { mutableStateOf(habitToEdit?.timeOfDay ?: TimeOfDay.ALL_DAY.name) }
    var selectedFrequency by remember { mutableStateOf(habitToEdit?.frequency ?: HabitFrequency.DAILY.name) }
    var titleError by remember { mutableStateOf(false) }

    val isEditing = habitToEdit != null

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .heightIn(max = 680.dp)
                .testTag("add_edit_habit_dialog"),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEditing) "Uredi Naviku" else "Nova Dnevna Navika",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_add_edit_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Zatvori",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Quick Templates (only when adding new habit)
                    if (!isEditing) {
                        Text(
                            text = "💡 Brzi predlošci (dodirni za odabir):",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PRESET_TEMPLATES.forEach { template ->
                                AssistChip(
                                    label = "${template.iconEmoji} ${template.title}",
                                    onClick = {
                                        title = template.title
                                        description = template.description
                                        selectedEmoji = template.iconEmoji
                                        selectedColorHex = template.colorHex
                                        selectedTimeOfDay = template.timeOfDay
                                        titleError = false
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Habit Name Input
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            if (it.isNotBlank()) titleError = false
                        },
                        label = { Text("Naziv navike *") },
                        placeholder = { Text("npr. Popij 2L vode") },
                        isError = titleError,
                        supportingText = if (titleError) {
                            { Text("Molimo unesite naziv navike", color = MaterialTheme.colorScheme.error) }
                        } else null,
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("habit_title_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Description / Motivating Note
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Kratak opis ili motivacija (neobavezno)") },
                        placeholder = { Text("npr. 8 čaša kroz dan za bolju energiju") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("habit_desc_input")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Icon / Emoji Selector
                    Text(
                        text = "Odaberi ikonu:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        EMOJI_OPTIONS.forEach { emoji ->
                            val isSelected = selectedEmoji == emoji
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    .then(
                                        if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)) else Modifier
                                    )
                                    .clickable { selectedEmoji = emoji },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = emoji, fontSize = 20.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Color Selector
                    Text(
                        text = "Boja teme:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        COLOR_OPTIONS.forEach { hex ->
                            val color = try {
                                Color(android.graphics.Color.parseColor(hex))
                            } catch (e: Exception) {
                                MaterialTheme.colorScheme.primary
                            }
                            val isSelected = selectedColorHex.equals(hex, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .then(
                                        if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape) else Modifier
                                    )
                                    .clickable { selectedColorHex = hex }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Time of Day Selector
                    Text(
                        text = "Doba dana:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TimeOfDay.values().forEach { timeOption ->
                            val isSelected = selectedTimeOfDay == timeOption.name
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedTimeOfDay = timeOption.name },
                                label = { Text("${timeOption.emoji} ${timeOption.label}") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Frequency Selector
                    Text(
                        text = "Učestalost:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        HabitFrequency.values().forEach { freq ->
                            val isSelected = selectedFrequency == freq.name
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedFrequency = freq.name },
                                label = { Text(freq.label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("cancel_habit_button")
                    ) {
                        Text("Odustani")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                titleError = true
                            } else {
                                onSave(
                                    title,
                                    description,
                                    selectedEmoji,
                                    selectedColorHex,
                                    selectedTimeOfDay,
                                    selectedFrequency
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("save_habit_button")
                    ) {
                        Text(if (isEditing) "Spremi promjene" else "Stvori naviku")
                    }
                }
            }
        }
    }
}

@Composable
private fun AssistChip(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}
