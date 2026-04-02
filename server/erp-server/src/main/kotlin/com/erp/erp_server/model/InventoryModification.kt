package com.erp.erp_server

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "inventory_modifications")
class InventoryModification(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val modifiedAt: LocalDateTime,

    @Column(nullable = false)
    val quantityMove: Int,

    @Column(nullable = false)
    val itemName: String,

    @Column(nullable = false)
    val userName: String
)