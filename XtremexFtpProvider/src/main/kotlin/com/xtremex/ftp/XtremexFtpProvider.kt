package com.xtremex.ftp

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*

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

    data class Server(
        val name: String,
        val url: String,
        val category: String
    )

    private val servers = listOf(

        Server(
            "Circle FTP",
            "http://main.circleftp.net/",
            "BDIX FTP"
        ),

        Server(
            "New Circle FTP",
            "http://new.circleftp.net/",
            "BDIX FTP"
        ),

        Server(
            "Discovery FTP",
            "https://discoveryftp.net/",
            "BDIX FTP"
        ),

        Server(
            "Natural BD",
            "http://naturalbd.com/",
            "BDIX FTP"
        ),

        Server(
            "Elaach Media",
            "http://elaach.com/",
            "BDIX FTP"
        ),

        Server(
            "Movie Haat",
            "https://moviehaat.net/",
            "Movie"
        ),

        Server(
            "Cinefreak BD",
            "https://cinefreak.net/",
            "Movie"
        ),

        Server(
            "Banglaplex",
            "https://banglaplex.lat/",
            "Movie"
        ),

        Server(
            "Anime Hub",
            "https://aniwatchtv.to/",
            "Anime"
        ),

        Server(
            "GogoAnime",
            "https://gogoanime3.co/",
            "Anime"
        )
    )

    override suspend fun getMainPage(
        page: Int,
        request: MainPageRequest
    ): HomePageResponse {

        val groups = servers.groupBy {
            it.category
        }

        val homeLists = groups.map { (category, list) ->

            val items = list.map { server ->

                newMovieSearchResponse(
                    name = server.name,
                    url = server.url,
                    type = TvType.Movie
                )
            }

            HomePageList(
                category,
                items
            )
        }

        return newHomePageResponse(
            homeLists,
            hasNext = false
        )
    }

    override suspend fun search(
        query: String
    ): List<SearchResponse> {

        return servers
            .filter {
                it.name.contains(
                    query,
                    ignoreCase = true
                )
            }
            .map {

                newMovieSearchResponse(
                    name = it.name,
                    url = it.url,
                    type = TvType.Movie
                )
            }
    }

    override suspend fun load(
        url: String
    ): LoadResponse {

        val server = servers.find {
            it.url == url
        }

        return newMovieLoadResponse(
            name = server?.name ?: "BDIX Server",
            url = url,
            type = TvType.Movie,
            dataUrl = url
        ) {

            plot =
                "Xtreme'x BDIX Local Media Server"
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {

        try {

            val doc = app.get(
                data,
                timeout = 10
            ).document

            val links = doc
                .select("a[href]")
                .mapNotNull {
                    it.attr("href")
                }
                .filter {

                    val url =
                        it.lowercase()

                    url.endsWith(".mp4") ||
                    url.endsWith(".mkv") ||
                    url.endsWith(".webm") ||
                    url.contains(".m3u8")
                }

            links.forEachIndexed { index, link ->

                val finalUrl =
                    when {

                        link.startsWith("http://") ||
                        link.startsWith("https://") ->
                            link

                        link.startsWith("//") ->
                            "https:$link"

                        else ->
                            java.net.URI(data)
                                .resolve(link)
                                .toString()
                    }

                callback(
                    ExtractorLink(
                        source = name,
                        name = "$name #${index + 1}",
                        url = finalUrl,
                        referer = data,
                        quality = Qualities.Unknown.value,
                        type =
                            if (
                                finalUrl
                                    .lowercase()
                                    .contains(".m3u8")
                            )
                                ExtractorLinkType.M3U8
                            else
                                ExtractorLinkType.VIDEO
                    )
                )
            }

            return links.isNotEmpty()

        } catch (e: Exception) {

            return false
        }
    }
}
