package konsyst.ru.features.events.models

import kotlinx.serialization.Serializable


@Serializable
data class LinkScenariosRequest(
    val eventId: Int,
    val scenarioIds: List<Int>
)