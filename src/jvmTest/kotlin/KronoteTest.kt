import fr.misterassm.kronote.api.builder.connectKronote
import kotlinx.datetime.LocalDate

suspend fun main() {

    val kronoteUser = connectKronote {
        username = "azemouchi"
        password = "Jemappelessam2013?!+"
        indexUrl = "https://0921555r.index-education.net/pronote/eleve.html?login=true"
    }.getOrThrow()

    val builder = StringBuilder("-----\n")

    kronoteUser.retrieveTimetable(LocalDate(2022, 9, 5)).courseList.forEach { builder.append("${it.subject}\n") }
    builder.append("-----")
    println(builder.toString())

    kronoteUser.disconnect()
    kronoteUser.retrieveTimetable(LocalDate(2022, 9, 5)).courseList.forEach { builder.append("${it.subject}") }
    println(builder.toString())
}