package com.astrum.data.converter

import com.astrum.data.annotation.ConverterScope
import com.astrum.ulid.ULID
import org.springframework.core.convert.TypeDescriptor
import org.springframework.core.convert.converter.GenericConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.stereotype.Component

@Component
@WritingConverter
@ConverterScope(ConverterScope.Type.NEO4J)
class ULIDToBinaryStringConverter : GenericConverter {
    override fun getConvertibleTypes(): MutableSet<GenericConverter.ConvertiblePair> {
        return mutableSetOf(
            GenericConverter.ConvertiblePair(ULID::class.java, String::class.java),
            GenericConverter.ConvertiblePair(String::class.java, ULID::class.java)
        )
    }

    override fun convert(
        source: Any?,
        sourceType: TypeDescriptor,
        targetType: TypeDescriptor
    ): Any {
        return if (ULID::class.java.isAssignableFrom(sourceType.type)) {
            (source as ULID).toString()
        } else if (String::class.java.isAssignableFrom(sourceType.type)) {
            ULID.fromString(source as String)
        } else {
            require(false) { "Unsupported type: $sourceType" }
        }
    }
}
