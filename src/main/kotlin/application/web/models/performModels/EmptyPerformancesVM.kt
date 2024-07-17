package application.web.models.performModels

import org.http4k.template.ViewModel

class EmptyPerformancesVM(
    val params: Map<String, String>,
    val userName: String,
    val userRole: String,
) : ViewModel
