package application.data

data class DataJson(
    val tickets: TicketsStorage,
    val performs: PerformsStorage,
    val shows: ShowsStorage,
)
