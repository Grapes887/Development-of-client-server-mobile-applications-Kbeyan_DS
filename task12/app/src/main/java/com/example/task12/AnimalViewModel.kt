package com.example.task12

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.random.Random

class AnimalViewModel : ViewModel() {

    private val facts = listOf(
        "Осьминоги имеют три сердца.",
        "Слоны могут узнавать себя в зеркале.",
        "Дельфины дают друг другу имена.",
        "Кошки могут прыгать в 6 раз выше своего роста.",
        "Крокодилы не могут высовывать язык.",
        "У жирафов такой же шейный отдел, как у человека — 7 позвонков.",
        "Совы могут поворачивать голову на 270 градусов.",
        "Акулы существуют дольше динозавров.",
        "Муравьи никогда не спят.",
        "У коал отпечатки пальцев похожи на человеческие."
    )

    fun getRandomFact(): Flow<String> = flow {

        delay(Random.nextLong(1500, 3000))

        val fact = facts.random()

        emit(fact)
    }
}