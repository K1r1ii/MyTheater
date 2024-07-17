package application.web.models.showModels

import org.http4k.template.ViewModel

class ConfirmDeleteShowVM(
    val dataForm: Map<String, String>,
    val errors: Map<String, String>,
    val userName: String,
    val userRole: String,
) : ViewModel
