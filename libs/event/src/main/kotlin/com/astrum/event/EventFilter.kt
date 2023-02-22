package com.astrum.event

interface EventFilter {
    suspend fun <E : Any> filter(event: E): Boolean
}
