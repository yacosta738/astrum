package com.astrum.data.repository.r2dbc

import com.astrum.data.expansion.columnName
import org.springframework.data.relational.core.query.Criteria
import kotlin.reflect.KProperty

fun <T> where(property: KProperty<T>): Criteria.CriteriaStep {
    return Criteria.where(columnName(property))
}
