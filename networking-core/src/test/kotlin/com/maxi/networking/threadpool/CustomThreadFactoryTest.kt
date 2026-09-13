package com.maxi.networking.threadpool

import org.junit.Assert.assertThrows
import org.junit.Test
import java.util.Collections
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CustomThreadFactoryTest {

    @Test
    fun `thread are named with configured prefix and incrementing values`() {
        val factory = CustomThreadFactory(poolName = "testPool")

        val t1 = factory.newThread {}
        val t2 = factory.newThread {}
        val t3 = factory.newThread {}

        assertTrue(t1.name.equals("testPool-Worker-1"))
        assertTrue(t2.name.equals("testPool-Worker-2"))
        assertTrue(t3.name.equals("testPool-Worker-3"))
    }

    @Test
    fun `daemon flag is applied to the created threads`() {
        val factory1 = CustomThreadFactory(
            poolName = "testPool1",
            daemon = false
        )
        val factory2 = CustomThreadFactory(
            poolName = "testPool2",
            daemon = true
        )

        assertFalse(factory1.newThread {}.isDaemon)
        assertTrue(factory2.newThread {}.isDaemon)
    }

    @Test
    fun `priority is applied to the created threads`() {
        val factory = CustomThreadFactory(
            poolName = "testPool",
            priority = Thread.MAX_PRIORITY
        )

        assertEquals(factory.newThread {}.priority, Thread.MAX_PRIORITY)
    }

    @Test
    fun `uncaught exception handler is applied when provided`() {
        val handler = Thread.UncaughtExceptionHandler { _, _ ->
            /* no-op */
        }

        val factory = CustomThreadFactory(
            poolName = "testPool",
            uncaughtExceptionHandler = handler
        )

        assertEquals(handler, factory.newThread { }.uncaughtExceptionHandler)
    }

    @Test
    fun `when no uncaught exception handler is provided it falls back to thread group`() {
        val factory = CustomThreadFactory(poolName = "testPool")

        val thread = factory.newThread { }
        assertEquals(thread.threadGroup, thread.uncaughtExceptionHandler)
    }

    @Test
    fun `blank poolName is rejected at construction`() {
        assertThrows(IllegalArgumentException::class.java) {
            CustomThreadFactory(poolName = "")
        }

        assertThrows(IllegalArgumentException::class.java) {
            CustomThreadFactory(poolName = "       ")
        }
    }

    @Test
    fun `out of range priority is rejected at construction`() {
        assertThrows(IllegalArgumentException::class.java) {
            CustomThreadFactory(
                poolName = "testPool",
                priority = Thread.MIN_PRIORITY - 1
            )
        }

        assertThrows(IllegalArgumentException::class.java) {
            CustomThreadFactory(
                poolName = "testPool",
                priority = Thread.MAX_PRIORITY + 1
            )
        }
    }

    @Test
    fun `counter is thread-safe under concurrent newThread calls`() {
        val factory = CustomThreadFactory(poolName = "testPool")
        val threadCount = 500

        val createdNames = Collections.synchronizedSet(mutableSetOf<String>())
        val latch = CountDownLatch(threadCount)
        val pool = Executors.newFixedThreadPool(16)

        repeat(threadCount) {
            pool.execute {
                createdNames.add(factory.newThread { }.name)
                latch.countDown()
            }
        }

        latch.await()
        pool.shutdown()

        assertEquals(threadCount, createdNames.size)
    }

}