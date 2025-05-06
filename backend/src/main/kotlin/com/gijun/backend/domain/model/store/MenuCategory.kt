package com.gijun.backend.domain.model.store

import com.gijun.backend.domain.enums.ActivatedStatus
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("menu_category")
data class MenuCategory(
    @Id
    val id: Long? = null,
    
    @Column("store_id")
    val storeId: Long,
    
    @Column("display_order")
    val displayOrder: Int,
    
    @Column("name")
    val name: String,
    
    @Column("description")
    val description: String? = null,
    
    @Column("activated_status")
    val activatedStatus: ActivatedStatus = ActivatedStatus.ACTIVE,
    
    @CreatedDate
    @Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @LastModifiedDate
    @Column("updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @Column("deleted_at")
    val deletedAt: LocalDateTime? = null
)
