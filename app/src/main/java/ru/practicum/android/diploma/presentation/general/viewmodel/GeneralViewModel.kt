package ru.practicum.android.diploma.presentation.general.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.domain.favorites.api.FavoritesInteractor
import ru.practicum.android.diploma.domain.general.api.SearchVacanciesByIdUseCase
import ru.practicum.android.diploma.domain.general.api.SearchVacanciesUseCase
import ru.practicum.android.diploma.domain.general.models.ResponseState
import ru.practicum.android.diploma.domain.models.Vacancy
import ru.practicum.android.diploma.domain.sharedpreferences.api.FiltersInteractor
import javax.inject.Inject

class GeneralViewModel @Inject constructor(
    private val searchVacanciesUseCase: SearchVacanciesUseCase,
    private val filtersInteractor: FiltersInteractor,
    private val favoritesInteractor: FavoritesInteractor,
    private val searchVacanciesByIdUseCase: SearchVacanciesByIdUseCase
) : ViewModel() {

    private var nextPageNumber = 0

    private val state = MutableLiveData<ResponseState>()

    private val stateFilters = MutableLiveData<Boolean>()

    private val favoriteState = MutableLiveData<FavoriteState>()

    private var currentListVacancies = mutableListOf<Vacancy>()

    private var isNextPageLoading = false

    fun observeUi(): LiveData<ResponseState> = state
    fun observeFilters(): LiveData<Boolean> = stateFilters
    fun observeIsFavorite(): LiveData<FavoriteState> = favoriteState

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

    private suspend fun fillFavorites(list: List<Vacancy>): List<Vacancy> {
        list.forEach {
            it.isFavorite = favoritesInteractor.isFavorite(it.id)
        }
        return list
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

    fun updateData() {
        stateFilters.postValue(filtersInteractor.getAllFilters().isNotEmpty())
        val currentValue = state.value
        viewModelScope.launch {
            if (currentValue is ResponseState.ContentVacanciesList) {
                val newValue = currentValue.copy(listVacancy = fillFavorites(currentValue.listVacancy))
                state.postValue(newValue)
            }
        }
    }

    fun switchFavorite(id: String, position: Int) {
        viewModelScope.launch {
            favoriteState.postValue(FavoriteState.Loading)
            val isFavorite = favoritesInteractor.isFavorite(id)
            val currentValue = state.value
            if (currentValue is ResponseState.ContentVacanciesList) {
                currentValue.listVacancy.get(position).isFavorite = isFavorite
            }

            if (isFavorite) {
                favoritesInteractor.deleteDbVacanciFromFavorite(id)
                favoriteState.postValue(FavoriteState.Content(position, false, id))
            } else {
                val vacanciesDetailResponse = searchVacanciesByIdUseCase(id)
                when (vacanciesDetailResponse) {
                    is ResponseState.ContentVacancyDetail -> {
                        favoritesInteractor.insertDbVacanciToFavorite(vacanciesDetailResponse.vacancyDetail)
                        favoriteState.postValue(FavoriteState.Content(position, true, id))
                    }

                    else -> favoriteState.postValue(FavoriteState.Error)
                }
            }
        }
    }

    companion object {
        const val PAG_COUNT: Int = 20
    }
}

sealed interface FavoriteState {
    class Content(
        val position: Int,
        val isFavorite: Boolean,
        val id: String
    ) : FavoriteState

    data object Error : FavoriteState

    data object Loading : FavoriteState
}
