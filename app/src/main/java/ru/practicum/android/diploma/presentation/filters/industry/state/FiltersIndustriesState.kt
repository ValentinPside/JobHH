package ru.practicum.android.diploma.presentation.filters.industry.state

import ru.practicum.android.diploma.presentation.filters.industry.fragment.IndustriesAdapterItem

sealed interface FiltersIndustriesState {
    data class Success(
        val data: List<IndustriesAdapterItem>,
        val currentIndustryId: String?
    ) : FiltersIndustriesState
    data object Loading : FiltersIndustriesState
    data object Error : FiltersIndustriesState
    data object Empty : FiltersIndustriesState
}
