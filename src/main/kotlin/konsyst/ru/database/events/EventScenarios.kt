package konsyst.ru.database.events

import konsyst.ru.database.scenarios.Scenarios
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object EventScenarios : Table("event_scenarios") {
    internal val eventId = integer("event_id").references(Events.id, onDelete = ReferenceOption.CASCADE)
    internal val scenarioId = integer("scenario_id").references(Scenarios.id, onDelete = ReferenceOption.CASCADE)

    fun insert(eventId: Int, scenarioId: Int) {
        transaction {
            EventScenarios.insert {
                it[EventScenarios.eventId] = eventId
                it[EventScenarios.scenarioId] = scenarioId
            }
        }
    }

    fun fetchScenarioIds(eventId: Int): List<Int> {
        return transaction {
            EventScenarios.select { EventScenarios.eventId eq eventId }
                .map { it[EventScenarios.scenarioId] }
        }
    }

    fun fetchScenarioIdsByEventName(searchQuery: Int): List<Int> {
        return transaction {
            EventScenarios
                .select { EventScenarios.eventId.eq(searchQuery) }
                .map { it[EventScenarios.scenarioId] }
                .toList()
        }
    }
    fun updateScenarioIds(eventId: Int, updatedScenarioIds: List<Int>) {
        transaction {
            // Получаем существующие связи между событием и сценариями
            val existingScenarios = EventScenarios
                .select { EventScenarios.eventId eq eventId }
                .map { it[EventScenarios.scenarioId] }
                .toSet()

            // Удаляем все существующие связи для данного события
            EventScenarios.deleteWhere { EventScenarios.eventId eq eventId }

            // Добавляем новые связи
            updatedScenarioIds.forEach { scenarioId ->
                EventScenarios.insert {
                    it[EventScenarios.eventId] = eventId
                    it[EventScenarios.scenarioId] = scenarioId
                }
            }

            // Восстанавливаем связи для других событий
            val otherScenarios = EventScenarios.selectAll().map { it[EventScenarios.scenarioId] to it[EventScenarios.eventId] }.toSet()
            val scenariosToRestore = otherScenarios - existingScenarios.map { it to eventId }.toSet()
            scenariosToRestore.forEach { (scenarioId, otherEventId) ->
                EventScenarios.insert {
                    it[EventScenarios.eventId] = otherEventId
                    it[EventScenarios.scenarioId] = scenarioId
                }
            }
        }
    }
}