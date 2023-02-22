package com.astrum.data.converter

import com.astrum.data.migration.MigrationStatus
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.WritingConverter
import org.springframework.stereotype.Component

@Component
@WritingConverter
class MigrationStatusToStringConverter : Converter<MigrationStatus, String> {
    override fun convert(source: MigrationStatus): String {
        return source.toString()
    }
}
