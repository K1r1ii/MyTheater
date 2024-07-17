package application.data

import java.time.LocalDate

data class Showing(
    val id: Int,
    val date: LocalDate,
    val theaterName: String,
    val hall: String,
    val performId: Int,
)
