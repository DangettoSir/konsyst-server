package konsyst.ru.database.tokens

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object Tokens: Table() {
    private val id = Tokens.varchar("id", 50)
    internal val login = Tokens.varchar("login", 25)
    internal val token = Tokens.varchar("token", 50)


    fun insert(tokenDTO: TokenDataTransferObject) {
        transaction {
            Tokens.insert {
                it[id] = tokenDTO.rowId
                it[login] = tokenDTO.login
                it[token] = tokenDTO.token
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
    fun fetchToken(login: String): TokenDataTransferObject? {
        return try {
            transaction {
                Tokens.select { Tokens.login eq login }
                    .map {
                        TokenDataTransferObject(
                            rowId = it[Tokens.id],
                            token = it[Tokens.token],
                            login = it[Tokens.login]
                        )
                    }
                    .singleOrNull()
            }
        } catch (e: Exception) {
            null
        }
    }
}
