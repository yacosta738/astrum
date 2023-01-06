package com.astrum.authentication.infrastructure.util

import mu.KotlinLogging
import org.springframework.http.HttpHeaders
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

private val log = KotlinLogging.logger {}

/**
 * Utility class for HTTP headers creation.
 */
object HeaderUtil {

    /**
     *
     * createAlert.
     *
     *
     *
     * @param applicationName a [java.lang.String] object.
     * @param message a [java.lang.String] object.
     * @param param a [java.lang.String] object.
     * @return a [org.springframework.http.HttpHeaders] object.
     */
    fun createAlert(applicationName: String, message: String?, param: String?): HttpHeaders {
        val headers = HttpHeaders()
        headers.add("X-$applicationName-alert", message)
        try {
            headers.add(
                "X-$applicationName-params",
                URLEncoder.encode(param, StandardCharsets.UTF_8.toString())
            )
        } catch (e: UnsupportedEncodingException) {
            // StandardCharsets are supported by every Java implementation so this exception will never happen
        }
        return headers
    }

    /**
     *
     * createEntityCreationAlert.
     *
     *
     *
     * @param applicationName a [java.lang.String] object.
     * @param enableTranslation a boolean.
     * @param entityName a [java.lang.String] object.
     * @param param a [java.lang.String] object.
     * @return a [org.springframework.http.HttpHeaders] object.
     */
    fun createEntityCreationAlert(
        applicationName: String,
        enableTranslation: Boolean,
        entityName: String,
        param: String
    ): HttpHeaders {
        val message =
            if (enableTranslation) "$applicationName.$entityName.created" else "A new $entityName is created with identifier $param"
        return createAlert(applicationName, message, param)
    }

    /**
     *
     * createEntityUpdateAlert.
     *
     *
     *
     * @param applicationName a [java.lang.String] object.
     * @param enableTranslation a boolean.
     * @param entityName a [java.lang.String] object.
     * @param param a [java.lang.String] object.
     * @return a [org.springframework.http.HttpHeaders] object.
     */
    fun createEntityUpdateAlert(
        applicationName: String,
        enableTranslation: Boolean,
        entityName: String,
        param: String
    ): HttpHeaders {
        val message =
            if (enableTranslation) "$applicationName.$entityName.updated" else "A $entityName is updated with identifier $param"
        return createAlert(applicationName, message, param)
    }

    /**
     *
     * createEntityDeletionAlert.
     *
     *
     *
     * @param applicationName a [java.lang.String] object.
     * @param enableTranslation a boolean.
     * @param entityName a [java.lang.String] object.
     * @param param a [java.lang.String] object.
     * @return a [org.springframework.http.HttpHeaders] object.
     */
    fun createEntityDeletionAlert(
        applicationName: String,
        enableTranslation: Boolean,
        entityName: String,
        param: String
    ): HttpHeaders {
        val message =
            if (enableTranslation) "$applicationName.$entityName.deleted" else "A $entityName is deleted with identifier $param"
        return createAlert(applicationName, message, param)
    }

    /**
     *
     * createFailureAlert.
     *
     *
     *
     * @param applicationName a [java.lang.String] object.
     * @param enableTranslation a boolean.
     * @param entityName a [java.lang.String] object.
     * @param errorKey a [java.lang.String] object.
     * @param defaultMessage a [java.lang.String] object.
     * @return a [org.springframework.http.HttpHeaders] object.
     */
    fun createFailureAlert(
        applicationName: String,
        enableTranslation: Boolean,
        entityName: String?,
        errorKey: String,
        defaultMessage: String?
    ): HttpHeaders {
        log.error("Entity processing failed, {}", defaultMessage)
        val message = if (enableTranslation) "error.$errorKey" else defaultMessage!!
        val headers = HttpHeaders()
        headers.add("X-$applicationName-error", message)
        headers.add("X-$applicationName-params", entityName)
        return headers
    }
}