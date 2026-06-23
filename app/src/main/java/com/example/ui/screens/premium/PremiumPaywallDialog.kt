package com.example.ui.screens.premium

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.state.UserState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random
import android.app.Activity
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import com.razorpay.Checkout
import org.json.JSONObject

@Composable
fun PremiumPaywallDialog(onDismiss: () -> Unit) {
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = true)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clip(RoundedCornerShape(32.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFE91E63), Color(0xFFFF9800))
                                )
                            )
                            .padding(24.dp)
                    ) {
                        IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.TopEnd)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Spark Plus", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Unlock your full potential", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
                        }
                    }

                    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        PremiumPerkRow(Icons.Default.Visibility, "See Who Liked You", "Instantly match with people who already like you")
                        PremiumPerkRow(Icons.Default.ChatBubble, "Unlimited Chatting", "No daily messaging limits with your matches")
                        PremiumPerkRow(Icons.Default.Videocam, "Instant Live Video", "Unlock random video and voice calls instantly")
                        PremiumPerkRow(Icons.Default.Star, "Free Profile Boost", "1 free profile boost per week")
                        
                        if (errorMessage != null) {
                            Text(
                                text = errorMessage!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        var selectedTier by remember { mutableStateOf(1) } // 1 for 1 month, 6 for 6 months
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            // 1 Month Tier
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedTier = 1 },
                                shape = RoundedCornerShape(16.dp),
                                border = if (selectedTier == 1) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFE91E63)) else null,
                                colors = CardDefaults.cardColors(containerColor = if (selectedTier == 1) Color(0xFFE91E63).copy(alpha = 0.05f) else MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("1 Month", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("₹499 / mo", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            
                            // 6 Months Tier
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedTier = 6 },
                                shape = RoundedCornerShape(16.dp),
                                border = if (selectedTier == 6) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFE91E63)) else null,
                                colors = CardDefaults.cardColors(containerColor = if (selectedTier == 6) Color(0xFFE91E63).copy(alpha = 0.05f) else MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("6 Months", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("₹299 / mo", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Save 40%", style = MaterialTheme.typography.labelSmall, color = Color(0xFFE91E63), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Button(
                            onClick = {
                                val activity = context as? Activity
                                if (activity != null) {
                                    try {
                                        val checkout = Checkout()
                                        checkout.setKeyID(com.example.BuildConfig.RAZORPAY_KEY_ID) // User configured Key
                                        val options = JSONObject()
                                        options.put("name", "Spark App")
                                        options.put("description", "Premium Subscription")
                                        options.put("theme.color", "#E91E63")
                                        options.put("currency", "INR")
                                        val amount = if (selectedTier == 1) "49900" else "179400"
                                        options.put("amount", amount)
                                        checkout.open(activity, options)
                                        onDismiss()
                                    } catch (e: Exception) {
                                        Log.e("Razorpay", "Error in starting Razorpay Checkout", e)
                                        errorMessage = "Failed to open payment gateway."
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text("Upgrade", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        Text(
                            text = "Recurring billing. Cancel anytime.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumPerkRow(icon: ImageVector, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFE91E63).copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = Color(0xFFE91E63))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
