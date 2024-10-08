package ru.practicum.android.diploma.di.components.favorites

import dagger.Subcomponent
import ru.practicum.android.diploma.presentation.favorites.viewmodel.FavoritesViewModel

@Subcomponent
interface FavoritesComponent {
    fun viewModel(): FavoritesViewModel

}
