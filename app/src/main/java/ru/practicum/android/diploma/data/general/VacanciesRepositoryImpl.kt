package ru.practicum.android.diploma.data.general

import ru.practicum.android.diploma.data.asDomain
import ru.practicum.android.diploma.data.dto.VacanciesResponse
import ru.practicum.android.diploma.data.dto.detail.VacancyDetailDto
import ru.practicum.android.diploma.data.network.NetworkClient
import ru.practicum.android.diploma.domain.api.VacanciesRepository
import ru.practicum.android.diploma.domain.general.models.ResponseState
import javax.inject.Inject

const val PAGINATION_COUNT_PAGES = "20"

class VacanciesRepositoryImpl @Inject constructor(
    private val networkClient: NetworkClient
) : VacanciesRepository {

    companion object {
        const val HTTP_OK = 200
        const val HTTP_CLIENT_ERROR = 400
    }

    override suspend fun search(text: String, page: Int, filters: Map<String, String>): ResponseState {
        val query = mapOf(
            "text" to text,
            "page" to page.toString(),
            "per_page" to PAGINATION_COUNT_PAGES
        ).plus(filters)
        val response = networkClient.doRequest(query)
        return if (response.resultCode == HTTP_OK && response is VacanciesResponse) {
            val listVacancies = response.items.asDomain()

            if (listVacancies.isEmpty()) {
                ResponseState.Empty
            } else {
                ResponseState.ContentVacanciesList(listVacancies, response.found, response.pages)
            }
        } else if (response.resultCode >= HTTP_CLIENT_ERROR) {
            ResponseState.ServerError
        } else {
            ResponseState.NetworkError(false)
        }
    }

    override suspend fun searchById(id: String): ResponseState {
        val response = networkClient.doRequestById(id)
        return if (
            response.resultCode == HTTP_OK && response is VacancyDetailDto
        ) {
            ResponseState.ContentVacancyDetail(response.asDomain())
        } else if (
            response.resultCode == HTTP_CLIENT_ERROR
        ) {
            ResponseState.ServerError
        } else {
            ResponseState.NetworkError(false)
        }
    }
}
