package com.gijun.backend.domain.model.order

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("order_item_option")
data class OrderItemOption(
    @Id
    val id: Long? = null,
    
    @Column("order_id")
    val orderId: Long,
    
    @Column("order_item_id")
    val orderItemId: Long,
    
    @Column("option_item_id")
    val optionItemId: Long,
    
    @Column("name")
    val name: String,
    
    @Column("price")
    val price: Double,
    
    @CreatedDate
    @Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @LastModifiedDate
    @Column("updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @Column("deleted_at")
    val deletedAt: LocalDateTime? = null
)
