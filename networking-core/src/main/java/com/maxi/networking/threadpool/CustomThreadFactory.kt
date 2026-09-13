package com.maxi.networking.threadpool

import java.util.concurrent.Executor
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

/**
 * A [ThreadFactory] that produces threads with a stable, human-readable naming
 * scheme of the form `"{poolName}-Worker-{n}"`, where `n` starts at 1 and
 * increments atomically for each thread the factory creates.
 *
 * Compared to the default factory returned by
 * `java.util.concurrent.Executors.defaultThreadFactory()`, this gives you
 * meaningful thread names in logs, stack traces, and profilers, at the cost
 * of a single small object per pool.
 *
 * ### Thread safety
 *
 * Instances are safe to share across threads. [newThread] is called
 * concurrently by [java.util.concurrent.ThreadPoolExecutor] when the pool
 * grows under load, and the internal counter is an [AtomicInteger] to keep
 * the naming sequence race-free.
 *
 * ### Example
 *
 * ```
 * val factory = NamedThreadFactory(
 *     poolName = "Network",
 *     daemon = false,
 *     priority = Thread.NORM_PRIORITY,
 * )
 * val executor = ThreadPoolExecutor(
 *     0, 8, 60L, TimeUnit.SECONDS, SynchronousQueue(), factory
 * )
 * // Threads created by this pool will be named "Network-Worker-1",
 * // "Network-Worker-2", and so on.
 * ```
 *
 * @property poolName prefix used in every thread name this factory produces.
 * @property daemon whether created threads should be daemon threads. Daemon
 *   threads do not prevent JVM shutdown; non-daemon threads do. Defaults to
 *   `false`, matching the JDK default.
 * @property priority thread priority applied to each created thread. Must be
 *   between [Thread.MIN_PRIORITY] and [Thread.MAX_PRIORITY], inclusive.
 *   Defaults to [Thread.NORM_PRIORITY].
 * @property uncaughtExceptionHandler optional handler invoked when a thread
 *   from this factory terminates due to an uncaught exception. When `null`,
 *   the JVM's current default handler is used.
 **/
public class CustomThreadFactory @JvmOverloads constructor(
    private val poolName: String,
    private val daemon: Boolean = false,
    private val priority: Int = Thread.NORM_PRIORITY,
    private val uncaughtExceptionHandler: Thread.UncaughtExceptionHandler? = null
) : ThreadFactory {

    init {
        require(poolName.isNotBlank()) {
            "poolName must not be blank"
        }
        require(priority in Thread.MIN_PRIORITY..Thread.MAX_PRIORITY) {
            "priority must be in [${Thread.MIN_PRIORITY}, ${Thread.MAX_PRIORITY}], was $priority"
        }
    }

    private val counter = AtomicInteger(1)
    override fun newThread(runnable: Runnable): Thread {
        val threadName = "$poolName-Worker-${counter.getAndIncrement()}"
        return Thread(runnable, threadName).apply {
            isDaemon = this@CustomThreadFactory.daemon
            priority = this@CustomThreadFactory.priority
            this@CustomThreadFactory.uncaughtExceptionHandler?.let {
                setUncaughtExceptionHandler(it)
            }
        }
    }
}