package application.config

import org.http4k.cloudnative.env.Environment
import org.http4k.cloudnative.env.EnvironmentKey
import org.http4k.lens.int
import org.http4k.lens.nonEmptyString
import java.io.File

data class AppConfig(
    val webPort: Int,
    val jwtSalt: String,
    val passwordSalt: String,
) {
    companion object {
        private val appEnv =
            Environment.from(File("config/app.properties")) overrides
                Environment.JVM_PROPERTIES overrides
                Environment.ENV
        val webPortLens = EnvironmentKey.int().required("web.port")
        val jwtSaltLens = EnvironmentKey.nonEmptyString().required("jwtSalt")
        val passwordSaltLens = EnvironmentKey.nonEmptyString().required("passwordSalt")

        fun getConfig(): AppConfig {
            return AppConfig(
                webPortLens(appEnv),
                jwtSaltLens(appEnv),
                passwordSaltLens(appEnv),
            )
        }
    }
}
