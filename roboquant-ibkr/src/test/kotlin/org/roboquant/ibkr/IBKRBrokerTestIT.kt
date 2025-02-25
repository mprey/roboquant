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

package org.roboquant.ibkr

import java.time.Instant
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.roboquant.common.Config
import org.roboquant.common.ConfigurationException
import kotlin.test.assertTrue

internal class IBKRBrokerTestIT {


    @Test
    fun wrongConfig() {

        assertThrows<ConfigurationException> {
            IBKRBroker {
                port = 87654
            }
        }

    }


    @Test
    fun test() {
        Config.getProperty("TEST_IBKR") ?: return
        val past = Instant.now()
        val broker = IBKRBroker()
        val account = broker.account
        assertTrue(account.lastUpdate >= past)
        broker.disconnect()
    }

}