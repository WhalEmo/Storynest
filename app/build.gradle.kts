import java.util.Properties
import java.io.FileInputStream

val localProps = Properties()
localProps.load(FileInputStream(rootProject.file("local.properties")))
val devBaseUrl = localProps.getProperty("DEV_BASE_URL") ?: ""

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
}




android {
    namespace = "com.example.storynest"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.storynest"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }


    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            buildConfigField("String", "BASE_URL", "\"$devBaseUrl\"")
        }

        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            buildConfigField("String", "BASE_URL", "\"$devBaseUrl\"")
        }

    }

    buildFeatures{
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidxCoreKtx)
    implementation(libs.androidxAppcompat)
    implementation(libs.material)
    implementation(libs.androidxActivity)
    implementation(libs.androidxConstraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidxJunit)
    androidTestImplementation(libs.androidxEspressoCore)

    implementation("io.coil-kt:coil:2.5.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.11")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.0")

}
