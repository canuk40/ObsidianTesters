package com.obsidiantesters.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.obsidiantesters.data.model.AppListing
import com.obsidiantesters.data.repository.AppRepository
import com.obsidiantesters.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

sealed class PostAppState {
    object Idle : PostAppState()
    object UploadingImage : PostAppState()
    object Loading : PostAppState()
    object Success : PostAppState()
    object InsufficientShards : PostAppState()
    object ProLimitReached : PostAppState()
    data class Error(val message: String) : PostAppState()
}

@HiltViewModel
class PostAppViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val userRepository: UserRepository,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _state = MutableStateFlow<PostAppState>(PostAppState.Idle)
    val state: StateFlow<PostAppState> = _state.asStateFlow()

    private val _shardBalance = MutableStateFlow(0)
    val shardBalance: StateFlow<Int> = _shardBalance.asStateFlow()

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

    fun loadBalance() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _shardBalance.value = userRepository.getShardBalance(uid)
        }
    }

    fun onImageSelected(uri: Uri?) {
        _selectedImageUri.value = uri
    }

    fun postApp(
        appName: String,
        developerName: String,
        description: String,
        playStoreLink: String,
        googleGroupEmail: String,
        shardCost: Int,
        testersRequired: Int,
        isPriority: Boolean
    ) {
        val userId = auth.currentUser?.uid ?: run {
            _state.value = PostAppState.Error("Not signed in"); return
        }

        // Validate required fields before spending any shards
        when {
            appName.isBlank() ->
                { _state.value = PostAppState.Error("App name is required."); return }
            developerName.isBlank() ->
                { _state.value = PostAppState.Error("Developer name is required."); return }
            description.isBlank() ->
                { _state.value = PostAppState.Error("Description is required."); return }
            playStoreLink.isBlank() ->
                { _state.value = PostAppState.Error("Play Store link is required."); return }
            !playStoreLink.startsWith("https://play.google.com/") ->
                { _state.value = PostAppState.Error("Play Store link must start with https://play.google.com/"); return }
            googleGroupEmail.isBlank() ->
                { _state.value = PostAppState.Error("Google Group email is required."); return }
        }

        viewModelScope.launch {
            // Verify shard balance server-side
            val balance = userRepository.getShardBalance(userId)
            if (balance < shardCost) {
                _state.value = PostAppState.InsufficientShards
                return@launch
            }

            // Upload image if selected
            val logoUrl = _selectedImageUri.value?.let { uri ->
                _state.value = PostAppState.UploadingImage
                uploadImage(userId, uri)
            } ?: ""

            _state.value = PostAppState.Loading
            val listing = AppListing(
                appId = UUID.randomUUID().toString(),
                devUserId = userId,
                appName = appName,
                developerName = developerName,
                logoUrl = logoUrl,
                playStoreLink = playStoreLink,
                googleGroupEmail = googleGroupEmail,
                description = description,
                testersRequired = testersRequired,
                testersJoined = 0,
                isPriority = isPriority,
                isActive = true,
                createdAt = System.currentTimeMillis(),
                expiresAt = System.currentTimeMillis() + 14L * 24 * 60 * 60 * 1000
            )

            appRepository.postApp(listing).fold(
                onSuccess = {
                    userRepository.deductShards(userId, shardCost)
                    _state.value = PostAppState.Success
                },
                onFailure = { e -> _state.value = PostAppState.Error(e.message ?: "Failed") }
            )
        }
    }

    private suspend fun uploadImage(userId: String, uri: Uri): String {
        val filename = "app_logos/${userId}/${UUID.randomUUID()}.jpg"
        val ref = storage.reference.child(filename)
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }
}
