package com.astrum.ulid.jackson

import com.astrum.ulid.ULID
import com.fasterxml.jackson.databind.module.SimpleModule
import org.springframework.stereotype.Component

@Component
class ULIDModule : SimpleModule() {
    init {
        addSerializer(ULID::class.java, ULIDSerializer())
        addDeserializer(ULID::class.java, ULIDDeserializer())
    }
}
