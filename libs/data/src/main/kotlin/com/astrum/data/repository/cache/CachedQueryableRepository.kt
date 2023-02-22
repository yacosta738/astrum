package com.astrum.data.repository.cache

import com.astrum.data.WeekProperty
import com.astrum.data.cache.Storage
import com.astrum.data.cache.createIndexes
import com.astrum.data.cache.getIndexNameAndValue
import com.astrum.data.criteria.Criteria
import com.astrum.data.criteria.where
import com.astrum.data.patch.Patch
import com.astrum.data.patch.SuspendPatch
import com.astrum.data.patch.async
import com.astrum.data.repository.QueryableRepository
import com.astrum.data.repository.Repository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import org.springframework.data.domain.Sort
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
class CachedQueryableRepository<T : Any, ID : Any>(
    private val delegator: QueryableRepository<T, ID>,
    private val storage: Storage<ID, T>,
    private val id: WeekProperty<T, ID?>,
    private val clazz: KClass<T>
) : QueryableRepository<T, ID>,
    Repository<T, ID> by SimpleCachedRepository(
        delegator,
        storage,
        id,
    ) {

    init {
        runBlocking { storage.createIndexes(clazz) }
    }

    override suspend fun exists(criteria: Criteria): Boolean {
        return getUniqueIndexNameAndValue(criteria)
            ?.let { (indexName, value) -> storage.getIfPresent(indexName, value) }
            ?.let { true }
            ?: delegator.exists(criteria)
    }

    override suspend fun findOne(criteria: Criteria): T? {
        val (indexName, value) = getUniqueIndexNameAndValue(criteria) ?: return kotlin.run {
            delegator.findOne(criteria)
                ?.also { storage.add(it) }
        }
        return storage.getIfPresent(indexName, value) { delegator.findOne(criteria) }
    }

    override fun findAll(criteria: Criteria?, limit: Int?, offset: Long?, sort: Sort?): Flow<T> {
        if (limit != null && limit <= 0) {
            return emptyFlow()
        }

        return flow {
            if (criteria != null && (offset == null || offset == 0L)) {
                val indexNameAndValue = getUniqueIndexNameAndValue(criteria)
                if (indexNameAndValue != null) {
                    val (indexName, value) = indexNameAndValue
                    storage.getIfPresent(indexName, value) { delegator.findOne(criteria) }
                        ?.let { emit(it) }
                    return@flow
                }
            }

            if (criteria != null && limit == null && offset == null && sort == null) {
                if (criteria is Criteria.In) {
                    val key = criteria.key
                    val value = criteria.value

                    if (storage.containsIndex(key)) {
                        val result = mutableListOf<T>()
                        val notCachedKey = mutableListOf<Any?>()

                        storage.getAll(key, value.map { ArrayList<Any?>().apply { add(it) } })
                            .collectIndexed { index, cached ->
                                val current = value[index]
                                if (cached == null) {
                                    notCachedKey.add(current)
                                } else {
                                    result.add(cached)
                                }
                            }

                        if (notCachedKey.isNotEmpty()) {
                            delegator.findAll(where(key).`in`(notCachedKey))
                                .onEach { storage.add(it) }
                                .collect { result.add(it) }
                        }

                        return@flow emitAll(result.asFlow())
                    }
                }
            }

            return@flow emitAll(
                delegator.findAll(criteria, limit, offset, sort)
                    .onEach { storage.add(it) }
            )
        }
    }

    override suspend fun update(criteria: Criteria, patch: Patch<T>): T? {
        return update(criteria, patch.async())
    }

    override suspend fun update(criteria: Criteria, patch: SuspendPatch<T>): T? {
        return delegator.update(criteria, patch)
            ?.also { storage.add(it) }
    }

    override fun updateAll(
        criteria: Criteria,
        patch: Patch<T>,
        limit: Int?,
        offset: Long?,
        sort: Sort?
    ): Flow<T> {
        return updateAll(criteria, patch.async(), limit, offset, sort)
    }

    override fun updateAll(
        criteria: Criteria,
        patch: SuspendPatch<T>,
        limit: Int?,
        offset: Long?,
        sort: Sort?
    ): Flow<T> {
        if (limit != null && limit <= 0) {
            return emptyFlow()
        }
        return flow {
            emitAll(
                delegator.updateAll(criteria, patch, limit, offset, sort)
                    .onEach { storage.add(it) }
            )
        }
    }

    override suspend fun count(criteria: Criteria?, limit: Int?): Long {
        if (limit != null && limit <= 0) {
            return 0
        }
        return delegator.count(criteria, limit)
    }

    override suspend fun deleteAll(criteria: Criteria?, limit: Int?, offset: Long?, sort: Sort?) {
        if (limit != null && limit <= 0) {
            return
        }
        if (criteria == null) {
            storage.clear()
            delegator.deleteAll()
        } else {
            deleteAll(findAll(criteria, limit, offset, sort).toList())
        }
    }

    private suspend fun getUniqueIndexNameAndValue(criteria: Criteria?): Pair<String, Any>? {
        if (criteria == null) return null

        val columnsAndValues = getSimpleJoinedColumnsAndValues(criteria) ?: return null
        val (columns, values) = columnsAndValues

        return storage.getIndexNameAndValue(columns, values)
    }

    private fun getSimpleJoinedColumnsAndValues(criteria: Criteria): Pair<MutableList<String>, MutableList<Any?>>? {
        val columns = mutableListOf<String>()
        val values = mutableListOf<Any?>()

        when (criteria) {
            is Criteria.And -> {
                if (criteria.value.size == 1) {
                    return getSimpleJoinedColumnsAndValues(criteria.value[0])
                }

                criteria.value.forEach {
                    val (childColumns, childValues) = getSimpleJoinedColumnsAndValues(it)
                        ?: return null
                    columns.addAll(childColumns)
                    values.addAll(childValues)
                }
            }

            is Criteria.Equals -> {
                val key = criteria.key
                val value = criteria.value

                columns.add(key)
                values.add(value)
            }

            else -> return null
        }

        return columns to values
    }
}
