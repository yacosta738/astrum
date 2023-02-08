package com.astrum.data.converter

import com.astrum.data.migration.MigrationStatus
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.stereotype.Component

@Component
@ReadingConverter
class StringToMigrationStatusConverter : Converter<String, MigrationStatus> {
    override fun convert(source: String): MigrationStatus {
        return MigrationStatus.valueOf(source)
    }
}
