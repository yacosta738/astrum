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
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.neo4j.cypherdsl.core.Statement
import org.slf4j.LoggerFactory
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
    private val log = LoggerFactory.getLogger(SimpleNeo4jRepository::class.java)
    private val idProperty = (
            clazz.memberProperties.find {
                it.javaField?.annotations?.find { id -> id is Id } != null
            } ?: throw PropertyOrFieldMustNotBeNull()
            ) as KProperty1<T, ID?>

    private val eventPublisher = EventBroadcaster()
    private val entityChapter = EntityChapter(clazz)

    init {
        log.debug("Initializing repository for {}", clazz)
        val localEventEmitter = EventEmitter()
            .apply {
                on(TypeMatchEventFilter(BeforeCreateEvent::class), CreateTimestamp())
                on(TypeMatchEventFilter(BeforeUpdateEvent::class), UpdateTimestamp())
            }

        this.eventPublisher.use(localEventEmitter)
        eventPublisher?.let { this.eventPublisher.use(it) }
    }

    override suspend fun create(entity: T): T {
        log.debug("Creating entity \uD83D\uDD73\uFE0F {}", entity)
        eventPublisher.publish(BeforeCreateEvent(entity))

        return template.save(entity)
            .subscribeOn(Schedulers.parallel())
            .awaitSingle()
            .also { eventPublisher.publish(AfterCreateEvent(it)) }
    }

    override fun createAll(entities: Flow<T>): Flow<T> {
        log.debug("Creating entities \uD83D\uDD73\uFE0F")
        return flow {
            val saved = entities.onEach { eventPublisher.publish(BeforeCreateEvent(it)) }
                .toList()

            if (saved.isNotEmpty()) {
                log.debug("Saving entities {}", saved)
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
        log.debug("Creating entities {}", entities)
        return createAll(entities.asFlow())
    }

    suspend fun exists(predicate: (T) -> Boolean): Boolean {
        log.debug("Checking if entity exists \uD83D\uDE8F")
        return template.findAll(clazz::class.java)
            .subscribeOn(Schedulers.parallel())
            .asFlow().map { it as T }
            .firstOrNull(predicate) != null
    }

    override suspend fun existsById(id: ID): Boolean {
        log.debug("Checking if entity with id {} exists 🚏", id)
        return exists { idProperty.get(it) == id }
    }

    override suspend fun findById(id: ID): T? {
        log.debug("Finding entity with id {}", id)
        return template.findById(id, clazz.java)
            .subscribeOn(Schedulers.parallel())
            .awaitSingleOrNull()
    }

    override fun findAll(criteria: Statement?, limit: Int?, offset: Long?, sort: Sort?): Flow<T> {
        log.debug(
            "Finding all entities with criteria {} and limit {} and offset {} and sort {}",
            criteria,
            limit,
            offset,
            sort
        )
        print("findAll from SimpleNeo4jRepository with $criteria $limit $offset $sort")
        if (limit != null && limit <= 0) {
            log.debug("Limit is $limit, returning empty flow")
            return emptyFlow()
        }
        return criteria?.let {
            log.debug("Finding all entities with criteria {}", criteria)
            print("criteria is not null")
            template.findAll(it, clazz.java)
                .subscribeOn(Schedulers.parallel())
                .asFlow()
        } ?: findAll()
    }

    override suspend fun findOne(criteria: Statement): T? {
        log.debug("Finding one entity with criteria {}", criteria)
        return template.findOne(criteria, criteria.parameters, clazz.java)
            .subscribeOn(Schedulers.parallel())
            .awaitSingleOrNull()
    }

    override suspend fun exists(criteria: Statement): Boolean {
        log.debug("Checking if entity exists with criteria {}", criteria)
        return findOne(criteria) != null
    }

    override fun findAll(): Flow<T> {
        log.debug("Finding all entities")
        return template.findAll(clazz.java)
            .subscribeOn(Schedulers.parallel())
            .asFlow()
    }

    override suspend fun count(criteria: Statement?, limit: Int?): Long {
        log.debug("Counting entities with criteria {} and limit {}", criteria, limit)
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
        log.debug( "Counting all entities")
        return count(criteria = null)
    }


    @OptIn(FlowPreview::class)
    override fun updateAll(
        criteria: Statement,
        patch: SuspendPatch<T>,
        limit: Int?,
        offset: Long?,
        sort: Sort?
    ): Flow<T> {
        log.debug(
            "Updating all entities with criteria {} and patch {} and limit {} and offset {} and sort {}",
            criteria,
            patch,
            limit,
            offset,
            sort
        )
        print("updateAll from SimpleNeo4jRepository with $criteria $patch $limit $offset $sort")
        return findAll(criteria, limit, offset, sort)
            .flatMapMerge { entity ->
                print("entity is $entity")
                flow { emit(update(entity, patch)) }
            }
            .filterNotNull()
    }


    override fun updateAll(
        criteria: Statement,
        patch: Patch<T>,
        limit: Int?,
        offset: Long?,
        sort: Sort?
    ): Flow<T> {
        log.debug(
            "Updating all entities with criteria {} and patch {} and limit {} and offset {} and sort {}",
            criteria,
            patch,
            limit,
            offset,
            sort
        )
        return updateAll(criteria, patch.async(), limit, offset, sort)
    }

    override suspend fun deleteAll(criteria: Statement?, limit: Int?, offset: Long?, sort: Sort?) {
        log.debug(
            "Deleting all entities with criteria {} and limit {} and offset {} and sort {}",
            criteria,
            limit,
            offset,
            sort
        )
        if (limit != null && limit <= 0) {
            log.debug("Limit is $limit, returning")
            return
        }

        findAll(criteria, limit, offset, sort)
            .map { delete(it) }
            .collect()
    }

    override suspend fun deleteAll() {
        log.debug("Deleting all entities")
        deleteAll(criteria = null)
    }

    override suspend fun deleteAll(entities: Iterable<T>) {
        log.debug("Deleting all entities {}", entities)
        val ids = entities.mapNotNull { idProperty.get(it) }
        return deleteAllById(ids)
    }

    override suspend fun deleteAllById(ids: Iterable<ID>) {
        log.debug("Deleting all entities with ids {}", ids)
        if (ids.count() == 0) {
            log.debug("No ids, returning")
            return
        }

        template.deleteAllById(ids, clazz.java)
            .subscribeOn(Schedulers.parallel())
            .awaitFirstOrNull()
    }

    override suspend fun delete(entity: T) {
        log.debug("Deleting entity {}", entity)
        eventPublisher.publish(BeforeDeleteEvent(entity))

        val id = idProperty.get(entity) ?: throw PropertyOrFieldMustNotBeNull()

        log.debug("Deleting entity with id {}", id)

        template.deleteById(id, clazz.java)
            .subscribeOn(Schedulers.parallel())
            .awaitSingleOrNull()

        eventPublisher.publish(AfterDeleteEvent(entity))
    }

    override suspend fun deleteById(id: ID) {
        log.debug("Deleting entity with id {}", id)
        template.deleteById(id, clazz.java)
            .subscribeOn(Schedulers.parallel())
            .awaitFirstOrNull()
    }

    override fun updateAll(entity: Iterable<T>, patch: SuspendPatch<T>): Flow<T?> {
        log.debug("Updating all entities {} with patch {}", entity, patch)
        return entity.asFlow()
            .map { update(it, patch) }
    }

    override fun updateAll(entity: Iterable<T>, patch: Patch<T>): Flow<T?> {
        log.debug("Updating all entities {} with patch {}", entity, patch)
        return updateAll(entity, patch.async())
    }

    override fun updateAll(entity: Iterable<T>): Flow<T?> {
        log.debug("Updating all entities {}", entity)
        return entity.asFlow()
            .map { update(it) }
    }

    override fun updateAllById(ids: Iterable<ID>, patch: SuspendPatch<T>): Flow<T?> {
        log.debug("Updating all entities with ids {} with patch {}", ids, patch)
        return findAllById(ids)
            .map { update(it, patch) }
    }

    override fun updateAllById(ids: Iterable<ID>, patch: Patch<T>): Flow<T?> {
        log.debug("Updating all entities with ids {} with patch {}", ids, patch)
        return updateAllById(ids, patch.async())
    }

    override suspend fun update(entity: T, patch: SuspendPatch<T>): T? {
        log.debug("Updating entity {} with patch {}", entity, patch)
        print("update from SimpleNeo4jRepository with $entity $patch")
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
        log.debug("Updating entity with criteria {} with patch {}", criteria, patch)
        return findOne(criteria)?.let { update(it, patch) }
    }


    override suspend fun update(entity: T, patch: Patch<T>): T? {
        log.debug("Updating entity {} with patch {}", entity, patch)
        return update(entity, patch.async())
    }

    override suspend fun update(entity: T): T? {
        log.debug("Updating entity {}", entity)
        return idProperty.get(entity)?.let { updateById(it, SuspendPatch.from { entity }) }
    }

    override suspend fun update(criteria: Statement, patch: Patch<T>): T? {
        log.debug("Updating entity with criteria {} with patch {}", criteria, patch)
        return update(criteria, patch.async())
    }

    override suspend fun updateById(id: ID, patch: SuspendPatch<T>): T? {
        log.debug("Updating entity with id {} with patch {}", id, patch)
        return findById(id)?.let { update(it, patch) }
    }

    override suspend fun updateById(id: ID, patch: Patch<T>): T? {
        log.debug("Updating entity with id {} with patch {}", id, patch)
        return updateById(id, patch.async())
    }

    override fun findAllById(ids: Iterable<ID>): Flow<T> {
        log.debug("Finding all entities with ids {}", ids)
        if (ids.count() == 0) {
            log.debug("No ids, returning")
            return emptyFlow()
        }

        return template.findAllById(ids, clazz.java)
            .subscribeOn(Schedulers.parallel())
            .asFlow()
    }
}
