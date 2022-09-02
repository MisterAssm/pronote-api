package fr.misterassm.kronote.internal.serializer

import fr.misterassm.kronote.api.KronoteSession.Companion.json
import fr.misterassm.kronote.api.models.identification.KronoteDoubleIdentification
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.json.*

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
                        this.firstOrNull { it.jsonObject["G"]?.jsonPrimitive?.int == 16 }?.let {
                            return@let json.decodeFromJsonElement(this)
                        }
                    }

                    CompositeDecoder.DECODE_DONE -> break
                    else -> throw UnsupportedOperationException("Unexpected index: $index")
                }
            }
        }

        throw SerializationException("A problem occurred during deserialization")
    }

    override fun serialize(encoder: Encoder, value: KronoteDoubleIdentification) = throw UnsupportedOperationException()
}