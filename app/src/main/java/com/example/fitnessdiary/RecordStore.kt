package com.example.fitnessdiary

import kotlin.time.Duration.Companion.minutes

//Глобальное хранилище упражнений в памяти приложения.
object RecordStore {

    // Внутренний изменяемый список — снаружи отдаём только для чтения
    private val _records: MutableList<Exercise> = mutableListOf(
        Exercise("Жим штанги лежа", 4, 6, 75.0),
        Exercise("Жим гантелей лежа", 4, 10, 22.0),
        Exercise("Жим от груди", 4, 8, 35.0),
        Exercise("Степпер", 30.minutes),
        Exercise("Отжимания", 3, 20),
        Exercise("Скручивания", 4, 15)
    )

    // Публичный доступ к списку только для чтения.
    val records: List<Exercise> get() = _records

    // Добавить упражнение в хранилище.
    fun add(exercise: Exercise) {
        _records.add(exercise)
    }

    // Удалить упражнение по id.
    fun removeById(id: Long): Boolean {
        return _records.removeAll { it.id == id }
    }

    // Очистить всё хранилище.
    fun clear() {
        _records.clear()
    }

    // Количество записей.
    val size: Int get() = _records.size

    // Есть ли записи.
    val isEmpty: Boolean get() = _records.isEmpty()
}