package ru.practicum.android.diploma.data.dto

import ru.practicum.android.diploma.domain.models.Contacts

data class VacancyDto(
    val id: String,
    val department: DepartmentDto?,
    val name: String,
    val area: VacancyAreaDto,
    val employer: EmployerDto,
    val salary: SalaryDto?,
    val type: VacancyTypeDto,
    val contacts: ContactsDto?
)
