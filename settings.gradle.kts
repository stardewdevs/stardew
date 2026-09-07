pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Stardew"
include(":stardew-app")

//This later
// include(":stardew-emulator")
// include(":stardew-view")
// include(":stardew-mpkg")
// include(":stardew-shared")
