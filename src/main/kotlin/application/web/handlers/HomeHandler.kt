package application.web.handlers

import application.data.UsersStorage
import application.web.models.HomeVM
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.RequestContextLens
import org.http4k.template.TemplateRenderer
import java.time.LocalDateTime

class HomeHandler(
    val renderer: TemplateRenderer,
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
        val viewModel = HomeVM(LocalDateTime.now(), userName, userRole)
        val htmlDocument = renderer(viewModel)
        return Response(Status.OK).body(htmlDocument)
    }
}
