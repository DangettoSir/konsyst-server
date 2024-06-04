package konsyst.ru.database.events

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger

object Events : Table("events") {
    internal val id = Events.integer("id")
    internal val title = Events.varchar("title", 255)
    internal val date = Events.varchar("date", 10)
    internal val scenariosCount = Events.integer("scenarios_count")
    internal val scenariosComplete = Events.integer("scenarios_complete")
    internal val userId = Events.integer("user_id")
    internal val status = Events.varchar("status", 20)

    fun insert(eventDTO: EventDataTransferObject) {
        transaction {
            Events.insert {
                it[id] = eventDTO.id ?: 0
                it[title] = eventDTO.title
                it[date] = eventDTO.date
                it[scenariosCount] = eventDTO.scenariosCount ?: 0
                it[scenariosComplete] = eventDTO.scenariosComplete ?: 0
                it[userId] = eventDTO.userId ?: 0
                it[status] = eventDTO.status.name
            }
        }
    }
    private val logger: Logger = org.slf4j.LoggerFactory.getLogger(this::class.java)
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
    data class EventName(
        val id: Int,
        val title: String
    )
    fun fetchAllEventTitles(): List<Events.EventName> {
        return Events.selectAll()
            .map { row ->
                Events.EventName(
                    id = row[Events.id]!!,
                    title = row[Events.title]!!
                )
            }
            .toList()
    }
    fun fetchEventTitleByEventId(searchQuery: Int): String? {
        return transaction {
            Events
                .select { Events.id.eq(searchQuery) }
                .mapNotNull { it[Events.title] }
                .singleOrNull()
        }
    }
    fun fetchEventComplete(searchQuery: Int): Boolean? {
        logger.info("fetchComplete for : $searchQuery")
        return transaction {
            Events
                .select { Events.id.eq(searchQuery) }
                .mapNotNull { event ->
                    val scenariosCount = event[Events.scenariosCount] ?: 0
                    logger.info("scenariosCount : $scenariosCount")
                    val scenariosComplete = event[Events.scenariosComplete] ?: 0
                    logger.info("scenariosComplete : $scenariosComplete")

                    when {
                        scenariosCount == 0 -> {
                            logger.info("scenariosCount==0, returning false")
                            false
                        }
                        scenariosComplete == 0 -> {
                            logger.info("scenariosComplete==0, returning false")
                            false
                        }
                        scenariosComplete % scenariosCount == 0 -> {
                            logger.info("scenariosComplete % scenariosCount == 0, returning true")
                            true
                        }
                        else -> {
                            logger.info("else, returning false")
                            false
                        }
                    }
                }
                .singleOrNull()
        }
    }
    fun updateEventStatus(searchQuery: Int): Boolean {
        logger.info("updateEventStatus for : $searchQuery")
        return transaction {
            val event = Events.select { Events.id.eq(searchQuery) }.singleOrNull()
            if (event != null) {
                val updatedRows = Events.update({ Events.id.eq(searchQuery) }) {
                    it[status] = EventStatus.REVIEW.toString()
                }
                updatedRows > 0
            } else {
                false
            }
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
