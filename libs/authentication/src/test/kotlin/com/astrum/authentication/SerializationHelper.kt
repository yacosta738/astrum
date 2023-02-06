package com.astrum.authentication

import org.springframework.beans.factory.ObjectFactory
import org.springframework.boot.autoconfigure.http.HttpMessageConverters
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import java.io.IOException


/**
 * Helps with HTTP requests body serialization using Spring registered message converters.
 *
 * @author Yuniel Acosta
 */

class SerializationHelper(private val messageConverters: ObjectFactory<HttpMessageConverters>) {

    fun <T> outputMessage(payload: T, mediaType: MediaType): ByteArrayHttpOutputMessage {
        if (payload == null) {
            throw IllegalArgumentException("Payload cannot be null")
        }

        val payloadClass = payload.javaClass
        val relevantConverters = messageConverters
            .getObject()
            .converters
            .filter { it.canWrite(payloadClass, mediaType) }
            .map { it as HttpMessageConverter<T> }

        val converted = ByteArrayHttpOutputMessage()
        var isConverted = false
        for (converter in relevantConverters) {
            try {
                converted.headers.contentType = mediaType
                converter.write(payload, mediaType, converted)
                isConverted = true
                break
            } catch (e: IOException) {
                // swallow exception so that next converter is tried
            }
        }

        if (!isConverted) {
            throw ConversionFailedException("Could not convert $payloadClass to $mediaType")
        }

        return converted
    }

    fun <T> asString(payload: T, mediaType: MediaType): String {
        return if (payload == null) throw IllegalArgumentException("Payload cannot be null")
        else outputMessage(payload, mediaType).out.toString()
    }

    fun <T> asJsonString(payload: T): String {
        return asString(payload, MediaType.APPLICATION_JSON)
    }

    fun <T> asXmlString(payload: T): String {
        return asString(payload, MediaType.APPLICATION_XML)
    }
}
