package org.roboquant.orders

import org.roboquant.common.Asset
import org.roboquant.orders.OrderStatus.*
import java.time.Instant

interface OrderState {
    val order: Order
    val status: OrderStatus
    val openedAt: Instant
    val closedAt: Instant
    val open: Boolean
    val closed: Boolean
    val asset: Asset
    val id: Int
}


/**
 * Various state that an order can be in. The  flow is straight forward:
 *
 *  - [INITIAL] -> [ACCEPTED] -> [COMPLETED] | [CANCELLED] | [EXPIRED]
 *  - [INITIAL] -> [REJECTED]
 *
 *  At any given time an order is either [open] or [closed]. Once an order reaches a [closed] state, it cannot be opened
 *  again.
 */
enum class OrderStatus {

    /**
     * State of an order that has just been created. It will remain in this state until it is either
     * rejected or accepted.
     */
    INITIAL,

    /**
     * The order has been received, validated and accepted.
     */
    ACCEPTED,

    /**
     * The order has been successfully completed. This is an end state
     */
    COMPLETED,

    /**
     * The order was cancelled, normally by a cancellation order. This is an end state
     */
    CANCELLED,

    /**
     *  The order has expired, normally by a time-in-force policy. This is an end state
     */
    EXPIRED,

    /**
     *  The order has been rejected. This is an end state
     */
    REJECTED;

    /**
     * Has the order been aborted. That implies it is in one of the following three "error" end states:
     * [CANCELLED], [EXPIRED], [REJECTED]
     */
    val aborted: Boolean
        get() = this === CANCELLED || this === EXPIRED || this === REJECTED

    /**
     * Is the order closed. This means it has reached an end-state that doesn't allow for any more trading. This implies
     * it is in one of these four possible end-states: [COMPLETED], [CANCELLED], [EXPIRED] or [REJECTED].
     */
    val closed: Boolean
        get() = this === COMPLETED || this === CANCELLED || this === EXPIRED || this === REJECTED


    /**
     * Is the order in an open state, so [INITIAL] or [ACCEPTED]
     */
    val open: Boolean
        get() = this === INITIAL || this === ACCEPTED


}
