package ru.practicum.android.diploma.domain.filters.industry.api

import kotlinx.coroutines.flow.Flow
import ru.practicum.android.diploma.domain.filters.industry.models.Industry
import ru.practicum.android.diploma.domain.models.Resource

interface IndustryRepository {
    suspend fun getIndustries(): Flow<Resource<List<Industry>>>
}
