package ru.practicum.android.diploma.data.industry.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.practicum.android.diploma.data.converters.FiltersConvertor
import ru.practicum.android.diploma.data.network.NetworkClient
import ru.practicum.android.diploma.domain.filters.industry.api.IndustryRepository
import ru.practicum.android.diploma.domain.filters.industry.models.Industry
import ru.practicum.android.diploma.domain.models.Constants
import ru.practicum.android.diploma.domain.models.IndustriesRequest
import ru.practicum.android.diploma.domain.models.Resource
import javax.inject.Inject

class IndustryRepositoryImpl @Inject constructor(
    private val networkClient: NetworkClient
) : IndustryRepository {
    override suspend fun getIndustries(): Flow<Resource<List<Industry>>> = flow {
        val response = networkClient.doIndustryRequest(IndustriesRequest)
        when (response.resultCode) {
            Constants.NO_CONNECTIVITY_MESSAGE -> {
                emit(Resource(data = null, code = Constants.NO_CONNECTIVITY_MESSAGE))
            }

            Constants.SUCCESS_RESULT_CODE -> {
                emit(
                    Resource(
                        data = response.industriesList.map { industryDto ->
                            FiltersConvertor.convertorToIndustry(
                                industryDto
                            )
                        },
                        code = Constants.SUCCESS_RESULT_CODE
                    )
                )
            }

            else -> {
                emit(Resource(data = null, code = Constants.SERVER_ERROR))
            }
        }
    }
}
