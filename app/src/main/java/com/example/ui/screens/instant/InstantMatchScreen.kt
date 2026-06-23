package com.example.ui.screens.instant

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.items
import com.example.R
import com.example.state.UserState
import com.example.ui.screens.premium.PremiumPaywallDialog
import kotlinx.coroutines.delay

enum class MatchState { IDLE, SEARCHING, IN_CALL }
enum class CallType { VIDEO, VOICE, TEXT }

val MOCK_PROFILES = listOf(
    R.drawable.profile_woman_1_1782193225608,
    R.drawable.profile_woman_2_1782193255580,
    R.drawable.profile_man_1_1782193240474,
    R.drawable.profile_man_2_1782193274311
)

@Composable
fun InstantMatchScreen() {
    var matchState by remember { mutableStateOf(MatchState.IDLE) }
    var callType by remember { mutableStateOf(CallType.VIDEO) }
    var currentProfileRes by remember { mutableStateOf(MOCK_PROFILES.random()) }
    var showPaywall by remember { mutableStateOf(false) }
    var showPermissionError by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false

        if (callType == CallType.VIDEO && cameraGranted && audioGranted) {
            matchState = MatchState.SEARCHING
        } else if (callType == CallType.VOICE && audioGranted) {
            matchState = MatchState.SEARCHING
        } else if (callType == CallType.TEXT) {
            matchState = MatchState.SEARCHING
        } else {
            showPermissionError = true
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        AnimatedContent(targetState = matchState, label = "Match State Transition") { state ->
            when (state) {
                MatchState.IDLE -> {
                    IdleScreen(
                        onSelectMode = { type ->
                            val canProceed = UserState.isPremium.value || when (type) {
                                CallType.VIDEO -> UserState.freeVideoCallsLeft.value > 0
                                CallType.VOICE -> UserState.freeVoiceCallsLeft.value > 0
                                CallType.TEXT -> UserState.freeTextChatsLeft.value > 0
                            }

                            if (canProceed) {
                                if (!UserState.isPremium.value) {
                                    when (type) {
                                        CallType.VIDEO -> UserState.freeVideoCallsLeft.value--
                                        CallType.VOICE -> UserState.freeVoiceCallsLeft.value--
                                        CallType.TEXT -> UserState.freeTextChatsLeft.value--
                                    }
                                }
                                callType = type
                                if (type == CallType.VIDEO) {
                                    permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
                                } else if (type == CallType.VOICE) {
                                    permissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
                                } else {
                                    matchState = MatchState.SEARCHING
                                }
                            } else {
                                showPaywall = true
                            }
                        }
                    )
                }
                MatchState.SEARCHING -> {
                    SearchingScreen(
                        callType = callType,
                        onCancel = { matchState = MatchState.IDLE },
                        onMatchFound = {
                            currentProfileRes = MOCK_PROFILES.random()
                            matchState = MatchState.IN_CALL
                        }
                    )
                }
                MatchState.IN_CALL -> {
                    ActiveCallScreen(
                        callType = callType,
                        profileImageRes = currentProfileRes,
                        onNext = { matchState = MatchState.SEARCHING },
                        onEnd = { matchState = MatchState.IDLE }
                    )
                }
            }
        }
        
        if (showPaywall) {
            PremiumPaywallDialog(onDismiss = { showPaywall = false })
        }

        if (showPermissionError) {
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                action = {
                    TextButton(onClick = { showPermissionError = false }) {
                        Text("Dismiss", color = MaterialTheme.colorScheme.inversePrimary)
                    }
                }
            ) {
                Text("Camera/Microphone permissions are required for Instant Connect.")
            }
        }
    }
}

@Composable
fun IdleScreen(onSelectMode: (CallType) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Instant Match",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Skip the swiping. Meet someone instantly through a random live connection.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        
        Spacer(modifier = Modifier.height(64.dp))
        
        // Video Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(24.dp))
                .clickable { onSelectMode(CallType.VIDEO) },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Videocam, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                }
                Spacer(modifier = Modifier.width(24.dp))
                Column {
                    Text("Random Video", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Face to face connections", style = MaterialTheme.typography.bodyMedium)
                    if (!UserState.isPremium.value) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("${UserState.freeVideoCallsLeft.value} left today", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Voice Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(24.dp))
                .clickable { onSelectMode(CallType.VOICE) },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Mic, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                }
                Spacer(modifier = Modifier.width(24.dp))
                Column {
                    Text("Random Voice", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Talk and listen anonymously", style = MaterialTheme.typography.bodyMedium)
                    if (!UserState.isPremium.value) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("${UserState.freeVoiceCallsLeft.value} left today", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Text Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(24.dp))
                .clickable { onSelectMode(CallType.TEXT) },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                }
                Spacer(modifier = Modifier.width(24.dp))
                Column {
                    Text("Random Text", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Chat instantly with someone new", style = MaterialTheme.typography.bodyMedium)
                    if (!UserState.isPremium.value) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("${UserState.freeTextChatsLeft.value} left today", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                    }
                }
            }
        }
    }
}

@Composable
fun SearchingScreen(callType: CallType, onCancel: () -> Unit, onMatchFound: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "SearchPulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAnimation"
    )

    LaunchedEffect(Unit) {
        delay(2000L) // Simulating network finding match
        onMatchFound()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (callType) {
                        CallType.VIDEO -> Icons.Filled.Videocam
                        CallType.VOICE -> Icons.Filled.Phone
                        CallType.TEXT -> Icons.Filled.Chat
                    },
                    contentDescription = "Searching",
                    modifier = Modifier.size(48.dp),
                    tint = Color.White
                )
            }
        }
        Spacer(modifier = Modifier.height(48.dp))
        Text("Looking for a match...", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(48.dp))
        OutlinedButton(onClick = onCancel) {
            Text("Cancel")
        }
    }
}

@Composable
fun ActiveCallScreen(
    callType: CallType,
    profileImageRes: Int,
    onNext: () -> Unit,
    onEnd: () -> Unit
) {
    var isMicEnabled by remember { mutableStateOf(true) }
    var isCamEnabled by remember { mutableStateOf(true) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (callType == CallType.VIDEO) {
            // Remote User Video
            Image(
                painter = painterResource(id = profileImageRes),
                contentDescription = "Remote User",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            startY = 600f
                        )
                    )
            )

            // Local PIP Box
            if (isCamEnabled) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .size(110.dp, 160.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.DarkGray)
                ) {
                    // Simulating local camera reflection (using one of the mock photos for demo)
                    Image(
                        painter = painterResource(id = R.drawable.profile_man_1_1782193240474),
                        contentDescription = "Local Video",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .size(110.dp, 160.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E1E24)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.VideocamOff, contentDescription = "Cam Off", tint = Color.White)
                }
            }
        } else if (callType == CallType.VOICE) {
            // VOICE ONLY UI
            val infiniteTransition = rememberInfiniteTransition(label = "VoicePulse")
            val auraScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.25f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "AuraAnimation"
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier.size(200.dp).scale(auraScale).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)))
                    Image(
                        painter = painterResource(id = profileImageRes),
                        contentDescription = "Contact",
                        modifier = Modifier.size(160.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
                Text("Stranger", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("00:14", style = MaterialTheme.typography.bodyLarge, color = Color.Gray) // Demo timer
            }
        } else {
            // TEXT ONLY UI
            var messages by remember { mutableStateOf(listOf(Pair("Stranger", "hi!"), Pair("Stranger", "how are you?"))) }
            var textInput by remember { mutableStateOf("") }
            Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = profileImageRes),
                        contentDescription = "Contact",
                        modifier = Modifier.size(40.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Stranger", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                // Messages
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.weight(1f).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages.size) { i ->
                        val msg = messages[i]
                        val isMe = msg.first == "Me"
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start) {
                            Text(
                                text = msg.second,
                                color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(12.dp)
                            )
                        }
                    }
                }
                
                // Text input row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 120.dp, start = 16.dp, end = 16.dp, top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Message...") },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FloatingActionButton(
                        onClick = {
                            if (textInput.isNotBlank()) {
                                messages = messages + Pair("Me", textInput)
                                textInput = ""
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        elevation = FloatingActionButtonDefaults.elevation(0.dp)
                    ) {
                        Icon(Icons.Filled.Send, contentDescription = "Send", tint = Color.White)
                    }
                }
            }
        }

        // Actions Row Bottom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp, start = 24.dp, end = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Skip to Next Person
            FloatingActionButton(
                onClick = onNext,
                containerColor = if (callType == CallType.TEXT) MaterialTheme.colorScheme.secondaryContainer else Color.White.copy(alpha = 0.2f),
                contentColor = if (callType == CallType.TEXT) MaterialTheme.colorScheme.onSecondaryContainer else Color.White,
                elevation = FloatingActionButtonDefaults.elevation(0.dp)
            ) {
                Icon(Icons.Filled.SkipNext, contentDescription = "Next Person")
            }

            if (callType == CallType.VIDEO) {
                FloatingActionButton(
                    onClick = { isCamEnabled = !isCamEnabled },
                    containerColor = if (isCamEnabled) Color.White.copy(alpha = 0.2f) else Color.White,
                    contentColor = if (isCamEnabled) Color.White else Color.Black,
                    elevation = FloatingActionButtonDefaults.elevation(0.dp)
                ) {
                    Icon(if (isCamEnabled) Icons.Filled.Videocam else Icons.Filled.VideocamOff, contentDescription = "Cam Toggle")
                }
            }

            if (callType == CallType.VIDEO || callType == CallType.VOICE) {
                FloatingActionButton(
                    onClick = { isMicEnabled = !isMicEnabled },
                    containerColor = if (isMicEnabled) Color.White.copy(alpha = 0.2f) else Color.White,
                    contentColor = if (isMicEnabled) Color.White else Color.Black,
                    elevation = FloatingActionButtonDefaults.elevation(0.dp)
                ) {
                    Icon(if (isMicEnabled) Icons.Filled.Mic else Icons.Filled.MicOff, contentDescription = "Mic Toggle")
                }
            }

            // End Call
            FloatingActionButton(
                onClick = onEnd,
                containerColor = Color(0xFFF44336), // Red
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(0.dp)
            ) {
                Icon(Icons.Filled.CallEnd, contentDescription = "End Call")
            }
        }
    }
}
