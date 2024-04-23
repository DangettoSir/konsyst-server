package konsyst.ru

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.testing.*
import konsyst.ru.plugins.*
import kotlin.test.*


class ApplicationTest{

@Test
fun testRoot() {
    withTestApplication(Application::configureRouting) {
        handleRequest(HttpMethod.Get, "/test").apply {
            assertEquals(HttpStatusCode.OK, response.status())
            assertEquals("Test Hello World", response.content)
        }
    }
}
}