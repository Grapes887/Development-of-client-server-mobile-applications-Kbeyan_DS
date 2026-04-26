package com.example.module5.domain.repository

import com.example.module5.domain.model.TodoItem
import kotlinx.coroutines.flow.Flow

interface TodoRepository {
    fun observeTasks(): Flow<List<TodoItem>>
    suspend fun getTask(id: Long): TodoItem?
    suspend fun upsertTask(item: TodoItem)
    suspend fun deleteTask(id: Long)
    suspend fun insertSeedTasks(items: List<TodoItem>)
}
