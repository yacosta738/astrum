package com.astrum.data

import com.astrum.data.annotation.GeneratedValue
import com.astrum.ulid.ULID
import org.springframework.data.annotation.Id

abstract class ULIDEntity : Entity<ULID>() {
    @Id
    @GeneratedValue
    override var id: ULID = ULID.randomULID()
}
