package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.SparkTheme
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.service.FirestoreNotificationManager

import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import com.example.state.UserState
import android.widget.Toast

class MainActivity : ComponentActivity(), PaymentResultWithDataListener {
    private lateinit var notificationManager: FirestoreNotificationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.razorpay.Checkout.preload(applicationContext)
        enableEdgeToEdge()
        
        try {
            val options = com.google.firebase.FirebaseOptions.Builder()
                .setProjectId("dummy-project-id")
                .setApplicationId("1:1234567890:android:abcdef123456")
                .setApiKey("dummy-api-key")
                .build()
            com.google.firebase.FirebaseApp.initializeApp(this, options)
        } catch (e: Exception) {
            android.util.Log.e("FirebaseInit", "Failed to initialize Firebase", e)
        }

        notificationManager = FirestoreNotificationManager(this)
        requestNotificationPermission()
        
        // Mock User ID. In a real app, this comes from Firebase Auth
        val currentUserId = "mock_user_123" 
        notificationManager.startListening(currentUserId)

        setContent {
            SparkTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation()
                }
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        notificationManager.stopListening()
    }

    override fun onPaymentSuccess(razorpayPaymentID: String?, paymentData: PaymentData?) {
        UserState.isPremium.value = true
        Toast.makeText(this, "Payment Successful", Toast.LENGTH_SHORT).show()
    }

    override fun onPaymentError(code: Int, response: String?, paymentData: PaymentData?) {
        Toast.makeText(this, "Payment Failed: $response", Toast.LENGTH_LONG).show()
    }
}
