package com.obsidiantesters.data.model

data class AppListing(
    val appId: String = "",
    val devUserId: String = "",
    val appName: String = "",
    val developerName: String = "",
    val logoUrl: String = "",
    val playStoreLink: String = "",
    val googleGroupEmail: String = "",
    val description: String = "",
    val testersRequired: Int = 12,
    val testersJoined: Int = 0,
    val isPriority: Boolean = false,
    val isBypass: Boolean = false,
    val isActive: Boolean = true,
    val createdAt: Long = 0L,
    val expiresAt: Long = 0L
)
