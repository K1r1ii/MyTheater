package application.web.handlers.userHandlers

import application.config.AppConfig
import application.data.Password
import application.data.UsersStorage
import application.web.models.userModels.SignInVM
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.body.form
import org.http4k.core.cookie.Cookie
import org.http4k.core.cookie.cookie
import org.http4k.core.findSingle
import org.http4k.lens.RequestContextLens
import org.http4k.template.TemplateRenderer

class AuthUserHandler(
    val renderer: TemplateRenderer,
    val users: UsersStorage,
    val key: RequestContextLens<String>,
    val config: AppConfig,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val form = request.form() // данные из формы от пользователя
        val userLogin = form.findSingle("login") ?: "" // логин пользователя
        val userPassword = form.findSingle("password") ?: "" // пароль пользователя
        val dataForm =
            mapOf(
                "login" to userLogin,
                "password" to userPassword,
            )
        val errorData =
            mutableMapOf(
                "login" to "",
                "password" to "",
            )
        var errorFlag = false

        val user = users.getUsers().find { it.login == userLogin }
        if (user != null) {
            if (user.hashPassword != Password.getHashPassword(userPassword, config.passwordSalt)) {
                errorData["password"] = "Введен не верный пароль"
                errorFlag = true
            }
        } else {
            errorData["login"] = "Пользователь с таким логином не найден"
            errorFlag = true
        }

        if (errorFlag) {
            return Response(Status.BAD_REQUEST).body(renderer(SignInVM(dataForm, errorData)))
        }

        val token = Password.getJWTToken(userLogin, config.jwtSalt) // создаем для пользователя токен
        val cookie = Cookie("auth", token, path = "/", httpOnly = true, maxAge = 86400) // создаем куки
        return Response(Status.FOUND).header("Location", "/").cookie(cookie)
    }
}
