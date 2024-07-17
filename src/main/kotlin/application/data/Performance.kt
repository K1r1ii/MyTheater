package application.data

import java.time.LocalDate

data class Performance(
    // класс для хранения информации о спектаклях (название, продолжительность, дата, список актеров)
    val id: Int,
    val performName: String,
    val performTime: Int,
    val performPremDate: LocalDate,
    val actors: List<Role>,
)
