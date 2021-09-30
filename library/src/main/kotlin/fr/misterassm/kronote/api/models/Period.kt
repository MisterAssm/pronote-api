package fr.misterassm.kronote.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Period(
    @SerialName("L") val name: String,
    @SerialName("G") val number: Int,
    @SerialName("N") val id: String,
    @SerialName("A") val progress: Boolean = false,
    @SerialName("GenreNotation") val notation: Int
)
