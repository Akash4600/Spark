package com.example.model

data class UserProfile(
    val id: String,
    val name: String,
    val age: Int,
    val location: String,
    val distance: String,
    val bio: String,
    val imageRes: Int,
    val willMatch: Boolean = false
)

data class MatchItem(
    val id: String,
    val name: String,
    val lastMessage: String,
    val time: String,
    val imageRes: Int,
    val isNew: Boolean = false
)

data class Message(val text: String, val isMe: Boolean)
