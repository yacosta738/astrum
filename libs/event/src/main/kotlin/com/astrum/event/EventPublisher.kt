package com.astrum.event

interface EventPublisher {
    suspend fun <E : Any> publish(event: E)
}
