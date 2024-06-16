package konsyst.ru.features.notifications

import io.ktor.server.application.*
import konsyst.ru.features.notifications.models.Event
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.sql.DriverManager

class NotificationsController(
    private val environment: ApplicationEnvironment? = null
) {
    private val logger = LoggerFactory.getLogger(NotificationsController::class.java)
    private val json = Json { prettyPrint = true }

    fun handleNewEvent(event: Event?) {
        if (event != null) {
            logger.info("Новое событие: $event")
            // Здесь можно дополнительно обработать событие, например, сохранить в базу данных
        } else {
            logger.warn("Получено пустое событие, пропускаем")
        }
    }

    init {
        GlobalScope.launch {
            DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/konsyst",
                "postgres",
                "fmn!-4737jnbs"
            ).use { connection ->
                val statement = connection.createStatement()
                logger.info("Ожидание событий из базы данных...")
                while (true) {
                    val payload = connection.createStatement().use { stmt ->
                        stmt.execute("SELECT payload FROM notification_queue ORDER BY id DESC LIMIT 1;")
                        stmt.resultSet.let {
                            if (it.next()) it.getString("payload") else null
                        }
                    }
                    if (payload != null) {
                        try {
                            val event = json.decodeFromString<Event>(payload)
                            handleNewEvent(event)
                        } catch (e: Exception) {
                            // Если JSON не удается десериализовать, пробуем обработать как строку
                            handleNewEvent(null)
                        }
                        statement.execute("DELETE FROM notification_queue WHERE id = (SELECT id FROM notification_queue ORDER BY id DESC LIMIT 1);")
                    } else {
                        delay(1000) // Небольшая задержка, чтобы не перегружать CPU
                    }
                }
            }
        }
    }
}

