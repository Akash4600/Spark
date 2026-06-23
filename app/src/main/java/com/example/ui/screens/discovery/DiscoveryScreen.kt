package com.example.ui.screens.discovery

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.UserProfile
import com.example.viewmodel.AppViewModel
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoveryScreen(appViewModel: AppViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val profiles by appViewModel.discoveryProfiles.collectAsState()

    var showMatchOverlayFor by remember { mutableStateOf<UserProfile?>(null) }
    var showFilters by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // App Bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.img_app_icon_1782189997125),
                        contentDescription = "Spark Icon",
                        modifier = Modifier.size(32.dp).clip(CircleShape),
                        tint = Color.Unspecified
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Spark",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                IconButton(onClick = { showFilters = true }) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filters", tint = MaterialTheme.colorScheme.onBackground)
                }
            }

            // Swiping Area
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (profiles.isEmpty()) {
                    Text(
                        text = "No more profiles nearby.\nTry adjusting your filters.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                } else {
                    // Render profiles from top down to simulate a stack
                    for (i in profiles.indices.reversed()) {
                        val profile = profiles[i]
                        ProfileCard(
                            profile = profile,
                            onSwiped = { direction ->
                                if (direction == SwipeDirection.Right) {
                                    if(profile.willMatch) {
                                        showMatchOverlayFor = profile
                                    }
                                    appViewModel.likeProfile(profile.id)
                                } else {
                                    appViewModel.passProfile(profile.id)
                                }
                            }
                        )
                    }
                }
            }
        }
        
        // Match Overlay
        AnimatedVisibility(
            visible = showMatchOverlayFor != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            showMatchOverlayFor?.let { profile ->
                MatchOverlay(
                    matchedUser = profile,
                    onDismiss = { showMatchOverlayFor = null }
                )
            }
        }

        // Filters Bottom Sheet
        if (showFilters) {
             ModalBottomSheet(
                onDismissRequest = { showFilters = false },
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                FiltersContent()
            }
        }
    }
}

enum class SwipeDirection {
    Left, Right
}

@Composable
fun ProfileCard(profile: UserProfile, onSwiped: (SwipeDirection) -> Unit) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val density = LocalDensity.current
    val screenWidthPx = with(density) { screenWidth.toPx() }

    val coroutineScope = rememberCoroutineScope()
    val offset = remember { Animatable(Offset(0f, 0f), Offset.VectorConverter) }
    
    // Calculate rotation based on X offset
    val rotation = (offset.value.x / screenWidthPx) * 20f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(bottom = 32.dp)
            .offset { IntOffset(offset.value.x.roundToInt(), offset.value.y.roundToInt()) }
            .graphicsLayer(
                rotationZ = rotation
            )
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        coroutineScope.launch {
                            val targetX = offset.value.x
                            if (targetX > screenWidthPx / 3) {
                                // Swiped Right
                                offset.animateTo(
                                    targetValue = Offset(screenWidthPx * 2, offset.value.y),
                                    animationSpec = tween(300)
                                )
                                onSwiped(SwipeDirection.Right)
                            } else if (targetX < -screenWidthPx / 3) {
                                // Swiped Left
                                offset.animateTo(
                                    targetValue = Offset(-screenWidthPx * 2, offset.value.y),
                                    animationSpec = tween(300)
                                )
                                onSwiped(SwipeDirection.Left)
                            } else {
                                // Return to center
                                offset.animateTo(
                                    targetValue = Offset(0f, 0f),
                                    animationSpec = tween(300)
                                )
                            }
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        coroutineScope.launch {
                            offset.snapTo(
                                Offset(
                                    offset.value.x + dragAmount.x,
                                    offset.value.y + dragAmount.y
                                )
                            )
                        }
                    }
                )
            }
            .clip(RoundedCornerShape(24.dp))
            .background(Color.DarkGray)
    ) {
        Image(
            painter = painterResource(id = profile.imageRes),
            contentDescription = "Profile Photo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Gradient at bottom for text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                        startY = 500f
                    )
                )
        )

        // Like / Nope indicators
        if (offset.value.x > 50f) {
            Text(
                "LIKE",
                color = Color(0xFF4CAF50),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(32.dp)
                    .border(4.dp, Color(0xFF4CAF50), RoundedCornerShape(8.dp))
                    .padding(8.dp)
                    .align(Alignment.TopStart)
                    .graphicsLayer(rotationZ = -15f)
            )
        } else if (offset.value.x < -50f) {
             Text(
                "NOPE",
                color = Color(0xFFF44336),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(32.dp)
                    .border(4.dp, Color(0xFFF44336), RoundedCornerShape(8.dp))
                    .padding(8.dp)
                    .align(Alignment.TopEnd)
                    .graphicsLayer(rotationZ = 15f)
            )
        }

        // Profile Info
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = profile.name,
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = profile.age.toString(),
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 24.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${profile.location} • ${profile.distance}",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = profile.bio,
                color = Color.White,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FloatingActionButton(
                    onClick = { 
                        coroutineScope.launch {
                            offset.animateTo(Offset(-screenWidthPx * 2, offset.value.y), animationSpec = tween(300))
                            onSwiped(SwipeDirection.Left)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = Color(0xFFF44336),
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Pass", modifier = Modifier.size(32.dp))
                }
                
                FloatingActionButton(
                    onClick = { /* Super Like Logic */ },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = Color(0xFF2196F3),
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Star, contentDescription = "Super Like", modifier = Modifier.size(24.dp))
                }

                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            offset.animateTo(Offset(screenWidthPx * 2, offset.value.y), animationSpec = tween(300))
                            onSwiped(SwipeDirection.Right)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = Color(0xFF4CAF50),
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Favorite, contentDescription = "Like", modifier = Modifier.size(32.dp))
                }
            }
        }
    }
}

@Composable
fun MatchOverlay(matchedUser: UserProfile, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "It's a Match!",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            
            Text(
                text = "You and ${matchedUser.name} have liked each other.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                modifier = Modifier.padding(bottom = 48.dp)
            )
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Own profile picture (mock)
                Image(
                    painter = painterResource(id = R.drawable.profile_man_1_1782193240474),
                    contentDescription = "You",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(120.dp).clip(CircleShape).border(4.dp, MaterialTheme.colorScheme.primary, CircleShape)
                )
                
                // Matched profile picture
                Image(
                    painter = painterResource(id = matchedUser.imageRes),
                    contentDescription = "Matched User",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(120.dp).clip(CircleShape).border(4.dp, MaterialTheme.colorScheme.primary, CircleShape)
                )
            }
            
            Spacer(modifier = Modifier.height(64.dp))
            
            Button(
                onClick = onDismiss, // Keep it simple for phase 2, later: navigate to chat
                modifier = Modifier.fillMaxWidth(0.8f).height(56.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                Icon(Icons.Filled.ChatBubble, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Send a Message", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(0.8f).height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                 Text("Keep Swiping", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun FiltersContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(bottom = 48.dp)
    ) {
        Text("Discovery Settings", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("Maximum Distance", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        var distance by remember { mutableStateOf(10f) }
        Text("${distance.toInt()} mi", color = MaterialTheme.colorScheme.primary)
        Slider(value = distance, onValueChange = { distance = it }, valueRange = 1f..100f)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Age Range", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        var ageRange by remember { mutableStateOf(18f..35f) }
        Text("${ageRange.start.toInt()} - ${ageRange.endInclusive.toInt()}", color = MaterialTheme.colorScheme.primary)
        RangeSlider(value = ageRange, onValueChange = { ageRange = it }, valueRange = 18f..100f)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Show Me", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            FilterChip(selected = true, onClick = {}, label = { Text("Women") })
            FilterChip(selected = false, onClick = {}, label = { Text("Men") })
            FilterChip(selected = false, onClick = {}, label = { Text("Everyone") })
        }
    }
}
