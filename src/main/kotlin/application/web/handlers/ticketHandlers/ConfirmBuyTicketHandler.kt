package application.web.handlers.ticketHandlers

import application.data.TicketsStorage
import application.data.UsersStorage
import application.web.models.errorsModels.PageBadRequestVM
import application.web.models.ticketModels.ConfirmBuyTicketVM
import application.web.models.userModels.UnauthorizedPageVM
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.RequestContextLens
import org.http4k.template.TemplateRenderer

class ConfirmBuyTicketHandler(
    val renderer: TemplateRenderer,
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
        if (userRole != "theaterWorker" && userRole != "admin" && userRole != "viewer") {
            return Response(Status.UNAUTHORIZED).body(renderer(UnauthorizedPageVM()))
        }
        val id = request.query("ticketId")?.toIntOrNull()
        return if (id != null) {
            val ticket = tickets.getTicket(id)
            if (ticket != null) {
                val dataForm =
                    mapOf(
                        "ticketId" to id.toString(),
                        "visualArea" to ticket.visualArea,
                        "rowNum" to ticket.rowNum.toString(),
                        "placeNum" to ticket.placeNum.toString(),
                        "price" to ticket.price.toString(),
                    )
                val viewModel = ConfirmBuyTicketVM(dataForm, emptyMap(), userName, userRole)
                val htmlDocument = renderer(viewModel)
                Response(Status.OK).body(htmlDocument)
            } else {
                Response(Status.BAD_REQUEST).body(renderer(PageBadRequestVM("Не найден объект покупки", userName, userRole)))
            }
        } else {
            Response(Status.BAD_REQUEST).body(renderer(PageBadRequestVM("Не найден объект покупки", userName, userRole)))
        }
    }
}
