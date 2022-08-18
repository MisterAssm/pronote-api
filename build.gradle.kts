import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `maven-publish`
    kotlin("js")
    kotlin("plugin.serialization")
    id("com.github.johnrengelman.shadow")
}

group = "fr.misterassm.kronote"

repositories {
    mavenCentral()
}

dependencies {
    implementation(Kotlin.stdlib.js)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0-RC")
    implementation("com.soywiz.korlibs.krypto:krypto-js:3.0.0")
    implementation("com.ionspin.kotlin:bignum:0.3.7")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
    implementation("io.ktor:ktor-client-core-js:2.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
}

kotlin {
    js(IR) {
        compilations.all {
            compileKotlinTask.kotlinOptions.freeCompilerArgs += listOf("-Xerror-tolerance-policy=SEMANTIC")
        }

        nodejs { }
//        useCommonJs()
        binaries.executable()
    }
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_1_8.toString()
            freeCompilerArgs = listOf(
                "-module-name",
                project.path.replace(":", ""),
                "-Xopt-in=kotlin.RequiresOptIn",
                "-Xjsr305=strict"
            )
        }
    }

    val shadowJar = withType<ShadowJar> {
        from(sourceSets["main"].output)
        archiveFileName.set("${rootProject.name}.${extension}")
    }

    build {
        dependsOn(shadowJar)
    }
}
