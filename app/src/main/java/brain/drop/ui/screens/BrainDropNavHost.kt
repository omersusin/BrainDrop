package brain.drop.ui.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

sealed class Screen(val route: String) {
    data object Chat : Screen("chat")
    data object Organized : Screen("organized")
    data object Projects : Screen("projects")
    data object Search : Screen("search")
    data object Settings : Screen("settings")
    data object NoteDetail : Screen("note/{noteId}") {
        fun createRoute(noteId: Long) = "note/$noteId"
    }
}

@Composable
fun BrainDropNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Chat.route
    ) {
        composable(Screen.Chat.route) {
            ChatScreen(navController = navController)
        }
        composable(Screen.Organized.route) {
            OrganizedScreen(navController = navController)
        }
        composable(Screen.Projects.route) {
            ProjectsScreen(navController = navController)
        }
        composable(Screen.Search.route) {
            SearchScreen(navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
        composable(Screen.NoteDetail.route) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")?.toLongOrNull()
            NoteDetailScreen(noteId = noteId, navController = navController)
        }
    }
}
