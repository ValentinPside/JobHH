package ru.practicum.android.diploma.presentation.vacancy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.domain.favorites.api.FavoritesInteractor
import ru.practicum.android.diploma.domain.impl.VacanciesRepository
import ru.practicum.android.diploma.domain.models.VacancyDetail
import javax.inject.Inject

class VacancyViewModel @Inject constructor(
    private val vacancyId: String,
    private val repository: VacanciesRepository,
    private val emailRepository: EmailRepository,
    private val favoritesInteractor: FavoritesInteractor
) : ViewModel() {
    private val state = MutableStateFlow(ViewState())
    private var vacancy: VacancyDetail? = null
    fun observeUi() = state.asStateFlow()

    init {
        viewModelScope.launch {
            vacancy = repository.searchById(vacancyId)
            state.update { it.copy(vacancy = vacancy, isLoading = false) }
        }
    }
    fun shareVacancy() {
        emailRepository.shareLink("https://ekaterinburg.hh.ru/vacancy/${vacancy!!.id}")
    }

    fun setIndb() {
        viewModelScope.launch {
            state.value.vacancy?.let {
                favoritesInteractor.insertDbVacanciToFavorite(
                    it
                )
            }
        }
    }
}

data class ViewState(
    val vacancy: VacancyDetail? = null,
    val isLoading: Boolean = true
)
