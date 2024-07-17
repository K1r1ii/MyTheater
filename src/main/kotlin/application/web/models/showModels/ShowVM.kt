package application.web.models.showModels

import application.data.Page
import application.data.Performance
import org.http4k.template.ViewModel

class ShowVM(
    val showPage: Page,
    val params: Map<String, String>,
    val perform: Performance,
    val userName: String,
    val userRole: String,
    val userTheater: String,
) : ViewModel
