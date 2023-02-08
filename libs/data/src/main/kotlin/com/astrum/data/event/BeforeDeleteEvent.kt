package com.astrum.data.event

data class BeforeDeleteEvent<T>(
    val entity: T
)
