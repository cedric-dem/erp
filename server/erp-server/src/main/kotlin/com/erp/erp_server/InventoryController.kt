package com.erp.erp_server

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.time.LocalDateTime

data class InventoryItemRequest(
    val name: String = "",
    val quantity: Int = 0,
    val price: BigDecimal = BigDecimal.ZERO,
    val userName: String = ""
)

data class InventoryItemResponse(
    val id: Long,
    val name: String,
    val quantity: Int,
    val price: BigDecimal
)

@RestController
@RequestMapping("/api/inventory")
class InventoryController(
    private val inventoryRepository: InventoryItemRepository,
    private val inventoryModificationRepository: InventoryModificationRepository
) {
    @GetMapping
    fun getInventory(): List<InventoryItemResponse> {
        return inventoryRepository.findAll().map { item ->
            InventoryItemResponse(
                id = item.id,
                name = item.name,
                quantity = item.quantity,
                price = item.price
            )
        }
    }

    @PostMapping
    fun addInventoryItem(@RequestBody request: InventoryItemRequest): ResponseEntity<InventoryItemResponse> {
        val name = request.name.trim()

        if (name.isEmpty() || request.quantity < 0 || request.price < BigDecimal.ZERO) {
            return ResponseEntity.badRequest().build()
        }

        val created = inventoryRepository.save(
            InventoryItem(
                name = name,
                quantity = request.quantity,
                price = request.price
            )
        )

        inventoryModificationRepository.save(
            InventoryModification(
                modifiedAt = LocalDateTime.now(),
                quantityMove = created.quantity,
                itemName = created.name,
                userName = request.userName.trim().ifEmpty { "User" }
            )
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(
            InventoryItemResponse(
                id = created.id,
                name = created.name,
                quantity = created.quantity,
                price = created.price
            )
        )
    }
}