package com.astrum.data.cache

import com.astrum.coroutine.test.CoroutineTestHelper
import com.astrum.util.username
import net.datafaker.Faker
import org.apache.commons.collections4.map.AbstractReferenceMap
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.security.SecureRandom

class PoolTest : CoroutineTestHelper() {
    private val faker = Faker(SecureRandom())

    @Test
    fun pop() = blocking {
        val pool = Pool(AbstractReferenceMap.ReferenceStrength.HARD) { faker.name().username(16) }
        val value1 = faker.name().username(16)
        val value2 = faker.name().username(16)

        pool.push(value1)
        assertEquals(value1, pool.pop())

        pool.push(value2)
        assertEquals(value2, pool.pop())

        val other = pool.pop()
        assertNotEquals(value1, other)
        assertNotEquals(value2, other)
    }

    @Test
    fun push() = blocking {
        val pool = Pool(AbstractReferenceMap.ReferenceStrength.HARD) { faker.name().username(16) }
        val value = faker.name().username(16)

        assertTrue(pool.push(value))
        assertFalse(pool.push(value))
    }
}
