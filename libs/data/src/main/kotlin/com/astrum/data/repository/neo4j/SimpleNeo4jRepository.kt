package com.astrum.data.repository.neo4j

import com.astrum.data.event.*
import com.astrum.data.patch.Patch
import com.astrum.data.patch.SuspendPatch
import com.astrum.data.patch.async
import com.astrum.data.repository.EntityChapter
import com.astrum.event.EventBroadcaster
import com.astrum.event.EventEmitter
import com.astrum.event.EventPublisher
import com.astrum.event.TypeMatchEventFilter
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.neo4j.cypherdsl.core.Statement
import org.springframework.data.domain.Sort
import org.springframework.data.neo4j.core.ReactiveNeo4jTemplate
import org.springframework.data.neo4j.core.schema.Id
import reactor.core.scheduler.Schedulers
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

@Suppress("UNCHECKED_CAST")
class SimpleNeo4jRepository<T : Any, ID : Any>(
    private var template: ReactiveNeo4jTemplate,
    val clazz: KClass<T>,
    eventPublisher: EventPublisher? = null,
) : Neo4jRepository<T, ID> {
    private val idProperty = (
            clazz.memberProperties.find {
                it.javaField?.annotations?.find { id -> id is Id } != null
            } ?: throw PropertyOrFieldMustNotBeNull()
            ) as KProperty1<T, ID?>

    private val eventPublisher = EventBroadcaster()
    private val entityChapter = EntityChapter(clazz)

    init {
        val localEventEmitter = EventEmitter()
            .apply {
                on(TypeMatchEventFilter(BeforeCreateEvent::class), CreateTimestamp())
                on(TypeMatchEventFilter(BeforeUpdateEvent::class), UpdateTimestamp())
            }

        this.eventPublisher.use(localEventEmitter)
        eventPublisher?.let { this.eventPublisher.use(it) }
    }

    override suspend fun create(entity: T): T {
        eventPublisher.publish(BeforeCreateEvent(entity))

        return template.save(entity)
            .subscribeOn(Schedulers.parallel())
            .awaitSingle()
            .also { eventPublisher.publish(AfterCreateEvent(it)) }
    }

    override fun createAll(entities: Flow<T>): Flow<T> {
        return flow {
            val saved = entities.onEach { eventPublisher.publish(BeforeCreateEvent(it)) }
                .toList()

            if (saved.isNotEmpty()) {
                emitAll(
                    template.saveAll(saved)
                        .subscribeOn(Schedulers.parallel())
                        .asFlow()
                        .onEach { eventPublisher.publish(AfterCreateEvent(it)) }
                )
            }
        }
    }

    override fun createAll(entities: Iterable<T>): Flow<T> {
        return createAll(entities.asFlow())
    }

    suspend fun exists(predicate: (T) -> Boolean): Boolean {
        return template.findAll(clazz::class.java)
            .subscribeOn(Schedulers.parallel())
            .asFlow().map { it as T }
            .firstOrNull(predicate) != null
    }

    override suspend fun existsById(id: ID): Boolean {
        return exists { idProperty.get(it) == id }
    }

    override suspend fun findById(id: ID): T? {
        return template.findById(id, clazz.java)
            .subscribeOn(Schedulers.parallel())
            .awaitSingleOrNull()
    }

    override fun findAll(criteria: Statement?, limit: Int?, offset: Long?, sort: Sort?): Flow<T> {
        if (limit != null && limit <= 0) {
            return emptyFlow()
        }
        return criteria?.let {
            template.findAll(it, clazz.java)
                .subscribeOn(Schedulers.parallel())
                .asFlow()
        } ?: findAll()
    }

    override suspend fun findOne(criteria: Statement): T? {
        val allData = template.findAll(clazz.java)
            .subscribeOn(Schedulers.parallel())
            .asFlow()
            .toList()
        println("clazz: ${clazz.java}")
        println("allData: $allData")
        println("cypher: ${criteria.cypher}")
        println("parameters: ${criteria.parameters}")
        println("parameterNames: ${criteria.parameterNames}")
        println("context: ${criteria.context}")
        println("identifiableExpressions: ${criteria.identifiableExpressions}")
        criteria.identifiableExpressions.forEach {
            println("isTrue: ${it.isTrue}")
            println("isNull: ${it.isNull}")
            println("isEmpty: ${it.isEmpty}")
        }

        return template.findOne(criteria.cypher, criteria.parameters, clazz.java)
            .subscribeOn(Schedulers.parallel())
            .awaitSingleOrNull()
    }

    override suspend fun exists(criteria: Statement): Boolean {
        return findOne(criteria) != null
    }

    override fun findAll(): Flow<T> {
        return template.findAll(clazz.java)
            .subscribeOn(Schedulers.parallel())
            .asFlow()
    }

    override suspend fun count(criteria: Statement?, limit: Int?): Long {
        if (limit != null && limit <= 0) {
            return 0
        }

        return criteria?.let {
            template.count(it)
                .subscribeOn(Schedulers.parallel())
                .awaitFirstOrNull()
        } ?: template.count(clazz.java)
            .subscribeOn(Schedulers.parallel())
            .awaitSingle()
    }

    override suspend fun count(): Long {
        return count(criteria = null)
    }


    override fun updateAll(
        criteria: Statement,
        patch: SuspendPatch<T>,
        limit: Int?,
        offset: Long?,
        sort: Sort?
    ): Flow<T> {
        return findAll(criteria, limit, offset, sort)
            .map { update(it, patch) }
            .filterNotNull()
    }

    override fun updateAll(
        criteria: Statement,
        patch: Patch<T>,
        limit: Int?,
        offset: Long?,
        sort: Sort?
    ): Flow<T> {
        return updateAll(criteria, patch.async(), limit, offset, sort)
    }

    override suspend fun deleteAll(criteria: Statement?, limit: Int?, offset: Long?, sort: Sort?) {
        if (limit != null && limit <= 0) {
            return
        }

        findAll(criteria, limit, offset, sort)
            .map { delete(it) }
            .collect()
    }

    override suspend fun deleteAll() {
        deleteAll(criteria = null)
    }

    override suspend fun deleteAll(entities: Iterable<T>) {
        val ids = entities.mapNotNull { idProperty.get(it) }
        return deleteAllById(ids)
    }

    override suspend fun deleteAllById(ids: Iterable<ID>) {
        if (ids.count() == 0) {
            return
        }

        template.deleteAllById(ids, clazz.java)
            .subscribeOn(Schedulers.parallel())
            .awaitFirstOrNull()
    }

    override suspend fun delete(entity: T) {
        eventPublisher.publish(BeforeDeleteEvent(entity))

        val id = idProperty.get(entity) ?: throw PropertyOrFieldMustNotBeNull()

        template.deleteById(id, clazz.java)
            .subscribeOn(Schedulers.parallel())
            .awaitSingleOrNull()

        eventPublisher.publish(AfterDeleteEvent(entity))
    }

    override suspend fun deleteById(id: ID) {
        template.deleteById(id, clazz.java)
            .subscribeOn(Schedulers.parallel())
            .awaitFirstOrNull()
    }

    override fun updateAll(entity: Iterable<T>, patch: SuspendPatch<T>): Flow<T?> {
        return entity.asFlow()
            .map { update(it, patch) }
    }

    override fun updateAll(entity: Iterable<T>, patch: Patch<T>): Flow<T?> {
        return updateAll(entity, patch.async())
    }

    override fun updateAll(entity: Iterable<T>): Flow<T?> {
        return entity.asFlow()
            .map { update(it) }
    }

    override fun updateAllById(ids: Iterable<ID>, patch: SuspendPatch<T>): Flow<T?> {
        return findAllById(ids)
            .map { update(it, patch) }
    }

    override fun updateAllById(ids: Iterable<ID>, patch: Patch<T>): Flow<T?> {
        return updateAllById(ids, patch.async())
    }

    override suspend fun update(entity: T, patch: SuspendPatch<T>): T? {
        val sourceDump = entityChapter.snapshot(entity)
        val target = patch.apply(entity)
        val targetDump = entityChapter.snapshot(target)
        val propertyDiff = entityChapter.diff(sourceDump, targetDump)

        eventPublisher.publish(BeforeUpdateEvent(target, propertyDiff))

        return template.findById(idProperty.get(target)!!, clazz.java)
            .subscribeOn(Schedulers.parallel())
            .awaitSingleOrNull()
            ?.let { template.save(target) }
            ?.subscribeOn(Schedulers.parallel())
            ?.awaitSingleOrNull()
            ?.also { eventPublisher.publish(AfterUpdateEvent(it, propertyDiff)) }
    }

    override suspend fun update(criteria: Statement, patch: SuspendPatch<T>): T? {
        return findOne(criteria)?.let { update(it, patch) }
    }


    override suspend fun update(entity: T, patch: Patch<T>): T? {
        return update(entity, patch.async())
    }

    override suspend fun update(entity: T): T? {
        return idProperty.get(entity)?.let { updateById(it, SuspendPatch.from { entity }) }
    }

    override suspend fun update(criteria: Statement, patch: Patch<T>): T? {
        return update(criteria, patch.async())
    }

    override suspend fun updateById(id: ID, patch: SuspendPatch<T>): T? {
        return findById(id)?.let { update(it, patch) }
    }

    override suspend fun updateById(id: ID, patch: Patch<T>): T? {
        return updateById(id, patch.async())
    }

    override fun findAllById(ids: Iterable<ID>): Flow<T> {
        if (ids.count() == 0) {
            return emptyFlow()
        }

        return template.findAllById(ids, clazz.java)
            .subscribeOn(Schedulers.parallel())
            .asFlow()
    }
}
