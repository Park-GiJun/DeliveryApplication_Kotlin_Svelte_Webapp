package com.gijun.backend.domain.model.store

import com.gijun.backend.domain.enums.ActivatedStatus
import com.gijun.backend.domain.enums.store.StoreCategory
import com.gijun.backend.domain.enums.store.StoreStatus
import org.springframework.data.annotation.Id
import java.time.LocalDateTime

data class Store(
    @Id
    val id: Long,
    val name: String,
    val storeCategory: StoreCategory,
    val businessNumber: String,
    val ownerName: String,
    val address: String,
    val detailAddress: String,
    val latitude: Double,
    val longitude: Double,
    val contactNumber: String,
    val email: String,
    val storeStatus: StoreStatus,
    val minOrderAmount: Double,
    val defaultDeliveryFee: Double,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val deletedAt: LocalDateTime?,
    val activatedStatus: ActivatedStatus
)
