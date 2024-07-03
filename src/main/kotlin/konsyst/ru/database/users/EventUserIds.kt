package konsyst.ru.database.users

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.transactions.transaction

object EventUserIds: Table() {
    internal val id = integer("id")
    internal val event_id = integer("event_id")
    internal val user_id = integer("user_id")
    internal val created_at = timestamp("created_at")

    fun insert(EventUserIdsDTO: EventUserIdsDTO) {
        transaction {
            EventUserIds.insert {
                it[id] = EventUserIdsDTO.id
                it[event_id] = EventUserIdsDTO.event_id
                it[user_id] = EventUserIdsDTO.user_id
            }
        }
    }
}