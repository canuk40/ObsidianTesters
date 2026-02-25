package com.obsidiantesters.viewmodel

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.obsidiantesters.data.model.AppListing
import com.obsidiantesters.data.repository.AppRepository
import com.obsidiantesters.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class TestUiState {
    object Idle : TestUiState()
    object Loading : TestUiState()
    object TimerRunning : TestUiState()
    object TimerPaused : TestUiState()   // user left app mid-timer
    object Success : TestUiState()
    data class Error(val message: String) : TestUiState()
    object AlreadyClaimed : TestUiState()
    object AppNotInstalled : TestUiState()
}

/** Tracks which sequential step the user is on (1 = join group, 2 = download, 3 = timer) */
data class TestSteps(
    val step1Complete: Boolean = false,   // joined ObsidianTesters group
    val step2Complete: Boolean = false,   // opened Play Store download
    val timerStartedAt: Long = 0L         // epoch ms when timer launched
)

@HiltViewModel
class TestViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<TestUiState>(TestUiState.Idle)
    val uiState: StateFlow<TestUiState> = _uiState.asStateFlow()

    private val _listing = MutableStateFlow<AppListing?>(null)
    val listing: StateFlow<AppListing?> = _listing.asStateFlow()

    private val _timerSeconds = MutableStateFlow(30)
    val timerSeconds: StateFlow<Int> = _timerSeconds.asStateFlow()

    private val _steps = MutableStateFlow(TestSteps())
    val steps: StateFlow<TestSteps> = _steps.asStateFlow()

    private var timerJob: Job? = null

    fun loadListing(appId: String) {
        viewModelScope.launch {
            _listing.value = appRepository.getListingById(appId)
        }
    }

    /** Step 1: user tapped Join ObsidianTesters group */
    fun completeStep1() {
        _steps.value = _steps.value.copy(step1Complete = true)
    }

    /** Step 2: user tapped Download on Play Store */
    fun completeStep2() {
        _steps.value = _steps.value.copy(step2Complete = true)
    }

    /** Step 3: start timer — records start time and launches countdown */
    fun startTimer() {
        val packageName = extractPackageName(_listing.value?.playStoreLink)
        if (packageName != null && !isAppInstalled(packageName)) {
            _uiState.value = TestUiState.AppNotInstalled
            return
        }
        val now = System.currentTimeMillis()
        _steps.value = _steps.value.copy(timerStartedAt = now)
        _uiState.value = TestUiState.TimerRunning
        _timerSeconds.value = 30
        timerJob = viewModelScope.launch {
            for (i in 29 downTo 0) {
                delay(1_000)
                _timerSeconds.value = i
            }
            // Timer elapsed while in app — complete
            completeTest()
        }
    }

    /**
     * Called from the screen's onResume (via LaunchedEffect on lifecycle).
     * If timer was started and enough time has passed while user was in the tested app,
     * we complete the test.
     */
    fun onAppResumed() {
        val started = _steps.value.timerStartedAt
        if (started == 0L) return
        val elapsed = System.currentTimeMillis() - started
        if (elapsed >= 30_000L && _uiState.value == TestUiState.TimerRunning) {
            timerJob?.cancel()
            _timerSeconds.value = 0
            completeTest()
        }
    }

    private fun completeTest() {
        val userId = auth.currentUser?.uid ?: return
        val appId = _listing.value?.appId ?: return
        val packageName = extractPackageName(_listing.value?.playStoreLink)

        // Final check: app must be installed
        if (packageName != null && !isAppInstalled(packageName)) {
            _uiState.value = TestUiState.AppNotInstalled
            return
        }

        viewModelScope.launch {
            _uiState.value = TestUiState.Loading
            userRepository.awardTestShards(userId, appId).fold(
                onSuccess = { _uiState.value = TestUiState.Success },
                onFailure = { e ->
                    val msg = e.message ?: "Error"
                    _uiState.value = if (msg.contains("24 hours"))
                        TestUiState.AlreadyClaimed else TestUiState.Error(msg)
                }
            )
        }
    }

    private fun isAppInstalled(packageName: String): Boolean = try {
        context.packageManager.getPackageInfo(packageName, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }

    /** Extracts package name from a Play Store URL, e.g. ?id=com.example.app */
    fun extractPackageName(playStoreLink: String?): String? {
        if (playStoreLink.isNullOrBlank()) return null
        return Uri.parse(playStoreLink).getQueryParameter("id")
    }
}

