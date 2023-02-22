package com.astrum.data.repository.neo4j

import kotlinx.coroutines.flow.Flow
import org.neo4j.cypherdsl.core.ResultStatement
import org.springframework.data.domain.Sort
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository
import org.springframework.data.neo4j.repository.support.ReactiveCypherdslStatementExecutor
import org.springframework.stereotype.Repository

@Repository
interface GeneralReactiveNeo4jRepository<T : Any, ID : Any> : ReactiveNeo4jRepository<T, ID>,
    ReactiveCypherdslStatementExecutor<T> {
    fun find(
        criteria: ResultStatement? = null,
        limit: Int? = null,
        offset: Long? = null,
        sort: Sort? = null
    ): Flow<T>
}
