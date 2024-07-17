package application.web.models.ticketModels

import application.data.Page
import application.data.Performance
import application.data.Showing
import org.http4k.template.ViewModel

class PerformanceVM(
    val ticketsPage: Page,
    val showData: Showing,
    val performData: Performance,
    val params: Map<String, String>,
    val userName: String,
    val userRole: String,
    val userTheater: String,
) : ViewModel
