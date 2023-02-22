package com.astrum.data.converter

import com.astrum.data.annotation.ConverterScope
import com.astrum.ulid.ULID
import org.neo4j.driver.Value
import org.neo4j.driver.Values
import org.springframework.core.convert.TypeDescriptor
import org.springframework.core.convert.converter.GenericConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.stereotype.Component

@Component
@WritingConverter
@ConverterScope(ConverterScope.Type.NEO4J)
class ULIDToValueConverter : GenericConverter {
    override fun getConvertibleTypes(): Set<GenericConverter.ConvertiblePair> {
        return setOf(
            GenericConverter.ConvertiblePair(
                ULID::class.java,
                Value::class.java
            ),
            GenericConverter.ConvertiblePair(
                Value::class.java,
                ULID::class.java
            )
        )
    }

    override fun convert(
        source: Any?,
        sourceType: TypeDescriptor,
        targetType: TypeDescriptor
    ): Any {
        if (source == null) {
            return Values.NULL
        }
        return if (ULID::class.java.isAssignableFrom(sourceType.type)) {
            Values.value((source as ULID).toBytes())
        } else {
            ULID.fromBytes((source as Value).asByteArray())
        }
    }
}
