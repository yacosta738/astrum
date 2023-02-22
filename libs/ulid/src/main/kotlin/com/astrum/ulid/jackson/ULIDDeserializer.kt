package com.astrum.ulid.jackson

import com.astrum.ulid.ULID
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer

class ULIDDeserializer : JsonDeserializer<ULID>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ULID {
        return ULID.fromString(p.valueAsString)
    }
}
