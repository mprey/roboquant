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

package org.roboquant.jupyter

import org.junit.jupiter.api.Test
import org.roboquant.feeds.RandomWalkFeed
import kotlin.test.assertTrue

internal class AssetPerformanceChartTest {

    @Test
    fun test() {
        val feed = RandomWalkFeed.lastYears(1)
        val chart = AssetPerformanceChart(feed)
        assertTrue(chart.asHTML().isNotBlank())
    }

}