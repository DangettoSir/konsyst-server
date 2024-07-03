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
    fun fetchAllEventScenarios(): List<EventScenario> {
        return EventScenarios.selectAll()
            .map { row ->
                EventScenario(
                    eventId = row[EventScenarios.eventId]!!,
                    scenarioId = row[EventScenarios.scenarioId]!!
                )
            }
            .toList()
    }

    data class EventScenario(
        val eventId: Int,
        val scenarioId: Int
    )
    fun fetchScenarioIds(eventId: Int): List<Int> {
        return transaction {
            EventScenarios.select { EventScenarios.eventId eq eventId }
                .map { it[EventScenarios.scenarioId] }
        }
    }
    fun getEventTitleByScenarioId(scenarioId: Int): String? {
        return transaction {
            val eventId = EventScenarios.select { EventScenarios.scenarioId eq scenarioId }
                .limit(1)
                .map { it[EventScenarios.eventId] }
                .singleOrNull()

            eventId?.let { id ->
                Events.select { Events.id eq id }
                    .map { it[Events.title] }
                    .singleOrNull()
            }
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
            val existingScenarios = EventScenarios
                .select { EventScenarios.eventId eq eventId }
                .map { it[EventScenarios.scenarioId] }
                .toSet()
            EventScenarios.deleteWhere { EventScenarios.eventId eq eventId }

            updatedScenarioIds.forEach { scenarioId ->
                EventScenarios.insert {
                    it[EventScenarios.eventId] = eventId
                    it[EventScenarios.scenarioId] = scenarioId
                }
            }
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
