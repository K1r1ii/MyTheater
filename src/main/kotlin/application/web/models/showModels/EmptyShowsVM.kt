package application.web.models.showModels

import application.data.Performance
import org.http4k.template.ViewModel

class EmptyShowsVM(
    val params: Map<String, String>,
    val perform: Performance,
    val userName: String,
    val userRole: String,
) : ViewModel
