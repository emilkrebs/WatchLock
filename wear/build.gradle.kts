import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

val versionMayor = 1
val versionMinor = 3
val versionPatch = 7

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
        versionCode = 100000 + versionMayor * 10000 + versionMinor * 100 + versionPatch
        versionName = "$versionMayor.$versionMinor.$versionPatch"

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
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }

        debug {
            isMinifyEnabled = false
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_19
        targetCompatibility = JavaVersion.VERSION_19
    }

    kotlinOptions {
        jvmTarget = "19"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.play.services.wearable)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.recyclerview)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui.ui)
    implementation(libs.androidx.compose.ui.ui.tooling.preview2)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.material.icons.extended)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.wear.remote.interactions)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.ui.test.manifest)

}