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


    private fun buildMockInventoryItems(): List<InventoryItem> {
        return mockInventoryItemDetailsByProject.flatMap { (project, itemsByName) ->
            itemsByName.map { (name, price) ->
                val quantity = mockHistoryByProjectAndItem[project]
                    ?.get(name)
                    ?.sumOf { it.quantityMove }
                    ?: 0

                InventoryItem(
                    name = name,
                    quantity = quantity,
                    price = price,
                    project = project
                )
            }
        }

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

        private val mockInventoryItemDetailsByProject = mapOf(
            "it_material" to mapOf(
                "USB-C Cable" to BigDecimal("9.99"),
                "Wireless Mouse" to BigDecimal("24.90"),
                "Laptop Stand" to BigDecimal("39.50"),
                "HDMI Adapter" to BigDecimal("14.75"),
                "Mechanical Keyboard" to BigDecimal("89.00"),
                "Webcam 1080p" to BigDecimal("54.99"),
                "Monitor Arm" to BigDecimal("74.50"),
                "Ethernet Switch" to BigDecimal("44.25"),
                "Desktop Computer" to BigDecimal("899.00"),
                "Office Desk" to BigDecimal("219.99"),
                "VR Headset" to BigDecimal("399.00"),
                "Gaming Chair" to BigDecimal("179.50"),
                "Docking Station" to BigDecimal("129.00"),
                "External SSD 1TB" to BigDecimal("109.00"),
                "Surge Protector" to BigDecimal("19.99"),
                "Projector" to BigDecimal("499.00"),
                "Printer Toner" to BigDecimal("64.40"),
                "Noise Cancelling Headphones" to BigDecimal("249.99"),
                "Smartphone Stand" to BigDecimal("16.25"),
                "Whiteboard" to BigDecimal("85.00")
            ),
            "garage" to mapOf(
                "Brake Pads" to BigDecimal("49.90"),
                "Oil Filter" to BigDecimal("12.50"),
                "Spark Plug Set" to BigDecimal("34.00"),
                "Headlight Bulb" to BigDecimal("18.75"),
                "Car Battery" to BigDecimal("129.00"),
                "Alternator Belt" to BigDecimal("27.40"),
                "Air Intake Filter" to BigDecimal("21.30"),
                "Windshield Wipers" to BigDecimal("22.90")
            )
        )

        private data class MockUserCredential(
            val username: String,
            val password: String,
            val project: String,
            val userType: String = "NORMAL"
        )

        private val mockUserCredentials = listOf(
            MockUserCredential("bobby.johnson", "azerty123", "it_material", userType = "ADMIN"),
            MockUserCredential("bobby.bobson", "minecraft73", "it_material"),
            MockUserCredential("johnny.johnson", "jooooohny", "it_material"),
            MockUserCredential("johnny.bobson", "azerty", "it_material"),
            MockUserCredential("jeffanie.jefferson", "azerty1234567891011121314151617181920", "it_material"),
            MockUserCredential("leroy.jenkin", "wow", "it_material"),

            MockUserCredential("ced.ric", "ced", "garage", userType = "ADMIN"),
            MockUserCredential("jeremy.hammond", "wrench42", "garage"),
            MockUserCredential("richard.may", "torque88", "garage"),
            MockUserCredential("james.clarkson", "piston777", "garage"),
            MockUserCredential("mia.tia", "garageflow", "garage"),
            MockUserCredential("geoff.jefferson", "tg99", "garage"),
        )

        private fun buildMockUsers(): List<UserCredential> {
            return mockUserCredentials.map { mockUser ->
                UserCredential(
                    username = mockUser.username,
                    password = mockUser.password,
                    project = mockUser.project,
                    userType = mockUser.userType
                )
            }
        }

        private fun buildMockInventoryHistory(savedItems: List<InventoryItem>): List<InventoryModification> {
            val savedItemsByName = savedItems.associateBy { it.name }

            return mockHistoryByProjectAndItem.flatMap { (project, historyByItem) ->
                historyByItem.flatMap { (itemName, entries) ->
                    val item = savedItemsByName[itemName]
                    if (item == null) {
                        emptyList()
                    } else {
                        entries.map { entry ->
                            InventoryModification(
                                modifiedAt = entry.modifiedAt,
                                quantityMove = entry.quantityMove,
                                itemName = item.name,
                                userName = entry.userName,
                                project = project
                            )
                        }
                    }
                }
            }
        }

        private val itMaterialMockHistoryByItem = mapOf(
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
            ),
            "Desktop Computer" to listOf(
                MockHistoryEntry(LocalDateTime.of(2017, Month.FEBRUARY, 6, 9, 30), 10, "bobby.johnson"),
                MockHistoryEntry(LocalDateTime.of(2022, Month.AUGUST, 17, 15, 20), 25, "johnny.bobson"),
                MockHistoryEntry(LocalDateTime.of(2025, Month.DECEMBER, 10, 10, 40), -3, "geoff.jefferson")
            ),
            "Office Desk" to listOf(
                MockHistoryEntry(LocalDateTime.of(2018, Month.MAY, 2, 8, 15), 8, "jeffanie.jefferson"),
                MockHistoryEntry(LocalDateTime.of(2023, Month.JANUARY, 19, 14, 55), 12, "leroy.jenkin"),
                MockHistoryEntry(LocalDateTime.of(2026, Month.MARCH, 18, 9, 25), -1, "bobby.bobson")
            ),
            "VR Headset" to listOf(
                MockHistoryEntry(LocalDateTime.of(2019, Month.JULY, 30, 11, 5), 6, "johnny.johnson"),
                MockHistoryEntry(LocalDateTime.of(2024, Month.APRIL, 8, 16, 30), 18, "bobby.bobson"),
                MockHistoryEntry(LocalDateTime.of(2026, Month.FEBRUARY, 22, 13, 45), -2, "johnny.bobson")
            ),
            "Gaming Chair" to listOf(
                MockHistoryEntry(LocalDateTime.of(2017, Month.SEPTEMBER, 11, 10, 0), 9, "geoff.jefferson"),
                MockHistoryEntry(LocalDateTime.of(2021, Month.NOVEMBER, 3, 12, 20), 20, "leroy.jenkin"),
                MockHistoryEntry(LocalDateTime.of(2025, Month.MAY, 15, 17, 10), -2, "johnny.johnson")
            ),
            "Docking Station" to listOf(
                MockHistoryEntry(LocalDateTime.of(2018, Month.JUNE, 7, 9, 40), 13, "bobby.bobson"),
                MockHistoryEntry(LocalDateTime.of(2023, Month.MARCH, 27, 14, 5), 16, "geoff.jefferson"),
                MockHistoryEntry(LocalDateTime.of(2026, Month.JANUARY, 23, 10, 50), -1, "jeffanie.jefferson")
            ),
            "External SSD 1TB" to listOf(
                MockHistoryEntry(LocalDateTime.of(2019, Month.DECEMBER, 1, 11, 10), 22, "johnny.bobson"),
                MockHistoryEntry(LocalDateTime.of(2022, Month.OCTOBER, 13, 13, 35), 14, "bobby.johnson"),
                MockHistoryEntry(LocalDateTime.of(2025, Month.AUGUST, 9, 15, 5), -4, "leroy.jenkin")
            ),
            "Surge Protector" to listOf(
                MockHistoryEntry(LocalDateTime.of(2016, Month.MAY, 18, 8, 25), 30, "johnny.johnson"),
                MockHistoryEntry(LocalDateTime.of(2020, Month.JULY, 21, 10, 45), 25, "jeffanie.jefferson"),
                MockHistoryEntry(LocalDateTime.of(2024, Month.NOVEMBER, 30, 16, 15), -5, "bobby.bobson")
            ),
            "Projector" to listOf(
                MockHistoryEntry(LocalDateTime.of(2018, Month.FEBRUARY, 24, 9, 55), 5, "geoff.jefferson"),
                MockHistoryEntry(LocalDateTime.of(2021, Month.APRIL, 12, 13, 50), 7, "leroy.jenkin"),
                MockHistoryEntry(LocalDateTime.of(2026, Month.MARCH, 1, 11, 30), -1, "johnny.bobson")
            ),
            "Printer Toner" to listOf(
                MockHistoryEntry(LocalDateTime.of(2017, Month.JANUARY, 9, 7, 45), 40, "bobby.johnson"),
                MockHistoryEntry(LocalDateTime.of(2022, Month.DECEMBER, 20, 14, 40), 30, "bobby.bobson"),
                MockHistoryEntry(LocalDateTime.of(2025, Month.OCTOBER, 2, 12, 55), -6, "jeffanie.jefferson")
            ),
            "Noise Cancelling Headphones" to listOf(
                MockHistoryEntry(LocalDateTime.of(2019, Month.MARCH, 16, 10, 20), 11, "johnny.johnson"),
                MockHistoryEntry(LocalDateTime.of(2023, Month.JULY, 5, 15, 15), 9, "johnny.bobson"),
                MockHistoryEntry(LocalDateTime.of(2026, Month.FEBRUARY, 27, 9, 35), -2, "geoff.jefferson")
            ),
            "Smartphone Stand" to listOf(
                MockHistoryEntry(LocalDateTime.of(2016, Month.JUNE, 22, 8, 5), 26, "leroy.jenkin"),
                MockHistoryEntry(LocalDateTime.of(2021, Month.JANUARY, 28, 11, 25), 18, "bobby.johnson"),
                MockHistoryEntry(LocalDateTime.of(2024, Month.AUGUST, 14, 16, 45), -3, "jeffanie.jefferson")
            ),
            "Whiteboard" to listOf(
                MockHistoryEntry(LocalDateTime.of(2018, Month.AUGUST, 31, 9, 10), 7, "johnny.bobson"),
                MockHistoryEntry(LocalDateTime.of(2022, Month.FEBRUARY, 10, 14, 20), 9, "geoff.jefferson"),
                MockHistoryEntry(LocalDateTime.of(2025, Month.DECEMBER, 12, 13, 15), -1, "bobby.bobson")
            )
        )
        private val garageMockHistoryByItem = mapOf(
            "Brake Pads" to listOf(
                MockHistoryEntry(LocalDateTime.of(2022, Month.APRIL, 9, 9, 5), 20, "jeremy.hammond"),
                MockHistoryEntry(LocalDateTime.of(2024, Month.DECEMBER, 11, 15, 10), 15, "richard.may"),
                MockHistoryEntry(LocalDateTime.of(2026, Month.MARCH, 20, 11, 30), -4, "mia.tia"),
                MockHistoryEntry(LocalDateTime.of(2026, Month.APRIL, 4, 9, 45), 12, "ced.ric")
            ),
            "Oil Filter" to listOf(
                MockHistoryEntry(LocalDateTime.of(2021, Month.JANUARY, 17, 8, 25), 60, "james.clarkson"),
                MockHistoryEntry(LocalDateTime.of(2025, Month.SEPTEMBER, 3, 16, 40), -7, "richard.may"),
                MockHistoryEntry(LocalDateTime.of(2026, Month.APRIL, 2, 10, 20), 20, "mia.tia")
            ),
            "Spark Plug Set" to listOf(
                MockHistoryEntry(LocalDateTime.of(2023, Month.JULY, 29, 10, 15), 25, "jeremy.hammond"),
                MockHistoryEntry(LocalDateTime.of(2026, Month.FEBRUARY, 8, 13, 55), -3, "mia.tia"),
                MockHistoryEntry(LocalDateTime.of(2026, Month.APRIL, 3, 13, 10), 9, "geoff.jefferson")
            ),
            "Headlight Bulb" to listOf(
                MockHistoryEntry(LocalDateTime.of(2024, Month.MARCH, 14, 9, 40), 40, "james.clarkson"),
                MockHistoryEntry(LocalDateTime.of(2026, Month.JANUARY, 30, 17, 20), -9, "richard.may"),
                MockHistoryEntry(LocalDateTime.of(2026, Month.APRIL, 5, 8, 35), 16, "ced.ric")
            ),
            "Car Battery" to listOf(
                MockHistoryEntry(LocalDateTime.of(2022, Month.OCTOBER, 6, 11, 35), 18, "jeremy.hammond"),
                MockHistoryEntry(LocalDateTime.of(2025, Month.NOVEMBER, 19, 14, 5), -2, "mia.tia"),
                MockHistoryEntry(LocalDateTime.of(2026, Month.APRIL, 1, 16, 25), 7, "richard.may")
            ),
            "Alternator Belt" to listOf(
                MockHistoryEntry(LocalDateTime.of(2023, Month.MAY, 2, 12, 45), 30, "richard.may"),
                MockHistoryEntry(LocalDateTime.of(2026, Month.MARCH, 15, 8, 50), -5, "james.clarkson"),
                MockHistoryEntry(LocalDateTime.of(2026, Month.APRIL, 2, 14, 40), 11, "jeremy.hammond")
            ),
            "Air Intake Filter" to listOf(
                MockHistoryEntry(LocalDateTime.of(2021, Month.NOVEMBER, 26, 9, 0), 28, "james.clarkson"),
                MockHistoryEntry(LocalDateTime.of(2024, Month.JUNE, 18, 15, 30), 22, "jeremy.hammond"),
                MockHistoryEntry(LocalDateTime.of(2026, Month.APRIL, 1, 10, 5), -6, "mia.tia"),
                MockHistoryEntry(LocalDateTime.of(2026, Month.APRIL, 5, 11, 50), 10, "james.clarkson")
            ),
            "Windshield Wipers" to listOf(
                MockHistoryEntry(LocalDateTime.of(2022, Month.FEBRUARY, 12, 14, 10), 35, "richard.may"),
                MockHistoryEntry(LocalDateTime.of(2025, Month.DECEMBER, 7, 11, 25), -8, "jeremy.hammond"),
                MockHistoryEntry(LocalDateTime.of(2026, Month.APRIL, 3, 15, 5), 14, "mia.tia")
            )
        )


        private val mockHistoryByProjectAndItem = mapOf(
            "it_material" to itMaterialMockHistoryByItem,
            "garage" to garageMockHistoryByItem
        )

        private data class MockHistoryEntry(
            val modifiedAt: LocalDateTime,
            val quantityMove: Int,
            val userName: String
        )
    }
}
