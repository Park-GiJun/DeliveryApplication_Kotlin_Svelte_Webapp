package com.gijun.backend.domain.model.store

import com.gijun.backend.domain.enums.ActivatedStatus
import com.gijun.backend.domain.enums.store.StoreCategory
import com.gijun.backend.domain.enums.store.StoreStatus
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("store")
data class Store(
    @Id
    val id: Long? = null,
    
    @Column("name")
    val name: String,
    
    @Column("store_category")
    val storeCategory: StoreCategory,
    
    @Column("business_number")
    val businessNumber: String,
    
    @Column("owner_name")
    val ownerName: String,
    
    @Column("address")
    val address: String,
    
    @Column("detail_address")
    val detailAddress: String,
    
    @Column("latitude")
    val latitude: Double,
    
    @Column("longitude")
    val longitude: Double,
    
    @Column("contact_number")
    val contactNumber: String,
    
    @Column("email")
    val email: String,
    
    @Column("store_status")
    val storeStatus: StoreStatus = StoreStatus.OPEN,
    
    @Column("min_order_amount")
    val minOrderAmount: Double,
    
    @Column("default_delivery_fee")
    val defaultDeliveryFee: Double,
    
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
