package fr.misterassm.kronote.api.models.retrieve

import fr.misterassm.kronote.api.models.identification.KronoteDoubleIdentification
import fr.misterassm.kronote.api.models.identification.KronoteTripleIdentification
import fr.misterassm.kronote.api.service.toKronoteDate
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import java.awt.Color
import java.time.LocalDateTime
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

const val GROUP_GENDER = 2
const val TEACHER_GENDER = 3
const val SUBJECT_GENDER = 16
const val ROOM_GENDER = 17

@Serializable(with = Course.Companion::class)
data class Course @OptIn(ExperimentalTime::class) constructor(
    val courseId: String,
    val modelType: Int,
    val p: Int,
    val status: CourseStatus,
    val isCancelled: Boolean = false,
    val place: Int = 0,
    val duration: Duration, // TODO: Convert to DURATION
    val backgroundColor: Color,
    val date: LocalDateTime,
    val teacher: KronoteDoubleIdentification?,
    val subject: KronoteTripleIdentification,
    val room: KronoteTripleIdentification?,
    val group: KronoteTripleIdentification?,
) {

    @OptIn(ExperimentalSerializationApi::class)
    @Serializer(forClass = Course::class)
    companion object : KSerializer<Course> {

        private fun findByGender(jsonArray: JsonArray, gender: Int): JsonObject? =
            jsonArray.firstOrNull { it.jsonObject["G"]!!.jsonPrimitive.int == gender }?.jsonObject

        private fun genderToTripleIdentification(jsonArray: JsonArray, gender: Int): KronoteTripleIdentification? =
            findByGender(jsonArray, gender)?.let {
                KronoteTripleIdentification(
                    it["N"]!!.jsonPrimitive.content,
                    it["L"]!!.jsonPrimitive.content,
                    gender
                )
            }

        override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Course") {
            element<String>("N")
            element<Int>("G")
            element<Int>("P")
            element<String>("Statut", isOptional = true)
            element<Boolean>("estAnnule", isOptional = true)
            element<Int>("place", isOptional = true)
            element<Int>("duree") // TODO: Convert to Duration
            element<String>("CouleurFond")
            element<JsonElement>("DateDuCours")
            element<JsonElement>("ListeContenus")
        }

        @OptIn(ExperimentalTime::class)
        override fun deserialize(decoder: Decoder): Course {

            (decoder as? JsonDecoder)?.decodeJsonElement()?.jsonObject?.let { json ->
                val listeContenus = json["ListeContenus"]!!.jsonObject["V"]!!.jsonArray

                return Course(
                    json["N"]!!.jsonPrimitive.content,
                    json["G"]!!.jsonPrimitive.int,
                    json["P"]!!.jsonPrimitive.int,
                    CourseStatus.statusMap[json["Statut"]?.jsonPrimitive?.contentOrNull] ?: CourseStatus.NONE,
                    json["estAnnule"]?.jsonPrimitive?.booleanOrNull ?: false,
                    json["place"]!!.jsonPrimitive.int,
                    Duration.minutes(json["duree"]!!.jsonPrimitive.int * 30),
                    Color.decode(json["CouleurFond"]!!.jsonPrimitive.content),
                    json["DateDuCours"]!!.jsonObject["V"]!!.jsonPrimitive.content.toKronoteDate(),
                    findByGender(listeContenus, TEACHER_GENDER)?.get("L")?.jsonPrimitive?.content?.let {
                        KronoteDoubleIdentification(
                            it,
                            TEACHER_GENDER
                        )
                    },
                    genderToTripleIdentification(listeContenus, SUBJECT_GENDER)!!,
                    genderToTripleIdentification(listeContenus, ROOM_GENDER),
                    genderToTripleIdentification(listeContenus, GROUP_GENDER)
                )
            } ?: error("Can be deserialized only by JSON")
        }

        override fun serialize(encoder: Encoder, value: Course) {
            TODO("Not yet implemented")
        }

    }

}

enum class CourseStatus(val status: String) {

    NONE("None"),
    EDITED("Cours modifié"),
    REPLACEMENT("Remplacement"),
    CANCELLED("Cours annulé");

    companion object {
        val statusMap = values().associateBy(CourseStatus::status)
    }

}
