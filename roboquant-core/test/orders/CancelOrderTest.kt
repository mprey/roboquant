/*
 * Copyright 2021 Neural Layer
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

package org.roboquant.orders

import org.junit.Test
import org.roboquant.TestData
import org.roboquant.brokers.sim.SimOrderState
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith


internal class CancelOrderTest {

    @Test
    fun test() {
        val asset = TestData.usStock()
        val order = MarketOrder(asset, 100.0)
        val state = SimOrderState(order)
        val oc = CancelOrder(state)
        assertEquals(order, oc.order.order)
    }

    @Test
    fun testFailure() {
        val asset = TestData.usStock()
        val order = MarketOrder(asset, 100.0)
        val state = SimOrderState(order, OrderStatus.COMPLETED)
        assertFailsWith<java.lang.IllegalArgumentException> {
            CancelOrder(state)
        }
    }
}