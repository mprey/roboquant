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

package org.roboquant.binance

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.roboquant.common.Logging
import org.roboquant.common.Timeframe
import org.roboquant.common.minutes
import org.roboquant.feeds.PriceBar
import org.roboquant.feeds.filter
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class BinanceLiveFeedTestIT {

    private val logger = Logging.getLogger(this::class)

    @Test
    fun testBinanceFeed() {
        System.getProperty("TEST_BINANCE") ?: return

        val feed = BinanceLiveFeed()
        val availableAssets = feed.availableAssets
        assertTrue(availableAssets.isNotEmpty())

        assertTrue(feed.assets.isEmpty())
        feed.subscribePriceBar("BTC/BUSD")
        assertFalse(feed.assets.isEmpty())

        assertThrows<IllegalArgumentException> {
            feed.subscribePriceBar("WRONG_SYMBOL")
        }

        assertThrows<IllegalArgumentException> {
            feed.subscribePriceBar("BTC/BUSD", "WRONG_INTERVAL")
        }

        val timeframe = Timeframe.next(3.minutes)
        val prices = feed.filter<PriceBar>(timeframe = timeframe) {
            logger.info { it }
            true
        }
        feed.close()

        logger.info("found ${prices.size} prices")
        assertTrue(prices.isNotEmpty())
        assertEquals("BTC/BUSD", prices.first().second.asset.symbol)
    }

}

