package com.gijun.backend.domain.enums.order

enum class OrderStatus {
    CREATED,
    ACCEPTED,
    REJECTED,
    COOKING,
    READY,
    ASSIGNED,
    PICKED_UP,
    DELIVERING,
    DELIVERED,
    CANCELLED
}