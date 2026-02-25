package com.obsidiantesters.ui.screens.myapps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.obsidiantesters.ui.components.ObsidianBottomBar
import com.obsidiantesters.ui.theme.CardBackground
import com.obsidiantesters.ui.theme.DarkBackground
import com.obsidiantesters.ui.theme.PrimaryAccent
import com.obsidiantesters.ui.theme.TextPrimary
import com.obsidiantesters.ui.theme.TextSecondary
import com.obsidiantesters.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAppsScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val myApps by viewModel.myApps.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadProfileData() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Apps", color = TextPrimary, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        bottomBar = { ObsidianBottomBar(navController) },
        containerColor = DarkBackground
    ) { padding ->
        if (myApps.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No apps posted yet.", color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(myApps) { app ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CardBackground, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(app.appName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(
                                if (app.isActive) "Active" else "Inactive",
                                color = if (app.isActive) PrimaryAccent else TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        Text(app.developerName, color = TextSecondary, fontSize = 12.sp)
                        val progress = if (app.testersRequired > 0)
                            app.testersJoined.toFloat() / app.testersRequired else 0f
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = PrimaryAccent,
                            trackColor = PrimaryAccent.copy(alpha = 0.2f)
                        )
                        Text(
                            "${app.testersJoined}/${app.testersRequired} testers joined",
                            color = TextSecondary, fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
