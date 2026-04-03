package com.erp.erp_server

import org.springframework.data.jpa.repository.JpaRepository


interface InventoryItemRepository : JpaRepository<InventoryItem, Long> {
    fun findAllByProject(project: String): List<InventoryItem>
}