package com.example.task4.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.*
import com.example.task4.data.SocialRepository
import com.example.task4.model.Post
import com.example.task4.model.Comment
import com.example.task4.model.PostWithDetails
import com.example.task4.model.LoadingState
import com.example.task4.ui.components.PostCard
import android.util.Log

private const val TAG = "FeedScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen() {
    val context = LocalContext.current
    val repository = remember { SocialRepository(context) }
    val scope = rememberCoroutineScope()

    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var postsWithDetails by remember { mutableStateOf<Map<Int, PostWithDetails>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Загрузка постов при запуске
    LaunchedEffect(Unit) {
        try {
            Log.d(TAG, "Начинаем загрузку постов")
            isLoading = true
            val loadedPosts = repository.loadPosts()
            Log.d(TAG, "Загружено постов: ${loadedPosts.size}")
            posts = loadedPosts
            isLoading = false

            if (loadedPosts.isEmpty()) {
                errorMessage = "Не удалось загрузить посты"
            } else {
                // Загружаем детали для каждого поста
                loadedPosts.forEach { post ->
                    scope.launch {
                        try {
                            Log.d(TAG, "Загружаем комментарии для поста ${post.id}")
                            val comments = repository.loadCommentsForPost(post.id)

                            postsWithDetails = postsWithDetails.toMutableMap().apply {
                                this[post.id] = PostWithDetails(
                                    post = post,
                                    comments = comments,
                                    state = LoadingState.READY
                                )
                            }
                            Log.d(TAG, "Комментарии для поста ${post.id} загружены: ${comments.size}")
                        } catch (e: Exception) {
                            Log.e(TAG, "Ошибка загрузки комментариев для поста ${post.id}: ${e.message}")
                            postsWithDetails = postsWithDetails.toMutableMap().apply {
                                this[post.id] = PostWithDetails(
                                    post = post,
                                    state = LoadingState.ERROR
                                )
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Критическая ошибка: ${e.message}")
            e.printStackTrace()
            isLoading = false
            errorMessage = "Ошибка: ${e.message}"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Социальная лента") }
            )
        }
    ) { paddingValues ->
        // ИСПРАВЛЕНИЕ: Добавляем Column с фиксированной высотой
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                errorMessage != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = errorMessage!!,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = {
                                errorMessage = null
                                isLoading = true
                                // Здесь можно добавить перезагрузку
                            }) {
                                Text("Попробовать снова")
                            }
                        }
                    }
                }

                isLoading && posts.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                posts.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Нет постов для отображения")
                    }
                }

                else -> {
                    // ИСПРАВЛЕНИЕ: LazyColumn теперь имеет правильные ограничения
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(posts) { post ->
                            val details = postsWithDetails[post.id]
                            PostCard(
                                postWithDetails = details ?: PostWithDetails(
                                    post = post,
                                    state = LoadingState.LOADING
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}