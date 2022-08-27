import fr.misterassm.kronote.internal.KronoteImpl
import java.time.format.DateTimeFormatter

suspend fun main() {

    val kronoteUser = KronoteImpl(
        "demonstration",
        "pronotevs",
        "https://demo.index-education.net/pronote/eleve.html?login=true"
    ).apply { connection() }

    kronoteUser.retrieveTimetable(5)
        .courseList.forEach {
            println(it.subject.name)
        }

}