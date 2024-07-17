package application.web.handlers.ticketHandlers

import application.data.PerformsStorage
import application.data.ShowsStorage
import application.data.Ticket
import application.data.TicketsStorage
import application.data.UsersStorage
import application.web.models.errorsModels.PageBadRequestVM
import application.web.models.errorsModels.PageNotFoundVM
import application.web.models.ticketModels.NewTicketVM
import application.web.models.userModels.UnauthorizedPageVM
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.FOUND
import org.http4k.core.body.form
import org.http4k.core.findSingle
import org.http4k.lens.RequestContextLens
import org.http4k.routing.path
import org.http4k.template.TemplateRenderer
import java.time.LocalDate

class AddTicketHandler(
    val renderer: TemplateRenderer,
    val performs: PerformsStorage,
    val shows: ShowsStorage,
    val tickets: TicketsStorage,
    val key: RequestContextLens<String>,
    val users: UsersStorage,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val user = users.getUser(key(request))
        var userName = ""
        var userRole = ""
        var userTheater = ""
        if (user != null) {
            userName = user.login
            userRole = user.permission
            userTheater = user.theater
        }
        val performId =
            request.path("performId")?.toIntOrNull()
                ?: return Response(
                    Status.NOT_FOUND,
                ).body(renderer(PageNotFoundVM("Не корректный идентифиактор спектакля", userName, userRole)))
        if (performId !in performs.getPerforms().map { it.id }) {
            return Response(Status.NOT_FOUND).body(renderer(PageNotFoundVM("Не корректный идентифиактор спектакля", userName, userRole)))
        }
        val showId =
            request.path("showId")?.toIntOrNull()
                ?: return Response(
                    Status.NOT_FOUND,
                ).body(renderer(PageNotFoundVM("Не корректный идентифиактор показа", userName, userRole)))
        if (showId !in shows.getShows().map { it.id }) {
            return Response(Status.NOT_FOUND).body(renderer(PageNotFoundVM("Не корректный идентифиактор показа", userName, userRole)))
        }

        val show =
            shows.getShow(showId)
                ?: return Response(BAD_REQUEST).body(renderer(PageBadRequestVM("Переданы не корректные данные", userName, userRole)))
        if (user != null) {
            // проверка на то, что работник может редактировать данный театр
            if (user.theater != show.theaterName) {
                return Response(Status.UNAUTHORIZED).body(renderer(UnauthorizedPageVM()))
            }
        }
        // проверка прав пользователя
        if (userRole != "theaterWorker" || userTheater != show.theaterName) {
            return Response(Status.UNAUTHORIZED).body(renderer(UnauthorizedPageVM()))
        }
        val form = request.form() // данные из формы от пользователя
        val visualArea = form.findSingle("visualArea") ?: "" // название зрительской зоны
        val rowNum = form.findSingle("rowNum")?.toIntOrNull() // номер ряда
        val placeNum = form.findSingle("placeNum")?.toIntOrNull() // номер места
        val price = form.findSingle("price")?.toIntOrNull() // цена
        val status = form.findSingle("ticketStatus") ?: "" // куплен или находится в продаже
        val ticketId = request.query("ticketId")?.toIntOrNull() // id редактируемого показа

        val visualAreas = listOf("Партер", "Балкон", "Амфитеатр") // список допустимых зрительских зон
        val statuses = listOf("Куплен", "В продаже") // статусы билета
        // словарь на случай не корретных данных
        val dataForm =
            mapOf(
                "ticketStatus" to status,
                "visualArea" to visualArea,
                "rowNum" to rowNum.toString(),
                "placeNum" to placeNum.toString(),
                "price" to price.toString(),
            )
        // словарь с возможными ошибками в полях
        val errorData =
            mutableMapOf(
                "ticketStatus" to "",
                "visualArea" to "",
                "rowNum" to "",
                "placeNum" to "",
                "price" to "",
            )
        var errorFlag = false // наличие ошибки
        if (visualArea !in visualAreas) {
            errorData["visualArea"] = "Зрительская зона - обязательный параметр и имеет 3 указанных допустимых значения"
            errorFlag = true
        }
        if (rowNum == null) {
            errorData["rowNum"] = "Номер ряда обязательный числовой параметр"
            errorFlag = true
        }
        if (placeNum == null) {
            errorData["placeNum"] = "Номер места обязательный числовой параметр"
            errorFlag = true
        }
        if (price == null) {
            errorData["price"] = "Цена обязательный числовой параметр"
            errorFlag = true
        }
        if (status !in statuses) {
            errorData["ticketStatus"] = "Статус билета обязательный параметр имеющий допустимые значения Куплен или В продаже"
            errorFlag = true
        }
        if (errorFlag) return Response(BAD_REQUEST).body(renderer(NewTicketVM(dataForm, errorData, userName, userRole)))
        val ticketDate = LocalDate.now()
        val statusBool = status == "В продаже"
        if (rowNum != null && placeNum != null && price != null) {
            if (ticketId != null) {
                val index =
                    tickets.getTickets().indexOf(tickets.getTicket(ticketId)) // id искомого билета
                tickets.getTickets()[index] =
                    Ticket(
                        ticketId,
                        visualArea,
                        rowNum,
                        placeNum,
                        price,
                        ticketDate,
                        statusBool,
                        showId,
                    )
            } else {
                val newId = (tickets.getTickets().maxBy { it.id }).id + 1
                tickets.addTicket(
                    Ticket(
                        newId,
                        visualArea,
                        rowNum,
                        placeNum,
                        price,
                        ticketDate,
                        statusBool,
                        showId,
                    ),
                )
            }
        }
        return Response(FOUND).header("Location", "/performances/$performId/$showId")
    }
}
