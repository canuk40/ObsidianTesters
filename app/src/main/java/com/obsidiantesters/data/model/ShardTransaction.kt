package com.obsidiantesters.data.model

data class ShardTransaction(
    val transactionId: String = "",
    val userId: String = "",
    val appId: String = "",
    val shardsAwarded: Int = 0,
    val reason: String = "",
    val timestamp: Long = 0L
)
