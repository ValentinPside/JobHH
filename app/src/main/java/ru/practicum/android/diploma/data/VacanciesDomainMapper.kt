package ru.practicum.android.diploma.data

import ru.practicum.android.diploma.data.dto.EmployerDto
import ru.practicum.android.diploma.data.dto.PhoneDto
import ru.practicum.android.diploma.data.dto.SalaryDto
import ru.practicum.android.diploma.data.dto.VacancyDto
import ru.practicum.android.diploma.data.dto.detail.VacancyDetailDto
import ru.practicum.android.diploma.domain.models.Employer
import ru.practicum.android.diploma.domain.models.Salary
import ru.practicum.android.diploma.domain.models.Vacancy
import ru.practicum.android.diploma.domain.models.VacancyDetail

fun List<VacancyDto>.asDomain(): List<Vacancy> = this.map { it.asDomain() }

fun VacancyDto.asDomain(): Vacancy = Vacancy(
    id = id,
    img = this.employer.logoUrls?.original,
    employer = this.employer.name,
    name = name,
    salary = salary?.asDomain(),
    area = this.area.name
)

fun SalaryDto.asDomain(): Salary = Salary(
    currency = currency,
    from = from,
    gross = gross,
    to = to
)

fun EmployerDto.asDomain(): Employer = Employer(
    id = id,
    logoUrls = this.logoUrls?.original,
    name = name,
    trusted = trusted,
    vacanciesUrl = vacanciesUrl
)

fun VacancyDetailDto.asDomain(): VacancyDetail {
    val employment = listOfNotNull(
        this.employment?.name,
        this.schedule?.name
    ).joinToString(", ")
    var contactComments = ""
    this.contacts?.phones?.forEach {
        if (it.comment?.isNotEmpty() == true) {
            contactComments += "${it.comment}\n"
        }
    }

    return VacancyDetail(
        id = id,
        name = name,
        salary = salary?.asDomain(),
        experience = this.experience?.name,
        description = description,
        employer = employer?.asDomain(),
        keySkills = keySkills?.map { it.name },
        area = area?.name,
        employment = employment,
        alternateUrl = alternateUrl,
        contactsEmail = contacts?.email,
        contactsName = contacts?.name,
        contactsPhones = contacts?.phones.let { list -> list?.map { createPhone(it) } },
        comment = contactComments,
    )
}

private fun createPhone(phone: PhoneDto): String {
    return "+${phone.country}" + " (${phone.city})" + " ${phone.number}"
}
