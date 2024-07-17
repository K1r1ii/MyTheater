package application.web.models.performModels

import application.data.Page
import org.http4k.template.ViewModel

class PerformancesVM(
    val performsPage: Page,
    val params: Map<String, String>,
    val userName: String,
    val userRole: String,
) : ViewModel
