package com.astrum.authentication

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpOutputMessage
import java.io.ByteArrayOutputStream
import java.io.OutputStream


class ByteArrayHttpOutputMessage : HttpOutputMessage {
    val out = ByteArrayOutputStream()
    val headers = HttpHeaders()
    override fun getBody(): OutputStream {
        return out
    }

    override fun getHeaders(): HttpHeaders {
        return headers
    }
}
