package konsyst.ru.database.users

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object Roles: Table("roles"){
    internal val id = integer("id")
    internal val name = varchar("name", 50)

    fun insert(rolesDTO: RolesDataTransferObject) {
        transaction {
            Roles.insert {
                it[id] = rolesDTO.id
                it[name] = rolesDTO.name
            }
        }
    }
    fun fetchRoles(): List<RolesDataTransferObject> {
        return try {
            transaction {
                Roles.selectAll().map {
                    RolesDataTransferObject(
                        id = it[Roles.id],
                        name = it[Roles.name]
                    )
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    fun fetchRole(id: Int): RolesDataTransferObject? {
        return try {
            transaction {
                val role = Roles.select { Roles.id eq id }.singleOrNull()
                role?.let {
                    RolesDataTransferObject(
                        id = it[Roles.id],
                        name = it[Roles.name]
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}