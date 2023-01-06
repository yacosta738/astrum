package com.astrum.authentication.infrastructure.errors

import com.astrum.authentication.infrastructure.util.HeaderUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.env.Environment
import org.springframework.dao.ConcurrencyFailureException
import org.springframework.dao.DataAccessException
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageConversionException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.ServerWebExchange
import org.zalando.problem.*
import org.zalando.problem.spring.webflux.advice.ProblemHandling
import org.zalando.problem.spring.webflux.advice.security.SecurityAdviceTrait
import org.zalando.problem.violations.ConstraintViolationProblem
import reactor.core.publisher.Mono
import java.net.URI

private const val FIELD_ERRORS_KEY = "fieldErrors"
private const val MESSAGE_KEY = "message"
private const val PATH_KEY = "path"
private const val VIOLATIONS_KEY = "violations"

/**
 * Controller advice to translate the server side exceptions to client-friendly json structures.
 * The error response follows RFC7807 - Problem Details for HTTP APIs (https://tools.ietf.org/html/rfc7807).
 */
@ControllerAdvice
class ExceptionTranslator(private val env: Environment) : ProblemHandling, SecurityAdviceTrait {

    @Value("\${spring.application.name:Astrum}")
    private val applicationName: String = "Astrum"

    /**
     * Post-process the Problem payload to add the message key for the front-end if needed.
     */
    override fun process(
        entity: ResponseEntity<Problem>,
        request: ServerWebExchange
    ): Mono<ResponseEntity<Problem>> {
        val problem = entity.body
        if (!(problem is ConstraintViolationProblem || problem is DefaultProblem)) {
            return Mono.just(entity)
        }

        val builder = Problem.builder()
            .withType(if (Problem.DEFAULT_TYPE == problem.type) DEFAULT_TYPE else problem.type)
            .withStatus(problem.status)
            .withTitle(problem.title)
            .with(PATH_KEY, request.request.path.value())

        if (problem is ConstraintViolationProblem) {
            builder
                .with(VIOLATIONS_KEY, problem.violations)
                .with(MESSAGE_KEY, ERR_VALIDATION)
        } else {
            builder
                .withCause((problem as DefaultProblem).cause)
                .withDetail(problem.detail)
                .withInstance(problem.instance)
            problem.parameters.forEach { (key, value) -> builder.with(key, value) }
            if (!problem.parameters.containsKey(MESSAGE_KEY) && problem.status != null) {
                builder.with(MESSAGE_KEY, "error.http." + problem.status!!.statusCode)
            }
        }
        return Mono.just(
            ResponseEntity<Problem>(
                builder.build(),
                entity.headers,
                entity.statusCode
            )
        )
    }

    override fun handleBindingResult(
        ex: WebExchangeBindException,
        request: ServerWebExchange
    ): Mono<ResponseEntity<Problem>> {
        val result = ex.bindingResult
        val fieldErrors = result.fieldErrors.map {
            FieldErrorVM(
                it.objectName.replaceFirst(Regex("DTO$"), ""),
                it.field,
                it.defaultMessage?.ifBlank { it.code }
            )
        }

        val problem = Problem.builder()
            .withType(CONSTRAINT_VIOLATION_TYPE)
            .withTitle("Data binding and validation failure")
            .withStatus(Status.BAD_REQUEST)
            .with(MESSAGE_KEY, ERR_VALIDATION)
            .with(FIELD_ERRORS_KEY, fieldErrors)
            .build()
        return create(ex, problem, request)
    }

    @ExceptionHandler
    fun handleBadRequestAlertException(
        ex: BadRequestAlertException,
        request: ServerWebExchange
    ): Mono<ResponseEntity<Problem>> =
        create(
            ex, request,
            HeaderUtil.createFailureAlert(
                applicationName,
                false,
                ex.entityName,
                ex.errorKey,
                ex.message
            )
        )

    @ExceptionHandler
    fun handleConcurrencyFailure(
        ex: ConcurrencyFailureException,
        request: ServerWebExchange
    ): Mono<ResponseEntity<Problem>> {
        val problem = Problem.builder()
            .withStatus(Status.CONFLICT)
            .with(MESSAGE_KEY, ERR_CONCURRENCY_FAILURE)
            .build()
        return create(ex, problem, request)
    }

    override fun prepare(throwable: Throwable, status: StatusType, type: URI): ProblemBuilder {
        val activeProfiles = env.activeProfiles
        var detail = throwable.message
        if (activeProfiles.contains("prod")) {
            detail = when (throwable) {
                is HttpMessageConversionException -> "Unable to convert http message"
                is DataAccessException -> "Failure during data access"
                else -> {
                    if (containsPackageName(throwable.message)) {
                        "Unexpected runtime exception"
                    } else {
                        throwable.message
                    }
                }
            }
        }
        return Problem.builder()
            .withType(type)
            .withTitle(status.reasonPhrase)
            .withStatus(status)
            .withDetail(detail)
            .withCause(throwable.cause.takeIf { isCausalChainsEnabled }?.let { toProblem(it) })
    }

    private fun containsPackageName(message: String?) = listOf(
        "org.",
        "java.",
        "net.",
        "javax.",
        "com.",
        "io.",
        "de.",
        "com.astrum.talentum"
    ).any { it == message }
}
