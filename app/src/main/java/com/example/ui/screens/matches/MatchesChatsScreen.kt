package com.example.ui.screens.matches

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.MatchItem
import com.example.state.UserState
import com.example.ui.screens.premium.PremiumPaywallDialog
import com.example.viewmodel.AppViewModel

val mockLikes = listOf(
    R.drawable.profile_man_2_1782193274311,
    R.drawable.profile_woman_2_1782193255580,
    R.drawable.profile_man_1_1782193240474,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchesChatsScreen(appViewModel: AppViewModel, onNavigateToChat: (MatchItem) -> Unit) {
    var selectedTab by remember { mutableStateOf(0) }
    var showPaywall by remember { mutableStateOf(false) }
    val isPremium = UserState.isPremium.value
    
    val matches by appViewModel.matches.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Matches", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Likes (${mockLikes.size})", fontWeight = FontWeight.Bold) }
            )
        }

        if (selectedTab == 0) {
            MatchesTab(
                matches = matches,
                isPremium = isPremium,
                onShowPaywall = { showPaywall = true },
                onNavigateToChat = onNavigateToChat
            )
        } else {
            LikesTab(
                isPremium = isPremium,
                onShowPaywall = { showPaywall = true }
            )
        }
    }

    if (showPaywall) {
        PremiumPaywallDialog(onDismiss = { showPaywall = false })
    }
}

@Composable
fun MatchesTab(matches: List<MatchItem>, isPremium: Boolean, onShowPaywall: () -> Unit, onNavigateToChat: (MatchItem) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
    ) {
        // New Matches Row
        item {
            Text(
                "New Matches",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val newMatches = matches.filter { it.isNew }
                items(newMatches.size) { index ->
                    val match = newMatches[index]
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onNavigateToChat(match) }) {
                        Image(
                            painter = painterResource(id = match.imageRes),
                            contentDescription = match.name,
                            modifier = Modifier.size(72.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(match.name, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Messages",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Messages List
        items(matches.size) { index ->
            val match = matches[index]
            val isLocked = !isPremium && index >= 3

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (isLocked) onShowPaywall() else onNavigateToChat(match)
                    }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    Image(
                        painter = painterResource(id = match.imageRes),
                        contentDescription = null,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .then(if (isLocked) Modifier.blur(10.dp) else Modifier),
                        contentScale = ContentScale.Crop
                    )
                    if (isLocked) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = "Locked", tint = Color.White)
                        }
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (isLocked) "Hidden Match" else match.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        if (isLocked) "Unlock Spark Plus to read messages" else match.lastMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isLocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                if (!isLocked) {
                    Text(match.time, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun LikesTab(isPremium: Boolean, onShowPaywall: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val rows = mockLikes.chunked(2)
            items(rows.size) { rowIndex ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    rows[rowIndex].forEach { imageRes ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(0.7f)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { if (!isPremium) onShowPaywall() }
                        ) {
                            Image(
                                painter = painterResource(id = imageRes),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .then(if (!isPremium) Modifier.blur(16.dp) else Modifier),
                                contentScale = ContentScale.Crop
                            )
                            if (!isPremium) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = "Locked", tint = Color.White, modifier = Modifier.size(48.dp))
                                }
                            }
                        }
                    }
                    if (rows[rowIndex].size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        if (!isPremium) {
             // Sticky Paywall container at bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, MaterialTheme.colorScheme.background.copy(alpha = 0.9f), MaterialTheme.colorScheme.background)
                        )
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${mockLikes.size} people liked you",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onShowPaywall,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("See Who Liked You", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
