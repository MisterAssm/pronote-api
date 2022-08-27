package fr.misterassm.kronote.api.models.retrieve

import fr.misterassm.kronote.api.models.enum.CourseStatus
import fr.misterassm.kronote.api.models.identification.KronoteDoubleIdentification
import fr.misterassm.kronote.api.models.identification.KronoteTripleIdentification
import fr.misterassm.kronote.internal.tools.*
import kotlinx.datetime.LocalDateTime
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
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@Serializable(with = Course.Companion::class)
data class Course constructor(
    val courseId: String,
    val modelType: Int,
    internal val p: Int,
    val status: CourseStatus,
    val isCancelled: Boolean = false,
    val place: Int = 0,
    val duration: Duration,
    val backgroundColorHEX: String,
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
                    (json["duree"]!!.jsonPrimitive.int * 30).minutes,
                    json["CouleurFond"]!!.jsonPrimitive.content,
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
