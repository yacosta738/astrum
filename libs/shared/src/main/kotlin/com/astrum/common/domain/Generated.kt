package com.astrum.common.domain

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS, AnnotationTarget.CONSTRUCTOR)
annotation class Generated(val reason: String = "")