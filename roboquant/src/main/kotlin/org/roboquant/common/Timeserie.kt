/*
 * Copyright 2020-2022 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.roboquant.common

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation
import java.time.Instant

/**
 * Observation represents a double [value] at a certain point in [time]. Observations can be compared based on
 * their [time].
 *
 * @property time the time when the value was observed expressed as an [Instant]
 * @property value the value that was observed expressed as a [Double]
 */
class Observation(val time: Instant, val value: Double) : Comparable<Observation> {

    /**
     * Compare to [other] observation based on their [time]
     */
    override fun compareTo(other: Observation): Int = time.compareTo(other.time)
}

/**
 * Timeserie is an ordered list of [Observations][Observation] sorted from oldest to newest. There can be no
 * observations with the same [Observation.time] in a timeserie.
 */
typealias Timeserie = List<Observation>

/**
 * Return the values of the timeserie as a [DoubleArray]
 */
fun Timeserie.toDoubleArray(): DoubleArray = map { it.value }.toDoubleArray()

/**
 * Return the correlation between two timeseries, [a] and [b]. Only observations at the same time will be taken into
 * account and correlation is calculated if there are at least [minObservations] observations.
 */
fun correlation(a: Timeserie, b: Timeserie, minObservations: Int = 3): Double {
    require(a.isNotEmpty() && b.isNotEmpty())
    var offset1 = 0
    var offset2 = 0
    val data1 = mutableListOf<Double>()
    val data2 = mutableListOf<Double>()
    while (true) {
        val obs1 = a[offset1]
        val obs2 = b[offset2]
        when {
            obs1 > obs2 -> offset2++
            obs2 > obs1 -> offset1++
            else -> {
                data1.add(obs1.value)
                data2.add(obs2.value)
                offset1++
                offset2++
            }
        }
        if (offset1 > a.lastIndex || offset2 > b.lastIndex) break
    }

    return if (data1.size >= minObservations) {
        val calc = PearsonsCorrelation()
        calc.correlation(data1.toDoubleArray(), data2.toDoubleArray())
    } else {
        Double.NaN
    }
}

/**
 * Return the correlations for the timeseries, ensuring that at least [minObservations] are available for calculating
 * the correlation. By default, [excludeSame] the correlation for the same asset are excluded since this is always 1.0
 */
fun Map<Asset, Timeserie>.correlation(
    minObservations: Int = 3,
    excludeSame: Boolean = true
): Map<Pair<Asset, Asset>, Double> {
    val result = mutableMapOf<Pair<Asset, Asset>, Double>()
    for ((asset1, timeserie1) in this) {
        for ((asset2, timeserie2) in this) {
            if (excludeSame && asset1 == asset2) continue
            val pair = Pair(asset2, asset1)
            if (pair !in result) {
                val corr = correlation(timeserie1, timeserie2, minObservations)
                result[Pair(asset1, asset2)] = corr
            }
        }
    }
    return result
}

/**
 * Return the [Timeline] of the [Timeserie]
 */
fun Timeserie.timeline(): Timeline = map { it.time }
