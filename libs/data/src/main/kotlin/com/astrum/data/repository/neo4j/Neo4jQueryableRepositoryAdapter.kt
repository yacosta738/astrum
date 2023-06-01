package com.astrum.data.repository.neo4j

import com.astrum.data.criteria.Criteria
import com.astrum.data.patch.Patch
import com.astrum.data.patch.SuspendPatch
import com.astrum.data.repository.QueryableRepository
import com.astrum.data.repository.Repository
import kotlinx.coroutines.flow.Flow
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import kotlin.reflect.KClass

class Neo4jQueryableRepositoryAdapter<T : Any, ID : Any>(
    private val delegator: Neo4jRepository<T, ID>,
    clazz: KClass<T>
) : QueryableRepository<T, ID>, Repository<T, ID> by delegator {
    private val log = LoggerFactory.getLogger(javaClass)
    private val parser = Neo4jCriteriaParser(clazz)
    override suspend fun exists(criteria: Criteria): Boolean {
        log.debug("Exists {}", criteria)
        return parser.parse(criteria).let { delegator.exists(it) }
    }

    override suspend fun findOne(criteria: Criteria): T? {
        log.debug("Find one {}", criteria)
        return parser.parse(criteria).let { delegator.findOne(it) }
    }

    override fun findAll(criteria: Criteria?, limit: Int?, offset: Long?, sort: Sort?): Flow<T> {
        log.debug("Find all {} {} {} {}", criteria, limit, offset, sort)
        return delegator.findAll(criteria?.let { parser.parse(it) }, limit, offset, sort)
    }

    override suspend fun count(criteria: Criteria?, limit: Int?): Long {
        log.debug("Count {} {}", criteria, limit)
        return delegator.count(criteria?.let { parser.parse(it) }, limit)
    }

    override suspend fun deleteAll(criteria: Criteria?, limit: Int?, offset: Long?, sort: Sort?) {
        log.debug("Delete all {} {} {} {}", criteria, limit, offset, sort)
        return delegator.deleteAll(criteria?.let { parser.parse(it) }, limit, offset, sort)
    }

    override fun updateAll(
        criteria: Criteria,
        patch: SuspendPatch<T>,
        limit: Int?,
        offset: Long?,
        sort: Sort?
    ): Flow<T> {
        log.debug("Update all {} {} {} {}", criteria, limit, offset, sort)
        return parser.parse(criteria).let { delegator.updateAll(it, patch, limit, offset, sort) }
    }

    override fun updateAll(
        criteria: Criteria,
        patch: Patch<T>,
        limit: Int?,
        offset: Long?,
        sort: Sort?
    ): Flow<T> {
        log.debug("Update all {} {} {} {}", criteria, limit, offset, sort)
        return parser.parse(criteria).let { delegator.updateAll(it, patch, limit, offset, sort) }
    }

    override suspend fun update(criteria: Criteria, patch: SuspendPatch<T>): T? {
        log.debug("Update {} {}", criteria, patch)
        return parser.parse(criteria).let { delegator.update(it, patch) }
    }

    override suspend fun update(criteria: Criteria, patch: Patch<T>): T? {
        log.debug("Update {} {}", criteria, patch)
        return parser.parse(criteria).let { delegator.update(it, patch) }
    }
}
