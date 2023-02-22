package com.astrum.data.migration

import com.astrum.data.ModifiableULIDEntity
import org.springframework.data.relational.core.mapping.Table

@Table("migration_checkpoints")
data class MigrationCheckpoint(
    var version: Int,
    var status: MigrationStatus
) : ModifiableULIDEntity()
