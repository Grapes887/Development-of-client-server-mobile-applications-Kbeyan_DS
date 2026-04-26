package com.example.task1.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DiaryViewModel(application: Application) : AndroidViewModel(application) {

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    private val filesDir: File = application.filesDir

    private val _entries = MutableStateFlow<List<DiaryEntry>>(emptyList())
    val entries: StateFlow<List<DiaryEntry>> = _entries.asStateFlow()

    init {
        loadEntriesOnce()
    }

    private fun loadEntriesOnce() {
        viewModelScope.launch(Dispatchers.IO) {
            val loadedEntries = filesDir
                .listFiles { file -> file.isFile && file.name.endsWith(".txt") }
                .orEmpty()
                .mapNotNull { file ->
                    parseEntryFromFile(file)
                }
                .sortedByDescending { it.timestamp }

            _entries.value = loadedEntries
        }
    }

    fun startNewEntry(): EditorState = EditorState()

    fun startEditEntry(fileName: String): EditorState {
        val selected = _entries.value.first { it.fileName == fileName }
        return EditorState(
            fileName = selected.fileName,
            title = selected.title,
            text = selected.text
        )
    }

    fun saveEntry(state: EditorState, onSaved: () -> Unit) {
        if (state.text.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            if (state.isNew) {
                val timestamp = System.currentTimeMillis()
                val fileName = buildFileName(timestamp = timestamp, title = state.title)
                val file = File(filesDir, fileName)
                file.writeText(state.text)

                val newEntry = DiaryEntry(
                    fileName = fileName,
                    title = state.title.trim(),
                    text = state.text,
                    timestamp = timestamp
                )
                _entries.value = listOf(newEntry) + _entries.value
            } else {
                val fileName = state.fileName ?: return@launch
                val file = File(filesDir, fileName)
                file.writeText(state.text)

                _entries.value = _entries.value.map { entry ->
                    if (entry.fileName == fileName) {
                        entry.copy(title = state.title.trim(), text = state.text)
                    } else {
                        entry
                    }
                }
            }
            withContext(Dispatchers.Main) {
                onSaved()
            }
        }
    }

    fun deleteEntry(fileName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val file = File(filesDir, fileName)
            if (file.exists()) {
                file.delete()
            }
            _entries.value = _entries.value.filterNot { it.fileName == fileName }
        }
    }

    fun formatDate(timestamp: Long): String = dateFormat.format(Date(timestamp))

    private fun parseEntryFromFile(file: File): DiaryEntry? {
        val fileName = file.name
        val timestamp = fileName.substringBefore("_").toLongOrNull()
            ?: fileName.substringBefore(".txt").toLongOrNull()
            ?: return null

        val titleFromName = fileName
            .removeSuffix(".txt")
            .substringAfter("_", missingDelimiterValue = "")
            .replace("_", " ")

        return DiaryEntry(
            fileName = fileName,
            title = titleFromName,
            text = file.readText(),
            timestamp = timestamp
        )
    }

    private fun buildFileName(timestamp: Long, title: String): String {
        val cleanTitle = title
            .trim()
            .replace(Regex("\\s+"), "_")
            .replace(Regex("[^a-zA-Z0-9_а-яА-Я-]"), "")
            .take(50)

        return if (cleanTitle.isBlank()) {
            "${timestamp}_.txt"
        } else {
            "${timestamp}_${cleanTitle}.txt"
        }
    }
}
