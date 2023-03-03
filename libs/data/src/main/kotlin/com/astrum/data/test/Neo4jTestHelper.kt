package com.astrum.data.test

import org.neo4j.configuration.GraphDatabaseSettings.DEFAULT_DATABASE_NAME
import org.neo4j.configuration.connectors.BoltConnector
import org.neo4j.configuration.helpers.SocketAddress
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.io.fs.FileUtils
import org.springframework.stereotype.Component
import java.nio.file.Path

@Component

class Neo4jTestHelper : ResourceTestHelper {

    // tag::startDb[]
    private val managementService = DatabaseManagementServiceBuilder(DB_PATH)
        .setConfig(BoltConnector.enabled, true)
        .setConfig(BoltConnector.listen_address, SocketAddress("localhost", 7687))
        .build()
    // end::startDb[]

    override fun setUp() {
        println("Setting up Neo4j test helper")
        val graphDb: GraphDatabaseService = managementService.database(DEFAULT_DATABASE_NAME)
        println("Graph database: $graphDb")
        if (graphDb.isAvailable) {
            println("Clearing database")
            graphDb.executeTransactionally("MATCH (n) DETACH DELETE n")
        }
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
