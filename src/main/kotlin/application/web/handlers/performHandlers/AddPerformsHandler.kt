package application.web.handlers.performHandlers

import application.data.Performance
import application.data.PerformsStorage
import application.data.Role
import application.data.UsersStorage
import application.isStringLocalDate
import application.web.models.performModels.NewPerformsVM
import application.web.models.userModels.UnauthorizedPageVM
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.FOUND
import org.http4k.core.Status.Companion.UNAUTHORIZED
import org.http4k.core.body.form
import org.http4k.core.findSingle
import org.http4k.lens.RequestContextLens
import org.http4k.template.TemplateRenderer
import java.time.LocalDate

class AddPerformsHandler(
    val renderer: TemplateRenderer,
    val performs: PerformsStorage,
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
            return Response(UNAUTHORIZED).body(renderer(UnauthorizedPageVM()))
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
        val performPremDate = LocalDate.parse(performPremDateString) // преобразуем к типа дата дату премьеры
        if (performId != null && performTime != null) {
            val index = performs.getPerforms().indexOf(performs.getPerform(performId))
            performs.getPerforms()[index] =
                Performance(
                    performId,
                    performName,
                    performTime.toInt(),
                    performPremDate,
                    actorsClassList,
                )
            return Response(FOUND).header("Location", "$performId")
        }
        val newId: Int = if (performs.getPerforms().isNotEmpty()) (performs.getPerforms().maxBy { it.id }).id + 1 else 0
        if (performTime != null) {
            performs.addPerform(
                Performance(
                    newId,
                    performName,
                    performTime.toInt(),
                    performPremDate,
                    actorsClassList,
                ),
            )
        }
        return Response(FOUND).header("Location", "$newId")
    }
}
