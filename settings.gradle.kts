pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://jitpack.io")
    }
}

rootProject.name = "cloudstream-bdix-repo"

include(":XtremexTvProvider")
include(":XtremexFtpProvider")
