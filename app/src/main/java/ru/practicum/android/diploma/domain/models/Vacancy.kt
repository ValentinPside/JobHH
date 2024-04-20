package ru.practicum.android.diploma.domain.models

data class Vacancy(
    val id: String,
    val img: String?,
    val employer: String,
    val name: String,
    val salary: Salary?,
    val area: String?,
    var isFavorite: Boolean = false
)
