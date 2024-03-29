package fr.misterassm.kronote.api.models.retrieve

import fr.misterassm.kronote.api.models.enum.CourseStatus
import fr.misterassm.kronote.api.models.enum.PronoteGender
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

        private fun findByGender(jsonArray: JsonArray, gender: PronoteGender): JsonObject? =
            findByGender(jsonArray, gender.genderId)

        private fun genderToTripleIdentification(jsonArray: JsonArray, gender: Int): KronoteTripleIdentification? =
            findByGender(jsonArray, gender)?.let {
                KronoteTripleIdentification(
                    it["N"]!!.jsonPrimitive.content,
                    it["L"]!!.jsonPrimitive.content,
                    gender
                )
            }

        private fun genderToTripleIdentification(
            jsonArray: JsonArray,
            gender: PronoteGender
        ): KronoteTripleIdentification? = genderToTripleIdentification(jsonArray, gender.genderId)

        override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Course") {
            element<String>("N")
            element<Int>("G")
            element<Int>("P")
            element<String>("Statut", isOptional = true)
            element<Boolean>("estAnnule", isOptional = true)
            element<Int>("place", isOptional = true)
            element<Int>("duree")
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
                    findByGender(listeContenus, PronoteGender.TEACHER_GENDER)?.get("L")?.jsonPrimitive?.content?.let {
                        KronoteDoubleIdentification(it, PronoteGender.TEACHER_GENDER.genderId)
                    },
                    genderToTripleIdentification(listeContenus, PronoteGender.SUBJECT_GENDER)!!,
                    genderToTripleIdentification(listeContenus, PronoteGender.ROOM_GENDER),
                    genderToTripleIdentification(listeContenus, PronoteGender.GROUP_GENDER)
                )
            } ?: throw UnsupportedOperationException("The object must be a json object to be deserialized")
        }

        override fun serialize(encoder: Encoder, value: Course) = throw UnsupportedOperationException()

    }

}
