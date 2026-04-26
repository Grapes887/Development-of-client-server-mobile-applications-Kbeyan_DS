package com.example.module5.domain.model

data class TodoItem(
    val id: Long = 0L,
    val title: String,
    val description: String,
    val isCompleted: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)
