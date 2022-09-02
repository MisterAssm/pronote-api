import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import java.util.*

plugins {
    `maven-publish`
    signing
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.github.johnrengelman.shadow")
}

group = "io.github.misterassm"
version = "0.3.1"

ext["signing.keyId"] = null
ext["signing.password"] = null
ext["signing.secretKeyRingFile"] = null
ext["ossrhUsername"] = null
ext["ossrhPassword"] = null

with(project.rootProject.file("local.properties")) {
    if (exists()) {
        reader().use {
            Properties().apply { load(it) }
        }.onEach { (name, value) ->
            ext[name.toString()] = value
        }
    }
}

fun getExtraString(name: String) = ext[name]?.toString()

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        withJava()

        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
            kotlinOptions.freeCompilerArgs = listOf(
                "-opt-in=kotlin.RequiresOptIn",
                "-Xjsr305=strict"
            )
        }

        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    js(IR) {
        compilations.all {
            compileKotlinTask.kotlinOptions.freeCompilerArgs += listOf("-Xerror-tolerance-policy=SEMANTIC")
        }
    }

    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }


    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(KotlinX.serialization.json)
                implementation(KotlinX.datetime)

                compileOnly(Ktor.client.core)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(Ktor.client.okHttp)
            }
        }
        val jvmTest by getting
        val jsMain by getting
        val jsTest by getting
        val nativeMain by getting
        val nativeTest by getting
    }

    val publicationsFromMainHost = listOf(jvm(), js()).map { it.name } + "kotlinMultiplatform"

    publishing {
        publications {
            matching { it.name in publicationsFromMainHost }.all {
                val targetPublication = this@all
                tasks.withType<AbstractPublishToMaven>()
                    .matching { it.publication == targetPublication }
                    .configureEach { onlyIf { findProperty("isMainHost") == "true" } }
            }
        }
    }
}

fun registerShadowJar(targetName: String) {
    kotlin.targets.named<KotlinJvmTarget>(targetName) {
        compilations.named("main") {
            tasks {
                val shadowJar = register<ShadowJar>("${targetName}ShadowJar") {
                    group = "build"
                    from(output)
                    configurations = listOf(runtimeDependencyFiles)
                    archiveAppendix.set(targetName)
                    archiveClassifier.set("all")
                    mergeServiceFiles()
                }
                getByName("${targetName}Jar") {
                    finalizedBy(shadowJar)
                }
            }
        }
    }
}

registerShadowJar("jvm")

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

val jvmShadowJar by tasks.named("jvmShadowJar")

publishing {
    // Configure maven central repository
    repositories {
        maven {
            name = "sonatype"
            setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = getExtraString("ossrhUsername")
                password = getExtraString("ossrhPassword")
            }
        }
    }

    // Configure all publications
    publications.withType<MavenPublication> {

        artifact(jvmShadowJar)
        artifact(javadocJar.get())

        // Provide artifacts information requited by Maven Central
        pom {
            name.set("Kronote")
            description.set("Library to easily retrieve information from a Pronote server (Index-Education) for JVM/JS/Native")
            url.set("https://github.com/MisterAssm/pronote-api")

            licenses {
                license {
                    name.set("MIT")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }
            developers {
                developer {
                    id.set("MisterAssm")
                    name.set("Assim ZEMOUCHI")
                    email.set("assim.zpr@gmail.com")
                }
            }
            scm {
                url.set("https://github.com/MisterAssm/pronote-api")
            }

        }
    }
}

signing {
    sign(publishing.publications)
}
