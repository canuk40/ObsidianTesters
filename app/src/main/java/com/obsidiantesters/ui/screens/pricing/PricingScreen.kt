package com.obsidiantesters.ui.screens.pricing

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.obsidiantesters.data.repository.BillingProducts
import com.obsidiantesters.ui.theme.CardBackground
import com.obsidiantesters.ui.theme.DarkBackground
import com.obsidiantesters.ui.theme.PrimaryAccent
import com.obsidiantesters.ui.theme.ProBadgeBlue
import com.obsidiantesters.ui.theme.ShardGold
import com.obsidiantesters.ui.theme.TextPrimary
import com.obsidiantesters.ui.theme.TextSecondary
import com.obsidiantesters.viewmodel.BillingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PricingScreen(
    navController: NavController,
    viewModel: BillingViewModel = hiltViewModel()
) {
    val products by viewModel.products.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) { viewModel.connectAndLoad() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pricing", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // FREE tier card
            PricingCard(
                title = "FREE",
                price = "$0",
                accentColor = PrimaryAccent,
                features = listOf(
                    "Earn shards by testing apps",
                    "75 shards to post 1 app",
                    "12 testers minimum",
                    "Standard queue position",
                    "Daily testing required"
                ),
                buttonLabel = "Current Plan",
                onButton = null
            )

            // PRO tier card
            val proProduct = products.find { it.productId == BillingProducts.SKU_PRO_MONTHLY }
            PricingCard(
                title = "PRO",
                price = "$5.00 CAD/month",
                accentColor = ProBadgeBlue,
                features = listOf(
                    "Post up to 5 apps simultaneously",
                    "20 testers per app",
                    "Priority queue — apps shown at top",
                    "No shard earning required",
                    "Priority support"
                ),
                buttonLabel = "Subscribe — $5.00 CAD/month",
                onButton = proProduct?.let {
                    { viewModel.launchPurchase(context as Activity, it) }
                }
            )

            // Bypass card
            val bypassProduct = products.find { it.productId == BillingProducts.SKU_BYPASS }
            PricingCard(
                title = "Bypass (One-Time)",
                price = "$0.99 CAD per app",
                accentColor = ShardGold,
                features = listOf(
                    "Skip shard earning requirement",
                    "Post 1 app immediately",
                    "75 shards equivalent credited instantly"
                ),
                buttonLabel = "Buy Bypass — $0.99 CAD",
                onButton = bypassProduct?.let {
                    { viewModel.launchPurchase(context as Activity, it) }
                }
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PricingCard(
    title: String,
    price: String,
    accentColor: Color,
    features: List<String>,
    buttonLabel: String,
    onButton: (() -> Unit)?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBackground, RoundedCornerShape(16.dp))
            .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(title, color = accentColor, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text(price, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.height(4.dp))
        features.forEach { feature ->
            Row {
                Text("• ", color = accentColor)
                Text(feature, color = TextSecondary, fontSize = 14.sp)
            }
        }
        if (onButton != null) {
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onButton,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text(buttonLabel, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
