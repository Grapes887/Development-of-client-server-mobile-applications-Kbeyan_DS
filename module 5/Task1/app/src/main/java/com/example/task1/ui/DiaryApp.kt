package com.example.task1.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun DiaryApp(viewModel: DiaryViewModel) {
    var currentEditorState by remember { mutableStateOf<EditorState?>(null) }
    val entries by viewModel.entries.collectAsState()

    if (currentEditorState == null) {
        DiaryListScreen(
            entries = entries,
            formatDate = viewModel::formatDate,
            onCreateNew = { currentEditorState = viewModel.startNewEntry() },
            onOpenEntry = { fileName -> currentEditorState = viewModel.startEditEntry(fileName) },
            onDeleteEntry = viewModel::deleteEntry
        )
    } else {
        DiaryEditorScreen(
            initialState = currentEditorState!!,
            onBack = { currentEditorState = null },
            onSave = { state ->
                viewModel.saveEntry(state) {
                    currentEditorState = null
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DiaryListScreen(
    entries: List<DiaryEntry>,
    formatDate: (Long) -> String,
    onCreateNew: () -> Unit,
    onOpenEntry: (String) -> Unit,
    onDeleteEntry: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Дневник") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateNew) {
                Text("+")
            }
        }
    ) { innerPadding ->
        if (entries.isEmpty()) {
            EmptyDiaryState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { Spacer(modifier = Modifier.height(2.dp)) }
                items(items = entries, key = { it.fileName }) { entry ->
                    EntryItem(
                        entry = entry,
                        dateText = formatDate(entry.timestamp),
                        onOpen = { onOpenEntry(entry.fileName) },
                        onDelete = { onDeleteEntry(entry.fileName) }
                    )
                }
                item { Spacer(modifier = Modifier.height(6.dp)) }
            }
        }
    }
}

@Composable
private fun EmptyDiaryState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "У вас пока нет записей",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Нажмите +, чтобы создать первую",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DiaryEditorScreen(
    initialState: EditorState,
    onBack: () -> Unit,
    onSave: (EditorState) -> Unit
) {
    var title by remember(initialState.fileName) { mutableStateOf(initialState.title) }
    var text by remember(initialState.fileName) { mutableStateOf(initialState.text) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (initialState.isNew) "Новая запись" else "Редактирование") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Назад") }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Заголовок (опционально)") }
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                label = { Text("Текст записи") }
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onBack) {
                    Text("Назад")
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = {
                        onSave(
                            initialState.copy(
                                title = title,
                                text = text
                            )
                        )
                    },
                    enabled = text.isNotBlank()
                ) {
                    Text("Сохранить")
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EntryItem(
    entry: DiaryEntry,
    dateText: String,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onOpen,
                onLongClick = { expanded = true }
            ),
        colors = CardDefaults.cardColors()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (entry.title.isBlank()) "Без заголовка" else entry.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = dateText,
                        style = MaterialTheme.typography.labelLarge
                    )
                    IconButton(onClick = { expanded = true }) { Text("⋮") }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = entry.preview,
                style = MaterialTheme.typography.bodyMedium
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Открыть") },
                    onClick = {
                        expanded = false
                        onOpen()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Удалить") },
                    onClick = {
                        expanded = false
                        onDelete()
                    }
                )
            }
        }
    }
}
