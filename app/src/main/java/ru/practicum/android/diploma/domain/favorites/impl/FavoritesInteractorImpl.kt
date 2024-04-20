package ru.practicum.android.diploma.domain.favorites.impl

import kotlinx.coroutines.flow.Flow
import ru.practicum.android.diploma.domain.favorites.api.FavoritesInteractor
import ru.practicum.android.diploma.domain.favorites.api.FavoritesRepository
import ru.practicum.android.diploma.domain.models.Vacancy
import ru.practicum.android.diploma.domain.models.VacancyDetail
import javax.inject.Inject

class FavoritesInteractorImpl
@Inject constructor(private val favoritesRepository: FavoritesRepository) : FavoritesInteractor {
    override fun favoritesVacancies(query: String): Flow<List<Vacancy>> {
        return favoritesRepository.favoritesVacancies(query)
    }

    override suspend fun insertDbVacanciToFavorite(vacancyDetail: VacancyDetail) {
        favoritesRepository.insertDbVacanciToFavorite(vacancyDetail)
    }

    override suspend fun deleteDbVacanciFromFavorite(vacID: String) {
        favoritesRepository.deleteDbVacanciFromFavorite(vacID)
    }

    override suspend fun isFavorite(vacID: String): Boolean {
        return favoritesRepository.isFavorite(vacID)
    }

    override suspend fun loadFavoriteVacancy(vacID: String): VacancyDetail? {
        return favoritesRepository.loadFavoriteVacancy(vacID)
    }
}
