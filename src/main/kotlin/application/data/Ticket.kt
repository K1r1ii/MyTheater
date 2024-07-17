package application.data

import java.time.LocalDate

// класс для хранения информации о билетах (наз.театра, зала, зр.зоны, ном.ряда, места, цена, инф.о спект, дата выпуска)
data class Ticket(
    val id: Int,
    val visualArea: String,
    val rowNum: Int,
    val placeNum: Int,
    val price: Int,
    val ticketDate: LocalDate,
    var bought: Boolean,
    val showId: Int,
    var buyer: String = "",
) {
    fun setBought() {
        // метод для изменения статуса билета  (true - в продаже, false - куплен)
        bought = !bought
    }

    fun setBuyerLogin(buyerLogin: String) {
        // метод для установки логина покупателя билета
        buyer = buyerLogin
    }
}
