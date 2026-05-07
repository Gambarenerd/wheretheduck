package com.whereduck.app.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.whereduck.app.ui.group.CreateGroupScreen
import com.whereduck.app.ui.group.GroupManagementScreen
import com.whereduck.app.ui.group.PendingInvitesScreen
import com.whereduck.app.ui.groupdetail.GroupDetailScreen
import com.whereduck.app.ui.home.HomeScreen
import com.whereduck.app.ui.login.LoginScreen
import com.whereduck.app.ui.permissions.PermissionSetupScreen

object Route {
    const val LOGIN = "login"
    const val PERMISSION_SETUP = "permissions"
    const val HOME = "home"
    const val CREATE_GROUP = "create_group"
    const val GROUP_DETAIL = "group_detail/{groupId}"
    const val GROUP_MANAGEMENT = "group_management/{groupId}"
    const val PENDING_INVITES = "invites"
    const val SETTINGS = "settings"
    const val PREMIUM = "premium"

    fun groupDetail(groupId: String) = "group_detail/$groupId"
    fun groupManagement(groupId: String) = "group_management/$groupId"
}

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    val startDestination = if (FirebaseAuth.getInstance().currentUser != null) {
        Route.HOME
    } else {
        Route.LOGIN
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Route.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Route.PERMISSION_SETUP) {
                        popUpTo(Route.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(Route.PERMISSION_SETUP) {
            PermissionSetupScreen(
                onAllPermissionsGranted = {
                    navController.navigate(Route.HOME) {
                        popUpTo(Route.PERMISSION_SETUP) { inclusive = true }
                    }
                }
            )
        }
        composable(Route.HOME) {
            HomeScreen(
                onNavigateToGroupDetail = { groupId ->
                    navController.navigate(Route.groupDetail(groupId))
                },
                onNavigateToCreateGroup = {
                    navController.navigate(Route.CREATE_GROUP)
                },
                onNavigateToInvites = {
                    navController.navigate(Route.PENDING_INVITES)
                }
            )
        }
        composable(Route.CREATE_GROUP) {
            CreateGroupScreen(
                onGroupCreated = { groupId ->
                    navController.navigate(Route.groupDetail(groupId)) {
                        popUpTo(Route.CREATE_GROUP) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Route.GROUP_DETAIL,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) {
            GroupDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToGroupManagement = { groupId ->
                    navController.navigate(Route.groupManagement(groupId))
                }
            )
        }
        composable(
            route = Route.GROUP_MANAGEMENT,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            GroupManagementScreen(
                groupId = groupId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Route.PENDING_INVITES) {
            PendingInvitesScreen(
                onNavigateBack = { navController.popBackStack() },
                onInviteAccepted = { navController.popBackStack() }
            )
        }
        composable(Route.SETTINGS) {
            PlaceholderScreen("Settings")
        }
        composable(Route.PREMIUM) {
            PlaceholderScreen("Premium")
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
