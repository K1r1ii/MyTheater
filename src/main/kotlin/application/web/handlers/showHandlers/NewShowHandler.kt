package application.web.handlers.showHandlers

import application.data.ShowsStorage
import application.data.UsersStorage
import application.web.models.showModels.NewShowVM
import application.web.models.userModels.UnauthorizedPageVM
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.RequestContextLens
import org.http4k.template.TemplateRenderer

class NewShowHandler(
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
        val emptyMap = mapOf<String, String>()
        var dataForm = mapOf<String, String>()
        if (showId != null) {
            val show = shows.getShow(showId)
            if (show != null) {
                dataForm =
                    mapOf(
                        "theaterName" to show.theaterName,
                        "hallName" to show.hall,
                        "date" to show.date.toString(),
                    )
            }
        }
        val viewModel = NewShowVM(dataForm, emptyMap, userName, userRole)
        val htmlDocument = renderer(viewModel)
        return Response(Status.OK).body(htmlDocument)
    }
}
