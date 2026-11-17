package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StreakOrange

/**
 * Ultra-slim, docked Bottom AdMob Banner component adhering to standard mobile banner height.
 */
@Composable
fun AdMobBanner(
    isAdFree: Boolean,
    onRemoveAdsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isAdFree) return

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("admob_banner_container"),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Subtle top divider line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp) // Standard 50-52dp mobile banner height
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Distinct Ad Badge
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color(0xFFFFB300), // Google Ads Amber Yellow
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "OGLAS",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1E1E1E)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = "Google AdMob Sponzor",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Podržite besplatnu verziju aplikacije",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Premium "Ukloni reklame" Action Button
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onRemoveAdsClick() }
                        .testTag("remove_ads_button"),
                    color = StreakOrange.copy(alpha = 0.15f),
                    contentColor = StreakOrange
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = StreakOrange
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Ukloni",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = StreakOrange
                        )
                    }
                }
            }
        }
    }
}

/**
 * Premium Dialog to remove ads permanently or toggle ads for testing.
 */
@Composable
fun MonetizationDialog(
    isAdFree: Boolean,
    onDismiss: () -> Unit,
    onUnlockLifetime: () -> Unit,
    onResetAds: () -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("monetization_dialog"),
        shape = RoundedCornerShape(28.dp),
        title = null,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(StreakOrange, Color(0xFFFFBBA6))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = if (isAdFree) "✨" else "⭐", fontSize = 30.sp)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (isAdFree) "Daily Premium Aktivan" else "Daily Premium",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (isAdFree)
                        "Hvala vam! Sve reklame su trajno uklonjene s vašeg računa."
                    else
                        "Uklonite sve reklame i uživajte u čistom sučelju bez ikakvih smetnji.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                // Feature List
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FeatureRow(emoji = "🚫", title = "100% Bez oglasa", subtitle = "Nema donjeg banera ni ometanja")
                        FeatureRow(emoji = "⚡", title = "Čisto i brzo sučelje", subtitle = "Fokus isključivo na tvojim navikama")
                        FeatureRow(emoji = "💖", title = "Jednokratna kupnja", subtitle = "Zauvijek tvoje, bez pretplate")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (!isAdFree) {
                    // Buy Lifetime In-App Purchase
                    Button(
                        onClick = {
                            onUnlockLifetime()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StreakOrange,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("buy_pro_button")
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ukloni reklame (1.99 €)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    // Option to re-enable ads for test purposes
                    Button(
                        onClick = {
                            onResetAds()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("reset_ads_button")
                    ) {
                        Text(
                            text = "Prikaži opet reklame (za test)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("close_monetization_button")
            ) {
                Text(text = "Zatvori")
            }
        }
    )
}

@Composable
private fun FeatureRow(
    emoji: String,
    title: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(text = emoji, fontSize = 20.sp)
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
