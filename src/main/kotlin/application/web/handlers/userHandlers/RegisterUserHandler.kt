package application.web.handlers.userHandlers

import application.config.AppConfig
import application.data.Password
import application.data.User
import application.data.UsersStorage
import application.web.models.errorsModels.PageBadRequestVM
import application.web.models.userModels.SignUpVM
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.body.form
import org.http4k.core.findSingle
import org.http4k.lens.RequestContextLens
import org.http4k.template.TemplateRenderer

class RegisterUserHandler(
    val renderer: TemplateRenderer,
    val users: UsersStorage,
    val key: RequestContextLens<String>,
    val config: AppConfig,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val user = users.getUser(key(request))
        if (user != null && user.permission != "admin") {
            return Response(
                Status.BAD_REQUEST,
            ).body(renderer(PageBadRequestVM("Данная страница не доступна для авторизованных пользователей", user.login, user.permission)))
        }
        val form = request.form() // данные из формы от пользователя
        val userLogin = form.findSingle("login") ?: "" // логин пользователя
        val userPassword = form.findSingle("password") ?: "" // пароль пользователя
        val userPasswordConfirm = form.findSingle("passwordConfirm") ?: "" // подтверждение пароля
        val dataForm =
            mapOf(
                "login" to userLogin,
                "password" to userPassword,
                "passwordConfirm" to userPasswordConfirm,
            )
        val errorData =
            mutableMapOf(
                "login" to "",
                "password" to "",
                "passwordConfirm" to "",
            )
        var errorFlag = false
        // проверка на существование введенного логина
        if (users.checkLogin(userLogin)) {
            errorData["login"] = "Пользователь с таким логином уже сущетсвует"
            errorFlag = true
        }
        if (userLogin == "unauthorized" || userLogin == "") {
            errorData["login"] = "Введен не корректный логин"
        }
        if (!Password.checkPassword(userPassword)) {
            errorData["password"] = "Пароль должен иметь минимум 8 символов, из них минимум одна цифра и одна заглавная буква"
            errorFlag = true
        }
        if (userPassword != userPasswordConfirm) {
            errorData["passwordConfirm"] = "Пароли не совпадают"
            errorFlag = true
        }

        if (errorFlag) {
            if (user != null) {
                return Response(Status.BAD_REQUEST).body(
                    renderer(
                        SignUpVM(
                            dataForm,
                            errorData,
                            user.login,
                            user.permission,
                        ),
                    ),
                )
            } else {
                return Response(Status.BAD_REQUEST).body(
                    renderer(
                        SignUpVM(
                            dataForm,
                            errorData,
                            "",
                            "",
                        ),
                    ),
                )
            }
        }

        val hashPassword: String = Password.getHashPassword(userPassword, config.passwordSalt) // хэширование пароля
        val newUser = User(login = userLogin, hashPassword = hashPassword) // создание нового пользователя
        users.addUser(newUser) // добавление нового пользователя
        return if (user != null && user.permission == "admin") {
            Response(Status.FOUND).header("Location", "users")
        } else {
            Response(Status.FOUND).header("Location", "signIn")
        }
    }
}
