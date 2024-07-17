package application.web.models.performModels

import org.http4k.template.ViewModel

class ConfirmDeletePerformsVM(
    val dataForm: Map<String, String>,
    val errors: Map<String, String>,
    val userName: String,
    val userRole: String,
) : ViewModel
