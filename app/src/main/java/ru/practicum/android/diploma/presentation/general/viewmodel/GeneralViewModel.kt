package ru.practicum.android.diploma.presentation.general.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.domain.general.api.SearchVacanciesUseCase
import ru.practicum.android.diploma.domain.general.models.ResponseState
import ru.practicum.android.diploma.domain.models.Vacancy
import ru.practicum.android.diploma.domain.sharedpreferences.api.FiltersInteractor
import javax.inject.Inject

class GeneralViewModel @Inject constructor(
    private val searchVacanciesUseCase: SearchVacanciesUseCase,
    private val filtersInteractor: FiltersInteractor
) : ViewModel() {

    private var nextPageNumber = 0

    private val state = MutableLiveData<ResponseState>()

    private val stateFilters = MutableLiveData<Boolean>()

    private var currentListVacancies = mutableListOf<Vacancy>()

    fun observeUi(): LiveData<ResponseState> = state
    fun observeFilters(): LiveData<Boolean> = stateFilters

    private var isNextPageLoading = false

    private var query: String? = null
        set(value) {
            maxPages = null
            field = value
        }

    private var maxPages: Int? = 0

    fun search(query: String) {
        if (this.query == query) return
        this.query = query

        if (query.isEmpty()) {
            state.postValue(ResponseState.Start)
            return
        }
        makeSearchRequest(query, false)
    }

    private fun makeSearchRequest(query: String, isPagination: Boolean) {
        state.postValue(ResponseState.Loading(isPagination))
        viewModelScope.launch {
            val filtersMap = filtersInteractor.getAllFilters()
            if (!isPagination) nextPageNumber = 0
            when (val response = searchVacanciesUseCase(query, nextPageNumber, filtersMap)) {
                is ResponseState.ContentVacanciesList -> {
                    nextPageNumber = response.page + 1
                    maxPages = response.pages
                    if (!isPagination) {
                        currentListVacancies.clear()
                    }

                    response.listVacancy.forEach {
                        if (currentListVacancies.find { vacancy -> it.id == vacancy.id } == null) {
                            currentListVacancies.add(it)
                        }
                    }

                    state.postValue(
                        ResponseState.ContentVacanciesList(
                            currentListVacancies,
                            response.found,
                            response.page,
                            response.pages
                        )
                    )
                }

                is ResponseState.NetworkError -> state.postValue(ResponseState.NetworkError(isPagination))

                is ResponseState.Loading -> state.postValue(ResponseState.Loading(isPagination))
                else -> {
                    state.postValue(response)
                }
            }
            stateFilters.postValue(filtersMap.isNotEmpty())
            isNextPageLoading = false
        }
    }

    private fun searchPagination(query: String) {
        if (isNextPageLoading) return
        maxPages?.let { if (nextPageNumber + 1 >= it) return }

        isNextPageLoading = true
        makeSearchRequest(query, true)
    }

    fun onLastItemReached() {
        query?.let { searchPagination(it) }
    }

    fun searchOnFilterChanged() {
        if (!this.query.isNullOrEmpty()) {
            val query = this.query
            this.query = null
            query?.let { search(it) }
        }
    }

    fun updateHasFilters() {
        stateFilters.postValue(filtersInteractor.getAllFilters().isNotEmpty())
    }

    companion object {
        const val PAG_COUNT: Int = 20
    }
}
