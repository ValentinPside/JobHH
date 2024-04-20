package ru.practicum.android.diploma.domain.favorites.api

import kotlinx.coroutines.flow.Flow
import ru.practicum.android.diploma.domain.models.Vacancy
import ru.practicum.android.diploma.domain.models.VacancyDetail

interface FavoritesRepository {
    fun favoritesVacancies(query: String): Flow<List<Vacancy>>
    suspend fun deleteDbVacanciFromFavorite(vacID: String)
    suspend fun insertDbVacanciToFavorite(vacancyDetail: VacancyDetail)
    suspend fun isFavorite(vacID: String): Boolean
    suspend fun loadFavoriteVacancy(vacID: String): VacancyDetail?
}
