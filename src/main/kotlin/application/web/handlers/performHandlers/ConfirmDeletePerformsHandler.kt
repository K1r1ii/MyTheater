package application.web.handlers.performHandlers

import application.data.PerformsStorage
import application.data.UsersStorage
import application.web.models.errorsModels.PageBadRequestVM
import application.web.models.performModels.ConfirmDeletePerformsVM
import application.web.models.userModels.UnauthorizedPageVM
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.RequestContextLens
import org.http4k.template.TemplateRenderer

class ConfirmDeletePerformsHandler(
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
        // проверка прав пользователя
        if (userRole != "theaterWorker" && userRole != "admin") {
            return Response(Status.UNAUTHORIZED).body(renderer(UnauthorizedPageVM()))
        }
        val performId = request.query("performId")?.toIntOrNull()
        if (performId != null) {
            val perform = performs.getPerform(performId)
            if (perform != null) {
                var actorsStr = ""
                for (actor in perform.actors) {
                    actorsStr += actor.actorName + " - " + actor.role + ","
                }
                val dataForm =
                    mapOf(
                        "performName" to perform.performName,
                        "performTime" to perform.performTime.toString(),
                        "performPremDate" to perform.performPremDate.toString(),
                        "actors" to actorsStr,
                    )
                val viewModel = ConfirmDeletePerformsVM(dataForm, emptyMap(), userName, userRole)
                val htmlDocument = renderer(viewModel)
                return Response(Status.OK).body(htmlDocument)
            } else {
                return Response(Status.BAD_REQUEST).body(renderer(PageBadRequestVM("Не найден объект удаления", userName, userRole)))
            }
        } else {
            return Response(Status.BAD_REQUEST).body(renderer(PageBadRequestVM("Не найден объект удаления", userName, userRole)))
        }
    }
}
