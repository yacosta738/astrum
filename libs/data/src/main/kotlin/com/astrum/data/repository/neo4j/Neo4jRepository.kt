package com.astrum.data.repository.neo4j

import com.astrum.data.patch.Patch
import com.astrum.data.patch.SuspendPatch
import com.astrum.data.repository.Repository
import kotlinx.coroutines.flow.Flow
import org.neo4j.cypherdsl.core.Statement
import org.springframework.data.domain.Sort

interface Neo4jRepository<T : Any, ID : Any> : Repository<T, ID> {
    suspend fun exists(criteria: Statement): Boolean
    suspend fun findOne(criteria: Statement): T?

    fun findAll(
        criteria: Statement? = null,
        limit: Int? = null,
        offset: Long? = null,
        sort: Sort? = null
    ): Flow<T>


    override suspend fun create(entity: T): T

    override fun createAll(entities: Flow<T>): Flow<T>

    override fun createAll(entities: Iterable<T>): Flow<T>

    override suspend fun existsById(id: ID): Boolean

    suspend fun update(criteria: Statement, patch: Patch<T>): T?

    suspend fun update(criteria: Statement, patch: SuspendPatch<T>): T?

    fun updateAll(
        criteria: Statement,
        patch: Patch<T>,
        limit: Int? = null,
        offset: Long? = null,
        sort: Sort? = null
    ): Flow<T>

    fun updateAll(
        criteria: Statement,
        patch: SuspendPatch<T>,
        limit: Int? = null,
        offset: Long? = null,
        sort: Sort? = null
    ): Flow<T>

    suspend fun count(criteria: Statement? = null, limit: Int? = null): Long

    suspend fun deleteAll(
        criteria: Statement? = null,
        limit: Int? = null,
        offset: Long? = null,
        sort: Sort? = null
    )
}
