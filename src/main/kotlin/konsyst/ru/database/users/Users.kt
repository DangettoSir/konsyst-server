package konsyst.ru.database.users

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object Users: Table(){
    internal val id = integer("id")
    internal val login = text("login")
    private val hashedPassword = text("hashed_password")
    internal val username = varchar("username",50)
    internal val userNickname = varchar("user_nickname",50)
    internal val roleId = integer("role_id")
    internal val argon2ParamsId = integer("argon2_params_id")

    fun insert(userDTO: UserDataTransferObject) {
        transaction {
            Users.insert {
                it[id] = userDTO.id
                it[login] = userDTO.login
                it[hashedPassword] = userDTO.hashedPassword
                it[username] = userDTO.username
                it[userNickname] = userDTO.userNickname
                it[roleId] = userDTO.roleId
                it[argon2ParamsId] = userDTO.argon2ParamsId
            }
        }
    }


    fun fetchUsers(): List<UserDataTransferObject> {
        return try {
            transaction {
                Users.selectAll().map {
                    UserDataTransferObject(
                        id = it[Users.id],
                        login = it[login],
                        hashedPassword = it[hashedPassword],
                        username = it[username],
                        userNickname = it[userNickname],
                        roleId = it[roleId],
                        argon2ParamsId = it[argon2ParamsId]
                    )
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun fetchUsersCount(): Int {
        return try {
            transaction {
                Users.selectAll().count().toInt()
            }
        } catch (e: Exception) {
            0
        }
    }


    fun fetchUsernameByLogin(login: String): String? {
        return transaction {
            Users
                .select { Users.login.eq(login) }
                .mapNotNull { it[username] }
                .singleOrNull()
        }
    }
    fun fetchUserNameById(id : Int): String?{
        return transaction {
            Users
                .select{Users.id.eq(id)}
                .mapNotNull { it[username] }
                .singleOrNull()
        }
    }

    fun fetchUsersNamesById(ids: List<Int>): List<String> {
        return transaction {
            Users.select { Users.id.inList(ids) }
                .map { it[Users.username] }
                .toList()
        }
    }


    fun fetchRoleByLogin(login: String): String? {
        return transaction {
            Users
                .select { Users.login.eq(login) }
                .mapNotNull {
                    when (it[Users.roleId]) {
                        0 -> "superadmin"
                        1 -> "admin"
                        2 -> "watchdog"
                        3 -> "support"
                        4 -> "user"
                        else -> null
                    }
                }
                .singleOrNull()
        }
    }

    fun fetchUser(login: String): UserDataTransferObject? {
        return try {
            transaction {
                val user = Users.select { Users.login eq login }.singleOrNull()
                user?.let {
                    UserDataTransferObject(
                        id = it[Users.id],
                        login = it[Users.login],
                        hashedPassword = it[hashedPassword],
                        username = it[username],
                        userNickname = it[userNickname],
                        roleId = it[roleId],
                        argon2ParamsId = it[argon2ParamsId]
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}