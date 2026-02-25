package com.obsidiantesters.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.obsidiantesters.ui.screens.auth.AuthScreen
import com.obsidiantesters.ui.screens.home.HomeScreen
import com.obsidiantesters.ui.screens.myapps.MyAppsScreen
import com.obsidiantesters.ui.screens.mytests.MyTestsScreen
import com.obsidiantesters.ui.screens.post.PostAppScreen
import com.obsidiantesters.ui.screens.pricing.PricingScreen
import com.obsidiantesters.ui.screens.profile.ProfileScreen
import com.obsidiantesters.ui.screens.splash.SplashScreen
import com.obsidiantesters.ui.screens.test.TestScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Auth : Screen("auth")
    object Home : Screen("home")
    object MyTests : Screen("my_tests")
    object PostApp : Screen("post_app")
    object MyApps : Screen("my_apps")
    object Profile : Screen("profile")
    object Test : Screen("test/{appId}") {
        fun createRoute(appId: String) = "test/$appId"
    }
    object Pricing : Screen("pricing")
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, "Browse", Icons.Default.Home),
    BottomNavItem(Screen.MyTests, "Testing", Icons.Default.PlayArrow),
    BottomNavItem(Screen.PostApp, "Post", Icons.Default.Add),
    BottomNavItem(Screen.MyApps, "My Apps", Icons.Default.List),
    BottomNavItem(Screen.Profile, "Profile", Icons.Default.AccountCircle)
)

@Composable
fun ObsidianTestersNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToAuth = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Auth.route) {
            AuthScreen(
                onSignInSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController,
                onTestApp = { appId ->
                    navController.navigate(Screen.Test.createRoute(appId))
                }
            )
        }

        composable(Screen.MyTests.route) {
            MyTestsScreen(
                navController = navController,
                onTestApp = { appId ->
                    navController.navigate(Screen.Test.createRoute(appId))
                }
            )
        }

        composable(Screen.PostApp.route) {
            PostAppScreen(
                navController = navController,
                onNavigateToPricing = { navController.navigate(Screen.Pricing.route) }
            )
        }

        composable(Screen.MyApps.route) {
            MyAppsScreen(navController = navController)
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                navController = navController,
                onSignOut = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onPricing = { navController.navigate(Screen.Pricing.route) }
            )
        }

        composable(Screen.Test.route) { backStack ->
            val appId = backStack.arguments?.getString("appId") ?: ""
            TestScreen(appId = appId, onBack = { navController.popBackStack() })
        }

        composable(Screen.Pricing.route) {
            PricingScreen(navController = navController)
        }
    }
}
