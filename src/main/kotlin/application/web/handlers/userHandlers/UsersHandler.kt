package application.web.handlers.userHandlers

import application.data.UsersStorage
import application.web.dataCorrect.pagination
import application.web.models.userModels.EmptyUsersVM
import application.web.models.userModels.UnauthorizedPageVM
import application.web.models.userModels.UsersVM
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.RequestContextLens
import org.http4k.template.TemplateRenderer

class UsersHandler(
    val renderer: TemplateRenderer,
    val users: UsersStorage,
    val key: RequestContextLens<String>,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val roles =
            mapOf(
                "admin" to "Администратор",
                "viewer" to "Зритель",
                "theaterWorker" to "Работник театра",
            )
        val user = users.getUser(key(request))
        var userName = ""
        var userRole = ""
        if (user != null) {
            userName = user.login
            userRole = user.permission
        }
        // проверка прав пользователя
        if (userRole != "admin") {
            return Response(Status.UNAUTHORIZED).body(renderer(UnauthorizedPageVM()))
        }
        val usersList = users.getUsers()
        if (usersList.isEmpty()) {
            // обработка случая с пустым списком пользователей
            return Response(Status.OK).body(renderer(EmptyUsersVM(userName, roles[userRole].toString())))
        }
        val usersPage = pagination(request, usersList)
        val viewModel = UsersVM(usersPage, userName, userRole)
        val htmlDocument = renderer(viewModel)
        return Response(Status.OK).body(htmlDocument)
    }
}
