package application.web.handlers.userHandlers

import application.data.UsersStorage
import application.web.models.errorsModels.PageBadRequestVM
import application.web.models.userModels.SignUpVM
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.RequestContextLens
import org.http4k.template.TemplateRenderer
import org.http4k.template.ViewModel

class SignUpHandler(
    val renderer: TemplateRenderer,
    val key: RequestContextLens<String>,
    val users: UsersStorage,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val user = users.getUser(key(request))
        if (user != null && user.permission != "admin") {
            return Response(
                Status.BAD_REQUEST,
            ).body(renderer(PageBadRequestVM("Данная страница не доступна для авторизованных пользователей", user.login, user.permission)))
        }
        val emptyMap = mapOf<String, String>()
        val dataForm = mapOf<String, String>()
        val viewModel: ViewModel =
            if (user != null) {
                SignUpVM(dataForm, emptyMap, user.login, user.permission)
            } else {
                SignUpVM(dataForm, emptyMap, "", "")
            }
        val htmlDocument = renderer(viewModel)
        return Response(Status.OK).body(htmlDocument)
    }
}
