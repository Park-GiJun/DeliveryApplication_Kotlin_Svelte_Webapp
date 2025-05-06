package com.gijun.backend.application.dto

data class RiderDTO(
    val id: Long,
    val name: String,
    val phone: String,
    val email: String,
    val status: String
)