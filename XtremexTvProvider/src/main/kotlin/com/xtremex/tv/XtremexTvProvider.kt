package com.xtremex.tv

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*

class XtremexTvProvider : MainAPI() {

    override var mainUrl =
        "https://xtreamcommunication.vercel.app"

    override var name =
        "Xtreme'x Live TV"

    override val supportedTypes =
        setOf(TvType.Live)

    override var lang = "bn"

    override val hasMainPage = true

    data class TvServer(
        val name: String,
        val url: String,
        val category: String
    )

    private val servers = listOf(

        TvServer(
            "XTREMEX TV",
            "https://xtremextv.vercel.app/",
            "Featured"
        ),

        TvServer(
            "Redforce Live",
            "http://redforce.live/",
            "General TV"
        ),

        TvServer(
            "Jatrapala Live TV",
            "http://jatrapala.com/live-tv.html",
            "General TV"
        ),

        TvServer(
            "Deltainfo IPTV",
            "http://iptv.deltainfonet.com/",
            "General TV"
        ),

        TvServer(
            "KSB NET TV",
            "http://tv.ksbnet.net/",
            "General TV"
        ),

        TvServer(
            "BdCinema TV",
            "http://10.253.253.244/",
            "BDIX TV"
        ),

        TvServer(
            "QUICK TV",
            "http://172.19.17.28/",
            "BDIX TV"
        ),

        TvServer(
            "CLOUD TV",
            "http://172.19.178.180/",
            "BDIX TV"
        ),

        TvServer(
            "LIVE SPORTS 1",
            "http://10.47.57.10/",
            "Sports"
        ),

        TvServer(
            "LIVE SPORTS 2",
            "http://172.16.200.211/",
            "Sports"
        )
    )

    override suspend fun getMainPage(
        page: Int,
        request: MainPageRequest
    ): HomePageResponse {

        val groups =
            servers.groupBy {
                it.category
            }

        val lists =
            groups.map { (category, items) ->

                HomePageList(
                    category,
                    items.map { server ->

                        newLiveSearchResponse(
                            name = server.name,
                            url = server.url,
                            type = TvType.Live
                        )
                    }
                )
            }

        return newHomePageResponse(
            lists,
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

                newLiveSearchResponse(
                    name = it.name,
                    url = it.url,
                    type = TvType.Live
                )
            }
    }

    override suspend fun load(
        url: String
    ): LoadResponse {

        val server =
            servers.find {
                it.url == url
            }

        return newLiveStreamLoadResponse(
            name =
                server?.name
                    ?: "BDIX Live TV",

            url = url,

            dataUrl = url
        ) {

            plot =
                "Xtreme'x BDIX Live TV"
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {

        return try {

            val doc =
                app.get(
                    data,
                    timeout = 10
                ).document

            val stream =
                doc.selectFirst(
                    "video source"
                )?.attr("src")

                    ?: doc.selectFirst(
                        "video"
                    )?.attr("src")

                    ?: doc.selectFirst(
                        "source"
                    )?.attr("src")

                    ?: ""

            if (stream.isBlank()) {

                callback(
                    ExtractorLink(
                        source = name,
                        name = "Direct Stream",
                        url = data,
                        referer = data,
                        quality =
                            Qualities.Unknown.value,
                        type =
                            if (
                                data.lowercase()
                                    .contains(".m3u8")
                            )
                                ExtractorLinkType.M3U8
                            else
                                ExtractorLinkType.VIDEO
                    )
                )

                return true
            }

            val finalUrl =
                when {

                    stream.startsWith(
                        "http://"
                    ) ||
                    stream.startsWith(
                        "https://"
                    ) ->
                        stream

                    stream.startsWith("//") ->
                        "https:$stream"

                    else ->
                        java.net.URI(data)
                            .resolve(stream)
                            .toString()
                }

            callback(
                ExtractorLink(
                    source = name,
                    name = "Live Stream",
                    url = finalUrl,
                    referer = data,
                    quality =
                        Qualities.P1080.value,
                    type =
                        if (
                            finalUrl.lowercase()
                                .contains(".m3u8")
                        )
                            ExtractorLinkType.M3U8
                        else
                            ExtractorLinkType.VIDEO
                )
            )

            true

        } catch (e: Exception) {

            false
        }
    }
}
