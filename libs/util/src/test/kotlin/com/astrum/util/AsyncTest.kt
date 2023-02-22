package com.astrum.util

import com.astrum.coroutine.test.CoroutineTestHelper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AsyncTest : CoroutineTestHelper() {

    @Test
    fun retry() = blocking {
        val counter = fun(limit: Int): suspend () -> Unit {
            var count = 0
            return suspend {
                count++
                if (count <= limit) {
                    throw RuntimeException()
                }
            }
        }

        assertThrows<RuntimeException> { retry(0, counter(1)) }
        retry(1, counter(1))
        retry(null, counter(2))
    }
}
