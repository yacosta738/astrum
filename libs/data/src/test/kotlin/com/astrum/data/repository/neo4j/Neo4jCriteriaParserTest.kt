package com.astrum.data.repository.neo4j

import com.astrum.data.criteria.Criteria
import com.astrum.data.criteria.and
import com.astrum.data.criteria.or
import com.astrum.data.criteria.where
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.regex.Pattern

class Neo4jCriteriaParserTest {
    private data class TestData(
        var name: String,
        var age: Int,
        var activate: Boolean
    )

    private data class TestCase(
        val query: Criteria,
        val cypher: String? = null,
    )

    private val parser = Neo4jCriteriaParser(TestData::class)

    @Test
    fun parse() {
        val testCases = listOf(
            TestCase(
                query = where(TestData::name).not("test"),
                cypher = "MATCH (n:`TestData`) WHERE n.name <> 'test' RETURN n, collect(n)"
            ),
            TestCase(
                query = where(TestData::name).`is`("test"),
                cypher = "MATCH (n:`TestData`) WHERE n.name = 'test' RETURN n, collect(n)"
            ),
            TestCase(
                query = where(TestData::age).between(0..10),
                cypher = "MATCH (n:`TestData`) WHERE (n.age >= 0 AND n.age <= 10) RETURN n, collect(n)"
            ),
            TestCase(
                query = where(TestData::age).notBetween(0..10),
                cypher = "MATCH (n:`TestData`) WHERE (n.age < 0 OR n.age > 10) RETURN n, collect(n)"
            ),
            TestCase(
                query = where(TestData::age).lessThan(0),
                cypher = "MATCH (n:`TestData`) WHERE n.age < 0 RETURN n, collect(n)"
            ),
            TestCase(
                query = where(TestData::age).lessThanOrEquals(0),
                cypher = "MATCH (n:`TestData`) WHERE n.age <= 0 RETURN n, collect(n)"
            ),
            TestCase(
                query = where(TestData::age).greaterThan(0),
                cypher = "MATCH (n:`TestData`) WHERE n.age > 0 RETURN n, collect(n)"
            ),
            TestCase(
                query = where(TestData::age).greaterThanOrEquals(0),
                cypher = "MATCH (n:`TestData`) WHERE n.age >= 0 RETURN n, collect(n)"
            ),
            TestCase(
                query = where(TestData::age).isNull(),
                cypher = "MATCH (n:`TestData`) WHERE n.age IS NULL RETURN n, collect(n)"
            ),
            TestCase(
                query = where(TestData::age).isNotNull(),
                cypher = "MATCH (n:`TestData`) WHERE n.age IS NOT NULL RETURN n, collect(n)"
            ),
            TestCase(
                query = where(TestData::name).regexp(Pattern.compile("test")),
                cypher = "MATCH (n:`TestData`) WHERE n.name =~ 'test' RETURN n, collect(n)"
            ),
            TestCase(
                query = where(TestData::name).notRegexp(Pattern.compile("test")),
                cypher = "MATCH (n:`TestData`) WHERE NOT (n.name =~ 'test') RETURN n, collect(n)"
            ),
            TestCase(
                query = where(TestData::name).like("%abc[%]%abc%"),
                cypher = "MATCH (n:`TestData`) WHERE n.name CONTAINS '%abc[%]%abc%' RETURN n, collect(n)"
            ),
            TestCase(
                query = where(TestData::name).notLike("%abc[%]%abc%"),
                cypher = "MATCH (n:`TestData`) WHERE n.name <> '%abc[%]%abc%' RETURN n, collect(n)"
            ),
            TestCase(
                query = where(TestData::name).`in`("test1", "test2"),
                cypher = "MATCH (n:`TestData`) WHERE n.name IN ['test1', 'test2'] RETURN n, collect(n)"
            ),
            TestCase(
                query = where(TestData::name).notIn("test1", "test2"),
                cypher = "MATCH (n:`TestData`) WHERE NOT (n.name IN ['test1', 'test2']) RETURN n, collect(n)"
            ),
            TestCase(
                query = where(TestData::name).`in`(listOf("test1", "test2")),
                cypher = "MATCH (n:`TestData`) WHERE n.name IN ['test1', 'test2'] RETURN n, collect(n)"
            ),
            TestCase(
                query = where(TestData::name).notIn(listOf("test1", "test2")),
                cypher = "MATCH (n:`TestData`) WHERE NOT (n.name IN ['test1', 'test2']) RETURN n, collect(n)"
            ),
            TestCase(
                query = where(TestData::activate).isTrue(),
                cypher = "MATCH (n:`TestData`) WHERE n.activate = true RETURN n, collect(n)"
            ),
            TestCase(
                query = where(TestData::activate).isFalse(),
                cypher = "MATCH (n:`TestData`) WHERE n.activate = false RETURN n, collect(n)"
            ),
            TestCase(
                query = where(TestData::name).not("test").and(where(TestData::name).`is`("test")),
                cypher = "MATCH (n:`TestData`) WHERE (n.name <> 'test' AND n.name = 'test') RETURN n, collect(n)"
            ),
            TestCase(
                query = where(TestData::name).not("test")
                    .and(where(TestData::name).not("test").and(where(TestData::name).`is`("test"))),
                cypher = "MATCH (n:`TestData`) WHERE (n.name <> 'test' AND n.name <> 'test' AND n.name = 'test') RETURN n, collect(n)"
            ),
            TestCase(
                query = where(TestData::name).not("test")
                    .and(where(TestData::name).not("test"))
                    .and(where(TestData::name).`is`("test")),
                cypher = "MATCH (n:`TestData`) WHERE (n.name <> 'test' AND n.name <> 'test' AND n.name = 'test') RETURN n, collect(n)"
            ),
            TestCase(
                query = where(TestData::name).not("test")
                    .and(
                        listOf(
                            where(TestData::name).not("test"),
                            where(TestData::name).`is`("test")
                        )
                    ),
                cypher = "MATCH (n:`TestData`) WHERE (n.name <> 'test' AND n.name <> 'test' AND n.name = 'test') RETURN n, collect(n)"
            ),
            TestCase(
                query = where(TestData::name).not("test").or(where(TestData::name).`is`("test")),
                cypher = "MATCH (n:`TestData`) WHERE (n.name <> 'test' OR n.name = 'test') RETURN n, collect(n)"
            ),
            TestCase(
                query = where(TestData::name).not("test")
                    .or(where(TestData::name).not("test").or(where(TestData::name).`is`("test"))),
                cypher = "MATCH (n:`TestData`) WHERE (n.name <> 'test' OR n.name <> 'test' OR n.name = 'test') RETURN n, collect(n)"
            ),
            TestCase(
                query = where(TestData::name).not("test")
                    .or(where(TestData::name).not("test"))
                    .or(where(TestData::name).`is`("test")),
                cypher = "MATCH (n:`TestData`) WHERE (n.name <> 'test' OR n.name <> 'test' OR n.name = 'test') RETURN n, collect(n)"
            ),
            TestCase(
                query = where(TestData::name).not("test")
                    .or(
                        listOf(
                            where(TestData::name).not("test"),
                            where(TestData::name).`is`("test")
                        )
                    ),
                cypher = "MATCH (n:`TestData`) WHERE (n.name <> 'test' OR n.name <> 'test' OR n.name = 'test') RETURN n, collect(n)"
            ),
            TestCase(
                query = where(TestData::name).not("test")
                    .or(where(TestData::name).not("test").and(where(TestData::name).`is`("test"))),
                cypher = "MATCH (n:`TestData`) WHERE (n.name <> 'test' OR (n.name <> 'test' AND n.name = 'test')) RETURN n, collect(n)"
            ),
            TestCase(
                query = where(TestData::name).not("test")
                    .and(where(TestData::name).not("test").or(where(TestData::name).`is`("test"))),
                cypher = "MATCH (n:`TestData`) WHERE (n.name <> 'test' AND (n.name <> 'test' OR n.name = 'test')) RETURN n, collect(n)"
            ),
            TestCase(
                query = where(TestData::name).not("test")
                    .or(where(TestData::name).not("test"))
                    .and(where(TestData::name).`is`("test")),
                cypher = "MATCH (n:`TestData`) WHERE ((n.name <> 'test' OR n.name <> 'test') AND n.name = 'test') RETURN n, collect(n)"
            ),
            TestCase(
                query = where(TestData::name).not("test")
                    .and(where(TestData::name).not("test"))
                    .or(where(TestData::name).`is`("test")),
                cypher = "MATCH (n:`TestData`) WHERE ((n.name <> 'test' AND n.name <> 'test') OR n.name = 'test') RETURN n, collect(n)"
            ),
        )

        testCases.forEach {
            if (it.cypher != null) {
                val criteria = parser.parse(it.query)
                assertEquals(it.cypher, criteria.cypher)
            }
        }
    }
}
