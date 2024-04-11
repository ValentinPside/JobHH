package ru.practicum.android.diploma.domain.models

data class VacancyDetail(
    val id: String,
    val name: String,
    val salary: Salary?,
    val experience: String?,
    val description: String?,
    val employer: Employer?,
    val keySkills: List<String>?,
    val area: String?,
    val employment: String?,
    val contacts: Contacts?
)
