package konsyst.ru.features.web.models

import konsyst.ru.database.steps.StepsDataTransferObject
import kotlinx.serialization.Serializable


@Serializable
data class WebModel(
    val userId: List<Int>,
    val stepsId: List<Int>
)


@Serializable
data class  FetchReportsResponse(
    val userName: List<String>,
    val steps: List<StepsDataTransferObject>
)