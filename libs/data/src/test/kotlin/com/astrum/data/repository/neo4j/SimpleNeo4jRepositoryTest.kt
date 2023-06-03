package com.astrum.data.repository.neo4j

import com.astrum.data.configuration.Neo4jConfiguration
import com.astrum.data.converter.ULIDToValueConverter
import com.astrum.data.entity.Person
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.neo4j.core.ReactiveNeo4jTemplate
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ComponentScan(basePackages = ["com.astrum.data"])
@ContextConfiguration(classes = [Neo4jConfiguration::class, ULIDToValueConverter::class])
class SimpleNeo4jRepositoryTest @Autowired constructor(
    private var reactiveNeo4jTemplate: ReactiveNeo4jTemplate
): Neo4jRepositoryTestHelper(
    reactiveNeo4jTemplate,
    repositories = {
        listOf(SimpleNeo4jRepository(reactiveNeo4jTemplate, Person::class))
    }
)