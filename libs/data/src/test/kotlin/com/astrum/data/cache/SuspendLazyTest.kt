package com.astrum.data.cache

import com.astrum.coroutine.test.CoroutineTestHelper
import com.astrum.util.username
import net.datafaker.Faker
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.security.SecureRandom

class SuspendLazyTest : CoroutineTestHelper() {
    private val faker = Faker(SecureRandom())

    @Test
    fun get() = blocking {
        val suspendLazy = SuspendLazy { faker.name().username(16) }
        val value = suspendLazy.get()
        assertEquals(value, suspendLazy.get())
    }

    @Test
    fun pop() = blocking {
        val suspendLazy = SuspendLazy { faker.name().username(16) }
        assertNull(suspendLazy.pop())
        val value = suspendLazy.get()
        assertEquals(value, suspendLazy.pop())
        assertNull(suspendLazy.pop())
    }

    @Test
    fun clear() = blocking {
        val suspendLazy = SuspendLazy { faker.name().username(16) }
        suspendLazy.get()
        suspendLazy.clear()
        assertNull(suspendLazy.pop())
    }
}
