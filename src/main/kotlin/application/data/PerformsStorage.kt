package application.data

import application.isStringLocalDate
import application.web.dataCorrect.PerformsFilters
import java.time.LocalDate

class PerformsStorage(
    private val performs: MutableList<Performance> = mutableListOf(),
) {
    fun getPerforms(): MutableList<Performance> {
        return performs
    }

    fun addPerform(newPerform: Performance) {
        performs.add(newPerform)
    }

    fun deletePerform(performId: Int): Int {
        performs.remove(performs.find { it.id == performId })
        return 0
    }

    fun getPerform(id: Int): Performance? {
        return performs.find { it.id == id }
    }

    fun performsFilters(
        firstDateStr: String,
        secondDateStr: String,
        minDuration: Int,
        maxDuration: Int,
    ): PerformsFilters {
        var filterPerform: List<Performance> // список отфильтрованных данных
        // считанные параметры
        val firstDate: LocalDate
        val secondDate: LocalDate
        // проверка параметров
        if (isStringLocalDate(firstDateStr) && isStringLocalDate(secondDateStr)) {
            firstDate = LocalDate.parse(firstDateStr)
            secondDate = LocalDate.parse(secondDateStr)
        } else {
            firstDate = (performs.minBy { it.performPremDate }).performPremDate
            secondDate = (performs.maxBy { it.performPremDate }).performPremDate
        }
        filterPerform =
            performs
                .filter { x -> x.performTime in minDuration..maxDuration }
                .filter { x -> x.performPremDate in firstDate..secondDate }
        filterPerform = filterPerform.sortedBy { it.performName } // сортировка по id по убыванию
        val paramsMap: Map<String, String> =
            mapOf(
                "minTime" to minDuration.toString(),
                "maxTime" to maxDuration.toString(),
                "firstDate" to firstDate.toString(),
                "secondDate" to secondDate.toString(),
            )
        return PerformsFilters(
            filterPerform,
            paramsMap,
        )
    }
}
