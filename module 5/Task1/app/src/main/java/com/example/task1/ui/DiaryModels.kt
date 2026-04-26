package com.example.task1.ui

data class DiaryEntry(
    val fileName: String,
    val title: String,
    val text: String,
    val timestamp: Long
) {
    val preview: String
        get() = text.trim().take(40)
}

data class EditorState(
    val fileName: String? = null,
    val title: String = "",
    val text: String = ""
) {
    val isNew: Boolean
        get() = fileName == null
}
