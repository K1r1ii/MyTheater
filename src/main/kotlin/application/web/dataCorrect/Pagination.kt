package application.web.dataCorrect

import application.data.Page
import org.http4k.core.Request

fun pagination(
    request: Request,
    elements: List<Any>,
): Page {
    var message = "OK"
    val listForPage: List<Any> // список данных для одной страницы
    var page = request.query("page")?.toIntOrNull() ?: 1 // узнаем номер страницы
    val itemPerPage = 50 // кол-во элементов на странице
    var totalPages = (elements.size + itemPerPage - 1) / itemPerPage // кол-во страниц
    if (totalPages == 0) {
        totalPages = 1
        message = "Нет подходящих элементов"
    }
    if (page > totalPages || page < 1) {
        page = 1
    }
    listForPage =
        elements.subList((page - 1) * itemPerPage, minOf(page * itemPerPage, elements.size))

    return Page(
        listForPage,
        page,
        totalPages,
        request,
        message,
    )
}
