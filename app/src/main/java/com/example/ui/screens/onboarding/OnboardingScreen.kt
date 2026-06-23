package com.example.ui.screens.onboarding

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.util.UUID
import androidx.compose.foundation.clickable
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.ui.components.ShimmerAnimation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    var step by remember { mutableStateOf(1) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // User Profile State
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") } // Starts empty, to simulate being fetched
    var interestedIn by remember { mutableStateOf("Men") }
    var location by remember { mutableStateOf("Los Angeles, CA") }
    var bio by remember { mutableStateOf("") }
    var profilePhotoUrl by remember { mutableStateOf<String?>(null) }

    var isSaving by remember { mutableStateOf(false) }
    var isInitialLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        // Simulate fetching initial profile data (e.g. from a third-party auth provider)
        delay(1500)
        name = "Alex" // Simulated fetched data
        gender = "Woman"
        isInitialLoading = false
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    LinearProgressIndicator(
                        progress = { if (isInitialLoading) 0f else step / 3f },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                },
                navigationIcon = {
                    if (step > 1 && !isInitialLoading) {
                        IconButton(onClick = { step-- }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(48.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                
                if (isInitialLoading) {
                    OnboardingSkeleton()
                } else {
                    AnimatedContent(targetState = step, label = "Onboarding Steps") { currentStep ->
                        when (currentStep) {
                            1 -> BasicInfoStep(name, { name = it }, age, { age = it }, gender, { gender = it })
                            2 -> PreferencesStep(interestedIn, { interestedIn = it }, location, { location = it })
                            3 -> BioPhotosStep(bio, { bio = it }, profilePhotoUrl, { profilePhotoUrl = it })
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(100.dp)) // Space for bottom button
            }

            if (!isInitialLoading) {
                Button(
                    onClick = {
                        if (step < 3) {
                            step++
                        } else {
                            // Save to Firestore
                            isSaving = true
                            try {
                                val db = FirebaseFirestore.getInstance()
                                val userProfile = hashMapOf(
                                    "name" to name,
                                    "age" to age.toIntOrNull(),
                                    "gender" to gender,
                                    "interestedIn" to interestedIn,
                                    "location" to location,
                                    "bio" to bio,
                                    "profilePhotoUrl" to profilePhotoUrl,
                                    "timestamp" to System.currentTimeMillis()
                                )
                                
                                db.collection("users")
                                    .add(userProfile)
                                    .addOnSuccessListener {
                                        isSaving = false
                                        onComplete()
                                    }
                                    .addOnFailureListener { e ->
                                        isSaving = false
                                        Log.e("Onboarding", "Error saving profile", e)
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Failed to save profile: ${e.message}")
                                        }
                                    }
                            } catch (e: IllegalStateException) {
                                // Firebase is not initialized locally (missing google-services.json)
                                isSaving = false
                                Log.e("Onboarding", "Firebase not initialized", e)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Firebase is not initialized. Please add google-services.json to the project. Proceeds locally.")
                                }
                                // Call onComplete anyway to not block the user during development
                                onComplete()
                            } catch (e: Exception) {
                                isSaving = false
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Error: ${e.message}")
                                }
                                onComplete()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                    } else {
                        Text(if (step == 3) "Let's find a Spark" else "Next", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingSkeleton() {
    Column {
        ShimmerAnimation(modifier = Modifier.width(200.dp).height(32.dp).clip(RoundedCornerShape(8.dp)))
        Spacer(modifier = Modifier.height(16.dp))
        ShimmerAnimation(modifier = Modifier.width(150.dp).height(20.dp).clip(RoundedCornerShape(4.dp)))
        
        Spacer(modifier = Modifier.height(48.dp))
        
        ShimmerAnimation(modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(12.dp)))
        Spacer(modifier = Modifier.height(16.dp))
        ShimmerAnimation(modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(12.dp)))
        
        Spacer(modifier = Modifier.height(32.dp))
        
        ShimmerAnimation(modifier = Modifier.width(80.dp).height(20.dp).clip(RoundedCornerShape(4.dp)))
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ShimmerAnimation(modifier = Modifier.width(80.dp).height(32.dp).clip(RoundedCornerShape(16.dp)))
            ShimmerAnimation(modifier = Modifier.width(80.dp).height(32.dp).clip(RoundedCornerShape(16.dp)))
            ShimmerAnimation(modifier = Modifier.width(80.dp).height(32.dp).clip(RoundedCornerShape(16.dp)))
        }
    }
}

@Composable
fun BasicInfoStep(
    name: String, onNameChange: (String) -> Unit,
    age: String, onAgeChange: (String) -> Unit,
    gender: String, onGenderChange: (String) -> Unit
) {
    Column {
        Text("Tell us about yourself", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Real connections start with honesty.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("First Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = age,
            onValueChange = onAgeChange,
            label = { Text("Age") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("I am a...", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val genders = listOf("Woman", "Man", "Non-binary")
            genders.forEach { option ->
                FilterChip(
                    selected = (gender == option),
                    onClick = { onGenderChange(option) },
                    label = { Text(option) }
                )
            }
        }
    }
}

@Composable
fun PreferencesStep(
    interestedIn: String, onInterestedInChange: (String) -> Unit,
    location: String, onLocationChange: (String) -> Unit
) {
    Column {
        Text("Who are you looking for?", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(32.dp))
        
        Text("I'm interested in...", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val interests = listOf("Women", "Men", "Everyone")
            interests.forEach { option ->
                FilterChip(
                    selected = (interestedIn == option),
                    onClick = { onInterestedInChange(option) },
                    label = { Text(option) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text("Age Range", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(12.dp))
        var sliderPosition by remember { mutableStateOf(25f..35f) }
        Text("${sliderPosition.start.toInt()} - ${sliderPosition.endInclusive.toInt()} years", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        RangeSlider(
            value = sliderPosition,
            steps = 0,
            onValueChange = { sliderPosition = it },
            valueRange = 18f..65f,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = location,
            onValueChange = onLocationChange,
            label = { Text("Location") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
    }
}

@Composable
fun BioPhotosStep(
    bio: String, onBioChange: (String) -> Unit,
    profilePhotoUrl: String?, onProfilePhotoUrlChange: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isUploading by remember { mutableStateOf(false) }

    val takePicturePreviewLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            isUploading = true
            coroutineScope.launch {
                try {
                    val storageRef = FirebaseStorage.getInstance().reference
                    val imageRef = storageRef.child("images/${UUID.randomUUID()}.jpg")

                    val baos = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                    val data = baos.toByteArray()

                    val uploadTask = imageRef.putBytes(data)
                    uploadTask.addOnSuccessListener {
                        imageRef.downloadUrl.addOnSuccessListener { uri ->
                            onProfilePhotoUrlChange(uri.toString())
                            isUploading = false
                        }
                    }.addOnFailureListener {
                        isUploading = false
                        Log.e("Upload", "Upload failed", it)
                    }
                } catch (e: Exception) {
                    isUploading = false
                    Log.e("Upload", "Upload error", e)
                }
            }
        }
    }

    Column {
        Text("A picture is worth a thousand words", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(24.dp))
        
        // Mock Photo Grid
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .weight(2f)
                    .aspectRatio(0.7f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { takePicturePreviewLauncher.launch(null) },
                contentAlignment = Alignment.Center
            ) {
                if (profilePhotoUrl != null) {
                    AsyncImage(
                        model = profilePhotoUrl,
                        contentDescription = "Profile Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (isUploading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                } else {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Add Best Photo", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Add Photo", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Add Photo", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text("Your Bio", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = bio,
            onValueChange = onBioChange,
            placeholder = { Text("Write something funny or interesting about yourself...") },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            shape = RoundedCornerShape(12.dp),
            maxLines = 4
        )
    }
}
