package com.astrum.data.event

data class AfterCreateEvent<T>(
    val entity: T
)
