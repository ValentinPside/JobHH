package ru.practicum.android.diploma.data.filters

import ru.practicum.android.diploma.data.dto.VacancyAreaDto
import ru.practicum.android.diploma.data.network.NetworkClient
import ru.practicum.android.diploma.data.network.models.AreasResponse
import ru.practicum.android.diploma.domain.filters.main.api.FiltersRepository
import ru.practicum.android.diploma.domain.filters.models.ResponseStateArea
import ru.practicum.android.diploma.domain.models.Area
import javax.inject.Inject

class FiltersRepositoryImpl @Inject constructor(
    private val retrofitNetworkClient: NetworkClient
) : FiltersRepository {

    companion object {
        const val HTTP_OK = 200
        const val HTTP_CLIENT_ERROR = 400
    }

    override suspend fun getAreas(): ResponseStateArea {
        val response = retrofitNetworkClient.getAreas()
        return if (response.resultCode == HTTP_OK && response is AreasResponse) {
            val listAreas = response.items.map { it.asDomain() }

            if (listAreas.isEmpty()) {
                ResponseStateArea.Empty
            } else {
                ResponseStateArea.ContentArea(listAreas)
            }
        } else if (response.resultCode >= HTTP_CLIENT_ERROR) {
            ResponseStateArea.ServerError
        } else {
            ResponseStateArea.NetworkError
        }
    }

    override suspend fun getAreasById(id: String): ResponseStateArea {
        val response = retrofitNetworkClient.getAreasById(id)
        return if (response.resultCode == HTTP_OK && response is AreasResponse) {
            val listAreas = response.items.map { it.asDomain() }

            val result = mutableListOf<Area>()
            listAreas.forEach {
                addAreas(it, it, result)
            }

            if (listAreas.isEmpty()) {
                ResponseStateArea.Empty
            } else {
                ResponseStateArea.ContentArea(result.toList())
            }
        } else if (response.resultCode >= HTTP_CLIENT_ERROR) {
            ResponseStateArea.ServerError
        } else {
            ResponseStateArea.NetworkError
        }
    }

    private fun addAreas(vacancyArea: Area, parentArea: Area, list: MutableList<Area>) {
        if (!vacancyArea.parentId.isNullOrEmpty()) list.add(vacancyArea)
        vacancyArea.areas.forEach {
            addAreas(
                it.copy(parentArea = parentArea),
                parentArea,
                list
            )
        }
    }
}

private fun VacancyAreaDto.asDomain(): Area {
    return Area(
        id,
        parentId ?: "",
        null,
        name,
        areas.map { it.asDomain() }
    )
}
