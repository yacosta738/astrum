package com.astrum.data.expansion

import org.neo4j.cypherdsl.core.Property
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField
import org.springframework.data.neo4j.core.schema.Id as IdGraph

fun <T> columnName(property: KProperty<T>): String {
    val column = property
        .javaField
        ?.annotations
        ?.filterIsInstance<Column>()
        ?.firstOrNull()

    return column?.value ?: property.name
}

fun <T> propertyName(property: KProperty<T>): String {
    val column = property
        .javaField
        ?.annotations?.firstOrNull { it is Property } as Property?

    return column?.name ?: property.name
}

@Suppress("UNCHECKED_CAST")
fun <T : Any, ID : Any?> idProperty(clazz: KClass<T>): KProperty1<T, ID> {
    return (
            clazz.memberProperties.find {
                it.javaField?.annotations?.find { id -> (id is Id) || (id is IdGraph) } != null
            }
                ?: throw IdColumnException(clazz)
            ) as KProperty1<T, ID>
}

data class IdColumnException(val clazz: KClass<*>) :
    RuntimeException("Can't find id column in ${clazz.simpleName}")
