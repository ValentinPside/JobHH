package ru.practicum.android.diploma.presentation.filters.industry.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.domain.filters.industry.api.IndustryInteractor
import ru.practicum.android.diploma.domain.filters.industry.models.Industry
import ru.practicum.android.diploma.domain.filters.industry.models.SubIndustry
import ru.practicum.android.diploma.domain.filters.models.FilterValue
import ru.practicum.android.diploma.domain.models.Constants.SERVER_ERROR
import ru.practicum.android.diploma.domain.models.Constants.SUCCESS_RESULT_CODE
import ru.practicum.android.diploma.domain.models.Resource
import ru.practicum.android.diploma.domain.sharedpreferences.api.FiltersInteractor
import ru.practicum.android.diploma.domain.sharedpreferences.model.SharedFilterNames
import ru.practicum.android.diploma.presentation.filters.industry.fragment.IndustriesAdapterItem
import ru.practicum.android.diploma.presentation.filters.industry.state.FiltersIndustriesState
import javax.inject.Inject

class IndustryViewModel @Inject constructor(
    private val industryInteractor: IndustryInteractor,
    private val filtersInteractor: FiltersInteractor
) : ViewModel() {

    private val _industriesState = MutableLiveData<ViewState>()
    val industriesState: LiveData<ViewState> = _industriesState
    private var currentIndustriesList = ArrayList<SubIndustry>()
    private var currentIndustry: SubIndustry? = null

    init {
        filtersInteractor.getFilter(SharedFilterNames.INDUSTRY)?.let {
            currentIndustry = SubIndustry(it.id, it.valueString)
        }
        getIndustries()
    }

    fun getIndustries() {
        _industriesState.postValue(ViewState(FiltersIndustriesState.Loading, currentIndustry != null))
        viewModelScope.launch {
            industryInteractor.getIndustries().collect { industry ->
                loadIndustries(industry)
            }
        }
    }

    private fun loadIndustries(industry: Resource<List<Industry>>) {
        if (industry.code == SUCCESS_RESULT_CODE) {
            if (industry.data != null) {
                currentIndustriesList.clear()
                currentIndustriesList.addAll(sortIndustries(industry.data))

                _industriesState.postValue(
                    ViewState(
                        successStatusOnList(currentIndustriesList),
                        currentIndustry != null
                    )
                )
            } else {
                _industriesState.postValue(ViewState(FiltersIndustriesState.Empty, currentIndustry != null))
            }
        } else if (industry.code == SERVER_ERROR) {
            _industriesState.postValue(ViewState(FiltersIndustriesState.Empty, currentIndustry != null))
        } else {
            _industriesState.postValue(ViewState(FiltersIndustriesState.Error, currentIndustry != null))
        }
    }

    private fun successStatusOnList(list: List<SubIndustry>): FiltersIndustriesState.Success {
        return FiltersIndustriesState.Success(data = list.map {
            IndustriesAdapterItem(it, it.id == currentIndustry?.id ?: "")
        }, currentIndustryId = currentIndustry?.id)
    }

    private fun sortIndustries(industriesList: List<Industry>): List<SubIndustry> {
        val sortedSubIndustriesList: MutableList<SubIndustry> = mutableListOf()
        for (industry in industriesList) {
            for (subindustry in industry.industries!!) {
                sortedSubIndustriesList.add(
                    SubIndustry(
                        id = subindustry.id,
                        name = subindustry.name
                    )
                )
            }
            sortedSubIndustriesList.add(
                SubIndustry(
                    id = industry.id,
                    name = industry.name
                )
            )
        }

        return sortedSubIndustriesList.sortedBy { it.name }
    }

    fun filterIndustries(editText: String) {
        if (editText.isNotEmpty()) {
            val filteredList = currentIndustriesList.filter {
                it.name.contains(editText, ignoreCase = true)
            }
            if (filteredList.isNotEmpty()) {
                _industriesState.postValue(ViewState(successStatusOnList(filteredList), currentIndustry != null))
            } else {
                _industriesState.postValue(ViewState(FiltersIndustriesState.Empty, currentIndustry != null))
            }
        } else {
            _industriesState.postValue(ViewState(successStatusOnList(currentIndustriesList), currentIndustry != null))
        }
    }

    fun setCurrentIndustry(industryAdapterItem: IndustriesAdapterItem) {
        currentIndustry = industryAdapterItem.industry

        _industriesState.postValue(
            ViewState(
                _industriesState.value?.stateData ?: FiltersIndustriesState.Empty,
                currentIndustry != null
            )
        )
    }

    fun saveCurrentIndustry() {
        currentIndustry?.let {
            filtersInteractor.setFilter(
                SharedFilterNames.INDUSTRY,
                FilterValue(
                    id = it.id,
                    name = SharedFilterNames.INDUSTRY,
                    valueString = it.name
                )
            )
        }
    }
}

class ViewState(
    val stateData: FiltersIndustriesState,
    var isSelected: Boolean
)
