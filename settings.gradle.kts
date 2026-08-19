pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }

    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.lagradost.cloudstream3.gradle") {
                useModule("com.github.recloudstream:gradle:master-SNAPSHOT")
            }
        }
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

rootProject.name = "XtremeXBDIXPlugins"

include(":XtremexTvProvider")
include(":XtremexFtpProvider")