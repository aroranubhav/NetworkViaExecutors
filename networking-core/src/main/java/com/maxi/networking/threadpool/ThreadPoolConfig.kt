package com.maxi.networking.threadpool

import java.util.concurrent.BlockingQueue
import java.util.concurrent.RejectedExecutionHandler
import java.util.concurrent.ThreadPoolExecutor

/**
 * An immutable description of a [ThreadPoolExecutor] configuration.
 *
 * Every knob that [ThreadPoolExecutor] exposes is captured here as a
 * property. Instances of this class are cheap value objects, safe to share,
 * and can be turned into a live [java.util.concurrent.ExecutorService] via
 * [ThreadPools.create].
 *
 * ### Why a config type rather than a many-parameter factory function?
 *
 * A [ThreadPoolConfig] can be described, logged, compared, or modified
 * without instantiating any threads. It also composes cleanly with
 * [copy]: grab a preset from [ThreadPools], change one field, and hand
 * the result to [ThreadPools.create].
 *
 * ### On [queueFactory]
 *
 * The work queue is passed as a *factory function*, not as an instance.
 * This ensures that each pool built from this config gets its own fresh,
 * empty queue. If two pools shared the same queue instance, they would
 * both pull from a single task backlog, which is almost never what you
 * want.
 *
 * @property name Human-readable name used as the prefix for worker
 *   threads created by pools built from this config. Must be non-blank.
 * @property coreSize The minimum number of threads to keep alive in the
 *   pool. Must be `>= 0`.
 * @property maxSize The upper bound on total threads in the pool. Must be
 *   `>= coreSize` and `> 0`.
 * @property keepAliveSeconds How long non-core threads (or all threads,
 *   if [allowCoreThreadTimeOut] is true) may sit idle before being
 *   terminated. Must be `>= 0`.
 * @property queueFactory Produces a fresh [BlockingQueue] for each pool
 *   built from this config. Common choices are `SynchronousQueue()` for
 *   elastic pools and `LinkedBlockingQueue(capacity)` for bounded ones.
 * @property daemon Whether worker threads should be daemon threads.
 *   Defaults to `false`.
 * @property priority Priority applied to each worker thread. Must be in
 *   `[Thread.MIN_PRIORITY, Thread.MAX_PRIORITY]`. Defaults to
 *   [Thread.NORM_PRIORITY].
 * @property allowCoreThreadTimeOut If `true`, even core threads will be
 *   terminated after [keepAliveSeconds] of idleness. Useful when you
 *   want the pool to shrink all the way to zero when unused. Defaults
 *   to `false`.
 * @property prestartCoreThreads If `true`, all core threads are started
 *   at pool construction time rather than lazily on first use. Useful
 *   when you want steady-state pools to be warm before the first
 *   request arrives. Defaults to `false`.
 * @property rejectedHandler Called when a task cannot be accepted
 *   because the pool is at [maxSize] and the queue refused it.
 *   Defaults to [ThreadPoolExecutor.AbortPolicy], which throws
 *   [java.util.concurrent.RejectedExecutionException].
 * @property uncaughtExceptionHandler Optional handler invoked when a
 *   worker thread terminates due to an uncaught exception.
 */
public data class ThreadPoolConfig(
    val name: String,
    val coreSize: Int,
    val maxSize: Int,
    val keepAliveSeconds: Long,
    val queueFactory: () -> BlockingQueue<Runnable>,
    val daemon: Boolean = false,
    val priority: Int = Thread.NORM_PRIORITY,
    val allowCoreThreadTimeOut: Boolean = false,
    val prestartCoreThreads: Boolean = false,
    val rejectedHandler: RejectedExecutionHandler = ThreadPoolExecutor.AbortPolicy(),
    val uncaughtExceptionHandler: Thread.UncaughtExceptionHandler? = null
) {
    init {
        require(name.isNotBlank()) {
            "name must not be blank"
        }
        require(coreSize >= 0) {
            "coreSize must be >=0, is $coreSize"
        }
        require(maxSize >= 0) {
            "maxSize must be >=0, is $maxSize"
        }
        require(maxSize >= coreSize) {
            "maxSize ($maxSize) must be >= coreSize ($coreSize)"
        }
        require(keepAliveSeconds >= 0) {
            "keepAliveSeconds must be >= 0, is $keepAliveSeconds"
        }
        require(priority in Thread.MIN_PRIORITY..Thread.MAX_PRIORITY) {
            "priority must be in [${Thread.MIN_PRIORITY}, ${Thread.MAX_PRIORITY}], is $priority"
        }
    }
}