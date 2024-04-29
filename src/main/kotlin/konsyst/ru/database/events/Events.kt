package konsyst.ru.database.events

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object Events: Table() {
    private val eventId = Events.varchar("eventId", 50)
    private val eventName = Events.varchar("eventName", 25)
    private val eventTag = Events.varchar("eventTag", 25)
    private val eventTaD = Events.varchar("eventTaD", 25)
    private val scenarioBundle = Events.varchar("scenarioBundle", 50)


    fun insert(eventsDataTransferObject: EventsDataTransferObject) {
        transaction {
            Events.insert {
                it[eventId] = eventsDataTransferObject.eventId
                it[eventName] = eventsDataTransferObject.eventName
                it[eventTag] = eventsDataTransferObject.eventTag
                it[eventTaD] = eventsDataTransferObject.eventTaD
                it[scenarioBundle] = eventsDataTransferObject.scenarioBundle
            }
        }
    }

    fun fetchEvents(): List<EventsDataTransferObject> {
        return try {
            transaction {
                Events.selectAll().toList()
                    .map {
                        EventsDataTransferObject(
                            eventId = it[Events.eventId],
                            eventName = it[Events.eventName],
                            eventTag = it[Events.eventTag],
                            eventTaD = it[Events.eventTaD],
                            scenarioBundle = it[Events.scenarioBundle]
                        )
                    }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
