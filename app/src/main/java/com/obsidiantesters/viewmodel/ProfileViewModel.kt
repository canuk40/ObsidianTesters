package com.obsidiantesters.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.obsidiantesters.data.model.AppListing
import com.obsidiantesters.data.model.TestProgress
import com.obsidiantesters.data.model.User
import com.obsidiantesters.data.repository.AppRepository
import com.obsidiantesters.data.repository.AuthRepository
import com.obsidiantesters.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val appRepository: AppRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _myApps = MutableStateFlow<List<AppListing>>(emptyList())
    val myApps: StateFlow<List<AppListing>> = _myApps.asStateFlow()

    private val _testingProgress = MutableStateFlow<List<TestProgress>>(emptyList())
    val testingProgress: StateFlow<List<TestProgress>> = _testingProgress.asStateFlow()

    val userFlow: StateFlow<User?> = auth.currentUser?.uid?.let { uid ->
        userRepository.getUserFlow(uid)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    } ?: MutableStateFlow(null)

    fun loadProfileData() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _myApps.value = appRepository.getUserListings(uid)
            _testingProgress.value = userRepository.getUserTestingProgress(uid)
        }
    }

    fun signOut() = authRepository.signOut()
}
