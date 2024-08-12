
pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven("https://artifacts.wolfyscript.com/artifactory/gradle-dev")
        maven("https://plugins.gradle.org/m2/")
    }
    plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

}


rootProject.name = "Realmcraft"
