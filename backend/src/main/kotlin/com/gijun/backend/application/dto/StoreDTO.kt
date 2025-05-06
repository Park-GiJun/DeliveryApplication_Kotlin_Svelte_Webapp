package com.gijun.backend.application.dto

data class StoreDTO(
    val id: Long,
    val name: String,
    val category: String,
    val address: String,
    val status: String
)