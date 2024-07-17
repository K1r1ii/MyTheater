package application.data

import com.google.gson.GsonBuilder
import java.io.File

fun jsonWriteData(
    tickets: List<Ticket>,
    performs: List<Performance>,
    shows: List<Showing>,
) {
    val data = mutableListOf<Map<String, Any>>()
    for (perform in performs) {
        val showsList = mutableListOf<Map<String, Any>>() // список с показами
        for (show in shows.filter { it.performId == perform.id }) {
            val ticketsList = mutableListOf<Map<String, Any>>()
            for (ticket in tickets.filter { it.showId == show.id }) {
                ticketsList.add(
                    mapOf(
                        "ticketId" to ticket.id.toString(),
                        "ticketDate" to ticket.ticketDate.toString(),
                        "visualArea" to ticket.visualArea,
                        "rowNum" to ticket.rowNum.toString(),
                        "placeNum" to ticket.placeNum.toString(),
                        "price" to ticket.price.toString(),
                        "bought" to ticket.bought.toString(),
                        "buyer" to ticket.buyer,
                    ),
                )
            }
            showsList.add(
                mapOf(
                    "idShow" to show.id.toString(),
                    "performDate" to show.date.toString(),
                    "theaterName" to show.theaterName,
                    "hallName" to show.hall,
                    "tickets" to ticketsList,
                ),
            )
        }
        val actorsList = perform.actors // список актеров
        val actorsListString = mutableListOf<String>() // такой же список в строковом формате
        // конвертация одного формата в другой
        for (actor in actorsList) {
            val actorStr = actor.actorName + " - " + actor.role + ","
            actorsListString.add(actorStr)
        }
        data.add(
            mutableMapOf(
                "id" to perform.id.toString(),
                "performName" to perform.performName,
                "performTime" to perform.performTime.toString(),
                "performPremDate" to perform.performPremDate.toString(),
                "actors" to actorsListString,
                "shows" to showsList,
            ),
        )
    }
    val gson = GsonBuilder().setPrettyPrinting().create() // создаем объект класса Gson для конвертации данных в json
    val json = gson.toJson(data)
    val jsonObject = gson.fromJson(json, Any::class.java)

    val prettyJson = gson.toJson(jsonObject)

    File("data.json").writeText(prettyJson) // записываем данные в файл
}

fun jsonWriteUsers(users: List<User>) {
    val usersList = mutableListOf<Map<String, String>>()
    for (user in users) {
        usersList.add(
            mapOf(
                "login" to user.login,
                "role" to user.permission,
                "hashPassword" to user.hashPassword,
                "theater" to user.theater,
            ),
        )
    }
    val gson = GsonBuilder().setPrettyPrinting().create() // создаем объект класса Gson для конвертации данных в json
    val json = gson.toJson(usersList)
    val jsonObject = gson.fromJson(json, Any::class.java)

    val prettyJson = gson.toJson(jsonObject)

    File("users.json").writeText(prettyJson) // записываем данные в файл
}
