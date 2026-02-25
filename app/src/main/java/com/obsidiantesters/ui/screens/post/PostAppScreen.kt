package com.obsidiantesters.ui.screens.post

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.obsidiantesters.ui.components.ObsidianBottomBar
import com.obsidiantesters.ui.components.ShardBalance
import com.obsidiantesters.ui.theme.CardBackground
import com.obsidiantesters.ui.theme.DarkBackground
import com.obsidiantesters.ui.theme.PrimaryAccent
import com.obsidiantesters.ui.theme.ShardGold
import com.obsidiantesters.ui.theme.TextPrimary
import com.obsidiantesters.ui.theme.TextSecondary
import com.obsidiantesters.viewmodel.PostAppState
import com.obsidiantesters.viewmodel.PostAppViewModel

data class TesterPackage(val label: String, val shardCost: Int, val testersRequired: Int)

val testerPackages = listOf(
    TesterPackage("75 shards — 12 testers (standard)", 75, 12),
    TesterPackage("100 shards — 20 testers", 100, 20)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostAppScreen(
    navController: NavController,
    onNavigateToPricing: () -> Unit,
    viewModel: PostAppViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val balance by viewModel.shardBalance.collectAsState()
    val selectedImageUri by viewModel.selectedImageUri.collectAsState()

    var appName by remember { mutableStateOf("") }
    var devName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var playStoreLink by remember { mutableStateOf("") }
    var googleGroupEmail by remember { mutableStateOf("") }
    var selectedPackage by remember { mutableStateOf(testerPackages[0]) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> viewModel.onImageSelected(uri) }

    LaunchedEffect(Unit) { viewModel.loadBalance() }
    LaunchedEffect(state) {
        if (state is PostAppState.Success) navController.popBackStack()
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = PrimaryAccent,
        unfocusedBorderColor = TextSecondary,
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        cursorColor = PrimaryAccent
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post App", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        bottomBar = { ObsidianBottomBar(navController) },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Your balance: ", color = TextSecondary, fontSize = 14.sp)
                ShardBalance(balance = balance)
            }

            // ── Play Console Notice ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryAccent.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                    .border(1.dp, PrimaryAccent.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = PrimaryAccent,
                    modifier = Modifier.size(20.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Required before submitting",
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Text(
                        "You must add obsidiantesters@googlegroups.com as a tester in your Google Play Console before your listing goes live. Go to Play Console → your app → Testing → Internal/Closed testing → Manage testers, then add the email above.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            // ── App Icon Picker ──
            Text("App Icon", color = TextPrimary, fontWeight = FontWeight.SemiBold)
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardBackground)
                    .border(
                        width = 2.dp,
                        color = if (selectedImageUri != null) PrimaryAccent else TextSecondary.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { imagePicker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "App icon preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Add, null, tint = TextSecondary, modifier = Modifier.size(28.dp))
                        Text("Add Icon", color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }
            if (selectedImageUri != null) {
                Text(
                    "Tap icon to change",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            OutlinedTextField(
                value = appName, onValueChange = { appName = it },
                label = { Text("App Name", color = TextSecondary) },
                modifier = Modifier.fillMaxWidth(), colors = fieldColors
            )
            OutlinedTextField(
                value = devName, onValueChange = { devName = it },
                label = { Text("Developer Name", color = TextSecondary) },
                modifier = Modifier.fillMaxWidth(), colors = fieldColors
            )
            OutlinedTextField(
                value = description,
                onValueChange = { if (it.length <= 1500) description = it },
                label = { Text("Description (max 1500 chars)", color = TextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3, maxLines = 6, colors = fieldColors,
                supportingText = { Text("${description.length}/1500", color = TextSecondary) }
            )
            OutlinedTextField(
                value = playStoreLink, onValueChange = { playStoreLink = it },
                label = { Text("Play Store Link", color = TextSecondary) },
                modifier = Modifier.fillMaxWidth(), colors = fieldColors
            )
            OutlinedTextField(
                value = googleGroupEmail, onValueChange = { googleGroupEmail = it },
                label = { Text("Google Group Email", color = TextSecondary) },
                modifier = Modifier.fillMaxWidth(), colors = fieldColors
            )

            // Tester package selection
            Text("Tester Package", color = TextPrimary, fontWeight = FontWeight.SemiBold)
            testerPackages.forEach { pkg ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (selectedPackage == pkg) PrimaryAccent.copy(alpha = 0.1f)
                            else CardBackground,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = selectedPackage == pkg,
                        onClick = { selectedPackage = pkg },
                        colors = RadioButtonDefaults.colors(selectedColor = PrimaryAccent)
                    )
                    Text(pkg.label, color = TextPrimary, fontSize = 14.sp)
                }
            }

            if (state is PostAppState.InsufficientShards) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardBackground, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Text("Insufficient shards!", color = ShardGold, fontWeight = FontWeight.Bold)
                    Text(
                        "You need ${selectedPackage.shardCost} shards but have $balance.",
                        color = TextSecondary, fontSize = 13.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = onNavigateToPricing,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                        shape = RoundedCornerShape(8.dp)
                    ) { Text("View Bypass Options") }
                }
            }

            if (state is PostAppState.Error) {
                Text((state as PostAppState.Error).message, color = Color.Red, fontSize = 13.sp)
            }

            val isLoading = state is PostAppState.Loading || state is PostAppState.UploadingImage
            val isFormValid = appName.isNotBlank() && devName.isNotBlank() &&
                description.isNotBlank() && playStoreLink.isNotBlank() && googleGroupEmail.isNotBlank()
            Button(
                onClick = {
                    viewModel.postApp(
                        appName = appName,
                        developerName = devName,
                        description = description,
                        playStoreLink = playStoreLink,
                        googleGroupEmail = googleGroupEmail,
                        shardCost = selectedPackage.shardCost,
                        testersRequired = selectedPackage.testersRequired,
                        isPriority = false
                    )
                },
                enabled = !isLoading && isFormValid,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent)
            ) {
                if (state is PostAppState.UploadingImage) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.size(8.dp))
                    Text("Uploading image...")
                } else if (state is PostAppState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Text("Submit (${selectedPackage.shardCost} shards)", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

