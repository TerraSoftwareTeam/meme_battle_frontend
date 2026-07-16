package com.dev.network.game.current.dto.ws

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class CentrifugoPush(
    @SerialName("push") val push: PushData
) {
    @Serializable
    data class PushData(
        @SerialName("channel") val channel: String,
        @SerialName("pub") val pub: PubData
    )

    @Serializable
    data class PubData(
        @SerialName("data") val data: CentrifugoPushEnvelope,
        @SerialName("offset") val offset: Long? = null,
        @SerialName("epoch") val epoch: String? = null
    )
}

@Serializable(with = CentrifugoPushEnvelopeSerializer::class)
data class CentrifugoPushEnvelope(
    val eventId: String? = null,
    val eventType: String,
    val gameId: String? = null,
    val userId: String? = null,
    val occurredAt: String? = null,
    val version: Int? = null,
    val payload: JsonElement
)

object CentrifugoPushEnvelopeSerializer : KSerializer<CentrifugoPushEnvelope> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("CentrifugoPushEnvelope") {
        element<String?>("event_id", isOptional = true)
        element<String>("event_type")
        element<String?>("game_id", isOptional = true)
        element<String?>("user_id", isOptional = true)
        element<String?>("occurred_at", isOptional = true)
        element<Int?>("version", isOptional = true)
        element<JsonElement>("payload")
    }

    override fun deserialize(decoder: Decoder): CentrifugoPushEnvelope {
        val input = decoder as? JsonDecoder ?: error("This class can be decoded only by Json format")
        val tree = input.decodeJsonElement() as JsonObject

        val eventId = tree["event_id"]?.jsonPrimitive?.content
        val eventType = tree["event_type"]?.jsonPrimitive?.content ?: error("Missing event_type")
        val gameId = tree["game_id"]?.jsonPrimitive?.content
        val userId = tree["user_id"]?.jsonPrimitive?.content
        val occurredAt = tree["occurred_at"]?.jsonPrimitive?.content
        val version = tree["version"]?.jsonPrimitive?.content?.toIntOrNull()
        
        val payloadElement = tree["payload"] ?: error("Missing payload")
        
        val modifiedPayload = if (payloadElement is JsonObject) {
            JsonObject(payloadElement.toMutableMap().apply { 
                put("type", JsonPrimitive(eventType)) 
            })
        } else {
            payloadElement
        }

        return CentrifugoPushEnvelope(
            eventId = eventId,
            eventType = eventType,
            gameId = gameId,
            userId = userId,
            occurredAt = occurredAt,
            version = version,
            payload = modifiedPayload
        )
    }

    override fun serialize(encoder: Encoder, value: CentrifugoPushEnvelope) {
        error("Serialization is not supported for CentrifugoPushEnvelope")
    }
}
