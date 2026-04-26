package com.example.module5.domain.usecase

import com.example.module5.domain.repository.TodoRepository

class ObserveTasksUseCase(
    private val todoRepository: TodoRepository
) {
    operator fun invoke() = todoRepository.observeTasks()
}
