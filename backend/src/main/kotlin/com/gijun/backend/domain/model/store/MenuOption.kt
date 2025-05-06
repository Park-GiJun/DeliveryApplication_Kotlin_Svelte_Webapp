package com.gijun.backend.domain.model.store

import com.gijun.backend.domain.enums.ActivatedStatus
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("menu_option")
data class MenuOption(
    @Id
    val id: Long? = null,
    
    @Column("menu_id")
    val menuId: Long,
    
    @Column("name")
    val name: String,
    
    @Column("is_required")
    val isRequired: Boolean = false,
    
    @Column("display_order")
    val displayOrder: Int,
    
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
