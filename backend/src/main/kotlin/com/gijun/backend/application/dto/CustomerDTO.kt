package com.gijun.backend.application.dto

data class CustomerDTO(
    val id: Long,
    val name: String,
    val phone: String,
    val email: String
)