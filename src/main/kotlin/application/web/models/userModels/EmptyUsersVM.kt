package application.web.models.userModels

import org.http4k.template.ViewModel

class EmptyUsersVM(
    val userName: String,
    val userRole: String,
) : ViewModel
