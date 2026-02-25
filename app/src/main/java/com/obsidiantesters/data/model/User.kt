package com.obsidiantesters.data.model

data class User(
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val shardBalance: Int = 0,
    val subscribedTier: String = "FREE",
    val appsPosted: List<String> = emptyList(),
    val createdAt: Long = 0L
)
