package application.web.handlers.userHandlers

import application.data.UsersStorage
import application.web.models.errorsModels.PageBadRequestVM
import application.web.models.userModels.UnauthorizedPageVM
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.body.form
import org.http4k.core.findSingle
import org.http4k.lens.RequestContextLens
import org.http4k.template.TemplateRenderer

class DeleteUserHandler(
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
        if (userRole != "theaterWorker" && userRole != "admin") {
            return Response(Status.UNAUTHORIZED).body(renderer(UnauthorizedPageVM()))
        }
        val form = request.form() // данные из формы от пользователя
        val userLogin = form.findSingle("login") ?: ""

        val user = users.getUser(userLogin) // получаем удаляемого пользвателя
        // удаление пользователя
        return if (user != null) {
            users.getUsers().remove(user)
            Response(Status.FOUND).header("Location", "/users")
        } else {
            Response(Status.BAD_REQUEST).body(renderer(PageBadRequestVM("Не найден объект удаления", userName, userRole)))
        }
    }
}
