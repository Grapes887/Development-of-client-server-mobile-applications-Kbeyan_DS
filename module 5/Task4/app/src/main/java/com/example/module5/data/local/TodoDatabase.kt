package com.example.module5.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.module5.data.model.TodoEntity

@Database(
    entities = [TodoEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TodoDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao
}
