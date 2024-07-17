package application.web.handlers.userHandlers

import application.data.UsersStorage
import application.web.models.errorsModels.PageBadRequestVM
import application.web.models.userModels.NewUserVM
import application.web.models.userModels.UnauthorizedPageVM
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.FOUND
import org.http4k.core.body.form
import org.http4k.core.findSingle
import org.http4k.lens.RequestContextLens
import org.http4k.template.TemplateRenderer

class AddEditUserHandler(
    val renderer: TemplateRenderer,
    val key: RequestContextLens<String>,
    var users: UsersStorage,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val roles = listOf("admin", "viewer", "theaterWorker")
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

        val form = request.form() // данные из формы от пользователя
        val newLogin = form.findSingle("login") ?: "" // новый логин пользователя
        val role = form.findSingle("role") ?: "" // роль пользователя
        val userLogin = request.query("login") ?: "" // логин пользователя
        val theater = form.findSingle("theater") ?: "" // название театра для работника театра
        // словарь на случай не корретных данных
        val dataForm =
            mapOf(
                "login" to newLogin,
                "role" to role,
            )
        // словарь с возможными ошибками в полях
        val errorData =
            mutableMapOf(
                "login" to "",
                "role" to "",
            )
        var errorFlag = false // наличие ошибки
        if (users.checkLogin(newLogin) && userLogin != newLogin) {
            errorData["login"] = "Пользователь с таким логином уже сущетсвует"
            errorFlag = true
        }
        if (newLogin == "unauthorized" || newLogin == "") {
            errorData["login"] = "Введен не корректный логин"
        }
        if (role !in roles) {
            errorData["role"] = "Некорректно выбрана роль пользователя"
            errorFlag = true
        }
        if (role != "theaterWorker" && theater != "") {
            errorData["theater"] = "Название театра можно установить только для работника театра"
            errorFlag = true
        }
        if (userLogin == "") {
            return Response(BAD_REQUEST).body(renderer(PageBadRequestVM("Данные не переданы", userName, userRole)))
        }
        if (errorFlag) return Response(BAD_REQUEST).body(renderer(NewUserVM(dataForm, errorData, userName, userRole)))
        val indexUser = users.getUsers().indexOf(users.getUser(userLogin))
        users.getUsers()[indexUser].setALogin(newLogin) // меняем логин
        users.getUsers()[indexUser].setAPermission(role) // меняем права
        if (theater != "") {
            users.getUsers()[indexUser].setTheaterName(theater) // меняем права
        }
        return Response(FOUND).header("Location", "/users")
    }
}
