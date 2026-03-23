package com.example.task4.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.SerializationException
import com.example.task4.model.Post
import com.example.task4.model.Comment
import java.io.IOException

private const val TAG = "SocialRepository"

class SocialRepository(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun loadPosts(): List<Post> = withContext(Dispatchers.IO) {
        delay(500)
        try {
            Log.d(TAG, "Попытка загрузить social_posts.json")
            val jsonString = context.assets.open("social_posts.json")
                .bufferedReader().use { it.readText() }
            Log.d(TAG, "Файл загружен, длина: ${jsonString.length}")
            Log.d(TAG, "Содержимое: $jsonString")

            val result = json.decodeFromString<List<Post>>(jsonString)
            Log.d(TAG, "Успешно декодировано ${result.size} постов")
            result
        } catch (e: IOException) {
            Log.e(TAG, "Ошибка чтения файла: ${e.message}")
            e.printStackTrace()
            emptyList()
        } catch (e: SerializationException) {
            Log.e(TAG, "Ошибка декодирования JSON: ${e.message}")
            e.printStackTrace()
            emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Неизвестная ошибка: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun loadCommentsForPost(postId: Int): List<Comment> = withContext(Dispatchers.IO) {
        delay(800)
        try {
            Log.d(TAG, "Попытка загрузить comments.json для postId: $postId")
            val jsonString = context.assets.open("comments.json")
                .bufferedReader().use { it.readText() }
            Log.d(TAG, "Файл comments.json загружен, длина: ${jsonString.length}")

            val allComments = json.decodeFromString<List<Comment>>(jsonString)
            Log.d(TAG, "Всего комментариев: ${allComments.size}")

            val filtered = allComments.filter { it.postId == postId }
            Log.d(TAG, "Для поста $postId найдено ${filtered.size} комментариев")
            filtered
        } catch (e: IOException) {
            Log.e(TAG, "Ошибка чтения файла comments.json: ${e.message}")
            e.printStackTrace()
            emptyList()
        } catch (e: SerializationException) {
            Log.e(TAG, "Ошибка декодирования comments.json: ${e.message}")
            e.printStackTrace()
            emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Неизвестная ошибка при загрузке комментариев: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun loadAvatar(url: String): String = withContext(Dispatchers.IO) {
        delay(600)
        Log.d(TAG, "Загрузка аватарки: $url")
        return@withContext url
    }
}