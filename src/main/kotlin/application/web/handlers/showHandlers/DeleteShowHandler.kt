package application.web.handlers.showHandlers

import application.data.PerformsStorage
import application.data.ShowsStorage
import application.data.TicketsStorage
import application.data.UsersStorage
import application.isStringLocalDate
import application.web.models.errorsModels.PageBadRequestVM
import application.web.models.errorsModels.PageNotFoundVM
import application.web.models.showModels.NewShowVM
import application.web.models.userModels.UnauthorizedPageVM
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.FOUND
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.body.form
import org.http4k.core.findSingle
import org.http4k.lens.RequestContextLens
import org.http4k.routing.path
import org.http4k.template.TemplateRenderer

class DeleteShowHandler(
    val renderer: TemplateRenderer,
    val performs: PerformsStorage,
    val shows: ShowsStorage,
    val tickets: TicketsStorage,
    val key: RequestContextLens<String>,
    val users: UsersStorage,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val user = users.getUser(key(request))
        var userName = ""
        var userRole = ""
        if (user != null) {
            userName = user.login
            userRole = user.permission
        }
        // проверка прав пользователя
        if (userRole != "theaterWorker" && userRole != "admin") {
            return Response(Status.UNAUTHORIZED).body(renderer(UnauthorizedPageVM()))
        }
        val performId =
            request.path("performId")?.toIntOrNull()
                ?: return Response(NOT_FOUND).body(renderer(PageNotFoundVM("Не корректный идентифиактор спектакля", userName, userRole)))
        if (performId !in performs.getPerforms().map { it.id }) {
            return Response(NOT_FOUND).body(renderer(PageNotFoundVM("Не корректный идентифиактор спектакля", userName, userRole)))
        }
        val showId = request.query("showId")?.toIntOrNull() // id редактируемого показа
        if (showId != null) {
            val show = shows.getShow(showId)
            if (user != null && show != null) {
                // проверка на то, что работник может редактировать данный театр
                if (user.theater != show.theaterName && userRole != "admin") {
                    return Response(Status.UNAUTHORIZED).body(renderer(UnauthorizedPageVM()))
                }
            }
        }
        val form = request.form() // данные из формы от пользователя
        val theaterName = form.findSingle("theaterName") ?: "" // название театра
        val hallName = form.findSingle("hallName") ?: "" // название зала
        val dateString = form.findSingle("date") ?: "" // дата показа
        // словарь на случай не корретных данных
        val dataForm =
            mapOf(
                "theaterName" to theaterName,
                "hallName" to hallName,
                "date" to dateString,
            )
        // словарь с возможными ошибками в полях
        val errorData =
            mutableMapOf(
                "theaterName" to "",
                "hallName" to "",
                "date" to "",
            )
        var errorFlag = false // наличие ошибки
        if (theaterName == "") {
            errorData["theaterName"] = "Название театра является обязательным параметром"
            errorFlag = true
        }
        if (hallName == "") {
            errorData["hallName"] = "Название зала является обязательным параметром"
            errorFlag = true
        }
        if (!isStringLocalDate(dateString)) {
            errorData["date"] = "Дата показа является обязательным параметром вида ДД.ММ.ГГГГ"
            errorFlag = true
        }
        if (errorFlag) return Response(BAD_REQUEST).body(renderer(NewShowVM(dataForm, errorData, userName, userRole)))
        return if (showId != null) {
            val deleteTickets = tickets.getTickets().filter { it.showId == showId }
            shows.deleteShow(showId) // удаление показа
            // удаление всех билетов
            for (delTick in deleteTickets) {
                tickets.deleteTicket(delTick.id)
            }
            Response(FOUND).header("Location", "/performances/$performId")
        } else {
            Response(BAD_REQUEST).body(renderer(PageBadRequestVM("Не найден объект удаления", userName, userRole)))
        }
    }
}
