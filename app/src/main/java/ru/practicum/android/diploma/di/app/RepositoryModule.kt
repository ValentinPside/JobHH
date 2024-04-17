package ru.practicum.android.diploma.di.app

import android.app.Application
import android.content.Context
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import ru.practicum.android.diploma.data.favorites.impl.FavoritesRepositoryImpl
import ru.practicum.android.diploma.data.filters.FiltersRepositoryImpl
import ru.practicum.android.diploma.data.filters.SharedPreferencesRepositoryImpl
import ru.practicum.android.diploma.data.general.VacanciesRepositoryImpl
import ru.practicum.android.diploma.data.industry.impl.IndustryRepositoryImpl
import ru.practicum.android.diploma.data.network.NetworkClient
import ru.practicum.android.diploma.data.network.RetrofitNetworkClient
import ru.practicum.android.diploma.domain.api.VacanciesRepository
import ru.practicum.android.diploma.domain.favorites.api.FavoritesInteractor
import ru.practicum.android.diploma.domain.favorites.api.FavoritesRepository
import ru.practicum.android.diploma.domain.favorites.impl.FavoritesInteractorImpl
import ru.practicum.android.diploma.domain.filters.FiltersInteractorImpl
import ru.practicum.android.diploma.domain.filters.industry.api.IndustryInteractor
import ru.practicum.android.diploma.domain.filters.industry.api.IndustryRepository
import ru.practicum.android.diploma.domain.filters.industry.impl.IndustryInteractorImpl
import ru.practicum.android.diploma.domain.filters.main.api.FiltersRepository
import ru.practicum.android.diploma.domain.sharedpreferences.api.FiltersInteractor
import ru.practicum.android.diploma.domain.sharedpreferences.api.SharedPreferencesRepository
import ru.practicum.android.diploma.presentation.vacancy.EmailRepository
import ru.practicum.android.diploma.presentation.vacancy.EmailRepositoryImpl

@Module
object RepositoryModule {

    @Provides
    fun providesVacanciesRepository(impl: VacanciesRepositoryImpl): VacanciesRepository = impl

    @Provides
    fun providesFavoritiesRepository(impl: FavoritesRepositoryImpl): FavoritesRepository = impl

    @Provides
    fun providesEmailRepository(impl: EmailRepositoryImpl): EmailRepository = impl

    @Provides
    fun providesFavoritiesInteractor(impl: FavoritesInteractorImpl): FavoritesInteractor = impl

    @Provides
    fun providesFiltersInteractor(impl: FiltersInteractorImpl): FiltersInteractor = impl

    @Provides
    fun providesFiltersRepository(impl: FiltersRepositoryImpl): FiltersRepository = impl

    @Provides
    fun providesIndustryInteractor(impl: IndustryInteractorImpl): IndustryInteractor = impl

    @Provides
    fun providesIndustryRepository(impl: IndustryRepositoryImpl): IndustryRepository = impl

    @Provides
    fun providesNetworkClient(impl: RetrofitNetworkClient): NetworkClient = impl

    @Provides
    fun providesSharedPreferencesRepository(context: Context): SharedPreferencesRepository {
        return SharedPreferencesRepositoryImpl(
            Gson(),
            context.getSharedPreferences(
                "hh_filters",
                Application.MODE_PRIVATE
            )
        )
    }
}
