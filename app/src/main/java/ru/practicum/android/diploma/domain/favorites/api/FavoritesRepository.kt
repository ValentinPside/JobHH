package ru.practicum.android.diploma.domain.favorites.api

import kotlinx.coroutines.flow.Flow
import ru.practicum.android.diploma.data.dto.detail.FavoriteVacancyDto
import ru.practicum.android.diploma.domain.models.Vacancy

interface FavoritesRepository {
    fun favoritesVacancies(): Flow<List<FavoriteVacancyDto>>
    suspend fun deleteDbVacanciFromFavorite(vacID: String)
    suspend fun insertDbVacanciToFavorite(vacanci: FavoriteVacancyDto)

}