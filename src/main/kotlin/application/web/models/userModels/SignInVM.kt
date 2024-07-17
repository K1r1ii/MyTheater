package application.web.models.userModels

import org.http4k.template.ViewModel

class SignInVM(val dataForm: Map<String, String>, val errors: Map<String, String>) : ViewModel
