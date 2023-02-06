package com.astrum.authentication.annotations

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class StringArrayClaim(val name: String, val value: Array<String>)
