package com.example.module5.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.module5.presentation.ui.screen.TodoEditorScreen
import com.example.module5.presentation.ui.screen.TodoListScreen
import com.example.module5.presentation.viewmodel.TodoViewModel

private const val LIST_ROUTE = "todo_list"
private const val EDITOR_ROUTE = "todo_editor/{taskId}"

@Composable
fun AppNavGraph(
    viewModel: TodoViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = LIST_ROUTE,
        modifier = modifier
    ) {
        composable(LIST_ROUTE) {
            TodoListScreen(
                tasks = uiState.tasks,
                highlightCompleted = uiState.highlightCompleted,
                onToggleHighlightCompleted = viewModel::setHighlightCompleted,
                onToggleTaskCompleted = viewModel::setTaskCompleted,
                onCreateTask = { navController.navigate("todo_editor/new") },
                onEditTask = { taskId -> navController.navigate("todo_editor/${Uri.encode(taskId.toString())}") },
                onDeleteTask = viewModel::deleteTask
            )
        }
        composable(
            route = EDITOR_ROUTE,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val rawTaskId = backStackEntry.arguments?.getString("taskId")?.let(Uri::decode)
            val currentTask = rawTaskId
                ?.takeUnless { it == "new" }
                ?.toLongOrNull()
                ?.let { taskId -> uiState.tasks.firstOrNull { it.id == taskId } }

            TodoEditorScreen(
                task = currentTask,
                onBack = { navController.popBackStack() },
                onSaveTask = { task ->
                    viewModel.saveTask(task)
                    navController.popBackStack()
                }
            )
        }
    }
}
