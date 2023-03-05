package com.astrum.data.configuration

import com.astrum.data.annotation.ConverterScope
import org.neo4j.driver.AuthTokens
import org.neo4j.driver.Driver
import org.neo4j.driver.GraphDatabase
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.GenericConverter
import org.springframework.core.env.Environment
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.neo4j.config.AbstractReactiveNeo4jConfig
import org.springframework.data.neo4j.core.convert.Neo4jConversions
import org.springframework.data.neo4j.repository.config.EnableReactiveNeo4jRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement

@Configuration
@EnableReactiveNeo4jRepositories("com.astrum")
@EnableTransactionManagement
class Neo4jConfiguration(
    private val applicationContext: ApplicationContext,
    private val env: Environment
) : AbstractReactiveNeo4jConfig() {

    @Bean
    override fun neo4jConversions(): Neo4jConversions {
        val converters = applicationContext.getBeansOfType(GenericConverter::class.java)
            .values
            .filter {
                it.javaClass.annotations.any { annotation ->
                    annotation is WritingConverter || annotation is ReadingConverter
                }
            }
            .filter {
                val scope = it.javaClass.annotations.filterIsInstance<ConverterScope>()
                scope.isEmpty() || scope.any { converterScope -> converterScope.type == ConverterScope.Type.NEO4J }
            }
        return Neo4jConversions(converters)
    }
    /**
     * The driver to be used for interacting with Neo4j.
     *
     * @return the Neo4j Java driver instance to work with.
     */
    @Bean
    override fun driver(): Driver {
        val uri = env.getProperty("spring.data.neo4j.uri") ?: "bolt://localhost:7687"
        val username = env.getProperty("spring.data.neo4j.username") ?: "neo4j"
        val password = env.getProperty("spring.data.neo4j.password") ?: "password"
        return GraphDatabase.driver(uri, AuthTokens.basic(username, password))
    }
}
