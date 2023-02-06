package com.astrum.authentication.infrastructure.domain

import org.springframework.core.convert.converter.Converter
import org.springframework.security.core.GrantedAuthority

interface OAuth2AuthoritiesConverter :
    Converter<Map<String, Any>, Collection<GrantedAuthority>>
