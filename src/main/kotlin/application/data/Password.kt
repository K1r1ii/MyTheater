package application.data
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.security.MessageDigest
import java.util.Date

class Password {
    companion object {
        fun checkPassword(password: String): Boolean {
            // метод для проверки валидности пароля
            val passwordRegex = """^(?=.*\d)(?=.*[A-Z]).{8,}$""".toRegex() // рег выражения для проверка пароля
            return passwordRegex.matches(password)
        }

        fun getHashPassword(
            password: String,
            saltPassword: String,
        ): String {
            // метод для хэширования пароля
            val bytes = MessageDigest.getInstance("SHA-256").digest((password + saltPassword).toByteArray())
            val stringBuilder = StringBuilder()
            for (byte in bytes) {
                stringBuilder.append(String.format("%02x", byte))
            }
            return stringBuilder.toString()
        }

        fun getJWTToken(
            login: String,
            saltJwt: String,
        ): String {
            // метод для создания токена для конкретного пользователя
            val algorithm = Algorithm.HMAC256(saltJwt)
            return JWT.create()
                .withSubject(login)
                .withExpiresAt(Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000))
                .withIssuer("MyTheater.ru")
                .sign(algorithm)
        }
    }
}
