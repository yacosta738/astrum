package com.astrum.data.test

import org.neo4j.configuration.GraphDatabaseSettings.DEFAULT_DATABASE_NAME
import org.neo4j.configuration.connectors.BoltConnector
import org.neo4j.configuration.helpers.SocketAddress
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.io.fs.FileUtils
import java.nio.file.Path

class Neo4jTestHelper : ResourceTestHelper {

    // tag::startDb[]
    private val managementService = DatabaseManagementServiceBuilder(DB_PATH)
        .setConfig(BoltConnector.enabled, true)
        .setConfig(BoltConnector.listen_address, SocketAddress("localhost", 7687))
        .build()
    private val graphDb: GraphDatabaseService = managementService.database(DEFAULT_DATABASE_NAME)
    // end::startDb[]

    override fun setUp() {
        println("Setting up Neo4j test helper")

        if (graphDb.isAvailable) {
            println("Neo4j is available")
            graphDb.executeTransactionally("MATCH (n) DETACH DELETE n")
        } else {
            println("Neo4j is not available")
        }
    }

    override fun tearDown() {
        graphDb.executeTransactionally("MATCH (n) DETACH DELETE n")
        managementService.shutdown()
        FileUtils.deleteDirectory(DB_PATH)
    }

    companion object {
        private val DB_PATH = Path.of("build/neo4j-store-with-bolt")
    }
}
