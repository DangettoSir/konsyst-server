package konsyst.ru.database.tokens


import konsyst.ru.database.users.UserDataTransferObject
import konsyst.ru.database.users.Users
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object Tokens: Table() {
    private val id = Tokens.varchar("id", 50)
    private val login = Tokens.varchar("login", 25)
    private val token = Tokens.varchar("token", 50)


    fun insert(tokenDataTransferObject: TokenDataTransferObject) {
        transaction {
            Tokens.insert {
                it[id] = tokenDataTransferObject.rowId
                it[login] = tokenDataTransferObject.login
                it[token] = tokenDataTransferObject.token
            }
        }
    }

    fun fetchTokens(): List<TokenDataTransferObject> {
        return try {
            transaction {
                Tokens.selectAll().toList()
                    .map {
                        TokenDataTransferObject(
                            rowId = it[Tokens.id],
                            token = it[Tokens.token],
                            login = it[Tokens.login]
                        )
                    }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
