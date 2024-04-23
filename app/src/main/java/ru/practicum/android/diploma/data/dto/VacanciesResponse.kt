package ru.practicum.android.diploma.data.dto

import ru.practicum.android.diploma.data.network.Response

data class VacanciesResponse(
    val items: List<VacancyDto>,
    val found: Int,
    val page: Int,
    val pages: Int
) : Response()
