package com.astrum.authentication.annotations

import com.astrum.authentication.OpenidClaimSetBuilder.*


@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class OpenIdAddress(
    val formattedAddress: String = "",
    val streetAddress: String = "",
    val locality: String = "",
    val region: String = "",
    val postalCode: String = "",
    val country: String = ""
) {
    companion object {
        fun of(addressAnnotation: OpenIdAddress): AddressClaim {
            return AddressClaim()
                .country(blankIfEmpty(addressAnnotation.country))
                .formatted(blankIfEmpty(addressAnnotation.formattedAddress))
                .locality(blankIfEmpty(addressAnnotation.locality))
                .postalCode(blankIfEmpty(addressAnnotation.postalCode))
                .region(blankIfEmpty(addressAnnotation.region))
                .streetAddress(blankIfEmpty(addressAnnotation.streetAddress))
        }

        private fun blankIfEmpty(str: String): String = str.ifBlank { "" }
    }
}
