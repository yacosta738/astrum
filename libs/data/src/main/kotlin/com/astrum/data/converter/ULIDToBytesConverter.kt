package com.astrum.data.converter

import com.astrum.data.annotation.ConverterScope
import com.astrum.ulid.ULID
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.WritingConverter
import org.springframework.stereotype.Component

@Component
@WritingConverter
@ConverterScope(ConverterScope.Type.R2DBC)
class ULIDToBytesConverter : Converter<ULID, ByteArray> {
    override fun convert(source: ULID): ByteArray {
        return source.toBytes()
    }
}
