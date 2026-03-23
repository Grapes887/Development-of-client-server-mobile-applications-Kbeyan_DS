package com.example.task3

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

suspend fun loadRepos(context: Context): List<Repo> =
    withContext(Dispatchers.IO) {

        val json = context.assets
            .open("github_repos.json")
            .bufferedReader()
            .use { it.readText() }

        val array = JSONArray(json)

        val repos = mutableListOf<Repo>()

        for (i in 0 until array.length()) {

            val obj = array.getJSONObject(i)

            repos.add(
                Repo(
                    id = obj.getInt("id"),
                    full_name = obj.getString("full_name"),
                    description = obj.getString("description"),
                    stargazers_count = obj.getInt("stargazers_count"),
                    language = obj.getString("language")
                )
            )
        }

        repos
    }