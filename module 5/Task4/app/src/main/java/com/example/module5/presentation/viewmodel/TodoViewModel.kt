package com.example.module5.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.module5.domain.model.TodoItem
import com.example.module5.domain.usecase.DeleteTodoUseCase
import com.example.module5.domain.usecase.EnsureSeedDataUseCase
import com.example.module5.domain.usecase.ObserveCompletedColorUseCase
import com.example.module5.domain.usecase.ObserveTasksUseCase
import com.example.module5.domain.usecase.SetCompletedColorUseCase
import com.example.module5.domain.usecase.UpsertTodoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TodoUiState(
    val tasks: List<TodoItem> = emptyList(),
    val highlightCompleted: Boolean = true
)

class TodoViewModel(
    private val observeTasksUseCase: ObserveTasksUseCase,
    private val upsertTodoUseCase: UpsertTodoUseCase,
    private val deleteTodoUseCase: DeleteTodoUseCase,
    private val observeCompletedColorUseCase: ObserveCompletedColorUseCase,
    private val setCompletedColorUseCase: SetCompletedColorUseCase,
    private val ensureSeedDataUseCase: EnsureSeedDataUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(TodoUiState())
    val uiState: StateFlow<TodoUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            ensureSeedDataUseCase()
        }
        viewModelScope.launch {
            observeTasksUseCase().collect { tasks ->
                _uiState.update { it.copy(tasks = tasks) }
            }
        }
        viewModelScope.launch {
            observeCompletedColorUseCase().collect { enabled ->
                _uiState.update { it.copy(highlightCompleted = enabled) }
            }
        }
    }

    fun saveTask(task: TodoItem) {
        viewModelScope.launch {
            upsertTodoUseCase(task)
        }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch {
            deleteTodoUseCase(id)
        }
    }

    fun setHighlightCompleted(enabled: Boolean) {
        viewModelScope.launch {
            setCompletedColorUseCase(enabled)
        }
    }

    fun setTaskCompleted(task: TodoItem, completed: Boolean) {
        saveTask(task.copy(isCompleted = completed, updatedAt = System.currentTimeMillis()))
    }

    class Factory(
        private val observeTasksUseCase: ObserveTasksUseCase,
        private val upsertTodoUseCase: UpsertTodoUseCase,
        private val deleteTodoUseCase: DeleteTodoUseCase,
        private val observeCompletedColorUseCase: ObserveCompletedColorUseCase,
        private val setCompletedColorUseCase: SetCompletedColorUseCase,
        private val ensureSeedDataUseCase: EnsureSeedDataUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TodoViewModel(
                observeTasksUseCase = observeTasksUseCase,
                upsertTodoUseCase = upsertTodoUseCase,
                deleteTodoUseCase = deleteTodoUseCase,
                observeCompletedColorUseCase = observeCompletedColorUseCase,
                setCompletedColorUseCase = setCompletedColorUseCase,
                ensureSeedDataUseCase = ensureSeedDataUseCase
            ) as T
        }
    }
}
