plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    id("androidx.navigation.safeargs.kotlin") version "2.7.7" apply false
}
