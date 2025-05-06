package com.gijun.backend.domain.model.delivery

import com.gijun.backend.domain.enums.delivery.DeliveryStatus
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("delivery")
data class Delivery(
    @Id
    val id: Long? = null,
    
    @Column("order_id")
    val orderId: Long,
    
    @Column("store_id")
    val storeId: Long,
    
    @Column("rider_id")
    val riderId: Long,
    
    @Column("delivery_status")
    val deliveryStatus: DeliveryStatus = DeliveryStatus.WAITING,
    
    @CreatedDate
    @Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column("assigned_at")
    val assignedAt: LocalDateTime? = null,
    
    @Column("pick_up_at")
    val pickUpAt: LocalDateTime? = null,
    
    @Column("delivering_at")
    val deliveringAt: LocalDateTime? = null,
    
    @Column("delivered_at")
    val deliveredAt: LocalDateTime? = null,
    
    @Column("cancelled_at")
    val cancelledAt: LocalDateTime? = null,
    
    @Column("destination_longitude")
    val destinationLongitude: Double,
    
    @Column("destination_latitude")
    val destinationLatitude: Double,
    
    @Column("destination_address")
    val destinationAddress: String,
    
    @Column("estimated_delivery_time")
    val estimatedDeliveryTime: LocalDateTime? = null,
    
    @Column("actual_delivery_time")
    val actualDeliveryTime: LocalDateTime? = null
)
