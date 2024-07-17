package application.web.handlers.ticketHandlers

import application.data.PerformsStorage
import application.data.ShowsStorage
import application.data.TicketsStorage
import application.data.UsersStorage
import application.web.models.errorsModels.PageBadRequestVM
import application.web.models.errorsModels.PageNotFoundVM
import application.web.models.ticketModels.ConfirmBuyTicketVM
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

class BuyTicketHandler(
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
        if (user != null) {
            userName = user.login
            userRole = user.permission
        }
        // проверка прав пользователя
        if (userRole != "viewer") {
            return Response(Status.UNAUTHORIZED).body(renderer(UnauthorizedPageVM()))
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

        val form = request.form() // данные из формы от пользователя
        val visualArea = form.findSingle("visualArea") ?: "" // название зрительской зоны
        val rowNum = form.findSingle("rowNum")?.toIntOrNull() // номер ряда
        val placeNum = form.findSingle("placeNum")?.toIntOrNull() // номер места
        val price = form.findSingle("price")?.toIntOrNull() // цена
        val ticketId = request.query("ticketId")?.toIntOrNull() // id редактируемого показа

        val visualAreas = listOf("Партер", "Балкон", "Амфитеатр") // список допустимых зрительских зон
        // словарь на случай не корретных данных
        val dataForm =
            mapOf(
                "visualArea" to visualArea,
                "rowNum" to rowNum.toString(),
                "placeNum" to placeNum.toString(),
                "price" to price.toString(),
            )
        // словарь с возможными ошибками в полях
        val errorData =
            mutableMapOf(
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
        if (errorFlag) return Response(BAD_REQUEST).body(renderer(ConfirmBuyTicketVM(dataForm, errorData, userName, userRole)))
        if (ticketId != null) {
            val index =
                tickets.getTickets().indexOf(tickets.getTickets().find { it.id == ticketId && it.bought }) // id искомого билета
            if (index == -1) return Response(BAD_REQUEST).body(renderer(ConfirmBuyTicketVM(dataForm, errorData, userName, userRole)))
            tickets.getTickets()[index].setBought()
            tickets.getTickets()[index].setBuyerLogin(userName)
            return Response(FOUND).header("Location", "/performances/$performId/$showId")
        } else {
            return Response(BAD_REQUEST).body(renderer(PageBadRequestVM("Не найден объект покупки", userName, userRole)))
        }
    }
}
