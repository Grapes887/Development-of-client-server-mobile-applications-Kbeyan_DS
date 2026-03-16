import kotlinx.coroutines.*
import java.io.File
import java.security.MessageDigest
import kotlin.system.measureTimeMillis

fun main() {
    runBlocking {
        val directoryPath = "./test_files" // путь к папке
        val timeoutSeconds = 5L

        val time = measureTimeMillis {
            val result = withTimeoutOrNull(timeoutSeconds * 1000) {
                findDuplicateFiles(directoryPath)
            }

            if (result == null) {
                println("Поиск прерван по таймауту!")
            } else {
                printResults(result)
            }
        }
        println("Время выполнения: ${time / 1000.0} сек.")
    }
}

suspend fun findDuplicateFiles(rootPath: String): Map<String, List<File>> {
    return coroutineScope {
        val rootDir = File(rootPath)
        if (!rootDir.exists()) {
            println("Директория не существует: $rootPath")
            return@coroutineScope emptyMap()
        }

        val jsonFiles = rootDir.walk()
            .filter { it.isFile && it.extension.lowercase() == "json" }
            .toList()

        println(" Найдено JSON-файлов: ${jsonFiles.size}")

        if (jsonFiles.isEmpty()) return@coroutineScope emptyMap()

        val deferredResults = jsonFiles.map { file ->
            async(Dispatchers.IO) {
                file to calcSHA256(file)
            }
        }

        deferredResults.awaitAll()
            .groupBy({ it.second }, { it.first })
            .filter { it.value.size > 1 }
    }
}

suspend fun calcSHA256(file: File): String = withContext(Dispatchers.IO) {
    val digest = MessageDigest.getInstance("SHA-256")
    val bytes = file.readBytes()
    val hashBytes = digest.digest(bytes)

    hashBytes.joinToString("") { "%02x".format(it) }
}

fun printResults(duplicates: Map<String, List<File>>) {
    if (duplicates.isEmpty()) {
        println("Дубликаты не найдены")
        return
    }

    println("\n Найдены дубликаты:")
    duplicates.forEach { (hash, files) ->
        println("\n🔹 Хэш: ${hash.take(8)}...")
        files.forEachIndexed { index, file ->
            println("   ${index + 1}. ${file.path} (${file.length()} байт)")
        }
    }
}