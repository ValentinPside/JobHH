package ru.practicum.android.diploma.presentation.vacancy.models

import ru.practicum.android.diploma.domain.models.VacancyDetail

sealed interface VacancyViewState {
    data class Content(val vacancyDetail: VacancyDetail, val isFavorite: Boolean) : VacancyViewState

    data object Loading : VacancyViewState

    data class Error(val isPageNotFound: Boolean = false) : VacancyViewState
}
