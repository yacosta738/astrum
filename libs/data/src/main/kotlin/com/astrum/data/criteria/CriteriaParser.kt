package com.astrum.data.criteria

interface CriteriaParser<Out : Any?> {
    fun parse(criteria: Criteria): Out
}
