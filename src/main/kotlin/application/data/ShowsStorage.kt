package application.data

import application.isStringLocalDate
import application.web.dataCorrect.ShowsFilters
import java.time.LocalDate

class ShowsStorage(
    private val shows: MutableList<Showing> = mutableListOf(),
) {
    fun getShows(): MutableList<Showing> {
        return shows
    }

    fun addShow(newShow: Showing) {
        shows.add(newShow)
    }

    fun deleteShow(showId: Int): Int {
        shows.remove(shows.find { it.id == showId })
        return 0
    }

    fun getShow(id: Int): Showing? {
        return shows.find { it.id == id }
    }

    fun showsFilters(
        firstDateStr: String,
        secondDateStr: String,
        performId: Int,
    ): ShowsFilters {
        var filterShows: List<Showing>
        val firstDate: LocalDate
        val secondDate: LocalDate
        // проверка параметров
        if (isStringLocalDate(firstDateStr) && isStringLocalDate(secondDateStr)) {
            firstDate = LocalDate.parse(firstDateStr)
            secondDate = LocalDate.parse(secondDateStr)
        } else {
            firstDate = (shows.filter { it.performId == performId }.minBy { it.date }).date
            secondDate = (shows.filter { it.performId == performId }.maxBy { it.date }).date
        }
        filterShows =
            shows
                .filter { x -> x.performId == performId }
                .filter { x -> x.date in firstDate..secondDate }
        filterShows = filterShows.sortedByDescending { it.date }
        val paramsMap: Map<String, String> =
            mapOf(
                "firstDate" to firstDate.toString(),
                "secondDate" to secondDate.toString(),
            )
        return ShowsFilters(
            filterShows,
            paramsMap,
        )
    }
}
