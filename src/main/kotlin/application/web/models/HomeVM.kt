package application.web.models

import org.http4k.template.ViewModel
import java.time.LocalDateTime

class HomeVM(val time: LocalDateTime, val userName: String, val userRole: String) : ViewModel
