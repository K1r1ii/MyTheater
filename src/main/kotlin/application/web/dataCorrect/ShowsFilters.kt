package application.web.dataCorrect

import application.data.Showing

data class ShowsFilters(
    val shows: List<Showing>,
    val paramsMap: Map<String, String>,
)
