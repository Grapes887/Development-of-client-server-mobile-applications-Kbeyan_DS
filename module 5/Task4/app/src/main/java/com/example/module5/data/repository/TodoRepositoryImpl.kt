package com.example.module5.data.repository

import com.example.module5.data.local.TodoDao
import com.example.module5.data.model.TodoEntity
import com.example.module5.domain.model.TodoItem
import com.example.module5.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TodoRepositoryImpl(
    private val todoDao: TodoDao
) : TodoRepository {
    override fun observeTasks(): Flow<List<TodoItem>> {
        return todoDao.observeAll().map { entities ->
            entities.map(TodoEntity::toDomain)
        }
    }

    override suspend fun getTask(id: Long): TodoItem? {
        return todoDao.getById(id)?.toDomain()
    }

    override suspend fun upsertTask(item: TodoItem) {
        if (item.id == 0L) {
            todoDao.insert(item.toEntity())
        } else {
            todoDao.update(item.toEntity())
        }
    }

    override suspend fun deleteTask(id: Long) {
        todoDao.deleteById(id)
    }

    override suspend fun insertSeedTasks(items: List<TodoItem>) {
        todoDao.insertAll(items.map(TodoItem::toEntity))
    }
}

private fun TodoEntity.toDomain(): TodoItem {
    return TodoItem(
        id = id,
        title = title,
        description = description,
        isCompleted = isCompleted,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

private fun TodoItem.toEntity(): TodoEntity {
    return TodoEntity(
        id = id,
        title = title,
        description = description,
        isCompleted = isCompleted,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
