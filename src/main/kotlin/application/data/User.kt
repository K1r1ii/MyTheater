package application.data

class User(
    var permission: String = "viewer",
    var login: String,
    val hashPassword: String,
    var theater: String = "",
) {
    fun setALogin(newLogin: String) {
        // смена логина
        login = newLogin
    }

    fun setAPermission(newPermission: String) {
        // смена прав
        permission = newPermission
    }

    fun setTheaterName(newTheater: String) {
        // смена названия театра
        theater = newTheater
    }
}
