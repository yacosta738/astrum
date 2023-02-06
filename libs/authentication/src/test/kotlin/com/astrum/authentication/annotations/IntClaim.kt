package com.astrum.authentication.annotations

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class IntClaim(val name: String, val value: Int)
