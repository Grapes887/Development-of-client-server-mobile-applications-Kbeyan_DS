package com.example.task8.data

data class PhotoData(
    val originalPath: String = "",
    val compressedPath: String = "",
    val watermarkedPath: String = "",
    val uploadedUrl: String = "",
    val currentStep: Int = 0,
    val progress: Int = 0,
    val error: String? = null
)