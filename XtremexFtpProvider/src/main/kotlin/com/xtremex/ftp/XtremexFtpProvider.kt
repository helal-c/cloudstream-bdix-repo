package com.xtremex.ftp

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.Jsoup
import java.net.URI

class XtremexFtpProvider : MainAPI() {

    override var mainUrl = "https://xtremexbd.com"
    override var name = "Xtreme'x BDIX"
    override val supportedTypes = setOf(
        TvType.Movie,
        TvType.TvSeries,
        TvType.Anime
    )
    override var lang = "bn"
    override val hasMainPage = true

    data class BDFtpServer(
        val name: String,
        val url: String,
        val category: String
    )

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

    override suspend fun getMainPage(
        page: Int,
        request: MainPageRequest
    ): HomePageResponse {

        val grouped = allFtpList.groupBy { it.category }

        val homeLists = grouped.map { (category, servers) ->

            val items = servers.map { server ->

                val poster = when (server.category) {
                    "Anime Hub" ->
                        "https://upload.wikimedia.org/wikipedia/commons/thumb/c/cf/Anime_eye.svg/1024px-Anime_eye.svg.png"

                    "Netflix & OTT Mirrors" ->
                        "https://assets.nflxext.com/ffe/siteui/common/icons/monogram/48x48.png"

                    else ->
                        "https://raw.githubusercontent.com/google/material-design-icons/master/png/file/folder/materialicons/48dp/2x/baseline_folder_black_48dp.png"
                }

                MovieSearchResponse(
                    name = server.name,
                    url = server.url,
                    apiName = this@XtremexFtpProvider.name,
                    type = TvType.Movie,
                    posterUrl = poster
                )
            }

            HomePageList(category, items)
        }

        return HomePageResponse(homeLists)
    }

    override suspend fun search(
        query: String
    ): List<SearchResponse> {

        return allFtpList
            .filter {
                it.name.contains(query, ignoreCase = true) ||
                it.url.contains(query, ignoreCase = true)
            }
            .map { server ->

                MovieSearchResponse(
                    name = server.name,
                    url = server.url,
                    apiName = this@XtremexFtpProvider.name,
                    type = TvType.Movie,
                    posterUrl = null
                )
            }
    }

    override suspend fun load(
        url: String
    ): LoadResponse {

        val targetServer = allFtpList.find {
            it.url == url
        }

        val defaultTitle = targetServer?.name ?: "Media Server"

        return try {

            val doc = app.get(
                url,
                timeout = 4
            ).document

            val title =
                doc.selectFirst("h1, h2, .title")
                    ?.text()
                    ?.takeIf { it.isNotBlank() }
                    ?: defaultTitle

            val poster =
                doc.selectFirst(
                    "img.poster, meta[property=og:image]"
                )?.let {
                    if (it.tagName() == "meta") {
                        it.attr("content")
                    } else {
                        it.attr("src")
                    }
                }

            MovieLoadResponse(
                name = title,
                url = url,
                apiName = this@XtremexFtpProvider.name,
                type = TvType.Movie,
                dataUrl = url
            ).apply {
                posterUrl = poster
                plot = "Streaming from $defaultTitle"
            }

        } catch (e: Exception) {

            MovieLoadResponse(
                name = defaultTitle,
                url = url,
                apiName = this@XtremexFtpProvider.name,
                type = TvType.Movie,
                dataUrl = url
            ).apply {
                plot = "Direct Connection"
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

            val doc = app.get(
                data,
                timeout = 5
            ).document

            val mediaFiles = mutableListOf<String>()

            doc.select("a[href], iframe[src], video[src], source[src]")
                .forEach { element ->

                    val rawLink = when {
                        element.hasAttr("src") ->
                            element.attr("src")

                        element.hasAttr("href") ->
                            element.attr("href")

                        else ->
                            ""
                    }

                    if (rawLink.isBlank()) return@forEach

                    val fullUrl = resolveUrl(
                        data,
                        rawLink
                    )

                    val lower = fullUrl.lowercase()

                    if (
                        lower.contains(".mp4") ||
                        lower.contains(".mkv") ||
                        lower.contains(".m3u8") ||
                        lower.contains(".mpd") ||
                        lower.contains("/stream/") ||
                        lower.contains("/storage/")
                    ) {
                        mediaFiles.add(fullUrl)
                    }
                }

            val uniqueLinks = mediaFiles
                .distinct()
                .take(10)

            if (uniqueLinks.isEmpty()) {

                callback(
                    newExtractorLink(
                        source = this@XtremexFtpProvider.name,
                        name = "Direct Stream",
                        url = data
                    ) {
                        referer = data
                        quality = Qualities.P1080.value
                    }
                )

                return true
            }

            uniqueLinks.forEachIndexed { index, streamUrl ->

                callback(
                    newExtractorLink(
                        source = this@XtremexFtpProvider.name,
                        name = "Stream #${index + 1}",
                        url = streamUrl
                    ) {
                        referer = data
                        quality = detectQuality(streamUrl)
                    }
                )
            }

            true

        } catch (e: Exception) {
            false
        }
    }

    private fun resolveUrl(
        baseUrl: String,
        link: String
    ): String {

        return try {

            when {
                link.startsWith("http://") ||
                link.startsWith("https://") -> link

                link.startsWith("//") ->
                    "https:$link"

                else ->
                    URI(baseUrl)
                        .resolve(link)
                        .toString()
            }

        } catch (e: Exception) {
            link
        }
    }

    private fun detectQuality(
        url: String
    ): Int {

        val lower = url.lowercase()

        return when {
            "2160" in lower || "4k" in lower ->
                Qualities.P2160.value

            "1440" in lower ->
                Qualities.P1440.value

            "1080" in lower ->
                Qualities.P1080.value

            "720" in lower ->
                Qualities.P720.value

            "480" in lower ->
                Qualities.P480.value

            "360" in lower ->
                Qualities.P360.value

            else ->
                Qualities.Unknown.value
        }
    }
}