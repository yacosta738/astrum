package com.astrum.data.event

import com.astrum.data.Modifiable
import com.astrum.event.EventConsumer
import java.time.Instant

class CreateTimestamp : EventConsumer<BeforeCreateEvent<*>> {
    override suspend fun consume(event: BeforeCreateEvent<*>) {
        val entity = event.entity

        if (entity !is Modifiable) {
            return
        }

        entity.createdAt = Instant.now()
        entity.updatedAt = Instant.now()
    }
}
