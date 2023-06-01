package com.astrum.data.repository.neo4j

import com.astrum.data.WeekProperty
import com.astrum.data.cache.*
import com.astrum.data.expansion.idProperty
import com.astrum.data.repository.QueryableRepository
import com.astrum.data.repository.cache.CachedQueryableRepository
import com.astrum.event.EventPublisher
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.common.base.CaseFormat
import com.google.common.cache.CacheBuilder
import org.redisson.api.RedissonClient
import org.slf4j.LoggerFactory
import org.springframework.data.neo4j.core.ReactiveNeo4jTemplate
import org.springframework.data.neo4j.core.schema.Node
import java.time.Duration
import java.time.Instant
import kotlin.reflect.KClass

private const val SIZE = 1000

@Suppress("UNCHECKED_CAST")
class Neo4jRepositoryBuilder<T : Any, ID : Any>(
    private var template: ReactiveNeo4jTemplate,
    private val clazz: KClass<T>
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private var eventPublisher: EventPublisher? = null
    private var cacheBuilder: (() -> CacheBuilder<Any, Any>)? = null

    private var cacheStorageManager: StorageManager? = null

    private var redisClient: RedissonClient? = null
    private var expiredAt: WeekProperty<T, Instant?>? = null
    private var size: Int? = null

    private var objectMapper: ObjectMapper? = null

    fun enableJsonMapping(objectMapper: ObjectMapper?): Neo4jRepositoryBuilder<T, ID> {
        log.debug("Enable json mapping {}", objectMapper)
        this.objectMapper = objectMapper
        return this
    }

    fun enableEvent(eventPublisher: EventPublisher?): Neo4jRepositoryBuilder<T, ID> {
        log.debug("Enable event {}", eventPublisher)
        this.eventPublisher = eventPublisher
        return this
    }

    fun enableCache(cacheBuilder: (() -> CacheBuilder<Any, Any>)?): Neo4jRepositoryBuilder<T, ID> {
        log.debug("Enable cache {}", cacheBuilder)
        this.cacheBuilder = cacheBuilder
        return this
    }

    fun enableCache(
        redisClient: RedissonClient?,
        expiredAt: WeekProperty<T, Instant?>?,
        size: Int?
    ): Neo4jRepositoryBuilder<T, ID> {
        log.debug("Enable cache {}", redisClient)
        this.redisClient = redisClient
        this.expiredAt = expiredAt
        this.size = size
        return this
    }

    fun build(): QueryableRepository<T, ID> {
        log.debug("Build repository {}", clazz)
        return applyCache(
            Neo4jQueryableRepositoryAdapter(
                SimpleNeo4jRepository(template, clazz, eventPublisher),
                clazz
            )
        )
    }

    private fun applyCache(repository: QueryableRepository<T, ID>): QueryableRepository<T, ID> {
        log.debug("Apply cache for class {} with repository {}", clazz, repository)
        val cacheBuilder = cacheBuilder ?: return repository
        val idProperty = createIdProperty()
        val redisClient = redisClient

        val storage = TransactionalStorage(
            if (redisClient != null) {
                MultiLevelNestedStorage(
                    RedisStorage(
                        redisClient,
                        name = CaseFormat.UPPER_CAMEL.to(
                            CaseFormat.LOWER_UNDERSCORE,
                            clazz.simpleName ?: ""
                        ),
                        size = size ?: SIZE,
                        objectMapper = objectMapper ?: jacksonObjectMapper(),
                        id = idProperty,
                        expiredAt = expiredAt ?: WeekProperty {
                            Instant.now().plus(Duration.ofMinutes(1))
                        },
                        keyClass = idProperty<T, ID?>(clazz).returnType.classifier as KClass<ID>,
                        valueClass = clazz,
                    ),
                    Pool { InMemoryStorage(cacheBuilder, idProperty) },
                    idProperty
                )
            } else {
                PoolingNestedStorage(Pool { InMemoryStorage(cacheBuilder, idProperty) }, idProperty)
            }
        )

        clazz.annotations.find { it is Node }?.let {
            (it as Node).value.forEach { label ->
                cacheStorageManager?.put(label, storage)
            }
        }

        log.debug("Storage {}", storage)

        return CachedQueryableRepository(repository, storage, idProperty, clazz)
    }

    private fun createIdProperty(): WeekProperty<T, ID?> {
        log.debug("Create id property for class {}", clazz)
        val idProperty = idProperty<T, ID?>(clazz)
        return WeekProperty { entity -> idProperty.get(entity) }
    }
}
