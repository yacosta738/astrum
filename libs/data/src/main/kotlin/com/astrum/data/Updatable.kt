package com.astrum.data

import com.astrum.data.annotation.GeneratedValue
import java.time.Instant

interface Updatable {
    @GeneratedValue
    var updatedAt: Instant?
}
