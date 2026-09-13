package com.maxi.networking.threadpool

import org.junit.Assert.assertThrows
import org.junit.Test
import java.util.concurrent.LinkedBlockingQueue
import kotlin.test.assertEquals
import kotlin.test.assertNotSame

class ThreadPoolConfigTest {

    private fun defaults() = ThreadPoolConfig(
        name = "Test",
        coreSize = 2,
        maxSize = 4,
        keepAliveSeconds = 30L,
        queueFactory = { LinkedBlockingQueue() }
    )

    @Test
    fun `valid config is accepted`() {
        val config = defaults()

        assertEquals("Test", config.name)
        assertEquals(2, config.coreSize)
        assertEquals(4, config.maxSize)
    }

    @Test
    fun `blank name is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            defaults().copy(
                name = "    "
            )
        }
    }

    @Test
    fun `negative coreSize is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            defaults().copy(
                coreSize = -1
            )
        }
    }

    @Test
    fun `zero maxSize is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            defaults().copy(
                maxSize = 0
            )
        }
    }

    @Test
    fun `maxSize smaller than coreSize is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            defaults().copy(
                coreSize = 2,
                maxSize = 1
            )
        }
    }

    @Test
    fun `negative keepAliveSeconds is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            defaults().copy(
                keepAliveSeconds = -1L
            )
        }
    }

    @Test
    fun `out of range capacity is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            defaults().copy(
                priority = Thread.MAX_PRIORITY + 1
            )
        }
    }

    @Test
    fun `queueFactory produces a fresh queue every time`() {
        val config = defaults()
        val q1 = config.queueFactory()
        val q2 = config.queueFactory()
        assertNotSame(q1, q2, "queueFactory must return distinct queue instances")
    }
}