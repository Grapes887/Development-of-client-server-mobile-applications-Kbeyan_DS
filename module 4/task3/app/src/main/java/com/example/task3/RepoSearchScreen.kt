package com.example.task3

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.*

@Composable
fun RepoSearchScreen() {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var query by remember { mutableStateOf("") }
    var repos by remember { mutableStateOf<List<Repo>>(emptyList()) }
    var filteredRepos by remember { mutableStateOf<List<Repo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    var searchJob by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(Unit) {
        repos = loadRepos(context)
        filteredRepos = repos
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        OutlinedTextField(
            value = query,
            onValueChange = { text ->

                query = text

                searchJob?.cancel()

                searchJob = scope.launch {

                    delay(500)

                    isLoading = true

                    filteredRepos = repos.filter {
                        it.full_name.contains(query, ignoreCase = true)
                    }

                    isLoading = false
                }
            },
            label = { Text("Search GitHub repositories") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
        }
        LazyColumn {

            items(filteredRepos) { repo ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {

                        Text(
                            text = repo.full_name,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(repo.description)

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(" ${repo.stargazers_count}   |   ${repo.language}")
                    }
                }
            }
        }
    }
}