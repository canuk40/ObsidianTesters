package com.obsidiantesters.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.obsidiantesters.data.model.ShardTransaction
import com.obsidiantesters.data.model.TestProgress
import com.obsidiantesters.data.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val SHARDS_PER_TEST = 5
private const val COOLDOWN_MS = 24L * 60 * 60 * 1000 // 24 hours in ms

@Singleton
class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun getUserFlow(userId: String): Flow<User?> = callbackFlow {
        val reg = firestore.collection("users").document(userId)
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(null); return@addSnapshotListener }
                trySend(snap?.toObject(User::class.java))
            }
        awaitClose { reg.remove() }
    }

    /** Always reads shard balance from server — never trusts local cache. */
    suspend fun getShardBalance(userId: String): Int {
        val snap = firestore.collection("users").document(userId)
            .get().await()
        return snap.getLong("shardBalance")?.toInt() ?: 0
    }

    suspend fun getUserTestingProgress(userId: String): List<TestProgress> {
        return firestore.collection("testProgress")
            .whereEqualTo("userId", userId)
            .get().await()
            .documents.mapNotNull { it.toObject(TestProgress::class.java) }
    }

    /**
     * Awards shards for completing a test. Uses a Firestore transaction to prevent
     * race conditions and double awards. Enforces 24-hour cooldown per app per user.
     */
    suspend fun awardTestShards(userId: String, appId: String): Result<Unit> = runCatching {
        val progressId = "${userId}_${appId}"
        val progressRef = firestore.collection("testProgress").document(progressId)
        val userRef = firestore.collection("users").document(userId)
        val txRef = firestore.collection("transactions").document()

        firestore.runTransaction { transaction ->
            val progressSnap = transaction.get(progressRef)
            val userSnap = transaction.get(userRef)

            val now = System.currentTimeMillis()
            val lastVerified = progressSnap.getLong("lastVerified") ?: 0L

            // Enforce 24-hour cooldown
            require(now - lastVerified >= COOLDOWN_MS) {
                "Shards already awarded for this app in the last 24 hours."
            }

            val currentBalance = userSnap.getLong("shardBalance")?.toInt() ?: 0
            val daysCounted = (progressSnap.getLong("daysCounted") ?: 0L).toInt()

            // Update test progress
            val progressUpdate = mapOf(
                "userId" to userId,
                "appId" to appId,
                "daysCounted" to (daysCounted + 1),
                "lastVerified" to now,
                "timerCompleted" to true
            )
            transaction.set(progressRef, progressUpdate)

            // Update shard balance
            transaction.update(userRef, "shardBalance", currentBalance + SHARDS_PER_TEST)

            // Record transaction
            val shardTx = ShardTransaction(
                transactionId = txRef.id,
                userId = userId,
                appId = appId,
                shardsAwarded = SHARDS_PER_TEST,
                reason = "test_completed",
                timestamp = now
            )
            transaction.set(txRef, shardTx)
        }.await()
    }

    /** Deducts shards for posting an app. Verifies server balance first. */
    suspend fun deductShards(userId: String, amount: Int): Result<Unit> = runCatching {
        val userRef = firestore.collection("users").document(userId)
        firestore.runTransaction { transaction ->
            val snap = transaction.get(userRef)
            val balance = snap.getLong("shardBalance")?.toInt() ?: 0
            require(balance >= amount) { "Insufficient shard balance." }
            transaction.update(userRef, "shardBalance", balance - amount)
        }.await()
    }

    /** Credits shards from bypass purchase — called after receipt verification. */
    suspend fun creditBypassShards(userId: String, amount: Int): Result<Unit> = runCatching {
        val userRef = firestore.collection("users").document(userId)
        firestore.runTransaction { transaction ->
            val snap = transaction.get(userRef)
            val balance = snap.getLong("shardBalance")?.toInt() ?: 0
            transaction.update(userRef, "shardBalance", balance + amount)
        }.await()
    }

    suspend fun updateSubscribedTier(userId: String, tier: String): Result<Unit> = runCatching {
        firestore.collection("users").document(userId)
            .update("subscribedTier", tier).await()
    }
}
