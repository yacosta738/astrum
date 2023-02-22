package com.astrum.data.aggregation

import com.astrum.data.cache.SelectQuery
import com.astrum.data.criteria.Criteria
import com.astrum.data.repository.QueryableRepository
import kotlin.reflect.KClass

class AggregateContext<T : Any>(
    repository: QueryableRepository<T, *>,
    clazz: KClass<T>,
) {
    private val queryAggregator = QueryAggregator(repository, clazz)

    suspend fun clear() {
        queryAggregator.clear()
    }

    suspend fun clear(entity: T) {
        queryAggregator.clear(entity)
    }

    fun join(criteria: Criteria?, limit: Int? = null): QueryFetcher<T> {
        return QueryFetcher(SelectQuery(criteria, limit), queryAggregator)
    }
}
