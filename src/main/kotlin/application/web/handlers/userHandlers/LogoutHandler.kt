package application.web.handlers.userHandlers

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.cookie.Cookie
import org.http4k.core.cookie.cookie

class LogoutHandler : HttpHandler {
    override fun invoke(request: Request): Response {
        val emptyCookie = Cookie("auth", "", maxAge = 0)
        return Response(Status.FOUND).header("Location", "/").cookie(emptyCookie)
    }
}
