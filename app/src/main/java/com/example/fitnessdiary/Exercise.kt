package com.example.fitnessdiary

import java.util.concurrent.atomic.AtomicLong
import kotlin.time.Duration

data class Exercise(
    val id: Long,
    var name: String,
    var approaches: Int? = null,
    var repetitions: Int? = null,
    var weight: Double? = null,
    var time: Duration? = null
) {
    init {
        require(weight == null || weight!! >= 0.0) {
            "Масса нагрузки не может быть отрицательной."
        }
    }

    constructor(name: String, time: Duration) : this(nextId(), name, time = time)
    constructor(name: String, approaches: Int, repetitions: Int) :
            this(nextId(), name, approaches, repetitions)
    constructor(name: String, approaches: Int, repetitions: Int, weight: Double) :
            this(nextId(), name, approaches, repetitions, weight)


    // Функция вывода данных о тренировке
    override fun toString() : String {
        var strRes = ""
        strRes += "Название: $name; "

        when {
            time != null && approaches == null && repetitions == null && weight == null -> {
                strRes += "Время: $time."
            }

            time == null && approaches != null && repetitions != null && weight == null -> {
                strRes += "Подходы: $approaches; "
                strRes += "Повторения: $repetitions."
            }

            time == null && approaches != null && repetitions != null && weight != null -> {
                strRes += "Подходы: $approaches; "
                strRes += "Повторения: $repetitions; "
                strRes += "Вес: $weight кг."
            }

            else -> {
                strRes += "Ошибка: неподдерживаемое сочетание параметров упражнения"
            }
        }
        return strRes
    }

    companion object {
        private val idGenerator = AtomicLong(0L)
        private fun nextId(): Long = idGenerator.incrementAndGet()
    }
}