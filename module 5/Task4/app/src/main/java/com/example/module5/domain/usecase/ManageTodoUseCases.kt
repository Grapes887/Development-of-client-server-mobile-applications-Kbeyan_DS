package com.example.module5.domain.usecase

import android.content.Context
import com.example.module5.data.model.SeedTodoItem
import com.example.module5.domain.model.TodoItem
import com.example.module5.domain.repository.AppPreferencesRepository
import com.example.module5.domain.repository.TodoRepository
import org.json.JSONArray

class UpsertTodoUseCase(
    private val todoRepository: TodoRepository
) {
    suspend operator fun invoke(item: TodoItem) {
        todoRepository.upsertTask(item)
    }
}

class DeleteTodoUseCase(
    private val todoRepository: TodoRepository
) {
    suspend operator fun invoke(id: Long) {
        todoRepository.deleteTask(id)
    }
}

class ObserveCompletedColorUseCase(
    private val preferencesRepository: AppPreferencesRepository
) {
    operator fun invoke() = preferencesRepository.observeCompletedColorEnabled()
}

class SetCompletedColorUseCase(
    private val preferencesRepository: AppPreferencesRepository
) {
    suspend operator fun invoke(enabled: Boolean) {
        preferencesRepository.setCompletedColorEnabled(enabled)
    }
}

class EnsureSeedDataUseCase(
    private val appContext: Context,
    private val todoRepository: TodoRepository,
    private val preferencesRepository: AppPreferencesRepository
) {
    suspend operator fun invoke() {
        if (preferencesRepository.isSeedImported()) return

        val rawJson = appContext.assets.open("todo_seed.json")
            .bufferedReader()
            .use { it.readText() }
        val array = JSONArray(rawJson)
        val now = System.currentTimeMillis()
        val items = buildList {
            for (index in 0 until array.length()) {
                val json = array.getJSONObject(index)
                val seedItem = SeedTodoItem(
                    title = json.getString("title"),
                    description = json.getString("description"),
                    isCompleted = json.getBoolean("isCompleted")
                )
                add(
                    TodoItem(
                        title = seedItem.title,
                        description = seedItem.description,
                        isCompleted = seedItem.isCompleted,
                        createdAt = now - (index * 60_000L),
                        updatedAt = now - (index * 60_000L)
                    )
                )
            }
        }
        todoRepository.insertSeedTasks(items)
        preferencesRepository.setSeedImported()
    }
}
