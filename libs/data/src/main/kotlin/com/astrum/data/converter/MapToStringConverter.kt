package com.astrum.data.converter

import com.astrum.data.annotation.ConverterScope
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.WritingConverter
import org.springframework.stereotype.Component

@Component
@WritingConverter
@ConverterScope(ConverterScope.Type.R2DBC)
class MapToStringConverter(
    private val objectMapper: ObjectMapper
) : Converter<Map<String, Any>, String> {
    override fun convert(source: Map<String, Any>): String {
        return objectMapper.writeValueAsString(source)
    }
}
