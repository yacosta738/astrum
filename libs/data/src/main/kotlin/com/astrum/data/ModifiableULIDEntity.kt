package com.astrum.data

import com.astrum.data.annotation.GeneratedValue
import java.time.Instant

abstract class ModifiableULIDEntity : ULIDEntity(), Modifiable {
    @GeneratedValue
    override var createdAt: Instant? = Instant.now()

    @GeneratedValue
    override var updatedAt: Instant? = Instant.now()
}
