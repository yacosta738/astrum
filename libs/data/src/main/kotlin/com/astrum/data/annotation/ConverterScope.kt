package com.astrum.data.annotation

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ConverterScope(
    val type: Type
) {
    enum class Type {
        NEO4J,
        R2DBC
    }
}
