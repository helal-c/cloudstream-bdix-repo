version = 1

cloudstream {
    description = "Xtreme'x BDIX Live TV"
    authors = listOf("Helal Uddin")
    status = 1
    tvTypes = listOf("Live")
    language = "bn"
}

android {
    namespace = "com.xtremex.tv"

    compileSdk = 35

    defaultConfig {
        minSdk = 21
    }
}
