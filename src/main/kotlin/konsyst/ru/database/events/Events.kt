package konsyst.ru.database.events

import konsyst.ru.database.events.Events.select
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object Events : Table("events") {
    internal val id = Events.integer("id").autoIncrement()
    internal val title = Events.varchar("title", 255)
    internal val date = Events.varchar("date", 10)
    internal val scenariosCount = Events.integer("scenarios_count")
    internal val scenariosComplete = Events.integer("scenarios_complete")
    internal val userId = Events.integer("user_id").nullable()
    internal val status = Events.varchar("status", 20)

    fun insert(eventDTO: EventDataTransferObject) {
        transaction {
            Events.insert {
                it[id] = eventDTO.id ?: 0
                it[title] = eventDTO.title
                it[date] = eventDTO.date
                it[scenariosCount] = eventDTO.scenariosCount ?: 0
                it[scenariosComplete] = eventDTO.scenariosComplete ?: 0
                it[userId] = eventDTO.userId
                it[status] = eventDTO.status.name
            }
        }
    }

    fun fetchEvents(): List<EventDataTransferObject> {
        return try {
            transaction {
                Events.selectAll().map {
                    EventDataTransferObject(
                        id = it[Events.id],
                        title = it[title],
                        date = it[date],
                        scenariosCount = it[scenariosCount],
                        scenariosComplete = it[scenariosComplete],
                        userId = it[userId],
                        status = EventStatus.valueOf(it[status])
                    )
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun fetchEvent(id: Int): EventDataTransferObject? {
        return try {
            transaction {
                Events.select { Events.id eq id }
                    .map {
                        EventDataTransferObject(
                            id = it[Events.id],
                            title = it[title],
                            date = it[date],
                            scenariosCount = it[scenariosCount],
                            scenariosComplete = it[scenariosComplete],
                            userId = it[userId],
                            status = EventStatus.valueOf(it[status])
                        )
                    }
                    .singleOrNull()
            }
        } catch (e: Exception) {
            null
        }
    }
}
