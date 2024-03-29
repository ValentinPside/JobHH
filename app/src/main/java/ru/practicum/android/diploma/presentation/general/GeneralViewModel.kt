package ru.practicum.android.diploma.presentation.general

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.domain.impl.VacanciesRepository
import ru.practicum.android.diploma.domain.models.Vacancy
import java.net.UnknownHostException
import javax.inject.Inject

class GeneralViewModel @Inject constructor(
    private val vacanciesRepository: VacanciesRepository
): ViewModel() {

    private val state = MutableStateFlow(ViewState())
    fun observeUi() = state.asStateFlow()

    fun search(query: String, page: Int = 0, isPagination: Boolean = false){
        if (query.isEmpty() && !isPagination){
            state.update { it.copy(status = ResponseState.Start) }
            return
        }
        viewModelScope.launch {
            try {
                val vacancies = vacanciesRepository.search(query, page)
                val currentList = if(isPagination){
                    state.value.vacancies + vacancies
                } else{
                    vacancies
                }
                state.update { it.copy(vacancies = currentList, status = if (vacancies.isNotEmpty()) ResponseState.Content else ResponseState.Empty) }
            }
            catch (e: UnknownHostException){
                state.update { it.copy(status = ResponseState.NetworkError) }
            }
            catch (e: Error){
                state.update { it.copy(status = ResponseState.ServerError) }
            }
        }
    }

    fun onLastItemReached(query: String){
        val page = state.value.vacancies.size / 20
        search(query, page, true)
    }
}

data class ViewState(
    val vacancies: List<Vacancy> = emptyList(),
    val status: ResponseState = ResponseState.Start
)

sealed class ResponseState(){

    data object Start: ResponseState()
    data object Empty: ResponseState()
    data object Content: ResponseState()
    data object NetworkError: ResponseState()
    data object ServerError: ResponseState()
}
