plugins {
    id("com.android.library")
    id("kotlin-android")
    id("com.lagradost.cloudstream3.gradle")
}

cloudstream {
    // এখানে আপনার গিটহাব ইউজারনেম helal-c বসানো হয়েছে
    setRepoUrl("https://raw.githubusercontent.com/helal-c/cloudstream-bdix-repo/builds")
}

android {
    namespace = "com.xtremex.tv"
    compileSdk = 34
    defaultConfig {
        minSdk = 21
    }
}

