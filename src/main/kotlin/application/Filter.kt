package application

import application.config.AppConfig
import application.data.UsersStorage
import application.web.models.errorsModels.PageBadRequestVM
import application.web.models.errorsModels.PageNotFoundVM
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.cookie.cookie
import org.http4k.core.with
import org.http4k.lens.RequestContextLens
import org.http4k.template.TemplateRenderer

fun errorFilter(
    key: RequestContextLens<String>,
    users: UsersStorage,
    renderer: TemplateRenderer,
) = Filter { next: HttpHandler ->
    { request: Request ->
        val user = users.getUser(key(request))
        var userName = ""
        var userRole = ""
        if (user != null) {
            userName = user.login
            userRole = user.permission
        }
        try {
            val response = next(request)
            // проверка некорректного пути
            if (response.status == Status.NOT_FOUND) {
                response.body(renderer(PageNotFoundVM("Некорректный путь", userName, userRole)))
            } else {
                response
            }
        } catch (e: IllegalArgumentException) {
            Response(Status.BAD_REQUEST).body(renderer(PageBadRequestVM("Переданы некорректные данные", userName, userRole)))
        }
    }
}

// фильтр для считывания куки
fun roleFilter(
    key: RequestContextLens<String>,
    users: UsersStorage,
    config: AppConfig,
) = Filter { next ->
    { request ->
        val token = request.cookie("auth")
        val algorithm: Algorithm = Algorithm.HMAC256(config.jwtSalt)
        val verifier: JWTVerifier = JWT.require(algorithm).build()
        try {
            val decodedJWT: DecodedJWT = verifier.verify(token?.value ?: "")
            val user = users.getUser(decodedJWT.subject)
            if (user == null) {
                next(request.with(key of "Unauthorized"))
            } else {
                next(request.with(key of decodedJWT.subject))
            }
        } catch (e: JWTVerificationException) {
            next(request.with(key of "Unauthorized"))
        }
    }
}
