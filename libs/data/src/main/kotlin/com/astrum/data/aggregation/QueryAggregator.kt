package com.astrum.data.aggregation

import com.astrum.data.cache.*
import com.astrum.data.criteria.Criteria
import com.astrum.data.criteria.RuntimeCriteriaParser
import com.astrum.data.criteria.or
import com.astrum.data.repository.QueryableRepository
import com.google.common.cache.CacheBuilder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Duration
import java.util.*
import kotlin.collections.Collection
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.Set
import kotlin.collections.all
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.emptyList
import kotlin.collections.first
import kotlin.collections.fold
import kotlin.collections.forEach
import kotlin.collections.getOrPut
import kotlin.collections.listOf
import kotlin.collections.map
import kotlin.collections.mapNotNull
import kotlin.collections.mapValues
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.collections.onEach
import kotlin.collections.reduce
import kotlin.collections.set
import kotlin.collections.toList
import kotlin.collections.toMutableSet
import kotlin.collections.toSet
import kotlin.reflect.KClass

class QueryAggregator<T : Any>(
    private val repository: QueryableRepository<T, *>,
    private val clazz: KClass<T>,
) {
    private val store = TransactionalQueryStorage(
        PoolingNestedQueryStorage(
            Pool {
                InMemoryQueryStorage(clazz) {
                    CacheBuilder.newBuilder()
                        .weakKeys()
                        .expireAfterWrite(Duration.ofSeconds(1))
                        .maximumSize(500)
                }
            }
        )
    )

    private val links = ReferenceStore<SelectQuery>()
    private val latest = WeakHashMap<SelectQuery, Collection<T>>()
    private val parser = RuntimeCriteriaParser(clazz)

    private val mutex = Mutex()

    suspend fun clear() {
        mutex.withLock {
            store.clear()
            latest.clear()
            links.clear()
        }
    }

    suspend fun clear(entity: T) {
        mutex.withLock {
            store.clear(entity)
        }
    }

    suspend fun clear(query: SelectQuery) {
        mutex.withLock {
            (store.getIfPresent(query) ?: latest[query])?.forEach {
                store.clear(it)
            } ?: store.clear()
            latest.remove(query)
            links.remove(query)
        }
    }

    fun link(query: SelectQuery) {
        links.push(query)
    }

    fun fetch(query: SelectQuery): Flow<T> {
        return flow {
            mutex.withLock {
                pop(query)?.onEach { emit(it) } ?: run {
                    val merged = mutableSetOf<SelectQuery>().also {
                        it.addAll(free())
                        it.add(query)
                    }

                    val limit = merged.map { it.limit }.fold(0 as Int?) { acc, cur ->
                        if (cur == null || acc == null) {
                            null
                        } else {
                            acc + cur
                        }
                    }

                    val result = repository.findAll(
                        merge(merged.mapNotNull { it.where }.toSet()),
                        limit = limit
                    ).toList()
                    val distributed = distribute(merged, result)

                    distributed.forEach { (key, value) ->
                        if (key != query) {
                            store.put(key, value)
                        } else {
                            latest[query] = value
                            value.onEach { emit(it) }
                        }
                    }
                }
            }
        }
    }

    private suspend fun pop(query: SelectQuery): Collection<T>? {
        return store.getIfPresent(query)?.let {
            store.remove(query)
            latest[query] = it
            it
        }
    }

    private suspend fun free(): Set<SelectQuery> {
        val curr = links.entries().toMutableSet()
        val used = store.entries().map { it.first }

        used.forEach {
            curr.remove(it)
        }

        return curr
    }

    private fun merge(criteria: Set<Criteria>): Criteria {
        return asInOperator(criteria) ?: criteria.reduce { acc, cur -> cur.or(acc) }
    }

    private fun asInOperator(criteria: Set<Criteria>): Criteria? {
        if (criteria.all { it is Criteria.In || it is Criteria.Equals }) {
            val keys = criteria.mapNotNull {
                when (it) {
                    is Criteria.In -> it.key
                    is Criteria.Equals -> it.key
                    else -> null
                }
            }.toSet()
            if (keys.size == 1) {
                return Criteria.In(
                    keys.first(),
                    criteria.map {
                        when (it) {
                            is Criteria.In -> it.value
                            is Criteria.Equals -> listOf(it.value)
                            else -> emptyList()
                        }
                    }.fold<List<Any?>, MutableList<Any?>>(mutableListOf()) { acc, cur ->
                        acc.also { it.addAll(cur) }
                    }.toSet().toList()
                )
            }
        }

        return null
    }

    private fun distribute(criteria: Set<SelectQuery>, values: List<T>): Map<SelectQuery, List<T>> {
        val result = mutableMapOf<SelectQuery, MutableList<T>>()

        criteria.forEach { q ->
            val filter = q.where?.let { parser.parse(it) } ?: { false }
            values.forEach { value ->
                if (filter(value)) {
                    result.getOrPut(q) { mutableListOf() }.add(value)
                }
            }
        }

        return result.mapValues { (key, value) ->
            if (key.limit != null && value.size > key.limit) {
                value.subList(0, key.limit)
            } else {
                value
            }
        }
    }
}
