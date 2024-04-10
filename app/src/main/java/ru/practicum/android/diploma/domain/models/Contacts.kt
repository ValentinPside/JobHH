package ru.practicum.android.diploma.domain.models

import ru.practicum.android.diploma.data.dto.PhoneDto

data class Contacts(
    val email: String?,
    val name: String?,
    val phones: List<PhoneDto>?
)
