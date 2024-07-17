package application.web.models.ticketModels

import org.http4k.template.ViewModel

class NewTicketVM(
    val dataForm: Map<String, String>,
    val errors: Map<String, String>,
    val userName: String,
    val userRole: String,
) : ViewModel
