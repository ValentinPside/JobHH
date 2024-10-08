package ru.practicum.android.diploma.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class Factory<T : ViewModel>(private val create: () -> T) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return create.invoke() as T
    }
}
