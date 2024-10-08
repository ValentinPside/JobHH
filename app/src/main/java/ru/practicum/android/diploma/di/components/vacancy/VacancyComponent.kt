package ru.practicum.android.diploma.di.components.vacancy

import dagger.BindsInstance
import dagger.Subcomponent
import ru.practicum.android.diploma.presentation.vacancy.viewmodel.VacancyViewModel

@Subcomponent
interface VacancyComponent {

    fun viewModel(): VacancyViewModel

    @Subcomponent.Factory
    interface VacancyComponentFactory {
        fun create(@BindsInstance vacancyId: String): VacancyComponent
    }
}
