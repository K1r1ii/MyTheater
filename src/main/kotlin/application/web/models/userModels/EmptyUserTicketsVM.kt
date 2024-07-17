package application.web.models.userModels

import org.http4k.template.ViewModel

class EmptyUserTicketsVM(
    val userName: String,
    val userRole: String,
) : ViewModel
