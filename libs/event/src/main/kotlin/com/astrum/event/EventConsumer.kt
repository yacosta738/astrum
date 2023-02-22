package com.astrum.event

interface EventConsumer<E : Any> {
    suspend fun consume(event: E)
}
