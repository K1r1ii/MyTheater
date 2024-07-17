package application.web.dataCorrect

import application.data.Ticket

data class TicketsFilters(
    val tickets: List<Ticket>,
    val params: Map<String, String>,
)
