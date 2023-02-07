package com.astrum.event

import kotlin.reflect.KClass

annotation class Subscribe(
    val filterBy: KClass<*>
)
