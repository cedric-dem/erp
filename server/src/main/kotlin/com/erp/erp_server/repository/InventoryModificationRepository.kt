package com.erp.erp_server

import org.springframework.data.jpa.repository.JpaRepository

interface InventoryModificationRepository : JpaRepository<InventoryModification, Long> {
    fun findAllByProject(project: String): List<InventoryModification>
}