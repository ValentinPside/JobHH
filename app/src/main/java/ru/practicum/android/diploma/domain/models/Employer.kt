package ru.practicum.android.diploma.domain.models

import com.google.gson.annotations.SerializedName

data class Employer(
    val id: String?,
    @SerializedName("logo_urls") val logoUrls: String?,
    val name: String?,
    val trusted: Boolean?,
    val vacanciesUrl: String?
)
