package com.example.module5.presentation.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.module5.domain.model.TodoItem
import com.example.module5.presentation.ui.component.TodoTaskCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListScreen(
    tasks: List<TodoItem>,
    highlightCompleted: Boolean,
    onToggleHighlightCompleted: (Boolean) -> Unit,
    onToggleTaskCompleted: (TodoItem, Boolean) -> Unit,
    onCreateTask: () -> Unit,
    onEditTask: (Long) -> Unit,
    onDeleteTask: (Long) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("TodoList + Room", fontWeight = FontWeight.Bold)
                        Text(
                            "Задачи лежат в SQLite через Room",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Цвет завершенных",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Switch(
                            checked = highlightCompleted,
                            onCheckedChange = onToggleHighlightCompleted
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateTask,
                icon = { androidx.compose.material3.Icon(Icons.Rounded.Add, contentDescription = null) },
                text = { Text("Добавить задачу") },
                modifier = Modifier.navigationBarsPadding()
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFF4F0E8), Color(0xFFF9F7F2), Color.White)
                    )
                )
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            if (tasks.isEmpty()) {
                EmptyTodoState(onCreateTask = onCreateTask)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 14.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(tasks, key = { task -> task.id }) { task ->
                        TodoTaskCard(
                            task = task,
                            highlightCompleted = highlightCompleted,
                            onToggleCompleted = { checked -> onToggleTaskCompleted(task, checked) },
                            onEdit = { onEditTask(task.id) },
                            onDelete = { onDeleteTask(task.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyTodoState(onCreateTask: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(shape = CircleShape, color = Color(0xFFE8F0DB)) {
            androidx.compose.material3.Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                modifier = Modifier.padding(18.dp)
            )
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = "Список пока пуст",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Добавьте новую задачу или дождитесь импорта стартовых данных из JSON.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(20.dp))
        ExtendedFloatingActionButton(
            onClick = onCreateTask,
            icon = { androidx.compose.material3.Icon(Icons.Rounded.Add, contentDescription = null) },
            text = { Text("Добавить первую задачу") }
        )
    }
}
