package application.web.models.userModels

import org.http4k.template.ViewModel

class ConfirmDeleteUserVM(
    val dataForm: Map<String, String>,
    val errors: Map<String, String>,
    val userName: String,
    val userRole: String,
) : ViewModel
