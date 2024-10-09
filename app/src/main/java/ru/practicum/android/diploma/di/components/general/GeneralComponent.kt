package ru.practicum.android.diploma.di.components.general

import dagger.Subcomponent
import ru.practicum.android.diploma.presentation.general.viewmodel.GeneralViewModel

@Subcomponent
interface GeneralComponent {
    fun viewModel(): GeneralViewModel
}
