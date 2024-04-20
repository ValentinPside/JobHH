package ru.practicum.android.diploma.data.favorites.impl

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.practicum.android.diploma.data.db.AppDatabase
import ru.practicum.android.diploma.data.favorites.entity.FavoritesVacanciesEntity
import ru.practicum.android.diploma.domain.favorites.api.FavoritesRepository
import ru.practicum.android.diploma.domain.models.Salary
import ru.practicum.android.diploma.domain.models.Vacancy
import ru.practicum.android.diploma.domain.models.VacancyDetail
import javax.inject.Inject

class FavoritesRepositoryImpl @Inject constructor(
    private val appDatabase: AppDatabase
) : FavoritesRepository {
    override fun favoritesVacancies(query: String): Flow<List<Vacancy>> = flow {
        val vacancyList = appDatabase.favoriteDao().getVacancyFromFavorite(query)
        emit(convertFromVacancyEntity(vacancyList))
    }

    override suspend fun deleteDbVacanciFromFavorite(vacID: String) {
        appDatabase.favoriteDao().deleteVacancyFromFavorite(vacID)
    }

    override suspend fun insertDbVacanciToFavorite(vacancyDetail: VacancyDetail) {
        appDatabase.favoriteDao().insertFavoritiesVacancy(convertToVacancyEntity(vacancyDetail))
    }

    override suspend fun isFavorite(vacID: String): Boolean {
        return appDatabase.favoriteDao().elementById(vacID).isNotEmpty()
    }

    override suspend fun loadFavoriteVacancy(vacID: String): VacancyDetail? {
        val list = appDatabase.favoriteDao().elementById(vacID)
        return if (list.isEmpty()) {
            null
        } else {
            list.first().mapToVacancyDetail()
        }
    }

    private fun convertFromVacancyEntity(vacancyList: List<FavoritesVacanciesEntity>): List<Vacancy> {
        return vacancyList.map { vac -> vac.mapToDomain() }
    }

    private fun convertToVacancyEntity(vacancyDetail: VacancyDetail): FavoritesVacanciesEntity {
        return vacancyDetail.mapToDomain()
    }
}

private fun FavoritesVacanciesEntity.mapToVacancyDetail(): VacancyDetail {
    val gson = Gson()
    return gson.fromJson<VacancyDetail>(
        vacancyDetailData,
        object : TypeToken<VacancyDetail>() {}.type
    )
}

private fun VacancyDetail.mapToDomain(): FavoritesVacanciesEntity {
    val gson = Gson()
    return FavoritesVacanciesEntity(
        id,
        employer?.logoUrls ?: "",
        employer?.name ?: "",
        name,
        area ?: "",
        gson.toJson(this),
        salary?.currency ?: "",
        salary?.from ?: 0,
        salary?.to ?: 0,
        salary?.gross ?: false
    )
}

private fun FavoritesVacanciesEntity.mapToDomain(): Vacancy {
    return Vacancy(
        id,
        img,
        employer,
        name,
        Salary(
            salaryCurrency,
            salaryFrom,
            salaryGross,
            salaryTo
        ),
        area
    )
}
