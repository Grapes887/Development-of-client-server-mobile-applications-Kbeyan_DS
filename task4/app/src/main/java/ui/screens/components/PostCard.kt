package com.example.task4.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.task4.model.PostWithDetails
import com.example.task4.model.LoadingState
import com.example.task4.model.Comment

@Composable
fun PostCard(postWithDetails: PostWithDetails) {
    val post = postWithDetails.post
    val state = postWithDetails.state

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Шапка с аватаркой и заголовком
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Аватарка
                when (state) {
                    LoadingState.LOADING -> {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(30.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                    LoadingState.ERROR -> {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "❌", fontSize = 24.sp)
                        }
                    }
                    LoadingState.READY -> {
                        AsyncImage(
                            model = post.avatarUrl,
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Заголовок
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Текст поста
            Text(
                text = post.body,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Разделитель
            HorizontalDivider()

            Spacer(modifier = Modifier.height(8.dp))

            // Заголовок комментариев
            Text(
                text = "Комментарии (${postWithDetails.comments.size}):",
                style = MaterialTheme.typography.labelMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Комментарии
            when (state) {
                LoadingState.LOADING -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
                LoadingState.ERROR -> {
                    Text(
                        text = "Не удалось загрузить комментарии",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(4.dp)
                    )
                }
                LoadingState.READY -> {
                    if (postWithDetails.comments.isEmpty()) {
                        Text(
                            text = "Нет комментариев",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(4.dp)
                        )
                    } else {
                        // Используем Column вместо LazyColumn внутри LazyColumn
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            postWithDetails.comments.forEach { comment ->
                                CommentItem(comment)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommentItem(comment: Comment) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = comment.name,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 12.sp
        )
        Text(
            text = comment.body,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 12.sp
        )
    }
}