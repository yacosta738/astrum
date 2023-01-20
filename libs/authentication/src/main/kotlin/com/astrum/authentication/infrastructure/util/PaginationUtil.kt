package com.astrum.authentication.infrastructure.util

import org.springframework.data.domain.Page
import org.springframework.http.HttpHeaders
import org.springframework.web.util.UriComponentsBuilder

import java.text.MessageFormat


object PaginationUtil {
    private const val HEADER_X_TOTAL_COUNT = "X-Total-Count"
    private const val HEADER_LINK_FORMAT = "<{0}>; rel=\"{1}\""

    /**
     * Generate pagination headers for a Spring Data [org.springframework.data.domain.Page] object.
     *
     * @param uriBuilder The URI builder.
     * @param page The page.
     * @return http header.
     */
    fun <T> generatePaginationHttpHeaders(
        uriBuilder: UriComponentsBuilder,
        page: Page<T>
    ): HttpHeaders {
        val headers = HttpHeaders()
        headers.add(HEADER_X_TOTAL_COUNT, page.totalElements.toString())
        val pageNumber: Int = page.number
        val pageSize: Int = page.size
        val link = StringBuilder()
        if (pageNumber < page.totalPages - 1) {
            link.append(prepareLink(uriBuilder, pageNumber + 1, pageSize, "next"))
                .append(",")
        }
        if (pageNumber > 0) {
            link.append(prepareLink(uriBuilder, pageNumber - 1, pageSize, "prev"))
                .append(",")
        }
        link.append(prepareLink(uriBuilder, page.totalPages - 1, pageSize, "last"))
            .append(",")
            .append(prepareLink(uriBuilder, 0, pageSize, "first"))
        headers.add(HttpHeaders.LINK, link.toString())
        return headers
    }

    private fun prepareLink(
        uriBuilder: UriComponentsBuilder,
        pageNumber: Int,
        pageSize: Int,
        relType: String
    ): String {
        return MessageFormat.format(
            HEADER_LINK_FORMAT,
            preparePageUri(uriBuilder, pageNumber, pageSize),
            relType
        )
    }

    private fun preparePageUri(
        uriBuilder: UriComponentsBuilder,
        pageNumber: Int,
        pageSize: Int
    ): String {
        return uriBuilder.replaceQueryParam("page", pageNumber.toString())
            .replaceQueryParam("size", pageSize.toString())
            .toUriString()
            .replace(",", "%2C")
            .replace(";", "%3B")
    }
}
