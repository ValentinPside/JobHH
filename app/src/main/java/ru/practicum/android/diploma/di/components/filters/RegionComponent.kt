package ru.practicum.android.diploma.di.components.filters

import dagger.BindsInstance
import dagger.Subcomponent
import ru.practicum.android.diploma.presentation.filters.region.viewmodel.FiltersRegionViewModel

@Subcomponent
interface RegionComponent {
    fun viewModel(): FiltersRegionViewModel

    @Subcomponent.Factory
    interface RegionComponentFactory {
        fun create(@BindsInstance countryId: String): RegionComponent
    }
}
