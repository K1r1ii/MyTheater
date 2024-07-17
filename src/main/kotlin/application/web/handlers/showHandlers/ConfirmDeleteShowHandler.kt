package application.web.handlers.showHandlers

import application.data.ShowsStorage
import application.data.UsersStorage
import application.web.models.errorsModels.PageBadRequestVM
import application.web.models.showModels.ConfirmDeleteShowVM
import application.web.models.userModels.UnauthorizedPageVM
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.RequestContextLens
import org.http4k.template.TemplateRenderer

class ConfirmDeleteShowHandler(
    val renderer: TemplateRenderer,
    val shows: ShowsStorage,
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
        if (userRole != "theaterWorker" && userRole != "admin") {
            return Response(Status.UNAUTHORIZED).body(renderer(UnauthorizedPageVM()))
        }
        val showId = request.query("showId")?.toIntOrNull()
        return if (showId != null) {
            val show = shows.getShow(showId)
            if (show != null) {
                val dataForm =
                    mapOf(
                        "theaterName" to show.theaterName,
                        "hallName" to show.hall,
                        "date" to show.date.toString(),
                    )
                val viewModel = ConfirmDeleteShowVM(dataForm, emptyMap(), userName, userRole)
                val htmlDocument = renderer(viewModel)
                Response(Status.OK).body(htmlDocument)
            } else {
                Response(Status.BAD_REQUEST).body(renderer(PageBadRequestVM("Не найден объект удаления", userName, userRole)))
            }
        } else {
            Response(Status.BAD_REQUEST).body(renderer(PageBadRequestVM("Не найден объект удаления", userName, userRole)))
        }
    }
}
