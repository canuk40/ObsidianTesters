package com.obsidiantesters.ui.screens.test

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import com.obsidiantesters.ui.theme.CardBackground
import com.obsidiantesters.ui.theme.DarkBackground
import com.obsidiantesters.ui.theme.PrimaryAccent
import com.obsidiantesters.ui.theme.ShardGold
import com.obsidiantesters.ui.theme.TextPrimary
import com.obsidiantesters.ui.theme.TextSecondary
import com.obsidiantesters.viewmodel.TestUiState
import com.obsidiantesters.viewmodel.TestViewModel

private const val OBSIDIAN_TESTERS_GROUP_EMAIL = "obsidiantesters@googlegroups.com"
private const val OBSIDIAN_TESTERS_GROUP_URL = "https://groups.google.com/g/obsidiantesters"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestScreen(
    appId: String,
    onBack: () -> Unit,
    viewModel: TestViewModel = hiltViewModel()
) {
    val listing by viewModel.listing.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val timerSeconds by viewModel.timerSeconds.collectAsState()
    val steps by viewModel.steps.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(appId) { viewModel.loadListing(appId) }

    // Notify ViewModel when user returns to this screen (e.g. after opening tested app)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.onAppResumed()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(listing?.appName ?: "Testing", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // App icon + description
            listing?.logoUrl?.takeIf { it.isNotBlank() }?.let { url ->
                AsyncImage(
                    model = url,
                    contentDescription = listing?.appName,
                    modifier = Modifier.size(72.dp).clip(RoundedCornerShape(14.dp))
                )
            }
            listing?.description?.let { desc ->
                Text(desc, color = TextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center)
            }

            Spacer(Modifier.height(4.dp))

            when (uiState) {
                is TestUiState.Success -> SuccessBanner()
                is TestUiState.AlreadyClaimed -> AlreadyClaimedBanner()
                is TestUiState.AppNotInstalled -> ErrorBanner("App not detected as installed.\nPlease download it from the Play Store first.")
                is TestUiState.Error -> ErrorBanner((uiState as TestUiState.Error).message)
                else -> {
                    // ── STEP 1: Join ObsidianTesters Google Group ──
                    StepCard(
                        stepNumber = 1,
                        title = "Join ObsidianTesters Group",
                        subtitle = "Copies group email & opens Google Groups",
                        isComplete = steps.step1Complete,
                        isLocked = false
                    ) {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("group email", OBSIDIAN_TESTERS_GROUP_EMAIL))
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(OBSIDIAN_TESTERS_GROUP_URL)))
                        viewModel.completeStep1()
                    }

                    // ── STEP 2: Download on Play Store ──
                    StepCard(
                        stepNumber = 2,
                        title = "Download on Play Store",
                        subtitle = "Opens the app's Play Store listing",
                        isComplete = steps.step2Complete,
                        isLocked = !steps.step1Complete
                    ) {
                        listing?.playStoreLink?.let { link ->
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
                        }
                        viewModel.completeStep2()
                    }

                    // ── STEP 3: Timer ──
                    when (uiState) {
                        is TestUiState.TimerRunning -> {
                            TimerDisplay(timerSeconds = timerSeconds)
                            Text(
                                "Keep ${listing?.appName ?: "the app"} open for the full 30 seconds,\nthen return here.",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                        is TestUiState.Loading -> {
                            CircularProgressIndicator(color = PrimaryAccent)
                        }
                        else -> {
                            StepCard(
                                stepNumber = 3,
                                title = "Start 30-Second Test",
                                subtitle = "Opens ${listing?.appName ?: "the app"} — keep it open for 30 seconds",
                                isComplete = false,
                                isLocked = !steps.step2Complete
                            ) {
                                // Launch the tested app first, then start timer
                                val pkg = viewModel.extractPackageName(listing?.playStoreLink)
                                if (pkg != null) {
                                    val launchIntent = context.packageManager.getLaunchIntentForPackage(pkg)
                                    if (launchIntent != null) context.startActivity(launchIntent)
                                    else context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(listing?.playStoreLink)))
                                }
                                viewModel.startTimer()
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StepCard(
    stepNumber: Int,
    title: String,
    subtitle: String,
    isComplete: Boolean,
    isLocked: Boolean,
    onClick: () -> Unit
) {
    val borderColor = when {
        isComplete -> Color(0xFF4CAF50)
        isLocked   -> TextSecondary.copy(alpha = 0.3f)
        else       -> PrimaryAccent
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBackground, RoundedCornerShape(14.dp))
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Step bubble
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isComplete) Color(0xFF4CAF50) else if (isLocked) TextSecondary.copy(alpha = 0.2f) else PrimaryAccent),
                contentAlignment = Alignment.Center
            ) {
                if (isComplete) {
                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                } else if (isLocked) {
                    Icon(Icons.Default.Lock, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                } else {
                    Text("$stepNumber", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = if (isLocked) TextSecondary else TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Text(subtitle, color = TextSecondary, fontSize = 12.sp)
            }
        }

        if (!isLocked && !isComplete) {
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent)
            ) {
                Text("Step $stepNumber", fontWeight = FontWeight.Bold)
            }
        }

        if (isComplete) {
            Spacer(Modifier.height(8.dp))
            Text("✓ Complete", color = Color(0xFF4CAF50), fontSize = 13.sp)
        }
    }
}

@Composable
private fun TimerDisplay(timerSeconds: Int) {
    val progress by animateFloatAsState(targetValue = timerSeconds / 30f, label = "timer")
    Box(contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(130.dp),
            color = PrimaryAccent,
            trackColor = PrimaryAccent.copy(alpha = 0.2f),
            strokeWidth = 8.dp
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$timerSeconds", color = TextPrimary, fontSize = 36.sp, fontWeight = FontWeight.Bold)
            Text("sec", color = TextSecondary, fontSize = 12.sp)
        }
    }
}

@Composable
private fun SuccessBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(ShardGold.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
            .border(1.dp, ShardGold.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("✓ Test Complete!", color = ShardGold, fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Spacer(Modifier.height(8.dp))
            Text("+5 Shards Earned", color = ShardGold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun AlreadyClaimedBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBackground, RoundedCornerShape(14.dp))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "You already earned shards for this app today.\nCome back in 24 hours.",
            color = TextSecondary, textAlign = TextAlign.Center, fontSize = 14.sp
        )
    }
}

@Composable
private fun ErrorBanner(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Red.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(message, color = Color.Red, textAlign = TextAlign.Center, fontSize = 14.sp)
    }
}
