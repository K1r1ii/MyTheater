package application.web.handlers.userHandlers

import application.data.TicketsStorage
import application.data.UsersStorage
import application.web.dataCorrect.pagination
import application.web.models.userModels.EmptyUserTicketsVM
import application.web.models.userModels.UnauthorizedPageVM
import application.web.models.userModels.UserTicketsVM
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.RequestContextLens
import org.http4k.template.TemplateRenderer

class UserTicketsHandler(
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
        if (userRole != "viewer") {
            return Response(Status.UNAUTHORIZED).body(renderer(UnauthorizedPageVM()))
        }

        val userTickets = tickets.getTickets().filter { it.buyer == userName }
        // проверка на пустоту основоного списка
        if (userTickets.isEmpty()) {
            return Response(Status.OK).body(
                renderer(
                    EmptyUserTicketsVM(
                        userName,
                        userRole,
                    ),
                ),
            )
        }

        val data = pagination(request, userTickets) // данные для заданной страницы

        val viewModel = UserTicketsVM(data, userName, userRole)
        val htmlDocument = renderer(viewModel)
        return Response(Status.OK).body(htmlDocument)
    }
}
