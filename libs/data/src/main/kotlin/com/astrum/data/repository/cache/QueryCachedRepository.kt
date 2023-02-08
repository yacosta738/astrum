package com.astrum.data.repository.cache

import com.astrum.data.cache.QueryStorage
import com.astrum.data.cache.SelectQuery
import com.astrum.data.cache.get
import com.astrum.data.cache.getIfPresent
import com.astrum.data.criteria.Criteria
import com.astrum.data.criteria.where
import com.astrum.data.expansion.idProperty
import com.astrum.data.patch.Patch
import com.astrum.data.patch.SuspendPatch
import com.astrum.data.repository.QueryableRepository
import kotlinx.coroutines.flow.*
import org.springframework.data.domain.Sort
import kotlin.reflect.KClass

class QueryCachedRepository<T : Any, ID : Any>(
    private val delegator: QueryableRepository<T, ID>,
    private val storage: QueryStorage<T>,
    clazz: KClass<T>
) : QueryableRepository<T, ID> {
    @Suppress("UNCHECKED_CAST")
    private val idProperty = idProperty<T, ID>(clazz)

    override suspend fun create(entity: T): T {
        storage.clear()
        return delegator.create(entity)
    }

    override fun createAll(entities: Flow<T>): Flow<T> {
        return flow {
            emitAll(
                delegator.createAll(
                    entities.onEach { storage.clear(it) }
                )
            )
        }
    }

    override fun createAll(entities: Iterable<T>): Flow<T> {
        return flow {
            emitAll(
                delegator.createAll(
                    entities.onEach { storage.clear(it) }
                )
            )
        }
    }

    override suspend fun existsById(id: ID): Boolean {
        return delegator.existsById(id)
    }

    override suspend fun count(): Long {
        return delegator.count()
    }

    override suspend fun count(criteria: Criteria?, limit: Int?): Long {
        if (limit != null && limit <= 0) {
            return 0
        }
        return delegator.count(criteria, limit)
    }

    override suspend fun findById(id: ID): T? {
        return storage.getIfPresent(where(idProperty).`is`(id)) {
            delegator.findById(id)
        }
    }

    override fun findAll(): Flow<T> {
        return storage.get(SelectQuery(null, null, null, null)) {
            delegator.findAll()
        }
    }

    override fun findAllById(ids: Iterable<ID>): Flow<T> {
        if (ids.count() == 0) {
            return emptyFlow()
        }
        val query = SelectQuery(where(idProperty).`in`(ids.toList()), ids.count(), null, null)
        return storage.get(query) {
            delegator.findAllById(ids)
        }
    }

    override suspend fun findOne(criteria: Criteria): T? {
        return storage.getIfPresent(criteria) {
            delegator.findOne(criteria)
        }
    }

    override fun findAll(criteria: Criteria?, limit: Int?, offset: Long?, sort: Sort?): Flow<T> {
        if (limit != null && limit <= 0) {
            return emptyFlow()
        }
        val query = SelectQuery(criteria, limit, offset, sort)
        return storage.get(query) {
            delegator.findAll(criteria, limit, offset, sort)
        }
    }

    override suspend fun updateById(id: ID, patch: Patch<T>): T? {
        return delegator.updateById(
            id,
            SuspendPatch.from {
                storage.clear(it)
                patch.apply(it)
            }
        )
    }

    override suspend fun updateById(id: ID, patch: SuspendPatch<T>): T? {
        return delegator.updateById(
            id,
            SuspendPatch.from {
                storage.clear(it)
                patch.apply(it)
            }
        )
    }

    override suspend fun update(entity: T): T? {
        storage.clear(entity)
        return delegator.update(entity)
    }

    override suspend fun update(entity: T, patch: Patch<T>): T? {
        storage.clear(entity)
        return delegator.update(entity, patch)
    }

    override suspend fun update(entity: T, patch: SuspendPatch<T>): T? {
        storage.clear(entity)
        return delegator.update(entity, patch)
    }

    override fun updateAllById(ids: Iterable<ID>, patch: Patch<T>): Flow<T?> {
        if (ids.count() == 0) {
            return emptyFlow()
        }
        return flow {
            emitAll(
                delegator.updateAllById(
                    ids,
                    SuspendPatch.from {
                        storage.clear(it)
                        patch.apply(it)
                    }
                )
            )
        }
    }

    override fun updateAllById(ids: Iterable<ID>, patch: SuspendPatch<T>): Flow<T?> {
        if (ids.count() == 0) {
            return emptyFlow()
        }
        return flow {
            emitAll(
                delegator.updateAllById(
                    ids,
                    SuspendPatch.from {
                        storage.clear(it)
                        patch.apply(it)
                    }
                )
            )
        }
    }

    override fun updateAll(entity: Iterable<T>): Flow<T?> {
        return flow {
            entity.forEach { storage.clear(it) }
            emitAll(delegator.updateAll(entity))
        }
    }

    override fun updateAll(entity: Iterable<T>, patch: Patch<T>): Flow<T?> {
        return flow {
            entity.forEach { storage.clear(it) }
            emitAll(delegator.updateAll(entity, patch))
        }
    }

    override fun updateAll(entity: Iterable<T>, patch: SuspendPatch<T>): Flow<T?> {
        return flow {
            entity.forEach { storage.clear(it) }
            emitAll(delegator.updateAll(entity, patch))
        }
    }

    override suspend fun update(criteria: Criteria, patch: Patch<T>): T? {
        return delegator.update(
            criteria,
            SuspendPatch.from {
                storage.clear(it)
                patch.apply(it)
            }
        )
    }

    override suspend fun update(criteria: Criteria, patch: SuspendPatch<T>): T? {
        return delegator.update(
            criteria,
            SuspendPatch.from {
                storage.clear(it)
                patch.apply(it)
            }
        )
    }

    override fun updateAll(
        criteria: Criteria,
        patch: Patch<T>,
        limit: Int?,
        offset: Long?,
        sort: Sort?
    ): Flow<T> {
        if (limit != null && limit <= 0) {
            return emptyFlow()
        }
        return flow {
            emitAll(
                delegator.updateAll(
                    criteria,
                    SuspendPatch.from {
                        storage.clear(it)
                        patch.apply(it)
                    },
                    limit, offset, sort
                )
            )
        }
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
                delegator.updateAll(
                    criteria,
                    SuspendPatch.from {
                        storage.clear(it)
                        patch.apply(it)
                    },
                    limit, offset, sort
                )
            )
        }
    }

    override suspend fun deleteById(id: ID) {
        storage.clear()
        return delegator.deleteById(id)
    }

    override suspend fun delete(entity: T) {
        storage.clear(entity)
        return delegator.delete(entity)
    }

    override suspend fun deleteAllById(ids: Iterable<ID>) {
        if (ids.count() == 0) {
            return
        }
        deleteAll(delegator.findAllById(ids).toList())
    }

    override suspend fun deleteAll(entities: Iterable<T>) {
        if (entities.count() == 0) {
            return
        }
        entities.forEach { storage.clear(it) }
        return delegator.deleteAll(entities)
    }

    override suspend fun deleteAll() {
        storage.clear()
        return delegator.deleteAll()
    }

    override suspend fun exists(criteria: Criteria): Boolean {
        return delegator.exists(criteria)
    }

    override suspend fun deleteAll(criteria: Criteria?, limit: Int?, offset: Long?, sort: Sort?) {
        if (limit != null && limit <= 0) {
            return
        }
        deleteAll(delegator.findAll(criteria, limit, offset, sort).toList())
    }
}
