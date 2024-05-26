package konsyst.ru.database.scenarios

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

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
