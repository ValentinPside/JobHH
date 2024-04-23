package ru.practicum.android.diploma.data.filters

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.practicum.android.diploma.domain.filters.models.FilterValue
import ru.practicum.android.diploma.domain.sharedpreferences.api.SharedPreferencesRepository
import ru.practicum.android.diploma.domain.sharedpreferences.model.SharedFilterNames

class SharedPreferencesRepositoryImpl(
    private val gson: Gson,
    private val sharedPref: SharedPreferences
) : SharedPreferencesRepository {

    private fun getFilterValue(json: String): FilterValue {
        return gson.fromJson(
            json,
            object : TypeToken<FilterValue>() {}.type
        )
    }

    override fun getAllFilters(): Map<String, String> {
        val result = HashMap<String, String>()

        val areaFilter = sharedPref.getString(SharedFilterNames.AREA, null)
        if (!areaFilter.isNullOrEmpty()) {
            result.put(SharedFilterNames.AREA, getFilterValue(areaFilter).id)
        } else {
            val areaFilterCountry = sharedPref.getString(SharedFilterNames.COUNTRY, null)
            if (!areaFilterCountry.isNullOrEmpty()) {
                result.put(SharedFilterNames.AREA, getFilterValue(areaFilterCountry).id)
            }
        }

        val industryFilter = sharedPref.getString(SharedFilterNames.INDUSTRY, null)
        if (!industryFilter.isNullOrEmpty()) {
            result.put(SharedFilterNames.INDUSTRY, getFilterValue(industryFilter).id)
        }

        val salaryFilter = sharedPref.getString(SharedFilterNames.SALARY, null)
        if (!salaryFilter.isNullOrEmpty()) {
            result.put(SharedFilterNames.SALARY, getFilterValue(salaryFilter).valueString)
        }

        val inlyWithSalaryFilter = sharedPref.getString(SharedFilterNames.ONLY_WITH_SALARY, null)
        if (!inlyWithSalaryFilter.isNullOrEmpty()) {
            result.put(SharedFilterNames.ONLY_WITH_SALARY, getFilterValue(inlyWithSalaryFilter).valueString)
        }

        return result.toMap()
    }

    @Synchronized
    override fun setFilter(filterName: String, filterValue: FilterValue?) {
        if (filterValue == null) {
            sharedPref.edit().remove(filterName).apply()
            return
        }
        val savingFilterValue = filterValue.copy(name = filterName)
        val json = gson.toJson(savingFilterValue)
        sharedPref.edit().putString(filterName, json).apply()
    }

    override fun getFilter(filterName: String): FilterValue? {
        return sharedPref.getString(filterName, null).let {
            if (it.isNullOrEmpty()) return null
            getFilterValue(it)
        }
    }
}
