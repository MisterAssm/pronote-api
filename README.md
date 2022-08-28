<p align="center">
    <a href="https://kotlinlang.org/docs/components-stability.html" target="_blank" rel="noopener"><img src="https://kotl.in/badges/alpha.svg" alt="Kotlin Alpha" /></a>
    <a href="https://search.maven.org/search?q=g:io.github.misterassm" target="_blank" rel="noopener"><img src="https://img.shields.io/maven-central/v/io.github.misterassm/kronote" alt="Maven Central" /></a>
    <a href="https://www.codefactor.io/repository/github/misterassm/pronote-api/overview/development"><img src="https://www.codefactor.io/repository/github/misterassm/pronote-api/badge/development" alt="CodeFactor" /></a>
</p>

**Kronote** est une librairie open source pour JVM (Java, Kotlin, Scala) pour le service d'administration des étudiants Pronote. Cette librairie n'utilise pas le service web [HYPERPLANNING](https://www.index-education.com/fr/hyperplanning-info196-service-web.php) mis en place par [Index-Education](https://www.index-education.com/). L'objectif est de permettre à chacun d'apprendre la programmation informatique à partir de nos outils étudiant du quotidien et de permettre de créer des outils spécifique aux étudiants

**Plus d'informations quant aux objectifs de cette librairie dans [l'issue #3](https://github.com/MisterAssm/pronote-api/issues/3)**

> ⚠️ Bien que les définitions de la librairie soient considérées comme stables, veuillez garder à l'esprit que Kronote n'est pas affilié à Index-Education, propriétaire de Pronote, que Kronote fonctionne comme un navigateur web et **récupère les mêmes données que sur un navigateur classique** par conséquent, une rétrocompatibilité totale avec les anciennes versions de pronote n'est pas garantie.

## Languages supportés
| Platforme | Status |
| -------- | ------ |
| JVM (Java, Kotlin, Scala) | 🚧 Développement |
| Javascript/Typescript     | ⛔ Non disponible |
| Natif (C/Go)              | ⛔ Non disponible |

## Installation

#### Gradle Kotlin Script (build.gradle.kts)
```kts
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.misterassm:kronote-jvm:0.2.1")
}
```

<details><summary>Gradle groovy (build.gradle)</summary><p>

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation 'io.github.misterassm:kronote-jvm:0.2.1'
}
```
</p></details>

<details><summary>Maven (pom.xml)</summary><p>

```xml
<dependencies>
    <dependency>
        <groupId>io.github.misterassm</groupId>
        <artifactId>kronote-jvm</artifactId>
        <version>0.2.1</version>
    </dependency>
</dependencies>
```
</p></details>


## Vue d'ensemble

<details><summary>Connection à un compte <strong>Pronote Étudiant</strong> via URL et CAS (<strong>bientôt</strong>)</summary><p>

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

<details><summary>Récupérer <strong>l'emploi du temps</strong> d'une semaine/date spécifique</summary><p>

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