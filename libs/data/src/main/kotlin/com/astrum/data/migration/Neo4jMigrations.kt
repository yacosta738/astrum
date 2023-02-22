package com.astrum.data.migration

import ac.simons.neo4j.migrations.core.JavaBasedMigration
import ac.simons.neo4j.migrations.core.MigrationContext
import ac.simons.neo4j.migrations.core.MigrationsException
import com.fasterxml.jackson.databind.ObjectMapper
import org.neo4j.driver.Values
import org.springframework.core.io.Resource
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import java.io.IOException
import java.util.*

/**
 * Initial database setup for Neo4j.
 */
@Suppress("unused")
class Neo4jMigrations {

    /**
     * Load users including authorities from JSON files.
     */
    @Suppress("unused")
    class V0001__CreateUsers : JavaBasedMigration {

        override fun apply(context: MigrationContext) {

            val om = ObjectMapper()
            val resourcePatternResolver = PathMatchingResourcePatternResolver()
            val resources: Array<Resource>
            try {
                resources =
                    resourcePatternResolver.getResources("classpath:config/neo4j/migrations/user__*.json")
            } catch (e: IOException) {
                throw MigrationsException("Could not load user definition resources.", e)
            }
            val type = om.typeFactory.constructMapType(
                Map::class.java,
                String::class.java,
                Object::class.java
            )
            val userLabel = "user"
            val authorityLabel = "authority"
            val query = String.format(
                "" +
                        "CREATE (u:%s) SET u = \$user WITH u " +
                        "UNWIND \$authorities AS authority " +
                        "MERGE (a:%s {name: authority}) " +
                        "CREATE (u) - [:HAS_AUTHORITY] -> (a) ",
                userLabel, authorityLabel
            )

            context.session.use { session ->
                resources.forEach {
                    try {
                        val user = om.readValue(it.inputStream, type) as MutableMap<String, Any>
                        user["user_id"] = UUID.randomUUID().toString() as Any
                        val authorities = user.remove("authorities") as List<*>
                        user.remove("_class")
                        session.executeWrite { t ->
                            t.run(
                                query,
                                Values.parameters("user", user, "authorities", authorities)
                            )
                                .consume()
                        }
                    } catch (e: IOException) {
                        throw MigrationsException("could not load resource ${it.description}.", e)
                    }
                }
            }
        }
    }
}
