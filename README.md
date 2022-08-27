**Kronote** est une librairie open source pour JVM (Java, Kotlin, Scala) pour le service d'administration des étudiants Pronote. Cette librairie n'utilise pas le service web [HYPERPLANNING](https://www.index-education.com/fr/hyperplanning-info196-service-web.php) mis en place par [Index-Education](https://www.index-education.com/). L'objectif est de permettre à chacun d'apprendre la programmation informatique à partir de nos outils étudiant du quotidien et de permettre de créer des outils spécifique aux étudiants

**Plus d'informations quant aux objectifs de cette librairie dans [l'issue #3](https://github.com/MisterAssm/pronote-api/issues/3)**

> ⚠️ Bien que les définitions de la librairie soient considérées comme stables, veuillez garder à l'esprit que Kronote n'est pas affilié à Index-Education, propriétaire de Pronote, que Kronote fonctionne comme un navigateur web et **récupère les mêmes données que sur un navigateur classique** par conséquent, une rétrocompatibilité totale avec les anciennes versions de pronote n'est pas garantie.

## Languages supportés
| Platforme | Status |
| -------- | ------ |
| JVM (Java, Kotlin, Scala) | 🚧 Développement |
| Javascript/Typescript     | ⛔ Non disponible |
| Natif (C/Go)              | ⛔ Non disponible |

## Vue d'ensemble

<details><summary>Connection à un compte **Pronote Étudiant** via URL et CAS (bientôt)</summary><p>

#### Kotlin

```kotlin
    val kronote = connectKronote { // or just ``kronote`` to create instance without connect to Pronote
        username = "demonstration"
        password = "pronotevs"
        indexUrl = "https://demo.index-education.net/pronote/eleve.html?login=true"
        autoReconnect = true // Default: false
    }.getOrThrow() // or Result#onSuccess / Result#onFailure
```
</p></details>

<details><summary>Récupérer **l'emploi du temps** d'une semaine/date spécifique</summary><p>

#### Kotlin

```kotlin
// Récupérer l'emploi du temps de la semaine actuelle
val timetable = kronote.retrieveTimetable()

// Récupérer l'emploi du temps d'une semaine spécifique
val timetable = kronote.retrieveTimetable(5) // Emploi du temps de la semaine n°5

// Récupérer l'emploi du temps d'un jour en particulier
val timetable = kronote.retrieveTimetable(Localdate(2022, Month.SEPTEMBER, 1)) // Emploi du temps du 1er septembre 2022

// Récupérer le nom de chaque matière et l'imprimer dans la console
timetable.courseList.forEach { println(it.subject) }
```
</p></details>



## Contributing

Kronote est un projet libre et open source sous licence [MIT License](LICENSE.md).

Vous pouvez contribuer à poursuivre son développement en :

- [Proposer de nouvelles fonctionnalités et signaler les problèmes](https://github.com/MisterAssm/pronote-api/issues)