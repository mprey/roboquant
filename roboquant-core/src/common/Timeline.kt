package org.roboquant.common

import java.time.Instant


/**
 * Timeline is a ordered list of [Instant] instances, sorted from old to new. Currently it is just a typealias for
 * List<Instant>, but this might change in the future.
 */
typealias Timeline = List<Instant>


/**
 * Get index of the time that is closets to the provided time
 * but doesn't exceed it. So it is the most recent time but without
 * looking into the future.
 *
 * If no such time is found return null
 *
 * @param time
 * @return
 */
fun Timeline.latestNotAfter(time: Instant): Int? {
    var idx = binarySearch(time)
    idx = if (idx < 0) -idx - 2 else idx
    return if (idx >= 0) idx else null
}

fun Timeline.earliestNotBefore(time: Instant): Int? {
    var idx = binarySearch(time)
    idx = if (idx < 0) -idx - 1 else idx
    return if (idx < size) idx else null
}

/**
 * Get the timeframe for this timeline. If the timeline is empty, an exception will be thrown.
 */
val Timeline.timeframe
    get() = Timeframe(first(), last())

/**
 * Split the timeline in chunks of [size]
 */
fun Timeline.split(size: Int): List<Timeframe> {
    return chunked(size).map { Timeframe(it.first(), it.last()) }
}
