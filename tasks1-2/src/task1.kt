import kotlinx.coroutines.*
import kotlin.random.Random
import kotlin.system.measureTimeMillis

fun main() = runBlocking {
    val time = measureTimeMillis {
        val usersDeferred = async {
            try {
                loadUsers()
            } catch (e: Exception) {
                println("Ошибка загрузки пользователей")
                emptyList()
            }
        }
        val salesDeferred = async {
            try {
                loadSales()
            } catch (e: Exception) {
                println("Ошибка загрузки продаж")
                emptyMap()
            }
        }
        val weatherDeferred = async {
            try {
                loadWeather()
            } catch (e: Exception) {
                println("Ошибка загрузки погоды")
                emptyList()
            }
        }
        val users = usersDeferred.await()
        val sales = salesDeferred.await()
        val weather = weatherDeferred.await()
        println("Пользователи: $users")
        println("Продажи: $sales")
        println("Погода: $weather")
    }
    println("Общее время выполнения: ${time / 1000.0} cек")
}

suspend fun loadWeather(): List<String> {
    delay(2500)
    randomFail()
    return listOf(
        "Москва: -3°C",
        "Нью-Йорк: -5°C",
        "Токио: 11°C"
    )
}

suspend fun loadUsers(): List<String> {
    delay(1800)
    randomFail()
    return listOf(
        "Alice",
        "Bob",
        "Ivan",
        "Olga"
    )
}

suspend fun loadSales(): Map<String, Int> {
    delay(1200)
    randomFail()
    return mapOf("Coffee" to 42, "Tea" to 19)
}

fun randomFail() {
    if (Random.nextInt(1, 10) == 5){
        throw RuntimeException("Случайный сбой")
    }
}