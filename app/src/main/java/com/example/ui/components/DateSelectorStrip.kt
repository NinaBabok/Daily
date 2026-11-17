package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DayProgress
import java.time.LocalDate

@Composable
fun DateSelectorStrip(
    days: List<DayProgress>,
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val todayString = LocalDate.now().toString()

    // Scroll to today or selected on first composition
    LaunchedEffect(selectedDate) {
        val selectedIndex = days.indexOfFirst { it.dateString == selectedDate }
        if (selectedIndex >= 0) {
            val targetScroll = (selectedIndex - 2).coerceAtLeast(0)
            listState.animateScrollToItem(targetScroll)
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Pregled po danima",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )

            if (selectedDate != todayString) {
                AssistChip(
                    onClick = { onDateSelected(todayString) },
                    label = { Text("Povratak na Danas", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Today,
                            contentDescription = "Danas",
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        labelColor = MaterialTheme.colorScheme.primary
                    ),
                    border = null,
                    modifier = Modifier
                        .height(30.dp)
                        .testTag("back_to_today_chip")
                )
            }
        }

        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("date_strip_row")
        ) {
            items(days, key = { it.dateString }) { day ->
                val isSelected = day.dateString == selectedDate

                val cardBg = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else if (day.isToday) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                }

                val textColor = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                } else if (day.isToday) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }

                val subTextColor = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }

                Box(
                    modifier = Modifier
                        .width(54.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(cardBg)
                        .clickable { onDateSelected(day.dateString) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = day.dayOfWeekLabel,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected || day.isToday) FontWeight.Bold else FontWeight.Normal,
                            color = subTextColor
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = day.dayOfMonth.toString(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Completion indicator dot
                        if (day.totalCount > 0) {
                            val dotColor = if (isSelected) {
                                if (day.completionRatio >= 1f) Color.White else Color.White.copy(alpha = 0.4f)
                            } else {
                                if (day.completionRatio >= 1f) {
                                    MaterialTheme.colorScheme.primary
                                } else if (day.completedCount > 0) {
                                    MaterialTheme.colorScheme.secondary
                                } else {
                                    Color.Transparent
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(dotColor)
                            )
                        } else {
                            Spacer(modifier = Modifier.size(6.dp))
                        }
                    }
                }
            }
        }
    }
}
