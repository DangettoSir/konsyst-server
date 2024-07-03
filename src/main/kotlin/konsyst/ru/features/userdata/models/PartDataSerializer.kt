package konsyst.ru.features.userdata.models

import io.ktor.http.content.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object PartDataFileItemSerializer : KSerializer<PartData.FileItem> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("PartData.FileItem", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: PartData.FileItem) {
        encoder.encodeString(value.originalFileName ?: "")
    }

    override fun deserialize(decoder: Decoder): PartData.FileItem {
        throw UnsupportedOperationException("PartData.FileItem cannot be deserialized")
    }
}
