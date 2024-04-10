package ru.practicum.android.diploma.domain.models

import ru.practicum.android.diploma.data.dto.ContactsDto

data class Vacancy(
    val id: String,
    val img: String?,
    val employer: String,
    val name: String,
    val salary: Salary?,
    val area: String?,
    val contacts: Contacts?
)
