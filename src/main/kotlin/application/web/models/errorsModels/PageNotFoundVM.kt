package application.web.models.errorsModels

import org.http4k.template.ViewModel

class PageNotFoundVM(val errorMessage: String, val userName: String, val userRole: String) : ViewModel
