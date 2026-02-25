package com.obsidiantesters.data.model

data class TestProgress(
    val userId: String = "",
    val appId: String = "",
    val daysCounted: Int = 0,
    val lastVerified: Long = 0L,
    val timerStarted: Long = 0L,
    val timerCompleted: Boolean = false,
    val completed: Boolean = false
)
