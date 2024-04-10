package ru.practicum.android.diploma.data.converters

import ru.practicum.android.diploma.data.dto.detail.FavoriteVacancyDto
import ru.practicum.android.diploma.data.entity.FavoritesVacanciesEntity

object VacancyDbConvertor {
    fun map(vacancy: FavoriteVacancyDto): FavoritesVacanciesEntity {
        return FavoritesVacanciesEntity(
            vacancy.id,
            vacancy.name,
            vacancy.salary,
            vacancy.experience,
            vacancy.description,
            vacancy.employer,
            vacancy.keySkills,
            vacancy.area,
            vacancy.employment,
            vacancy.schedule
        )
    }

    fun map(vacancy: FavoritesVacanciesEntity): FavoriteVacancyDto {
        return FavoriteVacancyDto(
            vacancy.id,
            vacancy.name,
            vacancy.salary,
            vacancy.experience,
            vacancy.description,
            vacancy.employerDto,
            vacancy.keySkills,
            vacancy.area,
            vacancy.employment,
            vacancy.schedule
        )
    }
}
