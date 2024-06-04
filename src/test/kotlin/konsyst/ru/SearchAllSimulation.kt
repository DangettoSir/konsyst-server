package konsyst.ru


//todo попытка сделать тест-кейс с помощью gatling конкретно на роутинг Search All
//Не особо вышло, нужно разбираться с документацией Гатлинга.
/*

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import io.gatling.core.Predef.*
import io.gatling.core.scenario.Simulation
import io.gatling.javaapi.http.HttpDsl.http
import io.gatling.core.Predef.atOnceUsers
import io.gatling.core.Predef.exec
import io.gatling.core.Predef.scenario
import io.gatling.http.Predef.http
import io.gatling.http.Predef.status
class SearchAllSimulation : Simulation() {

    private val httpConf = http
        .baseUrl("http://127.0.0.1:8080")
        .header("Bearer-Authorization", "99c97eb3-e1cc-4bf8-bd45-81e043d161ef")

    private val scn = scenario("SearchAll")
        .exec(
            http("scenarios")
                .get("/search-all")
                .check(status().equals(200))
        )

    init {
        setUp(
            scn.inject(atOnceUsers(100))
        ).protocols(httpConf)
    }
}*/
