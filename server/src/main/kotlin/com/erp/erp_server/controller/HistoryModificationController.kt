package com.erp.erp_server

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.format.DateTimeFormatter

data class InventoryModificationResponse(
    val modifiedAt: String,
    val quantityMove: Int,
    val itemName: String,
    val userName: String
)

@RestController
@RequestMapping("/api/history-modifications")
class HistoryModificationController(
    private val inventoryModificationRepository: InventoryModificationRepository,
    private val projectAccessService: ProjectAccessService
) {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    @GetMapping
    fun getInventoryModifications(@RequestParam username: String): List<InventoryModificationResponse> {
        val project = projectAccessService.resolveProjectOrThrow(username)

        return inventoryModificationRepository.findAllByProject(project)
            .sortedByDescending { it.modifiedAt }
            .map { modification ->
                InventoryModificationResponse(
                    modifiedAt = modification.modifiedAt.format(dateFormatter),
                    quantityMove = modification.quantityMove,
                    itemName = modification.itemName,
                    userName = modification.userName
                )
            }
    }
}