package com.example.state

import androidx.compose.runtime.mutableStateOf

object UserState {
    var isPremium = mutableStateOf(false)
    var freeVideoCallsLeft = mutableStateOf(6)
    var freeVoiceCallsLeft = mutableStateOf(6)
    var freeTextChatsLeft = mutableStateOf(10)
}
