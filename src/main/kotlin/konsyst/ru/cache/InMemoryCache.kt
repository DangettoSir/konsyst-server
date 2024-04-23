package konsyst.ru.cache

import konsyst.ru.features.register.RegisterReceive

data class TokenCache(
    val login: String,
    val token: String
)

object InMemoryCache {
    val userList: MutableList<RegisterReceive> = mutableListOf()
    val token: MutableList<TokenCache> = mutableListOf()
}