package konsyst.ru.database.steps

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object Steps : Table("steps") {
    internal val id = integer("id")
    internal val title = varchar("title", 50)
    internal val scenarioId = integer("scenario_id")
    internal val action = varchar("action",50)
    internal val number = integer("number")

    fun insert(stepDTO: StepsDataTransferObject) {
        transaction {
            Steps.insert {
                it[id] = stepDTO.id ?: 0
                it[title] = stepDTO.title
                it[scenarioId] = stepDTO.scenarioId ?: 0
                it[action] = stepDTO.action
                it[number] = stepDTO.number ?:0
            }
        }
    }
    fun fetchStepsByIds(stepIds: List<Int>): List<StepsDataTransferObject> {
        return transaction {
            Steps.select { Steps.id inList stepIds }
                .mapNotNull { row ->
                    StepsDataTransferObject(
                        id = row[Steps.id],
                        title = row[Steps.title],
                        scenarioId = row[Steps.scenarioId],
                        action = row[Steps.action],
                        number = row[Steps.number]
                    )
                }
                .toList()
        }
    }

    fun fetchStepById(stepId: Int): StepsDataTransferObject? {
        return transaction {
            Steps.select { Steps.id eq(stepId) }
                .mapNotNull { row ->
                    StepsDataTransferObject(
                        id = row[Steps.id],
                        title = row[Steps.title],
                        scenarioId = row[Steps.scenarioId],
                        action = row[Steps.action],
                        number = row[Steps.number]
                    )
                }
                .singleOrNull()
        }
    }

    fun fetchSteps(): List<StepsDataTransferObject> {
        return try {
            transaction {
                Steps.selectAll().map {
                    StepsDataTransferObject(
                        id = it[Steps.id],
                        title = it[Steps.title],
                        scenarioId = it[Steps.scenarioId],
                        action = it[Steps.action]
                    )
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}