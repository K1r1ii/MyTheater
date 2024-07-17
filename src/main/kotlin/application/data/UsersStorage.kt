package application.data

import application.config.AppConfig

class UsersStorage(
    private val users: MutableList<User> = mutableListOf(),
    val config: AppConfig,
) {
    // добавление администратора по умолчанию если его нет
    init {
        if (!checkLogin("admin")) {
            addUser(
                User(
                    login = "admin",
                    hashPassword = Password.getHashPassword("admin", config.passwordSalt),
                    permission = "admin",
                ),
            )
        }
    }

    fun getUsers(): MutableList<User> {
        // Метод для получения списка пользователей
        return users
    }

    fun addUser(newUser: User) {
        // метод для добавления нового пользователя
        users.add(newUser)
    }

    fun deleteUser(userLogin: String): Int {
        // метод для удаления пользователя
        users.remove(users.find { it.login == userLogin })
        return 0
    }

    fun checkLogin(login: String): Boolean {
        // метод для проверка на существование пользователя с заданным логином
        val user = users.find { it.login == login }
        return user != null
    }

    fun getUser(login: String): User? {
        // метод для поиска пользователя по его логину
        return users.find { it.login == login }
    }
}
