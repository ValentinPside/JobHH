package ru.practicum.android.diploma.di.modules

import dagger.Module
import dagger.Provides
import ru.practicum.android.diploma.domain.general.api.SearchVacanciesByIdUseCase
import ru.practicum.android.diploma.domain.general.api.SearchVacanciesUseCase
import ru.practicum.android.diploma.domain.general.impl.SearchVacanciesByIdUseCaseImpl
import ru.practicum.android.diploma.domain.general.impl.SearchVacanciesUseCaseImpl

@Module
object UseCaseModule {

    @Provides
    fun provideSearchVacanciesUseCase(impl: SearchVacanciesUseCaseImpl): SearchVacanciesUseCase = impl

    @Provides
    fun provideSearchVacanciesByIdUseCase(impl: SearchVacanciesByIdUseCaseImpl): SearchVacanciesByIdUseCase = impl

}
