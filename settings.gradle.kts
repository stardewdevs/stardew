rootProject.name = "Stardew"

include(
    ":stardew-app",
    ":stardew-shared",
    // emulator and view are Rust submodules, built separately
)

// If you want to include them as Gradle modules, uncomment:
// includeBuild("submodules/stardew-emulator")
// includeBuild("submodules/stardew-view")
