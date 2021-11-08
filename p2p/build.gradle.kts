plugins {
    id(Plugins.BuildPlugins.androidLib)
    id(Plugins.BuildPlugins.kotlinAndroid)
    kotlin("plugin.serialization") version Dependencies.Versions.Kotlin.stdlib
}

android {
    compileSdk = Sdk.compileSdk
    buildToolsVersion = Plugins.Versions.buildTools

    defaultConfig {
        minSdk = Sdk.minSdk
        targetSdk = Sdk.targetSdk
        testInstrumentationRunner = Dependencies.androidJunitRunner
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(project(":engine"))
}
