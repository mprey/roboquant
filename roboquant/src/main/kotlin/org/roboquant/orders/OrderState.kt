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

package org.roboquant.orders

import org.roboquant.common.Asset
import org.roboquant.orders.OrderStatus.*
import java.time.Instant
import org.roboquant.brokers.Account

/**
 * Order State keeps track of the execution state of an order. After an order is placed at a broker, the OrderState
 * informs you what is happening with that order. The most common way to access this information is through
 * [Account.openOrders] and [Account.closedOrders]
 *
 * This is an open class and can be extended with more advanced implementations.
 *
 * @property order The underlying order
 * @property status The latest status
 * @property openedAt When was the order execution first opened
 * @property closedAt When was the order execution closed
 * @constructor Create new Order state
 */
open class OrderState(
    val order: Order,
    val status: OrderStatus = INITIAL,
    val openedAt: Instant = Instant.MIN,
    val closedAt: Instant = Instant.MAX
) {

    /**
     * Returns true the order status is open, false otherwise
     */
    val open: Boolean
        get() = status.open

    /**
     * Returns true the order status is closed, false otherwise
     */
    val closed: Boolean
        get() = status.closed

    /**
     * Returns the underlying asset
     */
    val asset: Asset
        get() = order.asset

    /**
     * Returns the id od the order
     */
    val id: Int
        get() = order.id

    /**
     * Update the [time] and [newStatus] of and return the new order state.
     */
    fun copy(time: Instant, newStatus: OrderStatus = ACCEPTED): OrderState {
        return if (newStatus === ACCEPTED && status === INITIAL) {
            OrderState(order, newStatus, time)
        } else if (newStatus.closed && status.open) {
            val openTime = if (openedAt === Instant.MIN) time else openedAt
            OrderState(order, newStatus, openTime, time)
        } else {
            this
        }
    }

}

/**
 * The status an order can be in. The flow is straight forward and is adhered to by all broker implementations, even
 * brokers that have more order statuses.
 *
 *  - [INITIAL] -> [ACCEPTED] -> [COMPLETED] | [CANCELLED] | [EXPIRED]
 *  - [INITIAL] -> [REJECTED]
 *
 *  At any given time an [OrderState] is either [open] or [closed] state. Once an order reaches a [closed] state,
 *  it cannot be opened again and will not be further processed.
 */
enum class OrderStatus {

    /**
     * State of an order that has just been created. It will remain in this state until it is either
     * [REJECTED] or [ACCEPTED] by the broker.
     */
    INITIAL,

    /**
     * The order has been received, validated and accepted by the broker. The order remains in this state until it
     * goes to one of the end-states.
     */
    ACCEPTED,

    /**
     * The order has been successfully completed. This is an end-state.
     */
    COMPLETED,

    /**
     * The order was cancelled, normally by a cancellation order. This is an end-state.
     */
    CANCELLED,

    /**
     *  The order has expired, normally triggered by a time-in-force policy. This is an end-state.
     */
    EXPIRED,

    /**
     *  The order has been rejected. This is an end-state and could occur when:
     *  - The order is not valid, for example you try to short an asset while that is not allowed
     *  - You don't have enough buyingPower to place a new order
     *  - The order type is not supported by the broker
     *  - The provided asset is not recognised or cannot be traded on your account
     */
    REJECTED;

    /**
     * Returns true if the order has been aborted. That implies it is in one of the following three "error" end-states:
     * [CANCELLED], [EXPIRED], [REJECTED]
     */
    val aborted: Boolean
        get() = this === CANCELLED || this === EXPIRED || this === REJECTED

    /**
     * Returns true if the order closed. This means it has reached an end-state that doesn't allow for any more trading.
     * This implies it is in one of these four possible end-states: [COMPLETED], [CANCELLED], [EXPIRED] or [REJECTED].
     */
    val closed: Boolean
        get() = this === COMPLETED || this === CANCELLED || this === EXPIRED || this === REJECTED

    /**
     * Returns true if the order in an open state, so [INITIAL] or [ACCEPTED]
     */
    val open: Boolean
        get() = this === INITIAL || this === ACCEPTED

}
