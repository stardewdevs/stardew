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
        beta {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
        }
        debug {
        }
    }

    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            isUniversalApk = true
        }
    }

    applicationVariants.all {
        val variant = this
        variant.outputs.forEach { output ->
            val abi = output.filters?.find { it.filterType == "abi" }?.identifier
            val buildType = variant.buildType.name
            output.versionCodeOverride = getVersionCode()
            output.outputFileName = getApkFileName(abi, buildType)
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
        "release" -> project.properties["RELEASE_VERSION_NAME"] as String
        "beta" -> project.properties["BETA_VERSION_NAME"] as String
        else -> project.properties["DEBUG_VERSION_NAME"] as String
    }
}

fun getVersionCode(): Int {
    val buildType = System.getenv("BUILD_TYPE") ?: "debug"
    return when (buildType) {
        "release" -> (project.properties["RELEASE_VERSION_CODE"] as String).toInt()
        "beta" -> (project.properties["BETA_VERSION_CODE"] as String).toInt()
        else -> (project.properties["DEBUG_VERSION_CODE"] as String).toInt()
    }
}

fun getApkFileName(abi: String?, buildType: String): String {
    val version = getVersionName()
    if (abi == null) {
        return "stardew-universal.apk"
    }
    return when (buildType) {
        "release" -> "stardew-v$version-$buildType-$abi.apk"
        else -> "stardew-v$version-$abi.apk"
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
