plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.dongah.dispenser"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.dongah.dispenser"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    //AIDL directory add
    buildFeatures {
        // Determines whether to generate binder classes for your AIDL files.
        aidl = true
    }

    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("debug")
        }
        debug {
            isDebuggable = true
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    externalNativeBuild {
        ndkBuild {
            path = file("src/main/jni/Android.mk")
        }
    }
    ndkVersion = "26.1.10909125"
}

dependencies {
    implementation(libs.rxandroid)
    implementation(libs.rxjava)
    implementation(libs.okhttp)
    implementation(libs.okhttptls)
    implementation(libs.zxing)
    implementation(libs.jsch)
    implementation(libs.slf4j)
    implementation(libs.gson)
    implementation(libs.jaxb)
    implementation(libs.backport)
    implementation(libs.circulrprogress)
    implementation(libs.bouncycastle)
    implementation(libs.commonsnet)
    implementation(libs.avloading)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    coreLibraryDesugaring(libs.desugar)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}