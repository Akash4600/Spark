package com.example.viewmodel

import androidx.lifecycle.ViewModel
import com.example.R
import com.example.model.MatchItem
import com.example.model.Message
import com.example.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AppViewModel : ViewModel() {

    // Discovery Deck
    private val _discoveryProfiles = MutableStateFlow<List<UserProfile>>(emptyList())
    val discoveryProfiles = _discoveryProfiles.asStateFlow()

    // Matches & Likes
    private val _matches = MutableStateFlow<List<MatchItem>>(emptyList())
    val matches = _matches.asStateFlow()

    private val _likesCount = MutableStateFlow(3)
    val likesCount = _likesCount.asStateFlow()

    // Chat State
    private val _chatMessages = MutableStateFlow<Map<String, List<Message>>>(emptyMap())
    val chatMessages = _chatMessages.asStateFlow()
    
    // My Message Count (for free tier limits)
    private val _myMessageCount = MutableStateFlow(1)
    val myMessageCount = _myMessageCount.asStateFlow()

    init {
        loadMockData()
    }

    private fun loadMockData() {
        _discoveryProfiles.value = listOf(
            UserProfile("1", "Elena", 25, "Los Angeles", "2 miles away", "Love hiking and coffee.", R.drawable.profile_woman_1_1782193225608, willMatch = true),
            UserProfile("2", "Marcus", 28, "Santa Monica", "5 miles away", "Photographer and traveler.", R.drawable.profile_man_1_1782193240474),
            UserProfile("3", "Sarah", 30, "West Hollywood", "3 miles away", "Dogs are better than humans.", R.drawable.profile_woman_2_1782193255580, willMatch = true),
            UserProfile("4", "David", 26, "Downtown LA", "1 mile away", "Always looking for the best tacos.", R.drawable.profile_man_2_1782193274311)
        )

        _matches.value = listOf(
            MatchItem("1", "Elena", "Are we still on for later?", "10m", R.drawable.profile_woman_1_1782193225608, true),
            MatchItem("2", "Marcus", "Haha that's hilarious \uD83D\uDE02", "1h", R.drawable.profile_man_1_1782193240474),
            MatchItem("3", "Sarah", "See you then!", "2h", R.drawable.profile_woman_2_1782193255580, true),
            MatchItem("4", "David", "Do you like tacos?", "5h", R.drawable.profile_man_2_1782193274311),
            MatchItem("5", "Jessica", "Good morning!", "1d", R.drawable.profile_woman_1_1782193225608)
        )
    }

    fun passProfile(profileId: String) {
        _discoveryProfiles.update { list -> list.filterNot { it.id == profileId } }
    }

    fun likeProfile(profileId: String) {
        _discoveryProfiles.update { list -> list.filterNot { it.id == profileId } }
    }

    fun sendMessage(matchId: String, text: String, isMe: Boolean = true) {
        if (isMe) {
            _myMessageCount.value += 1
        }
        
        _chatMessages.update { currentMap ->
            val currentMessages = currentMap[matchId] ?: getDefaultMessages()
            val newMessages = currentMessages + Message(text, isMe)
            currentMap.toMutableMap().apply { put(matchId, newMessages) }
        }
        
        // Also update last message in the match list
        if (isMe) {
             _matches.update { matchesList ->
                 matchesList.map { 
                    if (it.id == matchId) it.copy(lastMessage = text, time = "Now") else it 
                 }
             }
        }
    }

    fun getMessagesForMatch(matchId: String): List<Message> {
        return _chatMessages.value[matchId] ?: getDefaultMessages()
    }

    private fun getDefaultMessages() = listOf(
        Message("Hey! How are you?", false),
        Message("I'm doing great, just checking out this app.", true),
        Message("Same here! Where are you from?", false)
    )
}
