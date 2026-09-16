package com.example.fitnessdiary

import kotlin.time.Duration

class Exercise private constructor(
    val id: Int,
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

    constructor(id: Int, name: String, time: Duration) :
            this(id, name, null, null, null, time)

    constructor(id: Int, name: String, approaches: Int, repetitions: Int) :
            this(id, name, approaches, repetitions, null, null)

    constructor(id: Int, name: String, approaches: Int, repetitions: Int, weight: Double) :
            this(id, name, approaches, repetitions, weight, null)

    // Функция вывода данных о тренировке
    fun toString(printId : Boolean = false) : String {
        var strRes : String = ""

        if (printId) {
            strRes += "ID: $id; "
        }
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
}