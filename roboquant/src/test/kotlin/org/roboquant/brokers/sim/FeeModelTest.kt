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
import org.roboquant.common.Size
import kotlin.test.assertEquals

internal class FeeModelTest {

    @Test
    fun testDefaultCostModel() {
        val model = PercentageFeeModel(feePercentage = 0.01)
        val order = TestData.usMarketOrder()
        val fee = model.calculate(Execution(order, Size(5), 100.0))
        assertEquals(5.0, fee)
    }

    @Test
    fun noCostModel() {
        val model = NoFeeModel()
        val order = TestData.usMarketOrder()
        val fee = model.calculate(Execution(order, Size(100), 10.0))
        assertEquals(0.0, fee)
    }

}