package com.astrum.authentication.annotations

import net.minidev.json.parser.JSONParser
import net.minidev.json.parser.ParseException

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class JsonArrayClaim(val name: String, val value: String) {
    companion object {
        fun parse(claim: JsonArrayClaim): Any {
            return claim.let {
                try {
                    JSONParser(JSONParser.MODE_PERMISSIVE).parse(it.value)
                } catch (e: ParseException) {
                    throw InvalidJsonException(e)
                }
            }
        }
    }
}
