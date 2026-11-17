package com.example.ui.components

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

private const val SUPPORT_ENDPOINT = "https://formsubmit.co/ajax/NikolinaBabok83@gmail.com"

enum class FeedbackCategory(val title: String, val emoji: String) {
    BUG("Prijava greške", "🐛"),
    FEATURE("Prijedlog značajke", "💡"),
    QUESTION("Pitanje ili pomoć", "❓"),
    GENERAL("Opći dojam", "💬")
}

private enum class SubmitState {
    IDLE,
    LOADING,
    SUCCESS,
    ERROR
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FeedbackDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedCategory by remember { mutableStateOf(FeedbackCategory.BUG) }
    var feedbackMessage by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }
    var submitState by remember { mutableStateOf(SubmitState.IDLE) }
    var submitErrorMessage by remember { mutableStateOf<String?>(null) }

    fun sendFeedback() {
        if (feedbackMessage.trim().isEmpty()) {
            validationError = "Molimo unesite opis prije slanja."
            return
        }

        submitState = SubmitState.LOADING
        validationError = null
        submitErrorMessage = null

        coroutineScope.launch {
            try {
                val success = withContext(Dispatchers.IO) {
                    val url = URL(SUPPORT_ENDPOINT)
                    val connection = (url.openConnection() as HttpURLConnection).apply {
                        requestMethod = "POST"
                        connectTimeout = 15000
                        readTimeout = 15000
                        doInput = true
                        doOutput = true
                        setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                        setRequestProperty("Accept", "application/json")
                    }

                    val payload = JSONObject().apply {
                        put("email", userEmail.trim().ifEmpty { "korisnik.aplikacije@daily.app" })
                        put("_subject", "[Daily App] ${selectedCategory.title} (${selectedCategory.emoji})")
                        put("kategorija", "${selectedCategory.title} ${selectedCategory.emoji}")
                        put("poruka", feedbackMessage.trim())
                        put("korisnicki_email_ili_kontakt", userEmail.trim().ifEmpty { "Nije navedeno" })
                        put("uredaj", "${Build.MANUFACTURER} ${Build.MODEL}")
                        put("android_verzija", "${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
                        put("aplikacija", "Daily Habit Tracker v1.0")
                        put("_template", "table")
                        put("_captcha", "false")
                    }

                    OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                        writer.write(payload.toString())
                        writer.flush()
                    }

                    val responseCode = connection.responseCode
                    if (responseCode in 200..299) {
                        val response = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
                        connection.disconnect()
                        true
                    } else {
                        connection.disconnect()
                        false
                    }
                }

                if (success) {
                    submitState = SubmitState.SUCCESS
                } else {
                    submitState = SubmitState.ERROR
                    submitErrorMessage = "Slanje nije uspjelo. Provjerite internetsku vezu i pokušajte ponovno."
                }
            } catch (e: Exception) {
                submitState = SubmitState.ERROR
                submitErrorMessage = "Došlo je do greške u povezivanju (${e.localizedMessage ?: "Mrežna greška"})."
            }
        }
    }

    AlertDialog(
        onDismissRequest = {
            if (submitState != SubmitState.LOADING) onDismiss()
        },
        modifier = modifier.testTag("feedback_dialog"),
        shape = RoundedCornerShape(28.dp),
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (submitState == SubmitState.SUCCESS) {
                    // Success View
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F5E9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = Color(0xFF2E7D32)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Poruka je poslana!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Hvala vam na javljanju. Vaša poruka je automatski zaprimljena te ćemo je pregledati u najkraćem mogućem roku.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                } else {
                    // Form View
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Feedback,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Povratne informacije & Pomoć",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Nešto ne radi kako treba ili imaš prijedlog? Napiši nam poruku i automatski ćemo je zaprimiti.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                    )

                    // Error banner if any
                    AnimatedVisibility(
                        visible = submitState == SubmitState.ERROR,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = submitErrorMessage ?: "Došlo je do greške.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }

                    // Category Selection Chips
                    Text(
                        text = "Odaberi temu:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FeedbackCategory.values().forEach { category ->
                            val isSelected = selectedCategory == category
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .clickable(enabled = submitState != SubmitState.LOADING) { selectedCategory = category }
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outlineVariant,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(text = category.emoji, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = category.title,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Message Text Field
                    OutlinedTextField(
                        value = feedbackMessage,
                        onValueChange = {
                            feedbackMessage = it
                            if (validationError != null) validationError = null
                        },
                        enabled = submitState != SubmitState.LOADING,
                        label = { Text("Opiši problem ili prijedlog...") },
                        placeholder = {
                            Text(
                                when (selectedCategory) {
                                    FeedbackCategory.BUG -> "Npr. 'Niz se nije povećao...' ili 'Ne mogu urediti naviku...'"
                                    FeedbackCategory.FEATURE -> "Npr. 'Bilo bi super dodati novi podsjetnik...'"
                                    else -> "Tvoja poruka ili pitanje..."
                                },
                                fontSize = 12.sp
                            )
                        },
                        minLines = 4,
                        maxLines = 6,
                        shape = RoundedCornerShape(14.dp),
                        isError = validationError != null,
                        supportingText = validationError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("feedback_message_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // User Email optional field
                    OutlinedTextField(
                        value = userEmail,
                        onValueChange = { userEmail = it },
                        enabled = submitState != SubmitState.LOADING,
                        label = { Text("Tvoj e-mail (za odgovor)") },
                        placeholder = { Text("npr. korisnik@email.com", fontSize = 12.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("feedback_contact_input")
                    )
                }
            }
        },
        confirmButton = {
            if (submitState == SubmitState.SUCCESS) {
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("close_feedback_success_button")
                ) {
                    Text(text = "U redu", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = { sendFeedback() },
                    enabled = submitState != SubmitState.LOADING,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("send_feedback_button")
                ) {
                    if (submitState == SubmitState.LOADING) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Slanje...")
                    } else {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Pošalji poruku", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        dismissButton = {
            if (submitState != SubmitState.SUCCESS) {
                TextButton(
                    onClick = onDismiss,
                    enabled = submitState != SubmitState.LOADING,
                    modifier = Modifier.testTag("cancel_feedback_button")
                ) {
                    Text(text = "Odustani")
                }
            }
        }
    )
}
