package konsyst.ru.database.scenarios

import konsyst.ru.database.steps.Steps
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object ScenarioSteps : Table("scenario_steps") {
    internal val scenarioId = integer("scenario_id").references(Scenarios.id, onDelete = ReferenceOption.CASCADE)
    internal val stepId = integer("step_id").references(Steps.id, onDelete = ReferenceOption.CASCADE)

    fun insert(scenarioId: Int, stepId: Int) {
        transaction {
            ScenarioSteps.insert {
                it[ScenarioSteps.scenarioId] = scenarioId
                it[ScenarioSteps.stepId] = stepId
            }
        }
    }

    fun fetchStepIds(scenarioId: Int): List<Int> {
        return transaction {
            ScenarioSteps.select { ScenarioSteps.scenarioId eq scenarioId }
                .map { it[ScenarioSteps.stepId] }
        }
    }

    fun fetchStepIdsByScenarioName(searchQuery: Int): List<Int> {
        return transaction {
            ScenarioSteps
                .select { ScenarioSteps.scenarioId.eq(searchQuery) }
                .map { it[ScenarioSteps.stepId] }
                .toList()
        }
    }
    fun updateStepIds(scenarioId: Int, updatedStepIds: List<Int>) {
        transaction {
            val existingScenarios = ScenarioSteps
                .select { ScenarioSteps.scenarioId eq scenarioId }
                .map { it[ScenarioSteps.scenarioId] }
                .toSet()

            ScenarioSteps.deleteWhere { ScenarioSteps.scenarioId eq scenarioId }

            updatedStepIds.forEach { stepId ->
                ScenarioSteps.insert {
                    it[ScenarioSteps.scenarioId] = scenarioId
                    it[ScenarioSteps.stepId] = stepId
                }
            }


            val otherSteps = ScenarioSteps.selectAll().map { it[ScenarioSteps.stepId] to it[ScenarioSteps.scenarioId] }.toSet()
            val scenariosToRestore = otherSteps - existingScenarios.map { it to scenarioId }.toSet()
            scenariosToRestore.forEach { (stepId, otherScenarioId) ->
                ScenarioSteps.insert {
                    it[ScenarioSteps.scenarioId] = otherScenarioId
                    it[ScenarioSteps.stepId] = stepId
                }
            }
        }
    }
}