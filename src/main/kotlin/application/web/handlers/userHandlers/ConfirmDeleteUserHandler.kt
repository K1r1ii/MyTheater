package application.web.handlers.userHandlers

import application.data.UsersStorage
import application.web.models.errorsModels.PageBadRequestVM
import application.web.models.userModels.ConfirmDeleteUserVM
import application.web.models.userModels.UnauthorizedPageVM
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.RequestContextLens
import org.http4k.template.TemplateRenderer

class ConfirmDeleteUserHandler(
    val renderer: TemplateRenderer,
    val key: RequestContextLens<String>,
    val users: UsersStorage,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val admin = users.getUser(key(request))
        var userName = ""
        var userRole = ""
        if (admin != null) {
            userName = admin.login
            userRole = admin.permission
        }
        // проверка прав пользователя
        if (userRole != "admin") {
            return Response(Status.UNAUTHORIZED).body(renderer(UnauthorizedPageVM()))
        }
        val userLogin = request.query("login") ?: "" // считываем логин удаляемого пользователя
        val user = users.getUser(userLogin)
        return if (user != null) {
            // данные для формы
            val dataForm =
                mapOf(
                    "login" to user.login,
                    "role" to user.permission,
                )
            val viewModel = ConfirmDeleteUserVM(dataForm, emptyMap(), userName, userRole)
            val htmlDocument = renderer(viewModel)
            Response(Status.OK).body(htmlDocument)
        } else {
            Response(Status.BAD_REQUEST).body(renderer(PageBadRequestVM("Не найден объект удаления", userName, userRole)))
        }
    }
}
