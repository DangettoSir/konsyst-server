package konsyst.ru.database.scenarios

import konsyst.ru.database.events.Events
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger


object Scenarios : Table("scenarios") {
    internal val id = integer("id")
    internal val title = varchar("title", 255)
    internal val description = text("description")
    internal val date = varchar("date", 15)
    internal val location = varchar("location", 255)
    internal val isCompleted = bool("is_completed").default(false)

    fun insert(scenarioDTO: ScenariosDataTransferObject) {
        transaction {
            Scenarios.insert {
                it[id] = scenarioDTO.id ?: 0
                it[title] = scenarioDTO.title
                it[description] = scenarioDTO.description
                it[date] = scenarioDTO.date
                it[location] = scenarioDTO.location
                it[isCompleted] = scenarioDTO.isCompleted
            }
        }
    }

    fun fetchScenariosIds(scenarioIds: List<Int>): List<ScenariosDataTransferObject> {
        return transaction {
            Scenarios.select { Scenarios.id inList scenarioIds }
                .map { rowToScenarioDTO(it) }
        }
    }
    private val logger: Logger = org.slf4j.LoggerFactory.getLogger(this::class.java)
    private fun rowToScenarioDTO(row: ResultRow): ScenariosDataTransferObject {
        return ScenariosDataTransferObject(
            id = row[id],
            title = row[title],
            description = row[description],
            date = row[date],
            location = row[location],
            isCompleted = row[isCompleted]
        )
    }

    fun fetchScenarios(): List<ScenariosDataTransferObject> {
        return try {
            transaction {
                Scenarios.selectAll().map {
                    ScenariosDataTransferObject(
                        id = it[Scenarios.id],
                        title = it[title],
                        description = it[description],
                        date = it[date],
                        location = it[location],
                        isCompleted = it[isCompleted]
                    )
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    fun updateStatus(id: Int, eventId: Int, isCompleted: Boolean): Boolean {
        return transaction {
            logger.info("Updating status for scenario ID: $id, isCompleted: $isCompleted")
            val rowsUpdatedScenarios = Scenarios.update({
                Scenarios.id eq id
            }) {
                it[Scenarios.isCompleted] = isCompleted
            }

            logger.info("Rows updated in Scenarios table: $rowsUpdatedScenarios")

            if (rowsUpdatedScenarios > 0) {
                val event = Events.select { Events.id eq eventId }.firstOrNull()
                if (event != null) {
                    logger.info("Updating event: $eventId")
                    Events.update({ Events.id eq event[Events.id] }) {
                        it[Events.scenariosComplete] = event[Events.scenariosComplete] + 1
                    }
                    logger.info("Event updated: $eventId")
                } else {
                    logger.warn("No event found for event ID: $eventId")
                }
            } else {
                logger.warn("No event found for ID: $eventId")
            }

            logger.info("Returning result: ${rowsUpdatedScenarios > 0}")
            rowsUpdatedScenarios > 0
        }
    }
    fun fetchScenario(ids: Int): ScenariosDataTransferObject? {
        return try {
            transaction {
                Scenarios.select { Scenarios.id eq ids }
                    .map {
                        ScenariosDataTransferObject(
                            id = it[Scenarios.id],
                            title = it[title],
                            description = it[description],
                            date = it[date],
                            location = it[location],
                            isCompleted = it[isCompleted]
                        )
                    }
                    .singleOrNull()
            }
        } catch (e: Exception) {
            null
        }
    }
}
