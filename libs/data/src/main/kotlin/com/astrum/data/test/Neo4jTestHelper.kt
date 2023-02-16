package com.astrum.data.test

import org.neo4j.configuration.GraphDatabaseSettings
import org.neo4j.configuration.GraphDatabaseSettings.DEFAULT_DATABASE_NAME
import org.neo4j.dbms.api.DatabaseManagementService
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder
import org.neo4j.driver.AuthTokens
import org.neo4j.driver.Driver
import org.neo4j.driver.GraphDatabase
import org.neo4j.graphdb.GraphDatabaseService
import org.springframework.data.neo4j.core.ReactiveNeo4jClient
import org.springframework.data.neo4j.core.ReactiveNeo4jTemplate
import org.springframework.data.neo4j.core.mapping.Neo4jMappingContext
import java.time.Duration
import kotlin.io.path.createTempDirectory
import kotlin.io.path.deleteExisting

class Neo4jTestHelper : ResourceTestHelper {
    lateinit var reactiveNeo4jTemplate: ReactiveNeo4jTemplate
    private lateinit var driver: Driver
    private lateinit var reactiveNeo4jClient: ReactiveNeo4jClient
    private val databaseDirectory = createTempDirectory("neo4j-test")
    private val managementService: DatabaseManagementService =
        DatabaseManagementServiceBuilder(databaseDirectory)
            .setConfig(GraphDatabaseSettings.pagecache_memory, 512)
            .setConfig(GraphDatabaseSettings.transaction_timeout, Duration.ofSeconds(60))
            .setConfig(GraphDatabaseSettings.preallocate_logical_logs, true).build()

    override fun setUp() {
        driver =
            GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "password"));
        reactiveNeo4jClient = ReactiveNeo4jClient.create(driver)
        val graphDb: GraphDatabaseService = managementService.database(DEFAULT_DATABASE_NAME)
        reactiveNeo4jTemplate =
            ReactiveNeo4jTemplate(
                reactiveNeo4jClient, Neo4jMappingContext.builder()
                    .build()
            )
        if (graphDb.isAvailable) {
            graphDb.executeTransactionally("MATCH (n) DETACH DELETE n")
        }
    }

    override fun tearDown() {
        managementService.shutdown()
        databaseDirectory.deleteExisting()
    }
}
