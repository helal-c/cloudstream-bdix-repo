package com.xtremex.ftp

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.Qualities

class XtremexFtpProvider : MainAPI() {
    override var mainUrl = "https://xtremexbd.com"
    override var name = "Xtreme'x BDIX"
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries, TvType.Anime)
    override var lang = "bn"
    override val hasMainPage = true

    data class BDFtpServer(val name: String, val url: String, val category: String)

    private val allFtpList = listOf(
        BDFtpServer("Aniwatch (Zoro)", "https://aniwatchtv.to/", "Anime Hub"),
        BDFtpServer("GogoAnime", "https://gogoanime3.co/", "Anime Hub"),
        BDFtpServer("KickassAnime", "https://kaas.to/", "Anime Hub"),
        BDFtpServer("Circle FTP - Anime", "http://main.circleftp.net/anime/", "Anime Hub"),
        BDFtpServer("ICC FTP - Anime", "http://10.16.100.244/dashboard.php?category=anime", "Anime Hub"),
        BDFtpServer("Netflix Mirror", "https://netmirror.app/", "Netflix & OTT Mirrors"),
        BDFtpServer("FlixHQ", "https://flixhq.to/", "Netflix & OTT Mirrors"),
        BDFtpServer("Cinefreak BD", "https://cinefreak.net/", "Netflix & OTT Mirrors"),
        BDFtpServer("Banglaplex", "https://banglaplex.lat/", "Netflix & OTT Mirrors"),
        BDFtpServer("Wow FTP", "http://172.27.27.84/", "Popular Mega FTP"),
        BDFtpServer("Circle FTP (New)", "http://new.circleftp.net/", "Popular Mega FTP"),
        BDFtpServer("Circle FTP (Old)", "http://main.circleftp.net/", "Popular Mega FTP"),
        BDFtpServer("Discovery FTP", "https://discoveryftp.net/", "Popular Mega FTP"),
        BDFtpServer("SamOnline FTP", "http://172.16.50.4/", "Popular Mega FTP"),
        BDFtpServer("ICC FTP Dashboard", "http://10.16.100.244/dashboard.php?session=1&category=0", "Popular Mega FTP"),
        BDFtpServer("Movie Haat", "https://moviehaat.net/", "Popular Mega FTP"),
        BDFtpServer("Natural BD FTP", "http://naturalbd.com/", "Popular Mega FTP"),
        BDFtpServer("Elaach Media", "http://elaach.com/", "Popular Mega FTP"),
        BDFtpServer("ICC Local Server", "http://10.16.100.244/", "Local IP Servers"),
        BDFtpServer("IBCCL Media", "http://103.203.93.2/", "Local IP Servers"),
        BDFtpServer("Ghuri Media FTP", "http://103.96.104.6/", "Local IP Servers"),
        BDFtpServer("Quick Media 172", "http://172.19.17.28/", "Local IP Servers"),
        BDFtpServer("BDIX IP 172.27", "http://172.27.27.27/", "Local IP Servers"),
        BDFtpServer("AmberIT Local FTP", "http://10.0.1.1/", "Local IP Servers"),
        BDFtpServer("Ihub Live Portal", "http://ihub.live/", "BDIX Web Portals"),
        BDFtpServer("Mooplex TV", "http://mooplex.net/", "BDIX Web Portals"),
        BDFtpServer("Roar BD Media", "http://roarbd.com/", "BDIX Web Portals"),
        BDFtpServer("Plex BDIX", "http://plex.bdix.org/", "BDIX Web Portals")
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val grouped = allFtpList.groupBy { serverObj -> serverObj.category }
        val homeLists = grouped.map { (catTitle, servers) ->
            val items = servers.map { server ->
                val poster = when (server.category) {
                    "Anime Hub" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/c/cf/Anime_eye.svg/1024px-Anime_eye.svg.png"
                    "Netflix & OTT Mirrors" -> "https://assets.nflxext.com/ffe/siteui/common/icons/monogram/48x48.png"
                    else -> "https://raw.githubusercontent.com/google/material-design-icons/master/png/file/folder/materialicons/48dp/2x/baseline_folder_black_48dp.png"
                }
                newMovieSearchResponse(
                    name = server.name,
                    url = server.url,
                    type = TvType.Movie
                ) {
                    this.posterUrl = poster
                }
            }
            HomePageList(catTitle, items)
        }
        return newHomePageResponse(homeLists, hasNext = false)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        return allFtpList.filter { serverObj -> 
            serverObj.name.contains(query, ignoreCase = true) || serverObj.url.contains(query, ignoreCase = true) 
        }.map { server -> 
            newMovieSearchResponse(
                name = server.name,
                url = server.url,
                type = TvType.Movie
            )
        }
    }

    override suspend fun load(url: String): LoadResponse {
        val targetServer = allFtpList.find { serverObj -> serverObj.url == url }
        val defaultTitle = targetServer?.name ?: "Media Server"

        return try {
            val doc = app.get(url, timeout = 4).document
            val title = doc.selectFirst("h1, h2, .title")?.text() ?: defaultTitle
            val poster = doc.selectFirst("img.poster, meta[property=og:image]")?.attr("src")

            newMovieLoadResponse(
                name = title,
                url = url,
                type = TvType.Movie,
                dataUrl = url
            ) {
                this.posterUrl = poster
                this.plot = "Streaming from $defaultTitle ($url)"
            }
        } catch (e: Exception) {
            newMovieLoadResponse(
                name = defaultTitle,
                url = url,
                type = TvType.Movie,
                dataUrl = url
            ) {
                this.plot = "Direct Connection: $url"
            }
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        return try {
            val doc = app.get(data, timeout = 5).document
            val mediaElements = doc.select("a, iframe, video source")
            val mediaFiles = ArrayList<String>()

            for (element in mediaElements) {
                var link = element.attr("src")
                if (link.isEmpty()) {
                    link = element.attr("href")
                }
                if (link.endsWith(".mp4") || link.endsWith(".mkv") || link.contains(".m3u8") || link.contains("/stream/") || link.contains("/storage/")) {
                    mediaFiles.add(link)
                }
            }

            if (mediaFiles.isNotEmpty()) {
                var idx = 1
                for (link in mediaFiles.take(10)) {
                    val fullUrl = when {
                        link.startsWith("http://") || link.startsWith("https://") -> link
                        link.startsWith("//") -> "https:$link"
                        else -> java.net.URI(data).resolve(link).toString()
                    }
                    callback(
                        ExtractorLink(
                            source = this@XtremexFtpProvider.name,
                            name = "Stream #$idx",
                            url = fullUrl,
                            referer = data,
                            quality = Qualities.P1080.value,
                            isM3u8 = fullUrl.contains(".m3u8")
                        )
                    )
                    idx++
                }
                true
            } else {
                callback(
                    ExtractorLink(
                        source = this@XtremexFtpProvider.name,
                        name = "Direct Stream",
                        url = data,
                        referer = "",
                        quality = Qualities.P1080.value,
                        isM3u8 = data.contains(".m3u8")
                    )
                )
                true
            }
        } catch (e: Exception) {
            false
        }
    }
}
