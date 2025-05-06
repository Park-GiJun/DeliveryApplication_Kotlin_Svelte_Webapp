package com.gijun.backend.domain.model.customer

import com.gijun.backend.domain.enums.ActivatedStatus
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("customer")
data class Customer(
    @Id
    val id: Long? = null,
    
    @Column("name")
    val name: String,
    
    @Column("phone")
    val phone: String,
    
    @Column("email")
    val email: String,
    
    @Column("password")
    val password: String,
    
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
