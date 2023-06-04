package com.astrum.data.repository.neo4j

import com.astrum.data.criteria.Criteria
import com.astrum.data.criteria.CriteriaParser
import org.neo4j.cypherdsl.core.*
import org.neo4j.cypherdsl.core.Conditions.noCondition
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass


private const val CONTAINER_NAME = "n"

class Neo4jCriteriaParser<T : Any>(
    clazz: KClass<T>
) : CriteriaParser<Statement> {
    private val log = LoggerFactory.getLogger(javaClass)
    private val properties =
        clazz.java.declaredFields.map { it.name }
            .map { Cypher.property(CONTAINER_NAME, it) }
            .toList()
    private val node: Node = Cypher.node(clazz.simpleName!!).named(CONTAINER_NAME)
    override fun parse(criteria: Criteria): Statement {
        log.debug("Parse {}", criteria)
        val condition = parseCriteriaToCondition(criteria)
        return Cypher.match(node).where(condition).returning(
            node.requiredSymbolicName, Functions.collect(node)
        ).build()
    }

    private fun parseCriteriaToCondition(criteria: Criteria): Condition {
        log.debug("Parse criteria to condition {}", criteria)
        return when (criteria) {
            is Criteria.Empty -> parse()
            is Criteria.And -> parse(criteria)
            is Criteria.Or -> parse(criteria)
            is Criteria.Equals -> parse(criteria)
            is Criteria.NotEquals -> parse(criteria)
            is Criteria.Between -> parse(criteria)
            is Criteria.NotBetween -> parse(criteria)
            is Criteria.LessThan -> parse(criteria)
            is Criteria.LessThanEquals -> parse(criteria)
            is Criteria.GreaterThan -> parse(criteria)
            is Criteria.GreaterThanEquals -> parse(criteria)
            is Criteria.IsNull -> parse(criteria)
            is Criteria.IsNotNull -> parse(criteria)
            is Criteria.Like -> parse(criteria)
            is Criteria.NotLike -> parse(criteria)
            is Criteria.Regexp -> parse(criteria)
            is Criteria.NotRegexp -> parse(criteria)
            is Criteria.In -> parse(criteria)
            is Criteria.NotIn -> parse(criteria)
            is Criteria.IsTrue -> parse(criteria)
            is Criteria.IsFalse -> parse(criteria)
        }
    }

    private fun parse(): Condition {
        log.debug("Parse empty criteria")
        return noCondition()
    }

    private fun parse(criteria: Criteria.And): Condition {
        log.debug("Parse AND criteria {}", criteria)
        val parsed: List<Condition> = criteria.value.map { parseCriteriaToCondition(it) }
        if (parsed.isEmpty()) {
            return parse()
        }
        if (parsed.size == 1) {
            return parsed[0]
        }
        return parsed.reduce { acc, cur -> acc.and(cur) }
    }

    private fun parse(criteria: Criteria.Or): Condition {
        log.debug("Parse OR criteria {}", criteria)
        val parsed: List<Condition> = criteria.value.map { parseCriteriaToCondition(it) }
        if (parsed.isEmpty()) {
            return parse()
        }
        if (parsed.size == 1) {
            return parsed[0]
        }

        return parsed.reduce { acc, cur -> acc.or(cur) }
    }


    private fun parse(criteria: Criteria.Equals): Condition {
        log.debug("Parse EQUALS criteria {}", criteria)
        val key = criteria.key
        val property = properties.find { it.name == key }
        return property?.isEqualTo(Cypher.literalOf<Any>(criteria.value)) ?: noCondition()
    }

    private fun parse(criteria: Criteria.NotEquals): Condition {
        log.debug("Parse NOT EQUALS criteria {}", criteria)
        val key = criteria.key
        val property = properties.find { it.name == key }
        return property?.isNotEqualTo(Cypher.literalOf<Any>(criteria.value)) ?: noCondition()
    }

    private fun parse(criteria: Criteria.Between): Condition {
        log.debug("Parse BETWEEN criteria {}", criteria)
        val a: Comparable<*> = criteria.value.start
        val b: Comparable<*> = criteria.value.endInclusive

        val key = criteria.key
        val property = properties.find { it.name == key }
        return property?.gte(Cypher.literalOf<Comparable<*>>(a))
            ?.and(property.lte(Cypher.literalOf<Comparable<*>>(b))) ?: noCondition()
    }

    private fun parse(criteria: Criteria.NotBetween): Condition {
        log.debug("Parse NOT BETWEEN criteria {}", criteria)
        val a = criteria.value.start
        val b = criteria.value.endInclusive
        val key = criteria.key
        val property = properties.find { it.name == key }
        return property?.lt(Cypher.literalOf<Comparable<*>>(a))
            ?.or(property.gt(Cypher.literalOf<Comparable<*>>(b))) ?: noCondition()
    }

    private fun parse(criteria: Criteria.LessThan): Condition {
        log.debug("Parse LESS THAN criteria {}", criteria)
        val key = criteria.key
        val property = properties.find { it.name == key }
        return property?.lt(Cypher.literalOf<Any>(criteria.value)) ?: noCondition()
    }

    private fun parse(criteria: Criteria.LessThanEquals): Condition {
        log.debug("Parse LESS THAN EQUALS criteria {}", criteria)
        val key = criteria.key
        val property = properties.find { it.name == key }
        return property?.lte(Cypher.literalOf<Any>(criteria.value)) ?: noCondition()
    }

    private fun parse(criteria: Criteria.GreaterThan): Condition {
        log.debug("Parse GREATER THAN criteria {}", criteria)
        val key = criteria.key
        val property = properties.find { it.name == key }
        return property?.gt(Cypher.literalOf<Any>(criteria.value)) ?: noCondition()
    }

    private fun parse(criteria: Criteria.GreaterThanEquals): Condition {
        log.debug("Parse GREATER THAN EQUALS criteria {}", criteria)
        val key = criteria.key
        val property = properties.find { it.name == key }
        return property?.gte(Cypher.literalOf<Any>(criteria.value)) ?: noCondition()
    }

    private fun parse(criteria: Criteria.IsNull): Condition {
        log.debug("Parse IS NULL criteria {}", criteria)
        val key = criteria.key
        val property = properties.find { it.name == key }
        return property?.isNull ?: noCondition()
    }

    private fun parse(criteria: Criteria.IsNotNull): Condition {
        log.debug("Parse IS NOT NULL criteria {}", criteria)
        val key = criteria.key
        val property = properties.find { it.name == key }
        return property?.isNotNull ?: noCondition()
    }

    private fun parse(criteria: Criteria.Like): Condition {
        log.debug("Parse LIKE criteria {}", criteria)
        val key = criteria.key
        val property = properties.find { it.name == key }
        return property?.contains(Cypher.literalOf<Any>(criteria.value)) ?: noCondition()
    }


    private fun parse(criteria: Criteria.NotLike): Condition {
        log.debug("Parse NOT LIKE criteria {}", criteria)
        val key = criteria.key
        val property = properties.find { it.name == key }
        return property?.ne(Cypher.literalOf<Any>(criteria.value)) ?: noCondition()
    }

    private fun parse(criteria: Criteria.Regexp): Condition {
        log.debug("Parse REGEXP criteria {}", criteria)
        val key = criteria.key
        val property = properties.find { it.name == key }
        return property?.matches(Cypher.literalOf<String>(criteria.value.pattern()))
            ?: noCondition()
    }

    private fun parse(criteria: Criteria.NotRegexp): Condition {
        log.debug("Parse NOT REGEXP criteria {}", criteria)
        val key = criteria.key
        val property = properties.find { it.name == key }
        return property?.matches(Cypher.literalOf<String>(criteria.value.pattern()))?.not()
            ?: noCondition()
    }

    private fun parse(criteria: Criteria.In): Condition {
        log.debug("Parse IN criteria {}", criteria)
        val key = criteria.key
        val property = properties.find { it.name == key }
        return property?.`in`(Cypher.literalOf<List<Any>>(criteria.value)) ?: noCondition()
    }

    private fun parse(criteria: Criteria.NotIn): Condition {
        log.debug("Parse NOT IN criteria {}", criteria)
        val key = criteria.key
        val property = properties.find { it.name == key }
        return property?.`in`(Cypher.literalOf<List<Any>>(criteria.value))?.not() ?: noCondition()
    }

    private fun parse(criteria: Criteria.IsTrue): Condition {
        log.debug("Parse IS TRUE criteria {}", criteria)
        val key = criteria.key
        val property = properties.find { it.name == key }
        return property?.isTrue ?: noCondition()
    }

    private fun parse(criteria: Criteria.IsFalse): Condition {
        log.debug("Parse IS FALSE criteria {}", criteria)
        val key = criteria.key
        val property = properties.find { it.name == key }
        return property?.isFalse ?: noCondition()
    }
}
