# Kronote
Library to easily retrieve information from a Pronote (Index-Education) server for JVM 🚀

## NOTICE ☣

This API is absolutely not stable at the moment, but any contribution is a pleasure to take! For the moment this API is still in development stage, only the schedule retrieval is currently functional.

## Try Kronote ( Not recommended at this time )

Retrieve the first course of the week

Kotlin:
```kotlin
fun main() {

    val kronoteUser = KronoteImpl(
        "demonstration",
        "pronotevs",
        "https://demo.index-education.net/pronote/eleve.html?login=true"
    ).apply { connection() }

    kronoteUser.retrieveTimetable()
        .courseList.minByOrNull { it.date }!!.let {
            println("""
                I start this week with a ${it.subject.name} class
                on ${it.date.format(DateTimeFormatter.ofPattern("dd'/'MM 'at' HH':'mm a" ))}
                in room ${it.room?.name ?: "UNKNOWN"}
            """.trimIndent())
        }

}
```

Java:
```java
public class Main {

    public static void main(String[] args) {

        KronoteImpl kronoteUser = new KronoteImpl("demonstration", "pronotevs", "https://demo.index-education.net/pronote/eleve.html?login=true");
        kronoteUser.connection();
        kronoteUser.retrieveTimetable(null)
                .getCourseList()
                .stream()
                .min(Comparator.comparing(Course::getDate))
                .ifPresent(course -> System.out.printf("I start this week with a %s class\n" +
                                "on %s\n" +
                                "in room %s", course.getSubject().getName(),
                        course.getDate().format(DateTimeFormatter.ofPattern("dd'/'MM 'at' HH':'mm a")),
                        Objects.requireNonNull(course.getRoom()).getName()));

    }

}
```
