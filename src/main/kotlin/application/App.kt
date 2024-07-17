package application

import application.config.AppConfig
import application.data.jsonWriteData
import application.data.jsonWriteUsers
import application.data.readJsonData
import application.data.readJsonUsers
import application.web.router
import org.http4k.core.RequestContexts
import org.http4k.core.then
import org.http4k.filter.ServerFilters
import org.http4k.lens.RequestContextKey
import org.http4k.routing.ResourceLoader
import org.http4k.routing.routes
import org.http4k.routing.static
import org.http4k.server.Netty
import org.http4k.server.asServer
import org.http4k.template.PebbleTemplates
import java.time.LocalDate
import java.time.format.DateTimeParseException

fun main() {
    val renderer = PebbleTemplates().CachingClasspath()
    val contexts = RequestContexts()
    val key = RequestContextKey.required<String>(contexts)
    val configData = AppConfig.getConfig()

    val allData = readJsonData() // объект со всеми данными
    val allTickets = allData.tickets // список всеъ билетов
    val allPerforms = allData.performs // список всех спектаклей
    val allShows = allData.shows // список всех показов
    val allUsers = readJsonUsers(configData) // список всех пользователй
    // СЕРВЕР
    val appWithStaticResources =
        routes(
            router(key, allTickets, allShows, allPerforms, allUsers, renderer, configData),
            static(ResourceLoader.Classpath("/application/public")),
        )
    val server =
        ServerFilters.InitialiseRequestContext(contexts)
            .then(roleFilter(key, allUsers, configData))
            .then(errorFilter(key, allUsers, renderer))
            .then(appWithStaticResources).asServer(Netty(configData.webPort)).start()
    println("Server started on http://localhost:" + server.port())
    println("Press enter to exit application.")
    readln()
    server.stop()
    Runtime.getRuntime().addShutdownHook(
        Thread {
            jsonWriteData(allTickets.getTickets(), allPerforms.getPerforms(), allShows.getShows()) // записываем данные
            jsonWriteUsers(allUsers.getUsers()) // записываем пользователей
        },
    )
}

fun isStringLocalDate(str: String): Boolean {
    // функция для проверки может ли строка быть датой
    if (str == "") {
        println("test")
        return false
    }
    return try {
        LocalDate.parse(str)
        true
    } catch (e: DateTimeParseException) {
        false
    }
}
