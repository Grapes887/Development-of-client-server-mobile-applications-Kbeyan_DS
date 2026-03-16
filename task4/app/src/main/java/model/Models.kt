package com.example.task4.model

import kotlinx.serialization.Serializable

@Serializable
data class Post(
    val id: Int,
    val userId: Int,
    val title: String,
    val body: String,
    val avatarUrl: String
)

@Serializable
data class Comment(
    val postId: Int,
    val id: Int,
    val name: String,
    val body: String
)

enum class LoadingState {
    LOADING, READY, ERROR
}

data class PostWithDetails(
    val post: Post,
    val comments: List<Comment> = emptyList(),
    val state: LoadingState = LoadingState.LOADING
)