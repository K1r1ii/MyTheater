package application.data

import org.http4k.core.Request

data class Page(
    val elements: List<Any>,
    val curPage: Int,
    val totalPages: Int,
    val request: Request,
    val message: String,
)
