package com.gijun.backend.domain.model.order

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("order_item")
data class OrderItem(
    @Id
    val id: Long? = null,
    
    @Column("order_id")
    val orderId: Long,
    
    @Column("menu_id")
    val menuId: Long,
    
    @Column("quantity")
    val quantity: Int,
    
    @Column("unit_price")
    val unitPrice: Double,
    
    @Column("total_price")
    val totalPrice: Double,
    
    @CreatedDate
    @Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @LastModifiedDate
    @Column("updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @Column("deleted_at")
    val deletedAt: LocalDateTime? = null
)
