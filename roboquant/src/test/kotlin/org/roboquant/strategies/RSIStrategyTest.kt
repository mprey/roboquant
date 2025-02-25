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

package org.roboquant.strategies

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.roboquant.Roboquant
import org.roboquant.TestData
import org.roboquant.loggers.SilentLogger
import kotlin.test.assertEquals

internal class RSIStrategyTest {

    @Test
    fun test() = runBlocking {
        val s = RSIStrategy()
        assertEquals(30.0, s.lowThreshold)
        assertEquals(70.0, s.highThreshold)
        val roboquant = Roboquant(s, logger = SilentLogger())
        roboquant.run(TestData.feed)

    }

}