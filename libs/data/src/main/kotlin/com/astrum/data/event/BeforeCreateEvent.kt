package com.astrum.data.event

data class BeforeCreateEvent<T>(
    val entity: T
)
