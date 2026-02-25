package com.obsidiantesters.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.obsidiantesters.data.model.AppListing
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    /** Returns active listings — PRO/priority apps first, then chronological. */
    val activeListings: Flow<List<AppListing>> = callbackFlow {
        val registration = firestore.collection("apps")
            .whereEqualTo("isActive", true)
            .orderBy("isPriority", Query.Direction.DESCENDING)
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { trySend(emptyList()); return@addSnapshotListener }
                val now = System.currentTimeMillis()
                val listings = snapshot?.documents?.mapNotNull {
                    it.toObject(AppListing::class.java)
                }?.filter { it.expiresAt == 0L || it.expiresAt > now } ?: emptyList()
                trySend(listings)
            }
        awaitClose { registration.remove() }
    }

    suspend fun postApp(listing: AppListing): Result<Unit> = runCatching {
        val docRef = firestore.collection("apps").document(listing.appId)
        docRef.set(listing).await()
    }

    suspend fun getListingById(appId: String): AppListing? {
        return firestore.collection("apps").document(appId).get().await()
            .toObject(AppListing::class.java)
    }

    suspend fun getUserListings(userId: String): List<AppListing> {
        return firestore.collection("apps")
            .whereEqualTo("devUserId", userId)
            .get().await()
            .documents.mapNotNull { it.toObject(AppListing::class.java) }
    }
}
