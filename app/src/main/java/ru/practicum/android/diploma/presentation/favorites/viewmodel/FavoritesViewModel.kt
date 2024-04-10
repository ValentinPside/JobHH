package ru.practicum.android.diploma.presentation.favorites.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.domain.favorites.api.FavoritesInteractor
import ru.practicum.android.diploma.presentation.favorites.state.FavoritesState
import ru.practicum.android.diploma.ui.SingleLiveEvent
import ru.practicum.android.diploma.util.debounceFun
import java.io.IOException
import javax.inject.Inject

class FavoritesViewModel @Inject constructor(
    private val favoritesInteractor: FavoritesInteractor
) : ViewModel() {
    private val state = MutableLiveData<FavoritesState>()

    private val onVacancyClickDebounce =
        debounceFun<String>(CLICK_DELAY, viewModelScope, false) { vacancyId ->
            showVacancy.postValue(vacancyId)
        }

    companion object {
        private const val CLICK_DELAY = 300L
    }

    private val showVacancy = SingleLiveEvent<String>()

    init {
        loadFavorites()
    }

    fun getShowPlayerTrigger(): SingleLiveEvent<String> = showVacancy

    fun getState(): LiveData<FavoritesState> = state

    fun loadFavorites() {
        state.postValue(FavoritesState.Loading)
        viewModelScope.launch {
            @Suppress("SwallowedException")
            try {
                favoritesInteractor
                    .favoritesVacancies()
                    .collect { vacancies ->
                        if (vacancies.isEmpty()) {
                            state.postValue(FavoritesState.Empty)
                        } else {
                            state.postValue(FavoritesState.Content(vacancies))
                        }
                    }
            } catch (e: IOException) {
                state.postValue(FavoritesState.Error)
            }
        }
    }

    fun showDetails(id: String) {
        onVacancyClickDebounce(id)
    }
}
