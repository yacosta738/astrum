package com.astrum.data

import com.astrum.data.annotation.GeneratedValue
import java.time.Instant

interface Creatable {
    @GeneratedValue
    var createdAt: Instant?
}
