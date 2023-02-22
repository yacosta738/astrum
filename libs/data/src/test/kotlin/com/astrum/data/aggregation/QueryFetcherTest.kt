package com.astrum.data.aggregation

import com.astrum.data.cache.SelectQuery
import com.astrum.data.criteria.where
import com.astrum.data.dummy.DummyPerson
import com.astrum.data.entity.Person
import com.astrum.data.repository.r2dbc.R2DBCRepositoryBuilder
import com.astrum.data.repository.r2dbc.migration.CreatePerson
import com.astrum.data.test.DataTestHelper
import com.astrum.ulid.ULID
import io.mockk.coVerify
import io.mockk.spyk
import kotlinx.coroutines.flow.toSet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class QueryFetcherTest : DataTestHelper() {
    data class TestCase(
        val queries: List<SelectQuery>,
        val results: List<Set<Person>>
    )

    private val repository =
        spyk(R2DBCRepositoryBuilder<Person, ULID>(entityOperations, Person::class).build())
    private val queryAggregator = spyk(QueryAggregator(repository, Person::class))

    init {
        migrationManager.register(CreatePerson(entityOperations))
    }

    @BeforeEach
    override fun setUp() {
        super.setUp()

        blocking {
            queryAggregator.clear()
        }
    }

    @Test
    fun fetch() = blocking {

        val person1 = DummyPerson.create()
            .let { repository.create(it) }
        val person2 = DummyPerson.create()
            .let { repository.create(it) }

        val testCase = listOf(
            TestCase(
                queries = listOf(
                    SelectQuery(where(Person::name).`is`(person1.name)),
                    SelectQuery(where(Person::name).`is`(person2.name))
                ),
                results = listOf(setOf(person1), setOf(person2))
            ),
            TestCase(
                queries = listOf(
                    SelectQuery(where(Person::name).`is`(person1.name)),
                    SelectQuery(where(Person::name).`in`(person2.name))
                ),
                results = listOf(setOf(person1), setOf(person2))
            ),
            TestCase(
                queries = listOf(
                    SelectQuery(where(Person::name).`in`(person1.name, person2.name)),
                    SelectQuery(where(Person::name).`in`(person2.name))
                ),
                results = listOf(setOf(person1, person2), setOf(person2))
            ),
            TestCase(
                queries = listOf(
                    SelectQuery(where(Person::name).`in`(person1.name, person2.name)),
                    SelectQuery(where(Person::name).like(person2.name))
                ),
                results = listOf(setOf(person1, person2), setOf(person2))
            )
        )

        testCase.forEachIndexed { i, case ->
            queryAggregator.clear()

            val fetchers = case.queries.map { QueryFetcher(it, queryAggregator) }

            fetchers.forEachIndexed { index, queryFetcher ->
                assertEquals(case.results[index], queryFetcher.fetch().toSet())
            }

            coVerify(exactly = i + 1) { repository.findAll(any()) }
        }
    }

    @Test
    fun clear() = blocking {
        val person = DummyPerson.create()
            .let { repository.create(it) }

        val query = SelectQuery(where(Person::name).`is`(person.name))
        val fetcher = QueryFetcher(query, queryAggregator)

        fetcher.clear()

        coVerify(exactly = 1) { queryAggregator.clear(query) }
    }
}
