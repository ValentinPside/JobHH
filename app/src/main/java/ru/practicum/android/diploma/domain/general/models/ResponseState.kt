package ru.practicum.android.diploma.domain.general.models

import ru.practicum.android.diploma.domain.models.Vacancy
import ru.practicum.android.diploma.domain.models.VacancyDetail

sealed class ResponseState {

    data object Start : ResponseState()
    data object Empty : ResponseState()
    data class ContentVacanciesList(val listVacancy: List<Vacancy>, val found: Int, val pages: Int) : ResponseState()
    data class ContentVacancyDetail(val vacancyDetail: VacancyDetail) : ResponseState()
    data object NetworkError : ResponseState()
    data object ServerError : ResponseState()
    data class Loading(val isPagination: Boolean) : ResponseState()

}
