package application.web.handlers.showHandlers

import application.data.PerformsStorage
import application.data.ShowsStorage
import application.data.UsersStorage
import application.web.dataCorrect.pagination
import application.web.models.errorsModels.PageBadRequestVM
import application.web.models.errorsModels.PageNotFoundVM
import application.web.models.showModels.EmptyShowsVM
import application.web.models.showModels.ShowVM
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.RequestContextLens
import org.http4k.routing.path
import org.http4k.template.TemplateRenderer

class ShowHandler(
    val renderer: TemplateRenderer,
    val performs: PerformsStorage,
    private val shows: ShowsStorage,
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
        val perform =
            performs.getPerform(performId)
                ?: return Response(Status.BAD_REQUEST).body(renderer(PageBadRequestVM("Переданы не корректные данные", userName, userRole)))
        // проверка на пустоту основного списка
        if (shows.getShows().none { it.performId == performId }) {
            // параметры из запроса
            val params =
                mapOf(
                    "firstDate" to request.query("firstDate").toString(),
                    "secondDate" to request.query("secondDate").toString(),
                )
            return Response(Status.OK).body(
                renderer(
                    EmptyShowsVM(
                        params,
                        perform,
                        userName,
                        userRole,
                    ),
                ),
            )
        }
        val firstDateStr = request.query("firstDate").toString()
        val secondDateStr = request.query("secondDate").toString()
        val dataFilter = shows.showsFilters(firstDateStr, secondDateStr, performId)
        val data = pagination(request, dataFilter.shows)
        val viewModel = ShowVM(data, dataFilter.paramsMap, perform, userName, userRole, userTheater)
        val htmlDocument = renderer(viewModel)
        return Response(Status.OK).body(htmlDocument)
    }
}
