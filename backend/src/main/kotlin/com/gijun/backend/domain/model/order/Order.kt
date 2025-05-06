package com.gijun.backend.domain.model.order

import com.gijun.backend.domain.enums.order.OrderStatus
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("orders")
data class Order(
    @Id
    val id: Long? = null,
    
    @Column("customer_id")
    val customerId: Long,
    
    @Column("store_id")
    val storeId: Long,
    
    @Column("address_id")
    val addressId: Long,
    
    @Column("order_number")
    val orderNumber: String,
    
    @Column("order_status")
    val orderStatus: OrderStatus = OrderStatus.CREATED,
    
    @Column("order_time")
    val orderTime: LocalDateTime,
    
    @Column("accepted_time")
    val acceptedTime: LocalDateTime? = null,
    
    @Column("ready_time")
    val readyTime: LocalDateTime? = null,
    
    @Column("delivered_time")
    val deliveredTime: LocalDateTime? = null,
    
    @Column("cancelled_time")
    val cancelledTime: LocalDateTime? = null,
    
    @CreatedDate
    @Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @LastModifiedDate
    @Column("updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @Column("deleted_at")
    val deletedAt: LocalDateTime? = null,
    
    @Column("total_amount")
    val totalAmount: Double,
    
    @Column("discount_amount")
    val discountAmount: Double = 0.0,
    
    @Column("delivery_fee")
    val deliveryFee: Double,
    
    @Column("payed_amount")
    val payedAmount: Double,
    
    @Column("request_store")
    val requestStore: String? = null,
    
    @Column("request_rider")
    val requestRider: String? = null,
    
    @Version
    @Column("version")
    val version: Int = 0
)
