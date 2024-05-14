package konsyst.ru.database.steps

import konsyst.ru.database.scenarios.Scenarios
import konsyst.ru.database.scenarios.ScenariosDataTransferObject
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object Steps : Table("steps") {
    private val id = integer("id").autoIncrement()
    private val title = varchar("title", 255)
    private val description = text("description")
    private val scenarioId = integer("scenario_id").references(Scenarios.id, onDelete = ReferenceOption.CASCADE)
    private val action = text("action")

    fun insert(stepDTO: StepsDataTransferObject) {
        transaction {
            Steps.insert {
                it[id] = stepDTO.id ?: 0
                it[title] = stepDTO.title
                it[description] = stepDTO.description
                it[scenarioId] = stepDTO.scenarioId ?: 0
                it[action] = stepDTO.action
            }
        }
    }
}