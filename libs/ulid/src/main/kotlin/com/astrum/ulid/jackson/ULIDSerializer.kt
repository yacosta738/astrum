package com.astrum.ulid.jackson

import com.astrum.ulid.ULID
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider

class ULIDSerializer : JsonSerializer<ULID>() {
    override fun serialize(value: ULID, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeString(value.toString())
    }
}
