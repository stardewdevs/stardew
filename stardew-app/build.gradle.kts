plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// VersionName
fun getVersionName(): String {
    val buildType = System.getenv("BUILD_TYPE") ?: "debug"
    val props = project.properties
    return when (buildType) {
        "release" -> (props["RELEASE_VERSION_NAME"] as? String) ?: "0.1"
        "beta" -> (props["BETA_VERSION_NAME"] as? String) ?: "0.1.10-beta"
        else -> (props["DEBUG_VERSION_NAME"] as? String) ?: "0.1.110-debug"
    }
}

// VersionCode
fun getVersionCode(): Int {
    val buildType = System.getenv("BUILD_TYPE") ?: "debug"
    val props = project.properties
    return when (buildType) {
        "release" -> (props["RELEASE_VERSION_CODE"] as? String)?.toInt() ?: 1
        "beta" -> (props["BETA_VERSION_CODE"] as? String)?.toInt() ?: 110
        else -> (props["DEBUG_VERSION_CODE"] as? String)?.toInt() ?: 1110
    }
}

android {
    namespace = "io.stardew"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.stardew"
        minSdk = 26
        targetSdk = 36
        versionCode = getVersionCode()
        versionName = getVersionName()
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

afterEvaluate {
    configure<com.android.build.gradle.AppExtension> {
        applicationVariants.all {
            val variant = this
            val currentVersionName = variant.versionName ?: "0.1"
            val typeName = variant.buildType.name
            variant.outputs.all {
                val output = this as com.android.build.gradle.internal.api.ApkVariantOutputImpl
                output.outputFileName = "stardew-v${currentVersionName}-${typeName}.apk"
            }
        }
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
