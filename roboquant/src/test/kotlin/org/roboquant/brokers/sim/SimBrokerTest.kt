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

package org.roboquant.brokers.sim

import org.junit.jupiter.api.Test
import org.roboquant.TestData
import org.roboquant.brokers.FixedExchangeRates
import org.roboquant.common.Config
import org.roboquant.common.Currency.Companion.EUR
import org.roboquant.common.Currency.Companion.USD
import org.roboquant.common.USD
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class SimBrokerTest {

    @Test
    fun basicSimBrokerTest() {
        val broker = SimBroker()
        val event = TestData.event()
        val account = broker.place(emptyList(), event)
        assertTrue(account.openOrders.isEmpty())
        assertTrue(account.closedOrders.isEmpty())
        assertTrue(account.trades.isEmpty())
        assertEquals(USD, account.baseCurrency)

        val broker2 = SimBroker(100_000.00.USD.toWallet())
        assertEquals(USD, broker2.account.baseCurrency)

        val metrics = broker.getMetrics()
        assertTrue(metrics.isEmpty())
    }

    @Test
    fun placeOrders() {
        val er = FixedExchangeRates(USD, EUR to 0.8)
        Config.exchangeRates = er
        val broker = SimBroker()
        val event = TestData.event()
        val orders = listOf(TestData.euMarketOrder(), TestData.usMarketOrder())
        var account = broker.place(orders, event)
        assertEquals(account.closedOrders.size, account.trades.size)
        assertEquals(1, account.openOrders.size)

        account = broker.place(emptyList(), TestData.event2())
        assertEquals(1, account.openOrders.size)
        assertEquals(1, account.assets.size)
        assertEquals(1, account.closedOrders.size)
        assertEquals(1, account.trades.size)

    }

    @Test
    fun liquidateTest() {
        val broker = SimBroker()
        val event = TestData.event()
        var account = broker.place(listOf(TestData.usMarketOrder()), event)
        assertEquals(1, account.positions.size)
        assertEquals(1, account.trades.size)
        assertEquals(1, account.closedOrders.size)

        account = broker.closePositions()
        assertEquals(0, account.openOrders.size)
        assertEquals(0, account.positions.size)
        assertEquals(2, account.trades.size)
        assertEquals(2, account.closedOrders.size)

    }


}
