package konsyst.ru.features.userdata

import io.ktor.server.application.*
import io.ktor.server.routing.*
import kotlin.io.encoding.ExperimentalEncodingApi

@ExperimentalEncodingApi
fun Application.configureUserDataRouting() {
    val userdDataController = UserDataController()
    routing {
        post("/userdata/get") {
            userdDataController.getDataSteps(call)
        }
        post("/userdata/request"){
            userdDataController.uploadFile(call)
        }
        get("/userdata/getcount"){
            userdDataController.GetUsersCount(call)
        }
        get("/userdata/getgroups"){
            userdDataController.GetUserGroups(call)
        }
        post("/userdata/creategroup"){
            userdDataController.createUserGroup(call)
        }
    }
}