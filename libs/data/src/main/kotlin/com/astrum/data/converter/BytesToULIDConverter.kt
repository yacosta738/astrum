package com.astrum.data.converter

import com.astrum.data.annotation.ConverterScope
import com.astrum.ulid.ULID
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.stereotype.Component

@Component
@ReadingConverter
@ConverterScope(ConverterScope.Type.R2DBC)
class BytesToULIDConverter : Converter<ByteArray, ULID> {
    override fun convert(source: ByteArray): ULID {
        return ULID.fromBytes(source)
    }
}
