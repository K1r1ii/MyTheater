package application.data

import application.config.AppConfig
import com.google.gson.Gson
import com.google.gson.JsonArray
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun readJsonData(): DataJson {
    // функция для чтения json файла с данными для приложения
    val filePath = "data.json" // выходные данные
    val json = File(filePath).readText() // считали файл
    val gson = Gson()

    val performs = mutableListOf<Performance>() // список объектов Ticket (класс для информации о билете)
    val shows = mutableListOf<Showing>() // список показов
    val tickets = mutableListOf<Ticket>() // список билетов

    val performsJson = gson.fromJson(json, JsonArray::class.java) // перевели в формат jsonArray основной список со спектаклями
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd") // для форматирования даты
    for (perform in performsJson) {
        val performId = perform.asJsonObject.get("id").asInt
        val performName = perform.asJsonObject.get("performName").asString
        val performTime = perform.asJsonObject.get("performTime").asInt
        val performPremDate = LocalDate.parse(perform.asJsonObject.get("performPremDate").asString, formatter)
        val actorsJList = perform.asJsonObject.get("actors").asJsonArray.map { it.asString }
        val actors = mutableListOf<Role>()
        for (actorString in actorsJList) {
            val actorData = actorString.split(" - ")
            val actor = Role(actorData.first(), actorData.last().replace(",", ""))
            actors.add(actor)
        }
        // собираем объект спектакля
        val performClass =
            Performance(
                performId,
                performName,
                performTime,
                performPremDate,
                actors,
            )
        performs.add(performClass) // добавляем в список спектаклей
        val showsJ = perform.asJsonObject.get("shows").asJsonArray
        // перебираем список показов
        for (show in showsJ) {
            val showId = show.asJsonObject.get("idShow").asInt
            val performDate = LocalDate.parse(show.asJsonObject.get("performDate").asString, formatter)
            val theaterName = show.asJsonObject.get("theaterName").asString
            val hallName = show.asJsonObject.get("hallName").asString
            // собираем объект показа
            val showClass =
                Showing(
                    showId,
                    performDate,
                    theaterName,
                    hallName,
                    performClass.id,
                )
            shows.add(showClass) // добавляем в общий список показов
            val ticketsJ = show.asJsonObject.get("tickets").asJsonArray
            // перебираем список билетов
            for (ticket in ticketsJ) {
                val ticketId = ticket.asJsonObject.get("ticketId").asInt
                val ticketDate = LocalDate.parse(ticket.asJsonObject.get("ticketDate").asString, formatter)
                val visualArea = ticket.asJsonObject.get("visualArea").asString
                val rowNum = ticket.asJsonObject.get("rowNum").asInt
                val placeNum = ticket.asJsonObject.get("placeNum").asInt
                val price = ticket.asJsonObject.get("price").asInt
                val statusBuy = ticket.asJsonObject.get("bought").asBoolean
                val buyer = ticket.asJsonObject.get("buyer").asString
                // добавляем объект билета в общий список билетов
                tickets.add(
                    Ticket(
                        ticketId,
                        visualArea,
                        rowNum,
                        placeNum,
                        price,
                        ticketDate,
                        statusBuy,
                        showClass.id,
                        buyer,
                    ),
                )
            }
        }
    }
    // возвращаем объект класса хранящего все три списка
    return DataJson(TicketsStorage(tickets), PerformsStorage(performs), ShowsStorage(shows))
}

fun readJsonUsers(config: AppConfig): UsersStorage {
    val filePath = "users.json" // выходные данные
    val json = File(filePath).readText() // считали файл
    val gson = Gson()

    val users = mutableListOf<User>()
    val usersJson = gson.fromJson(json, JsonArray::class.java)
    for (user in usersJson) {
        val login = user.asJsonObject.get("login").asString
        val role = user.asJsonObject.get("role").asString
        val hashPassword = user.asJsonObject.get("hashPassword").asString
        val theater = user.asJsonObject.get("theater").asString
        users.add(User(role, login, hashPassword, theater))
    }
    return UsersStorage(users, config)
}
