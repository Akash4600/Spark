package com.example.ui.screens.matches

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.Message
import com.example.state.UserState
import com.example.ui.screens.premium.PremiumPaywallDialog
import com.example.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(matchId: String, appViewModel: AppViewModel, onBack: () -> Unit) {
    
    val matches by appViewModel.matches.collectAsState()
    val matchName = matches.find { it.id == matchId }?.name ?: "Match"
    val allChatMessages by appViewModel.chatMessages.collectAsState()
    val messages = allChatMessages[matchId] ?: appViewModel.getMessagesForMatch(matchId)
    val myMessageCount by appViewModel.myMessageCount.collectAsState()
    
    var inputText by remember { mutableStateOf("") }
    val isPremium = UserState.isPremium.value
    var showPaywall by remember { mutableStateOf(false) }
    
    val reachedLimit = !isPremium && myMessageCount >= 5

    Scaffold(
        topBar = {
             TopAppBar(
                 title = { Text(matchName, fontWeight = FontWeight.Bold) },
                 navigationIcon = {
                     IconButton(onClick = onBack) {
                         Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                     }
                 },
                 actions = {
                     IconButton(onClick = { /* Demo Profile */ }) {
                         Icon(Icons.Default.Info, contentDescription = "Profile")
                     }
                 },
                 colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
             )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Limits Info Header
             if (!isPremium) {
                 Box(
                     modifier = Modifier
                         .fillMaxWidth()
                         .background(MaterialTheme.colorScheme.secondaryContainer)
                         .padding(8.dp),
                     contentAlignment = Alignment.Center
                 ) {
                     Text(
                         text = "Free Tier: ${5 - myMessageCount} messages remaining today.",
                         style = MaterialTheme.typography.bodySmall,
                         color = MaterialTheme.colorScheme.onSecondaryContainer,
                         fontWeight = FontWeight.Bold
                     )
                 }
             }

             // Chat Messages
             LazyColumn(
                 modifier = Modifier.weight(1f),
                 contentPadding = PaddingValues(16.dp),
                 verticalArrangement = Arrangement.spacedBy(8.dp),
                  reverseLayout = true // Modern chat apps build from bottom
             ) {
                  items(messages.reversed()) { msg ->
                      ChatBubble(msg)
                  }
             }

             // Input Area
             if (reachedLimit) {
                 // Block input
                 Box(
                     modifier = Modifier
                         .fillMaxWidth()
                         .padding(16.dp)
                         .clip(RoundedCornerShape(24.dp))
                         .background(MaterialTheme.colorScheme.surfaceVariant)
                         .padding(16.dp),
                     contentAlignment = Alignment.Center
                 ) {
                     Column(horizontalAlignment = Alignment.CenterHorizontally) {
                         Text("Daily message limit reached", color = MaterialTheme.colorScheme.onSurfaceVariant)
                         Spacer(modifier = Modifier.height(8.dp))
                         Button(
                             onClick = { showPaywall = true },
                             colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                         ) {
                              Text("Unlock Unlimited Chat")
                         }
                     }
                 }
             } else {
                 Row(
                     modifier = Modifier
                         .fillMaxWidth()
                         .padding(horizontal = 16.dp, vertical = 8.dp),
                     verticalAlignment = Alignment.CenterVertically
                 ) {
                     OutlinedTextField(
                         value = inputText,
                         onValueChange = { inputText = it },
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
                             if (inputText.isNotBlank()) {
                                 appViewModel.sendMessage(matchId, inputText)
                                 inputText = ""
                             }
                         },
                         containerColor = MaterialTheme.colorScheme.primary,
                         modifier = Modifier.size(48.dp),
                         shape = CircleShape,
                         elevation = FloatingActionButtonDefaults.elevation(0.dp)
                     ) {
                         Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
                     }
                 }
             }
         }
         
         if (showPaywall) {
             PremiumPaywallDialog(onDismiss = { showPaywall = false })
         }
    }
}

@Composable
fun ChatBubble(message: Message) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isMe) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .wrapContentWidth(if (message.isMe) Alignment.End else Alignment.Start)
                .clip(
                    RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (message.isMe) 20.dp else 4.dp,
                        bottomEnd = if (message.isMe) 4.dp else 20.dp
                    )
                )
                .background(if (message.isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                text = message.text,
                color = if (message.isMe) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
