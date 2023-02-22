package com.astrum.data.converter

import com.astrum.ulid.ULID
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class ULIDToStringConverter : Converter<ULID, String> {
    override fun convert(source: ULID): String {
        return source.toString()
    }
}
