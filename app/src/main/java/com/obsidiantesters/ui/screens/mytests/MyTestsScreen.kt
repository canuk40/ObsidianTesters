package com.obsidiantesters.ui.screens.mytests

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun MyTestsScreen(
    navController: NavController,
    onTestApp: (String) -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val testingProgress by viewModel.testingProgress.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadProfileData() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Tests", color = TextPrimary, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        bottomBar = { ObsidianBottomBar(navController) },
        containerColor = DarkBackground
    ) { padding ->
        if (testingProgress.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Not testing any apps yet.\nBrowse and tap Test to start.", color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(testingProgress) { progress ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CardBackground, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(progress.appId, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("Day ${progress.daysCounted}", color = TextSecondary, fontSize = 13.sp)
                        }
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { onTestApp(progress.appId) },
                            modifier = Modifier.fillMaxWidth().height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent)
                        ) {
                            Text("Continue Testing", fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
