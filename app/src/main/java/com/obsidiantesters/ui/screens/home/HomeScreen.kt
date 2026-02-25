package com.obsidiantesters.ui.screens.home

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.obsidiantesters.data.model.AppListing
import com.obsidiantesters.ui.components.ObsidianBottomBar
import com.obsidiantesters.ui.theme.CardBackground
import com.obsidiantesters.ui.theme.DarkBackground
import com.obsidiantesters.ui.theme.PrimaryAccent
import com.obsidiantesters.ui.theme.ProBadgeBlue
import com.obsidiantesters.ui.theme.TextPrimary
import com.obsidiantesters.ui.theme.TextSecondary
import com.obsidiantesters.viewmodel.HomeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    onTestApp: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val listings by viewModel.listings.collectAsState()
    var refreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Browse Apps", color = TextPrimary, fontWeight = FontWeight.Bold)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        bottomBar = { ObsidianBottomBar(navController) },
        containerColor = DarkBackground
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = refreshing,
            onRefresh = {
                scope.launch {
                    refreshing = true
                    delay(800)
                    refreshing = false
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 16.dp, vertical = 8.dp
                )
            ) {
                items(listings) { listing ->
                    AppListingCard(listing = listing, onTest = { onTestApp(listing.appId) })
                }
                if (listings.isEmpty()) {
                    item {
                        Box(
                            Modifier.fillMaxWidth().padding(top = 80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No listings yet.", color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppListingCard(listing: AppListing, onTest: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App icon
            if (listing.logoUrl.isNotBlank()) {
                AsyncImage(
                    model = listing.logoUrl,
                    contentDescription = listing.appName,
                    modifier = Modifier.size(52.dp).clip(RoundedCornerShape(10.dp))
                )
            } else {
                Box(
                    Modifier.size(52.dp).clip(RoundedCornerShape(10.dp))
                        .background(PrimaryAccent.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(listing.appName.take(1), color = PrimaryAccent, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        listing.appName,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (listing.isPriority) {
                        Spacer(Modifier.width(6.dp))
                        Badge(containerColor = ProBadgeBlue) {
                            Text("PRO", fontSize = 9.sp, color = Color.White)
                        }
                    }
                }
                Text(listing.developerName, color = TextSecondary, fontSize = 12.sp)
                Spacer(Modifier.height(6.dp))

                // Progress bar
                val progress = if (listing.testersRequired > 0)
                    listing.testersJoined.toFloat() / listing.testersRequired else 0f
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color = PrimaryAccent,
                    trackColor = PrimaryAccent.copy(alpha = 0.2f)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "${listing.testersJoined}/${listing.testersRequired} testers",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                val daysLeft = if (listing.expiresAt > 0L)
                    ((listing.expiresAt - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).coerceAtLeast(0)
                    else 14L
                Text(
                    "${daysLeft}d remaining",
                    color = if (daysLeft <= 2) androidx.compose.ui.graphics.Color(0xFFFF6B6B) else TextSecondary,
                    fontSize = 11.sp
                )
            }

            Spacer(Modifier.width(8.dp))
            Button(
                onClick = onTest,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text("Test", fontSize = 13.sp)
            }
        }
    }
}
