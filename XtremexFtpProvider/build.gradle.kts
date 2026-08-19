plugins {
    id("com.android.library")
    id("kotlin-android")
    id("com.lagradost.cloudstream3.gradle")
}

android {
    namespace = "com.xtremex.ftp"
    compileSdk = 34
    defaultConfig {
        minSdk = 21
    }
}
