package com.obsidiantesters.ui.screens.profile

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.obsidiantesters.ui.theme.TextPrimary
import com.obsidiantesters.ui.theme.TextSecondary
import com.obsidiantesters.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    onSignOut: () -> Unit,
    onPricing: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val user by viewModel.userFlow.collectAsState()
    val myApps by viewModel.myApps.collectAsState()
    val testingProgress by viewModel.testingProgress.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) { viewModel.loadProfileData() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", color = TextPrimary, fontWeight = FontWeight.Bold) },
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
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // Avatar
            user?.photoUrl?.takeIf { it.isNotBlank() }?.let { url ->
                AsyncImage(
                    model = url,
                    contentDescription = "avatar",
                    modifier = Modifier.size(88.dp).clip(CircleShape)
                )
            }

            Text(user?.displayName ?: "", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text(user?.email ?: "", color = TextSecondary, fontSize = 13.sp)

            ShardBalance(balance = user?.shardBalance ?: 0)

            // My Apps section
            SectionHeader("My Apps (${myApps.size})")
            if (myApps.isEmpty()) {
                Text("No apps posted yet.", color = TextSecondary, fontSize = 13.sp)
            } else {
                myApps.forEach { app ->
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .background(CardBackground, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(app.appName, color = TextPrimary, modifier = Modifier.weight(1f))
                        Text("${app.testersJoined}/${app.testersRequired}", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }

            // Testing Progress section
            SectionHeader("Currently Testing (${testingProgress.size})")
            if (testingProgress.isEmpty()) {
                Text("Not testing any apps yet.", color = TextSecondary, fontSize = 13.sp)
            } else {
                testingProgress.forEach { progress ->
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .background(CardBackground, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(progress.appId, color = TextPrimary, modifier = Modifier.weight(1f), fontSize = 13.sp)
                        Text("Day ${progress.daysCounted}", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Community buttons
            Button(
                onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://groups.google.com/g/obsidiantesters")))
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CardBackground)
            ) { Text("Join ObsidianTesters Google Group", color = TextPrimary) }

            Button(
                onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.gg/6nPNYErx")))
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CardBackground)
            ) { Text("Discord Community", color = TextPrimary) }

            Button(
                onClick = onPricing,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CardBackground)
            ) { Text("Pricing & Plans", color = TextPrimary) }

            Button(
                onClick = { viewModel.signOut(); onSignOut() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f))
            ) { Text("Sign Out", color = Color.White) }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = TextPrimary,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        modifier = Modifier.fillMaxWidth()
    )
}
