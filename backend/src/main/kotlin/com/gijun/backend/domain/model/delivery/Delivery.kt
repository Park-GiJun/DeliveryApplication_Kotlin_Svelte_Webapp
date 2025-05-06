package com.gijun.backend.domain.model.delivery

import com.gijun.backend.domain.enums.delivery.DeliveryStatus
import org.springframework.data.annotation.Id
import java.time.LocalDateTime

data class Delivery(
    @Id
    val id: Integer,
    val orderId: Integer,
    val storeId: Integer,
    val riderId: Integer,
    val orderStatus: DeliveryStatus,
    val createdAt: LocalDateTime,
    val assignedAt: LocalDateTime?,
    val pickUpAt: LocalDateTime?,
    val deliveringAt: LocalDateTime?,
    val deliveredAt: LocalDateTime?,
    val cancelledAt: LocalDateTime?,
    val destinationLongitude: Double,
    val destinationLatitude: Double,
    val destinationAddress: String
)
