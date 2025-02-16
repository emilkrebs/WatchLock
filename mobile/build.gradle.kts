import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

val versionMayor = 1
val versionMinor = 3
val versionPatch = 8

val keystoreProperties = kotlin.run {
    val file = rootProject.file("keystore.properties")
    Properties().apply {
        load(file.inputStream())
    }
}

android {
    namespace = "com.emilkrebs.watchlock"
    compileSdk = 35


    defaultConfig {
        applicationId = "com.emilkrebs.watchlock"
        minSdk = 30
        targetSdk = 35
        versionCode = 200000 + versionMayor * 10000 + versionMinor * 100 + versionPatch
        versionName = "$versionMayor.$versionMinor.$versionPatch"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
        }
    }


    buildTypes {
        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = false // todo: make shrinkResources true without any issues
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }

        debug {
            isMinifyEnabled = false
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
        }
    }


    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }


    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_19
        targetCompatibility = JavaVersion.VERSION_19
    }

    kotlinOptions {
        jvmTarget = "19"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.play.services.wearable)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.work.runtime.ktx)

    implementation(libs.androidx.biometric)
    implementation(libs.wear.remote.interactions)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)

    debugImplementation(libs.ui.tooling)
    implementation(libs.androidx.compose.ui.ui.tooling.preview)
    debugImplementation(libs.ui.test.manifest)
}
