package com.astrum.data.test

import com.astrum.data.converter.ULIDToValueConverter
import org.neo4j.configuration.GraphDatabaseSettings.DEFAULT_DATABASE_NAME
import org.neo4j.configuration.connectors.BoltConnector
import org.neo4j.configuration.helpers.SocketAddress
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder
import org.neo4j.driver.AuthTokens
import org.neo4j.driver.Driver
import org.neo4j.driver.GraphDatabase
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.io.fs.FileUtils
import org.springframework.context.support.GenericApplicationContext
import org.springframework.data.neo4j.config.Neo4jEntityScanner
import org.springframework.data.neo4j.core.ReactiveNeo4jClient
import org.springframework.data.neo4j.core.ReactiveNeo4jTemplate
import org.springframework.data.neo4j.core.convert.Neo4jConversions
import org.springframework.data.neo4j.core.mapping.Neo4jMappingContext
import java.nio.file.Path


class Neo4jTestHelper : ResourceTestHelper {
    lateinit var reactiveNeo4jTemplate: ReactiveNeo4jTemplate
    private lateinit var driver: Driver
    private lateinit var reactiveNeo4jClient: ReactiveNeo4jClient

    // tag::startDb[]
    private val managementService = DatabaseManagementServiceBuilder(DB_PATH)
        .setConfig(BoltConnector.enabled, true)
        .setConfig(BoltConnector.listen_address, SocketAddress("localhost", 7687))
        .build()
    // end::startDb[]

    override fun setUp() {
        println("Setting up Neo4j test helper")
        driver =
            GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "password"))
        reactiveNeo4jClient = ReactiveNeo4jClient.create(driver)
        val graphDb: GraphDatabaseService = managementService.database(DEFAULT_DATABASE_NAME)
        println("Graph database: $graphDb")
        reactiveNeo4jTemplate = reactiveNeo4jTemplate()
        if (graphDb.isAvailable) {
            println("Clearing database")
            graphDb.executeTransactionally("MATCH (n) DETACH DELETE n")
        }
    }

    private fun reactiveNeo4jTemplate(): ReactiveNeo4jTemplate {
        println("Creating reactive Neo4j template")
        val genericApplicationContext = GenericApplicationContext()
        genericApplicationContext.refresh()

        val ctx: Neo4jMappingContext = Neo4jMappingContext.builder()
            .withNeo4jConversions(Neo4jConversions(setOf(ULIDToValueConverter())))
            .build()
        ctx.setInitialEntitySet(Neo4jEntityScanner.get().scan("com.astrum.data.entity"))

        val template = ReactiveNeo4jTemplate(reactiveNeo4jClient, ctx)
        template.setBeanFactory(genericApplicationContext)
        return template
    }

    override fun tearDown() {
        println { "Tearing down Neo4j test helper" }
        managementService.shutdown()
        // delete the database directory and all its contents recursively (if it exists) after the test
        FileUtils.deleteDirectory(DB_PATH)
    }

    companion object {
        private val DB_PATH = Path.of("build/neo4j-store-with-bolt")
    }
}
