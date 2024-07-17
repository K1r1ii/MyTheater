package application.web.handlers.userHandlers

import application.data.UsersStorage
import application.web.models.errorsModels.PageBadRequestVM
import application.web.models.userModels.NewUserVM
import application.web.models.userModels.UnauthorizedPageVM
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.RequestContextLens
import org.http4k.template.TemplateRenderer

class EditUserHandler(
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
        val userLogin = request.query("login") ?: ""
        val emptyMap = mapOf<String, String>()
        val user = users.getUser(userLogin) // находим редактируемого пользователя
        return if (user != null) {
            val dataForm =
                mapOf(
                    "login" to user.login,
                    "role" to user.permission,
                    "theater" to user.theater,
                )
            val viewModel = NewUserVM(dataForm, emptyMap, userName, userRole)
            val htmlDocument = renderer(viewModel)
            Response(Status.OK).body(htmlDocument)
        } else {
            Response(Status.BAD_REQUEST).body(renderer(PageBadRequestVM("Переданы не корректные данные", userName, userRole)))
        }
    }
}
