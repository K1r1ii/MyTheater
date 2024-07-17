package application.web.handlers.ticketHandlers

import application.data.PerformsStorage
import application.data.ShowsStorage
import application.data.TicketsStorage
import application.data.UsersStorage
import application.web.dataCorrect.pagination
import application.web.models.errorsModels.PageBadRequestVM
import application.web.models.errorsModels.PageNotFoundVM
import application.web.models.ticketModels.EmptyTicketsVM
import application.web.models.ticketModels.PerformanceVM
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.RequestContextLens
import org.http4k.routing.path
import org.http4k.template.TemplateRenderer

class PerformHandler(
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
        val showId =
            request.path("showId")?.toIntOrNull()
                ?: return Response(
                    Status.NOT_FOUND,
                ).body(renderer(PageNotFoundVM("Не корректный идентифиактор показа", userName, userRole)))
        if (showId !in shows.getShows().map { it.id }) {
            return Response(Status.NOT_FOUND).body(renderer(PageNotFoundVM("Не корректный идентифиактор показа", userName, userRole)))
        }
        val performId =
            request.path("performId")?.toIntOrNull()
                ?: return Response(
                    Status.NOT_FOUND,
                ).body(renderer(PageNotFoundVM("Не корректный идентифиактор спектакля", userName, userRole)))
        if (performId !in performs.getPerforms().map { it.id }) {
            return Response(Status.NOT_FOUND).body(renderer(PageNotFoundVM("Не корректный идентифиактор спектакля", userName, userRole)))
        }

        val show =
            shows.getShow(showId)
                ?: return Response(Status.BAD_REQUEST).body(renderer(PageBadRequestVM("Переданы не корректные данные", userName, userRole)))
        val perform =
            performs.getPerform(performId)
                ?: return Response(Status.BAD_REQUEST).body(renderer(PageBadRequestVM("Переданы не корректные данные", userName, userRole)))
        // проверка на пустоту основоного списка
        if (tickets.getTickets().none { it.showId == showId }) {
            val params =
                mapOf(
                    "parter" to request.query("parter").toString(),
                    "balcony" to request.query("balcony").toString(),
                    "amphitheater" to request.query("amphitheater").toString(),
                    "minPrice" to request.query("minPrice").toString(),
                    "maxPrice" to request.query("maxPrice").toString(),
                )
            return Response(Status.OK).body(
                renderer(
                    EmptyTicketsVM(
                        show,
                        perform,
                        params,
                        userName,
                        userRole,
                    ),
                ),
            )
        }
        val ticketStatusStr = request.query("ticketStatus") ?: "" // куплен или в продаже
        val areas =
            listOf(
                request.query("parter").toString(),
                request.query("balcony").toString(),
                request.query("amphitheater").toString(),
            )
        val minPrice =
            request.query(
                "minPrice",
            )?.toIntOrNull() ?: (tickets.getTickets().filter { it.showId == showId }.minBy { it.price }).price
        val maxPrice =
            request.query(
                "maxPrice",
            )?.toIntOrNull() ?: (tickets.getTickets().filter { it.showId == showId }.maxBy { it.price }).price
        val dataFilter = tickets.ticketsFilter(showId, ticketStatusStr, areas, minPrice, maxPrice) // отфильтрованный список билетов
        val data = pagination(request, dataFilter.tickets) // данные для заданной страницы
        // проверка на некорректную страницу
        if (data.message != "OK" && data.message != "Нет подходящих элементов") {
            return Response(Status.NOT_FOUND).body(renderer(PageNotFoundVM(data.message, userName, userRole)))
        }
        // проверка на пустой список
        if (data.message == "Нет подходящих элементов") {
            return Response(Status.OK).body(renderer(EmptyTicketsVM(show, perform, dataFilter.params, userName, userRole)))
        }

        val viewModel = PerformanceVM(data, show, perform, dataFilter.params, userName, userRole, userTheater)
        val htmlDocument = renderer(viewModel)
        return Response(Status.OK).body(htmlDocument)
    }
}
