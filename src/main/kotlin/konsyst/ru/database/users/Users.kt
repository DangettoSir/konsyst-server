package konsyst.ru.database.users

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object Users: Table(){
    private val login = Users.varchar("login", 25)
    private val password = Users.varchar("password", 25)
    private val username = Users.varchar("username", 30)

    fun insert(userDataTransferObject: UserDataTransferObject) {
        transaction {
            Users.insert {
                it[login] = userDataTransferObject.login
                it[password] = userDataTransferObject.password
                it[username] = userDataTransferObject.username
            }
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
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}