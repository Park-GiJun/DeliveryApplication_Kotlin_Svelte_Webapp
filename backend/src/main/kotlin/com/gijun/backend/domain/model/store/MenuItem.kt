package com.gijun.backend.domain.model.store

import com.gijun.backend.domain.enums.ActivatedStatus
import com.gijun.backend.domain.enums.store.ItemStatus
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("menu_item")
data class MenuItem(
    @Id
    val id: Long? = null,
    
    @Column("category_id")
    val categoryId: Long,
    
    @Column("name")
    val name: String,
    
    @Column("description")
    val description: String? = null,
    
    @Column("price")
    val price: Double,
    
    @Column("image_url")
    val imageUrl: String? = null,
    
    @Column("item_status")
    val itemStatus: ItemStatus = ItemStatus.AVAILABLE,
    
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
