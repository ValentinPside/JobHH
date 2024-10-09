package ru.practicum.android.diploma.di.modules

import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.practicum.android.diploma.BuildConfig
import ru.practicum.android.diploma.data.network.HeadHunterService
import javax.inject.Singleton

const val BASE_URL = "https://api.hh.ru/"

@Module
object NetworkModule {

    @Provides
    fun provideHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply { HttpLoggingInterceptor.Level.BODY })
        .addInterceptor { chain ->
            chain.run {
                proceed(
                    request()
                        .newBuilder()
                        .addHeader("Authorization", "Bearer ${BuildConfig.HH_ACCESS_TOKEN}")
                        .addHeader("HH-User-Agent", "practicum-android-diploma (valentin.pside@mail.ru)")
                        .build()
                )
            }
        }
        .build()

    @Provides
    fun provideRetrofit(client: OkHttpClient) = Retrofit.Builder()
        .client(client)
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideHeadHunterService(retrofit: Retrofit): HeadHunterService = retrofit.create(HeadHunterService::class.java)
}
