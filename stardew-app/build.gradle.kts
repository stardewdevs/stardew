plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "io.stardew"
    compileSdk = 35

    defaultConfig {
        applicationId = "io.stardew"
        minSdk = 26
        targetSdk = 35
        versionName = getVersionName()
        versionCode = getVersionCode()
    }

    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("KEYSTORE_PATH") ?: "keystore.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
            keyAlias = System.getenv("KEY_ALIAS") ?: ""
            keyPassword = System.getenv("KEY_PASSWORD") ?: ""
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        create("beta") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
        }
        debug {
            // Debug builds use default debug keystore
        }
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

fun getVersionName(): String {
    val buildType = System.getenv("BUILD_TYPE") ?: "debug"
    return when (buildType) {
        "release" -> "0.1"
        "beta" -> "0.1.10-beta"
        else -> "0.1.110-debug"
    }
}

fun getVersionCode(): Int {
    val buildType = System.getenv("BUILD_TYPE") ?: "debug"
    return when (buildType) {
        "release" -> 1
        "beta" -> 110
        else -> 1110
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
