package ru.practicum.android.diploma.app

import android.app.Application
import ru.practicum.android.diploma.di.components.app.DaggerAppComponent

class App : Application() {
    val appComponent = DaggerAppComponent.factory().create(this)
}
