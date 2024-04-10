package ru.practicum.android.diploma.data.dto.detail

import java.util.Calendar

data class FavoriteVacancyDto(
    val id: String,
    val name: String,
    val salary: String,
    val experience: String,
    val description: String,
    val employer: String,
    val keySkills: String,
    val area: String,
    val employment: String,
    val schedule: String,
    val inDbTime: Long = Calendar.getInstance().time.time
)
