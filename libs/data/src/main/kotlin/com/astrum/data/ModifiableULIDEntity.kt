package com.astrum.data

import com.astrum.data.annotation.GeneratedValue
import java.time.Instant

abstract class ModifiableULIDEntity : ULIDEntity(), Modifiable {
    @GeneratedValue
    override var createdAt: Instant? = null

    @GeneratedValue
    override var updatedAt: Instant? = null
}
