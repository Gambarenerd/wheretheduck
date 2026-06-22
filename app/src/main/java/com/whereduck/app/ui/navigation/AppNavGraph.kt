package com.whereduck.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import com.whereduck.app.ui.login.LoginScreen
import com.whereduck.app.ui.contactdetail.ContactDetailScreen
import com.whereduck.app.ui.main.ContactsTab
import com.whereduck.app.ui.main.DashboardTab
import com.whereduck.app.ui.main.CustomizeTab
import com.whereduck.app.ui.main.HistoryTab
import com.whereduck.app.ui.main.MainShell
import com.whereduck.app.ui.permissions.PermissionSetupScreen
import com.whereduck.app.ui.settings.SettingsScreen
import com.whereduck.app.ui.starnazzocall.StarnazzoCallScreen

object Route {
    const val LOGIN = "login"
    const val PERMISSION_SETUP = "permissions"
    const val HOME = "home"
    const val CREATE_GROUP = "create_group"
    const val GROUP_DETAIL = "group_detail/{groupId}"
    const val GROUP_MANAGEMENT = "group_management/{groupId}"
    const val PENDING_INVITES = "invites"
    const val STARNAZZO_CALL = "starnazzo_call/{alertId}/{toName}/{level}"
    const val CONTACT_DETAIL = "contact_detail/{contactId}"
    const val SETTINGS = "settings"
    const val CUSTOMIZE = "customize"

    fun contactDetail(contactId: String) = "contact_detail/$contactId"
    fun groupDetail(groupId: String) = "group_detail/$groupId"
    fun groupManagement(groupId: String) = "group_management/$groupId"
    fun starnazzoCall(alertId: String, toName: String, level: String) =
        "starnazzo_call/$alertId/${java.net.URLEncoder.encode(toName, "UTF-8")}/$level"
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
            MainShell(
                onOpenUserMenu = {
                    navController.navigate(Route.SETTINGS)
                },
                onCreateGroup = {
                    navController.navigate(Route.CREATE_GROUP)
                },
                dashboardContent = {
                    DashboardTab(
                        onSendStarnazzo = { contactId ->
                            navController.navigate(Route.contactDetail(contactId))
                        },
                        onNavigateToContact = { contactId ->
                            navController.navigate(Route.contactDetail(contactId))
                        }
                    )
                },
                contactsContent = { inviteTrigger ->
                    ContactsTab(
                        onNavigateToContactDetail = { contactId ->
                            navController.navigate(Route.contactDetail(contactId))
                        },
                        onNavigateToGroupDetail = { groupId ->
                            navController.navigate(Route.groupDetail(groupId))
                        },
                        onNavigateToInvites = {
                            navController.navigate(Route.PENDING_INVITES)
                        },
                        inviteTrigger = inviteTrigger
                    )
                },
                historyContent = {
                    HistoryTab(
                        onNavigateToContactDetail = { contactId ->
                            navController.navigate(Route.contactDetail(contactId))
                        }
                    )
                },
                customizeContent = {
                    CustomizeTab()
                }
            )
        }
        composable(
            route = Route.CONTACT_DETAIL,
            arguments = listOf(navArgument("contactId") { type = NavType.StringType }),
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
            exitTransition = { fadeOut(tween(150)) },
            popEnterTransition = { fadeIn(tween(150)) },
            popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) }
        ) {
            ContactDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onContactRemoved = {
                    navController.popBackStack(Route.HOME, inclusive = false)
                }
            )
        }
        composable(
            Route.CREATE_GROUP,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
            exitTransition = { fadeOut(tween(150)) },
            popEnterTransition = { fadeIn(tween(150)) },
            popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) }
        ) {
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
            arguments = listOf(navArgument("groupId") { type = NavType.StringType }),
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
            exitTransition = { fadeOut(tween(150)) },
            popEnterTransition = { fadeIn(tween(150)) },
            popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) }
        ) {
            GroupDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToGroupManagement = { groupId ->
                    navController.navigate(Route.groupManagement(groupId))
                },
                onNavigateToStarnazzoCall = { alertId, toName, level ->
                    navController.navigate(Route.starnazzoCall(alertId, toName, level))
                },
                onNavigateToContactDetail = { contactId ->
                    navController.navigate(Route.contactDetail(contactId))
                }
            )
        }
        composable(
            route = Route.GROUP_MANAGEMENT,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType }),
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
            exitTransition = { fadeOut(tween(150)) },
            popEnterTransition = { fadeIn(tween(150)) },
            popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) }
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            GroupManagementScreen(
                groupId = groupId,
                onNavigateBack = { navController.popBackStack() },
                onGroupDeleted = {
                    navController.popBackStack(Route.HOME, inclusive = false)
                }
            )
        }
        composable(
            Route.PENDING_INVITES,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
            exitTransition = { fadeOut(tween(150)) },
            popEnterTransition = { fadeIn(tween(150)) },
            popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) }
        ) {
            PendingInvitesScreen(
                onNavigateBack = { navController.popBackStack() },
                onInviteAccepted = { navController.popBackStack() }
            )
        }
        composable(
            route = Route.STARNAZZO_CALL,
            arguments = listOf(
                navArgument("alertId") { type = NavType.StringType },
                navArgument("toName") { type = NavType.StringType },
                navArgument("level") { type = NavType.StringType }
            )
        ) {
            StarnazzoCallScreen(
                onDismiss = { navController.popBackStack() }
            )
        }
        composable(
            Route.SETTINGS,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
            exitTransition = { fadeOut(tween(150)) },
            popEnterTransition = { fadeIn(tween(150)) },
            popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) }
        ) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Route.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
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
