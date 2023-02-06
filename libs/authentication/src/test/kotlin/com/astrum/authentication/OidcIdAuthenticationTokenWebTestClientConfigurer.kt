package com.astrum.authentication

import com.astrum.authentication.infrastructure.OAuthentication
import com.astrum.authentication.infrastructure.OpenidClaimSet

object OidcIdAuthenticationTokenWebTestClientConfigurer :
    OAuthenticationTestingBuilder<OidcIdAuthenticationTokenWebTestClientConfigurer>(),
    AuthenticationConfigurer<OAuthentication<OpenidClaimSet>> {
    fun oidcId(): OidcIdAuthenticationTokenWebTestClientConfigurer {
        return OidcIdAuthenticationTokenWebTestClientConfigurer
    }

    override fun build(): OAuthentication<OpenidClaimSet> {
        TODO("Not yet implemented")
    }
}
