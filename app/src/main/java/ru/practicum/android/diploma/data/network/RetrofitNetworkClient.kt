package ru.practicum.android.diploma.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import kotlinx.coroutines.flow.update
import retrofit2.HttpException
import ru.practicum.android.diploma.domain.general.models.ResponseState
import java.io.IOException
import javax.inject.Inject

class RetrofitNetworkClient @Inject constructor(
    private val headHunterService: HeadHunterService,
    private val context: Context
) : NetworkClient {
    override suspend fun doRequest(query: Map<String, String>): Response {
        if (!isOnline(context)) return Response().apply { resultCode = -1 }

        return try {
            val resp = headHunterService.vacancies(query)
            resp.apply { resultCode = 200 }
        } catch (e: IOException) {
            Response().apply { resultCode = 500 }
        } catch (e: HttpException) {
            Response().apply { resultCode = e.code() }
        }
    }

    override suspend fun doRequestById(id: String): Response {
        if (!isOnline(context)) return Response().apply { resultCode = -1 }

        return try {
            val resp = headHunterService.getVacancyById(id)
            resp.apply { resultCode = 200 }
        } catch (e: IOException) {
            Response().apply { resultCode = 500 }
        } catch (e: HttpException) {
            Response().apply { resultCode = e.code() }
        }
    }

    private fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        return capabilities != null
    }

}