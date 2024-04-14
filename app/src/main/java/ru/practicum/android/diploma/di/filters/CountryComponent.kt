package ru.practicum.android.diploma.di.filters

import dagger.Subcomponent
import ru.practicum.android.diploma.presentation.filters.region.viewmodel.FiltersCountryViewModel

@Subcomponent
interface CountryComponent {

    fun viewModel(): FiltersCountryViewModel

}
