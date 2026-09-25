package com.example.fitnessdiary

import java.util.concurrent.atomic.AtomicLong
import kotlin.time.Duration

class Exercise private constructor(
    val id: Long,
    var name: String,
    var approaches: Int?,
    var repetitions: Int?,
    weight: Double?,
    var time: Duration?
) {
    // Масса нагрузки в кг
    var weight: Double? = weight
        set(value) {
            if (value != null) {
                require(value >= 0.0) {
                    "Масса нагрузки не может быть отрицательной."
                }
            }
            field = value
        }

    constructor(name: String, time: Duration) :
            this(nextId(), name, null, null, null, time)

    constructor(name: String, approaches: Int, repetitions: Int) :
            this(nextId(), name, approaches, repetitions, null, null)

    constructor(name: String, approaches: Int, repetitions: Int, weight: Double) :
            this(nextId(), name, approaches, repetitions, weight, null)

    // Функция вывода данных о тренировке
    override fun toString() : String {
        var strRes : String = ""
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