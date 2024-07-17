package application.web.models.ticketModels

import application.data.Performance
import application.data.Showing
import org.http4k.template.ViewModel

class EmptyTicketsVM(
    val showData: Showing,
    val performData: Performance,
    val params: Map<String, String>,
    val userName: String,
    val userRole: String,
) : ViewModel
