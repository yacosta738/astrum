package com.astrum.data.event

data class AfterDeleteEvent<T>(
    val entity: T
)
