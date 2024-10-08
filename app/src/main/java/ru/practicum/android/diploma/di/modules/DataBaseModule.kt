package ru.practicum.android.diploma.di.modules

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import ru.practicum.android.diploma.data.db.AppDatabase
import javax.inject.Singleton

@Module
object DataBaseModule {
    @Singleton
    @Provides
    fun provideAppDatabase(context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "HHDatabase3.db")
            .build()
    }

}
