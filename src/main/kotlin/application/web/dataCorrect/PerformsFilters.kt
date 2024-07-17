package application.web.dataCorrect

import application.data.Performance

data class PerformsFilters(
    val performs: List<Performance>,
    val paramsMap: Map<String, String>,
)
