package application.web.handlers.performHandlers

import application.data.PerformsStorage
import application.data.UsersStorage
import application.web.dataCorrect.pagination
import application.web.models.performModels.EmptyPerformancesVM
import application.web.models.performModels.PerformancesVM
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.RequestContextLens
import org.http4k.template.TemplateRenderer

class PerformsHandler(
    val renderer: TemplateRenderer,
    val performs: PerformsStorage,
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

        // проверка на пустоту основного списка
        if (performs.getPerforms().isEmpty()) {
            // параметры из запроса
            val params =
                mapOf(
                    "minTime" to request.query("minTime").toString(),
                    "maxTime" to request.query("maxTime").toString(),
                    "firstDate" to request.query("firstDate").toString(),
                    "secondDate" to request.query("secondDate").toString(),
                )
            return Response(Status.OK).body(
                renderer(
                    EmptyPerformancesVM(
                        params,
                        userName,
                        userRole,
                    ),
                ),
            )
        }
        val firstDateStr = request.query("firstDate").toString()
        val secondDateStr = request.query("secondDate").toString()
        val minDuration = request.query("minTime")?.toIntOrNull() ?: (performs.getPerforms().minBy { it.performTime }).performTime
        val maxDuration = request.query("maxTime")?.toIntOrNull() ?: (performs.getPerforms().maxBy { it.performTime }).performTime
        val dataFilter = performs.performsFilters(firstDateStr, secondDateStr, minDuration, maxDuration) // данные с примененными фильтрами
        val data = pagination(request, dataFilter.performs)
        // проверка на пустоту отфильтрованных данных
        if (data.message == "Нет подходящих элементов") {
            return Response(Status.OK).body(renderer(EmptyPerformancesVM(dataFilter.paramsMap, userName, userRole)))
        }
        val viewModel = PerformancesVM(data, dataFilter.paramsMap, userName, userRole)
        val htmlDocument = renderer(viewModel)
        return Response(Status.OK).body(htmlDocument)
    }
}
