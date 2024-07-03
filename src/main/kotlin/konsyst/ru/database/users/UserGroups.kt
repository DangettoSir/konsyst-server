package konsyst.ru.database.users
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

object UserGroups : Table("user_groups") {
    val id = integer("id").autoIncrement()
    val groupName = varchar("group_name", 50)
    val userCount = integer("user_count")
    val userIds = text("user_ids") // используем тип Array<Int>
}

object UserGroupsUserIds : Table("user_groups_user_ids") {
    val userGroupId = integer("user_group_id").references(UserGroups.id)
    val userId = integer("user_id")
}

fun insert(userGroupsDTO: UserGroupsDTO) {
    transaction {
        val userGroupId = UserGroups.insert {
            it[groupName] = userGroupsDTO.groupName
            it[userCount] = userGroupsDTO.userIds.size
            it[userIds] = userGroupsDTO.userIds.joinToString(",") // сохраняем userIds как строку
        } get UserGroups.id

        userGroupsDTO.userIds.forEach { userId ->
            UserGroupsUserIds.insert {
                it[this.userGroupId] = userGroupId
                it[this.userId] = userId
            }
        }
    }
}





