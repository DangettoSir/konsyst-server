package konsyst.ru.database.events

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object EventUserIds : Table("event_user_ids") {
    private val eventId = integer("event_id").references(Events.id, onDelete = ReferenceOption.CASCADE)
    private val userId = integer("user_id")

    fun insert(eventId: Int, userId: Int) {
        transaction {
            EventUserIds.insert {
                it[EventUserIds.eventId] = eventId
                it[EventUserIds.userId] = userId
            }
        }
    }

    fun fetchUserIds(eventId: Int): List<Int> {
        return transaction {
            EventUserIds.select { EventUserIds.eventId eq eventId }
                .map { it[EventUserIds.userId] }
        }
    }
}
