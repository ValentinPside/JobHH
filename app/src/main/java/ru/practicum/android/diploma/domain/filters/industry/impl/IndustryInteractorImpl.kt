package ru.practicum.android.diploma.domain.filters.industry.impl

import kotlinx.coroutines.flow.Flow
import ru.practicum.android.diploma.domain.filters.industry.api.IndustryInteractor
import ru.practicum.android.diploma.domain.filters.industry.api.IndustryRepository
import ru.practicum.android.diploma.domain.filters.industry.models.Industry
import ru.practicum.android.diploma.domain.models.Resource
import javax.inject.Inject

class IndustryInteractorImpl @Inject constructor(private val industryRepository: IndustryRepository) :
    IndustryInteractor {
    override suspend fun getIndustries(): Flow<Resource<List<Industry>>> {
        return industryRepository.getIndustries()
    }
}
