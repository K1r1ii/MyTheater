package application.web.handlers.performHandlers

import application.data.PerformsStorage
import application.data.Role
import application.data.ShowsStorage
import application.data.TicketsStorage
import application.data.UsersStorage
import application.isStringLocalDate
import application.web.models.errorsModels.PageBadRequestVM
import application.web.models.performModels.NewPerformsVM
import application.web.models.userModels.UnauthorizedPageVM
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.FOUND
import org.http4k.core.body.form
import org.http4k.core.findSingle
import org.http4k.lens.RequestContextLens
import org.http4k.template.TemplateRenderer

class DeletePerformsHandler(
    val renderer: TemplateRenderer,
    val performs: PerformsStorage,
    private val shows: ShowsStorage,
    private val tickets: TicketsStorage,
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
        val performId = request.query("performId")?.toIntOrNull() // id редактируемого спектакля
        val form = request.form() // данные из формы от пользователя
        val performName = form.findSingle("performName") ?: "" // название спектакля
        val performTime = form.findSingle("performTime")?.toIntOrNull() // продолжительность спектакля
        val performPremDateString = form.findSingle("performPremDate") ?: "" // дата премьеры спекткля
        val actors = form.findSingle("actors") ?: "" // строка со списком актеров
        // словарь на случай не корретных данных
        val dataForm =
            mapOf(
                "performName" to performName,
                "performTime" to performTime.toString(),
                "performPremDate" to performPremDateString,
                "actors" to actors,
            )

        // словарь с возможными ошибками в полях
        val errorData =
            mutableMapOf(
                "performName" to "",
                "performTime" to "",
                "performPremDate" to "",
                "actors" to "",
            )
        var errorFlag = false // наличие ошибки
        if (performName == "") {
            errorData["performName"] = "Название спектакля является обязательным параметром"
            errorFlag = true
        }
        if (performTime == null) {
            errorData["performTime"] = "Продолжительность спектакля является обязательным числовым параметром"
            errorFlag = true
        }
        if (!isStringLocalDate(performPremDateString)) {
            errorData["performPremDate"] = "Дата премьеры спектакля является обязательным параметром вида ДД.ММ.ГГГГ"
            errorFlag = true
        }
        val actorsClassList = mutableListOf<Role>() // будущий список акторов и ролей для отправки в общий список
        try {
            val actorsList = actors.split(",").toMutableList() // список актеров и их ролей разделенный
            if (actorsList.last() == "") {
                actorsList.removeLast()
            }

            // набор списка актеров
            for (actor in actorsList) {
                val actorName = actor.split("-")[0]
                val actorRole = actor.split("-")[1]
                actorsClassList.add(
                    Role(
                        actorName,
                        actorRole,
                    ),
                )
            }
        } catch (e: IndexOutOfBoundsException) {
            errorData["actors"] = "Список актеров обязательный параметр вида: имя - роль. Каждый актер через запятую."
            return Response(BAD_REQUEST).body(renderer(NewPerformsVM(dataForm, errorData, userName, userRole)))
        }
        if (errorFlag) return Response(BAD_REQUEST).body(renderer(NewPerformsVM(dataForm, errorData, userName, userRole)))
        return if (performId != null) {
            val deleteShows = shows.getShows().filter { it.performId == performId } // показы которые надо удалить
            val deleteShowsId = deleteShows.map { it.id }
            val deleteTickets = tickets.getTickets().filter { it.showId in deleteShowsId } // список удаляемых билетов
            val perform = performs.getPerform(performId)
            if (perform == null) {
                Response(BAD_REQUEST).body(renderer(PageBadRequestVM("Не найден объект удаления", userName, userRole)))
            }
            performs.deletePerform(performId) // удаление спектакля
            // удаление всех показов
            for (delShow in deleteShows) {
                shows.deleteShow(delShow.id)
            }
            // удаление всех билетов
            for (delTick in deleteTickets) {
                tickets.deleteTicket(delTick.id)
            }
            Response(FOUND).header("Location", "/performances")
        } else {
            Response(BAD_REQUEST).body(renderer(PageBadRequestVM("Не найден объект удаления", userName, userRole)))
        }
    }
}
