package ru.practicum.android.diploma.di.filters

import dagger.Subcomponent
import ru.practicum.android.diploma.presentation.filters.region.viewmodel.FiltersWorkPlaceViewModel

@Subcomponent
interface WorkPlaceComponent {
    fun viewModel(): FiltersWorkPlaceViewModel

}
