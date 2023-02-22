package com.astrum.data

import com.astrum.data.annotation.GeneratedValue
import org.springframework.data.annotation.Id

abstract class LongIDEntity : Entity<Long?>() {
    @Id
    @GeneratedValue
    override var id: Long? = null
}
