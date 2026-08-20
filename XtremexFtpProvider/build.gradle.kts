version = 1

cloudstream {
    description = "Xtreme'x BDIX FTP & Media Servers"
    authors = listOf("Helal Uddin")
    status = 1
    tvTypes = listOf(
        "Movie",
        "TvSeries",
        "Anime"
    )
    language = "bn"
}

android {
    namespace = "com.xtremex.ftp"

    compileSdk = 35

    defaultConfig {
        minSdk = 21
    }
}
