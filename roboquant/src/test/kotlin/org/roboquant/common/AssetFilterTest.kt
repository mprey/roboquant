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

import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class AssetFilterTest {


    @Test
    fun testFilter() {
        val asset1 = Asset("abc")
        val assets = listOf(asset1, Asset("BCD"), Asset("CDE"))
        val time = Instant.now()
        var a = assets.filter { AssetFilter.all().filter(it, time) }
        assertEquals(3, a.size)

        a = assets.filter { AssetFilter.excludeSymbols("ABC").filter(it, time) }
        assertFalse(asset1 in a)

        a = assets.filter { AssetFilter.includeSymbols("ABC").filter(it, time) }
        assertTrue(asset1 in a)

        a = assets.filter { AssetFilter.includeCurrencies(Currency.USD).filter(it, time) }
        assertTrue(asset1 in a)
    }

}