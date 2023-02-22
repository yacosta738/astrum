package com.astrum.data.cache

import com.astrum.coroutine.test.CoroutineTestHelper
import com.astrum.util.username
import net.datafaker.Faker
import org.apache.commons.collections4.map.AbstractReferenceMap
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.security.SecureRandom

class PoolStoreTest : CoroutineTestHelper() {
    private val faker = Faker(SecureRandom())

    @Test
    fun push() = blocking {
        val pool = PoolStore<String>(AbstractReferenceMap.ReferenceStrength.HARD)
        val value = faker.name().username(16)

        assertTrue(pool.push(value))
        assertFalse(pool.push(value))
    }

    @Test
    fun remove() = blocking {
        val pool = PoolStore<String>(AbstractReferenceMap.ReferenceStrength.HARD)
        val value = faker.name().username(16)

        assertTrue(pool.push(value))
        assertTrue(pool.remove(value))
        assertFalse(pool.remove(value))
    }

    @Test
    fun pop() = blocking {
        val pool = PoolStore<String>(AbstractReferenceMap.ReferenceStrength.HARD)
        val value = faker.name().username(16)

        assertNull(pool.pop())
        assertTrue(pool.push(value))
        assertEquals(value, pool.pop())
        assertNull(pool.pop())
    }
}
