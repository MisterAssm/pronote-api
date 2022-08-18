@file:Suppress("UnstableApiUsage")

import de.fayard.refreshVersions.RefreshVersionsExtension

plugins {
    // https://jmfayard.github.io/refreshVersions
    id("de.fayard.refreshVersions") version "0.40.1"
}

refreshVersions(RefreshVersionsExtension::enableBuildSrcLibs)

rootProject.name = "Kronote"

