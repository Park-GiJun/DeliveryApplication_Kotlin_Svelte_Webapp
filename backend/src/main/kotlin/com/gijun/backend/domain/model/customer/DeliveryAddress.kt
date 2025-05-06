package com.gijun.backend.domain.model.customer

import com.gijun.backend.domain.enums.ActivatedStatus
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("delivery_address")
data class DeliveryAddress(
    @Id
    val id: Long? = null,
    
    @Column("customer_id")
    val customerId: Long,
    
    @Column("name")
    val name: String,
    
    @Column("address")
    val address: String,
    
    @Column("detail_address")
    val detailAddress: String,
    
    @Column("latitude")
    val latitude: Double,
    
    @Column("longitude")
    val longitude: Double,
    
    @Column("is_default")
    val isDefault: Boolean = false,
    
    @CreatedDate
    @Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @LastModifiedDate
    @Column("updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @Column("deleted_at")
    val deletedAt: LocalDateTime? = null,
    
    @Column("activated_status")
    val activatedStatus: ActivatedStatus = ActivatedStatus.ACTIVE
)
