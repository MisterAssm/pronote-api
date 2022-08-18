package fr.misterassm.kronote.api.models.identification

import fr.misterassm.kronote.internal.KronoteImpl
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.json.*

@Serializable
data class KronoteDoubleIdentification(
    @SerialName("L") val name: String,
    @SerialName("G") val gender: Int,
) {
    override fun toString(): String = name
}

object DoubleIdentificationSerializer : KSerializer<KronoteDoubleIdentification> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("DoubleIdentification") {
        element<Array<String>>("V")
    }

    @OptIn(InternalSerializationApi::class)
    override fun deserialize(decoder: Decoder): KronoteDoubleIdentification {

        decoder.decodeStructure(descriptor) {

            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> decodeSerializableElement(descriptor, 0, JsonArray::class.serializer()).apply {
                        this.firstOrNull { it.jsonObject["G"]?.jsonPrimitive?.int == 16 }?.apply {
                            return@apply KronoteImpl.json.decodeFromJsonElement(this)
                        }
                    }
                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index: $index")
                }
            }

        }

        TODO("AN ERROR OCCURRED")

    }

    override fun serialize(encoder: Encoder, value: KronoteDoubleIdentification) {
        TODO("Not yet implemented")
    }
}
