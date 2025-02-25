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

package org.roboquant.policies

import org.roboquant.brokers.Account
import org.roboquant.brokers.Position
import org.roboquant.brokers.contains
import org.roboquant.brokers.getPosition
import org.roboquant.common.*
import org.roboquant.feeds.Event
import org.roboquant.orders.MarketOrder
import org.roboquant.orders.Order
import org.roboquant.orders.contains
import org.roboquant.strategies.Signal
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * This is the default policy that will be used if no other policy is specified. There are several properties that
 * can be specified during construction that changes its behavior.
 *
 * Also, many of its methods can be overwritten in a subclass to provide even more flexibility. For example, this
 * policy will create Market Orders by default, but this can be changed by overwriting the [createOrder] method in
 * a subclass.
 *
 * @property orderPercentage The percentage of the equity value to allocate to a single order, default is 1% (0.01)
 * @property shorting Can the policy create orders that possibly lead to short positions, default is false
 * @property priceType The type of price to use, default is "DEFAULT"
 * @property fractions For fractional trading, the amount of fractions (decimals) to allow for. Default is 0
 * @property oneOrderOnly Only allow one order to be open for a given asset at a time, default is true
 * @property safetyMargin the percentage of the equity value that you don't want to allocate to orders. This way you
 * are more likely stay away from bounced orders or margin calls. Default is same value as [orderPercentage]
 * @constructor Create new Default Policy
 */
open class FlexPolicy(
    private val orderPercentage: Double = 0.01,
    private val shorting: Boolean = false,
    private val priceType: String = "DEFAULT",
    private val fractions: Int = 0,
    private val oneOrderOnly: Boolean = true,
    private val safetyMargin: Double = orderPercentage
) : BasePolicy() {

    private val logger = Logging.getLogger(FlexPolicy::class)

    /**
     * Would the [signal] generate a reduced position size based on the current [position]. Reduced positions signals
     * have some unique properties:
     *
     * 1. They are allowed independent of buying power available. So you can always close a position.
     * 2. Only signals that are compatible with an exit strategy are allowed
     */
    private fun reducedPositionSignal(position: Position, signal: Signal): Boolean {
        with(position) {
            if (open && signal.exit) {
                if (long && signal.rating.isNegative) return true
                if (short && signal.rating.isPositive) return true
            }
        }
        return false
    }


    /**
     * Return the size that can be bought/sold with the provided [amount] and [price] of the asset. This implementation
     * also takes into consideration the configured [fractions].
     *
     * This method will only be invoked if the signal is not reducing a position.
     */
    open fun calcSize(amount: Double, signal: Signal, price: Double): Size {
        val singleContractPrice = signal.asset.value(Size.ONE, price).value
        val size = BigDecimal(amount / singleContractPrice).setScale(fractions, RoundingMode.DOWN)
        return Size(size) * signal.rating.direction
    }

    /**
     * Create a new order based on the [signal], [size] and current [price]. Overwrite this method if you want to
     * create other orders types than the default [MarketOrder]. This method should return null to indicate no order
     * should be created at all.
     */
    open fun createOrder(signal: Signal, size: Size, price: Double): Order? {
        return MarketOrder(signal.asset, size)
    }


    /**
     * @see Policy.act
     */
    @Suppress("ComplexMethod")
    override fun act(signals: List<Signal>, account: Account, event: Event): List<Order> {
        if (signals.isEmpty()) return emptyList()

        val orders = mutableListOf<Order>()
        val equityAmount = account.equityAmount
        val amountPerOrder = equityAmount * orderPercentage
        val safetyAmount =  equityAmount * safetyMargin
        var buyingPower = account.buyingPower - safetyAmount.value

        @Suppress("LoopWithTooManyJumpStatements")
        for (signal in signals) {
            val asset = signal.asset

            if (oneOrderOnly && (account.openOrders.contains(asset)|| orders.contains(asset))) continue

            val price = event.getPrice(asset, priceType)
            logger.debug { "signal=${signal} buyingPower=$buyingPower amount=$amountPerOrder price=$price" }

            // Don't create an order if we don't know the current price
            if (price !== null) {

                val position = account.positions.getPosition(asset)
                if (reducedPositionSignal(position, signal)) {
                    val order = createOrder(signal, -position.size, price) // close position
                    orders.addNotNull(order)
                } else {
                    if (position.open) continue // we don't increase position sizing
                    if (!signal.entry) continue // signal doesn't allow to open new positions
                    if (amountPerOrder > buyingPower) continue

                    val assetAmount = amountPerOrder.convert(asset.currency, event.time).value
                    val size = calcSize(assetAmount, signal, price)
                    if (size.iszero) continue
                    if (size < 0 && !shorting) continue

                    val order = createOrder(signal, size, price)
                    if (order != null) {
                        val assetExposure = asset.value(size, price).absoluteValue
                        val exposure = assetExposure.convert(buyingPower.currency, event.time).value
                        orders.add(order)
                        buyingPower -= exposure // reduce buying power with exposed amount
                        logger.debug { "signal=${signal} amount=$amountPerOrder exposure=$exposure order=$order" }
                    }
                }

            }
        }
        record("policy.signals", signals.size)
        record("policy.orders", orders.size)
        return orders
    }
}
