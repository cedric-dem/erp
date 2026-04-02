package com.erp.erp_server

import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Month
import java.time.LocalDateTime
import kotlin.system.exitProcess

@Component
class DatabaseResetRunner(
    private val inventoryItemRepository: InventoryItemRepository,
    private val inventoryModificationRepository: InventoryModificationRepository,
    private val userCredentialRepository: UserCredentialRepository,
    private val environment: Environment,
    private val applicationContext: ApplicationContext
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        val mode = ResetMode.from(environment.getProperty(MODE_PROPERTY))
        if (mode == ResetMode.NONE) {
            return
        }

        resetDatabase(mode)

        logger.info("Database reset mode '{}' completed. Shutting down application.", mode.name.lowercase())
        val exitCode = SpringApplication.exit(applicationContext, { 0 })

        exitProcess(exitCode)
    }

    @Transactional
    fun resetDatabase(mode: ResetMode) {
        inventoryModificationRepository.deleteAllInBatch()
        inventoryItemRepository.deleteAllInBatch()
        userCredentialRepository.deleteAllInBatch()

        if (mode == ResetMode.MOCK) {
            val mockInventoryItems = buildMockInventoryItems()
            val savedItems = inventoryItemRepository.saveAll(mockInventoryItems)
            inventoryModificationRepository.saveAll(buildMockInventoryHistory(savedItems))
            userCredentialRepository.saveAll(buildMockUsers())
        }

        logger.info(
            "Database reset finished with mode '{}': inventory items={}, history entries={}, users={}",
            mode.name.lowercase(),
            inventoryItemRepository.count(),
            inventoryModificationRepository.count(),
            userCredentialRepository.count()
        )
    }

    enum class ResetMode {
        NONE,
        EMPTY,
        MOCK;

        companion object {
            fun from(rawValue: String?): ResetMode {
                if (rawValue.isNullOrBlank()) {
                    return NONE
                }

                return entries.firstOrNull { it.name.equals(rawValue.trim(), ignoreCase = true) }
                    ?: throw IllegalArgumentException(
                        "Unsupported value '$rawValue' for $MODE_PROPERTY. Supported values: empty, mock"
                    )
            }
        }
    }
    companion object {
        private val logger = LoggerFactory.getLogger(DatabaseResetRunner::class.java)
        const val MODE_PROPERTY = "app.db-reset.mode"

        private val mockInventoryItemDetails = mapOf(
            "USB-C Cable" to BigDecimal("9.99"),
            "Wireless Mouse" to BigDecimal("24.90"),
            "Laptop Stand" to BigDecimal("39.50"),
            "HDMI Adapter" to BigDecimal("14.75"),
            "Mechanical Keyboard" to BigDecimal("89.00"),
            "Webcam 1080p" to BigDecimal("54.99"),
            "Monitor Arm" to BigDecimal("74.50"),
            "Ethernet Switch" to BigDecimal("44.25")
        )

        private val mockUserCredentials = listOf(
            "ced" to "ced",
            "bobby.johnson" to "azerty123",
            "bobby.bobson" to "minecraft73",
            "johnny.johnson" to "jooooohny",
            "johnny.bobson" to "azerty",
            "jeffanie.jefferson" to "azerty1234567891011121314151617181920",
            "geoff.jefferson" to "tg99",
            "leroy.jenkin" to "wow"
        )

        private fun buildMockUsers(): List<UserCredential> {
            return mockUserCredentials.map { (username, password) ->
                UserCredential(username = username, password = password)
            }
        }

        private fun buildMockInventoryItems(): List<InventoryItem> {
            val quantityByItem = mockHistoryByItem.mapValues { (_, entries) ->
                entries.sumOf { it.quantityMove }
            }

            return quantityByItem.mapNotNull { (itemName, quantity) ->
                val price = mockInventoryItemDetails[itemName]
                if (price == null) {
                    null
                } else {
                    InventoryItem(name = itemName, quantity = quantity, price = price)
                }
            }
        }

        private fun buildMockInventoryHistory(savedItems: List<InventoryItem>): List<InventoryModification> {
            val savedItemsByName = savedItems.associateBy { it.name }

            return mockHistoryByItem.flatMap { (itemName, entries) ->
                val item = savedItemsByName[itemName]
                if (item == null) {
                    emptyList()
                } else {
                    entries.map { entry ->
                        InventoryModification(
                            modifiedAt = entry.modifiedAt,
                            quantityMove = entry.quantityMove,
                            itemName = item.name,
                            userName = entry.userName
                        )
                    }
                }
            }
        }

        private val mockHistoryByItem = mapOf(
            "USB-C Cable" to listOf(
                MockHistoryEntry(LocalDateTime.of(2016, Month.MARCH, 3, 9, 15), 12, "johnny.bobson"),
                MockHistoryEntry(LocalDateTime.of(2017, Month.JULY, 22, 14, 30), -2, "bobby.johnson"),
                MockHistoryEntry(LocalDateTime.of(2019, Month.JANUARY, 11, 16, 5), 49, "bobby.bobson"),
                MockHistoryEntry(LocalDateTime.of(2022, Month.MARCH, 11, 16, 5), -1, "johnny.johnson"),
                MockHistoryEntry(LocalDateTime.of(2025, Month.JANUARY, 4, 11, 20), 53, "johnny.bobson")
            ),
            "Wireless Mouse" to listOf(
                MockHistoryEntry(LocalDateTime.of(2016, Month.DECEMBER, 14, 10, 0), 18, "jeffanie.jefferson"),
                MockHistoryEntry(LocalDateTime.of(2018, Month.SEPTEMBER, 8, 12, 45), 52, "geoff.jefferson"),
                MockHistoryEntry(LocalDateTime.of(2023, Month.OCTOBER, 8, 12, 45), -2, "leroy.jenkin"),
                MockHistoryEntry(LocalDateTime.of(2026, Month.MARCH, 9, 9, 10), 8, "leroy.jenkin")
            ),
            "Laptop Stand" to listOf(
                MockHistoryEntry(LocalDateTime.of(2017, Month.JUNE, 10, 8, 35), 21, "johnny.johnson"),
                MockHistoryEntry(LocalDateTime.of(2021, Month.MAY, 29, 15, 10), 51, "johnny.bobson"),
                MockHistoryEntry(LocalDateTime.of(2024, Month.JULY, 16, 13, 5), -1, "bobby.johnson")
            ),
            "HDMI Adapter" to listOf(
                MockHistoryEntry(LocalDateTime.of(2018, Month.NOVEMBER, 5, 13, 40), 30, "geoff.jefferson"),
                MockHistoryEntry(LocalDateTime.of(2020, Month.DECEMBER, 14, 9, 50), 50, "leroy.jenkin"),
                MockHistoryEntry(LocalDateTime.of(2023, Month.JANUARY, 14, 9, 50), -1, "jeffanie.jefferson"),
                MockHistoryEntry(LocalDateTime.of(2026, Month.FEBRUARY, 14, 9, 50), -2, "bobby.bobson")
            ),
            "Mechanical Keyboard" to listOf(
                MockHistoryEntry(LocalDateTime.of(2016, Month.AUGUST, 9, 11, 30), 15, "leroy.jenkin"),
                MockHistoryEntry(LocalDateTime.of(2019, Month.FEBRUARY, 3, 16, 20), 50, "leroy.jenkin"),
                MockHistoryEntry(LocalDateTime.of(2024, Month.SEPTEMBER, 19, 10, 10), -1, "johnny.johnson")
            ),
            "Webcam 1080p" to listOf(
                MockHistoryEntry(LocalDateTime.of(2017, Month.APRIL, 28, 8, 0), 11, "bobby.johnson"),
                MockHistoryEntry(LocalDateTime.of(2022, Month.JUNE, 15, 13, 25), 55, "geoff.jefferson"),
                MockHistoryEntry(LocalDateTime.of(2025, Month.NOVEMBER, 21, 18, 40), -2, "johnny.bobson")
            ),
            "Monitor Arm" to listOf(
                MockHistoryEntry(LocalDateTime.of(2018, Month.JANUARY, 12, 10, 45), 9, "jeffanie.jefferson"),
                MockHistoryEntry(LocalDateTime.of(2020, Month.APRIL, 6, 14, 15), 48, "bobby.bobson"),
                MockHistoryEntry(LocalDateTime.of(2024, Month.DECEMBER, 2, 17, 30), -1, "leroy.jenkin")
            ),
            "Ethernet Switch" to listOf(
                MockHistoryEntry(LocalDateTime.of(2016, Month.OCTOBER, 1, 7, 55), 14, "johnny.johnson"),
                MockHistoryEntry(LocalDateTime.of(2021, Month.SEPTEMBER, 27, 12, 35), 52, "jeffanie.jefferson"),
                MockHistoryEntry(LocalDateTime.of(2026, Month.JANUARY, 7, 9, 5), -2, "geoff.jefferson")
            )
        )

        private data class MockHistoryEntry(
            val modifiedAt: LocalDateTime,
            val quantityMove: Int,
            val userName: String
        )
    }
}