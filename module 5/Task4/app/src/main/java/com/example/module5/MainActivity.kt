package com.example.module5

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.room.Room
import com.example.module5.data.local.TodoDatabase
import com.example.module5.data.preferences.AppPreferencesDataStore
import com.example.module5.data.repository.TodoRepositoryImpl
import com.example.module5.domain.usecase.DeleteTodoUseCase
import com.example.module5.domain.usecase.EnsureSeedDataUseCase
import com.example.module5.domain.usecase.ObserveCompletedColorUseCase
import com.example.module5.domain.usecase.ObserveTasksUseCase
import com.example.module5.domain.usecase.SetCompletedColorUseCase
import com.example.module5.domain.usecase.UpsertTodoUseCase
import com.example.module5.navigation.AppNavGraph
import com.example.module5.presentation.viewmodel.TodoViewModel

class MainActivity : ComponentActivity() {
    private val todoDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            TodoDatabase::class.java,
            "todo_database"
        ).build()
    }
    private val todoRepository by lazy {
        TodoRepositoryImpl(todoDatabase.todoDao())
    }
    private val preferencesRepository by lazy {
        AppPreferencesDataStore(applicationContext)
    }
    private val viewModel by viewModels<TodoViewModel> {
        TodoViewModel.Factory(
            observeTasksUseCase = ObserveTasksUseCase(todoRepository),
            upsertTodoUseCase = UpsertTodoUseCase(todoRepository),
            deleteTodoUseCase = DeleteTodoUseCase(todoRepository),
            observeCompletedColorUseCase = ObserveCompletedColorUseCase(preferencesRepository),
            setCompletedColorUseCase = SetCompletedColorUseCase(preferencesRepository),
            ensureSeedDataUseCase = EnsureSeedDataUseCase(
                appContext = applicationContext,
                todoRepository = todoRepository,
                preferencesRepository = preferencesRepository
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TodoTheme {
                AppNavGraph(viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun TodoTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}
