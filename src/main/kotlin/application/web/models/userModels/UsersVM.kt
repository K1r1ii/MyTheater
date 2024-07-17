package application.web.models.userModels

import application.data.Page
import org.http4k.template.ViewModel

class UsersVM(
    val usersPage: Page,
    val userName: String,
    val userRole: String,
) : ViewModel
