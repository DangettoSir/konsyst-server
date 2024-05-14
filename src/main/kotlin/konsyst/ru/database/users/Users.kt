package konsyst.ru.database.users

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object Users: Table(){
    internal val login = varchar("login", 25)
    private val password = varchar("password", 25)
    private val username = varchar("username", 30)
    internal val id = integer("id")

    fun insert(userDTO: UserDataTransferObject) {
        transaction {
            Users.insert {
                it[login] = userDTO.login
                it[password] = userDTO.password
                it[username] = userDTO.username
                it[id] = userDTO.id ?: 0
            }
        }
    }
    fun fetchUsers(): List<UserDataTransferObject> {
        return try {
            transaction {
                Users.selectAll().map {
                    UserDataTransferObject(
                        login = it[login],
                        password = it[password],
                        username = it[username],
                        id = it[Users.id]
                    )
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    fun fetchUser(login: String): UserDataTransferObject? {
        return try {
            transaction {
                val user = Users.select { Users.login eq login }.singleOrNull()
                user?.let {
                    UserDataTransferObject(
                        login = it[Users.login],
                        password = it[password],
                        username = it[username],
                        id = it[Users.id]
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}