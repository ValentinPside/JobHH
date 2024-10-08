package ru.practicum.android.diploma.di.components.app

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import ru.practicum.android.diploma.di.components.favorites.FavoritesComponent
import ru.practicum.android.diploma.di.components.filters.CountryComponent
import ru.practicum.android.diploma.di.components.filters.FiltersMainComponent
import ru.practicum.android.diploma.di.components.filters.IndustryComponent
import ru.practicum.android.diploma.di.components.filters.RegionComponent
import ru.practicum.android.diploma.di.components.filters.WorkPlaceComponent
import ru.practicum.android.diploma.di.components.general.GeneralComponent
import ru.practicum.android.diploma.di.modules.DataBaseModule
import ru.practicum.android.diploma.di.modules.NetworkModule
import ru.practicum.android.diploma.di.modules.RepositoryModule
import ru.practicum.android.diploma.di.modules.UseCaseModule
import ru.practicum.android.diploma.di.components.vacancy.VacancyComponent
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        RepositoryModule::class,
        NetworkModule::class,
        DataBaseModule::class,
        UseCaseModule::class
    ]
)
interface AppComponent {

    fun generalComponent(): GeneralComponent

    fun industryComponent(): IndustryComponent

    fun vacancyComponent(): VacancyComponent.VacancyComponentFactory

    fun favoriteslComponent(): FavoritesComponent

    fun workPlaceComponent(): WorkPlaceComponent

    fun countryComponent(): CountryComponent

    fun regionComponent(): RegionComponent.RegionComponentFactory

    fun filtersMainComponent(): FiltersMainComponent

    @Component.Factory
    interface AppComponentFactory {
        fun create(@BindsInstance context: Context): AppComponent
    }
}
