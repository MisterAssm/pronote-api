package fr.misterassm.kronote.api.models.identification

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class KronoteTripleIdentification(
    @SerialName("N") val id: String,
    @SerialName("L") val name: String,
    @SerialName("G") val gender: Int,
) {
    override fun toString(): String = name
}
