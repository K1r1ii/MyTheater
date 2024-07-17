package application.data

import application.web.dataCorrect.TicketsFilters

class TicketsStorage(
    private val tickets: MutableList<Ticket> = mutableListOf(),
) {
    fun getTickets(): MutableList<Ticket> {
        return tickets
    }

    fun addTicket(newTicket: Ticket) {
        tickets.add(newTicket)
    }

    fun deleteTicket(ticketId: Int): Int {
        // ДОБАВИТЬ ИСКЛЮЧЕНИЯ НА УДАЛЕНИЯ НЕ СУЩЕСТВУЮЩЕГО
        tickets.remove(tickets.find { it.id == ticketId })
        return 0
    }

//    fun checkLogin(login: String): Boolean {
//        // метод для проверка на существование пользователя с заданным логином
//        val user = users.find { it.login == login }
//        return user != null
//    }
    fun getTicket(id: Int): Ticket? {
        return tickets.find { it.id == id }
    }

    fun ticketsFilter(
        showId: Int,
        ticketStatusStr: String,
        areas: List<String>,
        minPrice: Int,
        maxPrice: Int,
    ): TicketsFilters {
        var filterTicket: List<Ticket> // список итоговых отфильтрованных данных
        val visualAreas = mutableListOf<String>() // список подходящих зрительских зон

        var ticketStatus = true
        if (ticketStatusStr != "") {
            ticketStatus = ticketStatusStr == "В продаже"
        }
        // параметры зрительской зоны
        val parter: Boolean = areas[0] == "on"
        val balcony: Boolean = areas[1] == "on"
        val amphitheater: Boolean = areas[2] == "on"
        // добавляем выбранные пункты
        if (parter) visualAreas.add("Партер")
        if (balcony) visualAreas.add("Балкон")
        if (amphitheater) visualAreas.add("Амфитеатр")

        filterTicket =
            tickets
                .filter { it.bought == ticketStatus }
                .filter { it.showId == showId }
                .filter { if (visualAreas.size != 0) it.visualArea in visualAreas else true }
                .filter { it.price in minPrice..maxPrice }
        filterTicket = filterTicket.sortedByDescending { it.ticketDate } // сортировка по дате добавления
        val paramsMap: Map<String, String> =
            mapOf(
                "ticketStatus" to ticketStatusStr,
                "parter" to if (parter) "on" else "",
                "balcony" to if (balcony) "on" else "",
                "amphitheater" to if (amphitheater) "on" else "",
                "minPrice" to minPrice.toString(),
                "maxPrice" to maxPrice.toString(),
            )
        return TicketsFilters(
            filterTicket,
            paramsMap,
        )
    }
}
