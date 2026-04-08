package com.erp.erp_server

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.sql.DataSource

data class TableOverview(
    val schema: String,
    val table: String,
    val columns: List<String>,
    val rowCount: Long,
    val sampleRows: List<Map<String, Any?>>
)

data class DatabaseOverviewResponse(
    val tables: List<TableOverview>
)

data class DatabaseResetResponse(
    val mode: String,
    val inventoryItems: Long,
    val historyEntries: Long,
    val users: Long
)

@RestController
@RequestMapping("/api/debug")
class DatabaseOverviewController(
    private val dataSource: DataSource,
    private val jdbcTemplate: JdbcTemplate,
    private val databaseResetRunner: DatabaseResetRunner
) {
    @GetMapping("/database-overview")
    fun databaseOverview(): DatabaseOverviewResponse {
        val metadata = dataSource.connection.use { connection ->
            val metaData = connection.metaData
            val quote = metaData.identifierQuoteString?.trim().orEmpty().ifEmpty { "\"" }

            val tables = mutableListOf<TableOverview>()
            val resultSet = metaData.getTables(null, null, "%", arrayOf("TABLE"))

            resultSet.use { rs ->
                while (rs.next()) {
                    val schema = rs.getString("TABLE_SCHEM") ?: ""
                    val table = rs.getString("TABLE_NAME") ?: continue

                    if (isSystemSchema(schema)) {
                        continue
                    }

                    val columns = readColumns(metaData, schema, table)
                    val fullTableName = buildQualifiedName(schema, table, quote)
                    val rowCount =
                        jdbcTemplate.queryForObject("SELECT COUNT(*) FROM $fullTableName", Long::class.java) ?: 0L
                    val sampleRows = jdbcTemplate.queryForList("SELECT * FROM $fullTableName LIMIT 5")

                    tables += TableOverview(
                        schema = schema,
                        table = table,
                        columns = columns,
                        rowCount = rowCount,
                        sampleRows = sampleRows
                    )
                }
            }

            tables.sortedWith(compareBy<TableOverview> { it.schema }.thenBy { it.table })
        }

        return DatabaseOverviewResponse(tables = metadata)
    }

    @PostMapping("/reset-database")
    fun resetDatabase(@RequestParam mode: String): DatabaseResetResponse {
        val resetMode = DatabaseResetRunner.ResetMode.from(mode)
        require(resetMode != DatabaseResetRunner.ResetMode.NONE) {
            "Unsupported value '$mode' for mode. Supported values: empty, mock"
        }

        databaseResetRunner.resetDatabase(resetMode)

        return DatabaseResetResponse(
            mode = resetMode.name.lowercase(),
            inventoryItems = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM inventory_items", Long::class.java)
                ?: 0L,
            historyEntries = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM inventory_modifications",
                Long::class.java
            )
                ?: 0L,
            users = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_credentials", Long::class.java) ?: 0L
        )
    }

    private fun readColumns(metaData: java.sql.DatabaseMetaData, schema: String, table: String): List<String> {
        val columns = mutableListOf<String>()
        metaData.getColumns(null, schema.ifBlank { null }, table, "%").use { columnsResult ->
            while (columnsResult.next()) {
                columns += columnsResult.getString("COLUMN_NAME")
            }
        }
        return columns
    }

    private fun buildQualifiedName(schema: String, table: String, quote: String): String {
        fun quoted(identifier: String): String = "$quote${identifier.replace(quote, quote + quote)}$quote"
        return if (schema.isBlank()) {
            quoted(table)
        } else {
            "${quoted(schema)}.${quoted(table)}"
        }
    }

    private fun isSystemSchema(schema: String): Boolean {
        val normalized = schema.lowercase()
        return normalized in setOf(
            "information_schema",
            "pg_catalog",
            "pg_toast",
            "sys",
            "performance_schema"
        )
    }
}