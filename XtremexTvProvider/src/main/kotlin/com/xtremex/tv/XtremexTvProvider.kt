package com.xtremex.tv

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*

class XtremexTvProvider : MainAPI() {
    override var mainUrl = "https://xtreamcommunication.vercel.app"
    override var name = "Xtreme'x Live TV"
    override val supportedTypes = setOf(TvType.Live)
    override var lang = "bn"
    override val hasMainPage = true

    data class TvPortal(val id: Int, val name: String, val url: String, val category: String)

    private val tvServers = listOf(
        TvPortal(101, "BdCinema TV", "http://10.253.253.244/", "General TV"),
        TvPortal(102, "XTREMEX TV Portal", "https://xtremextv.vercel.app/", "Featured"),
        TvPortal(103, "BDIPTV Net", "http://tv.bdiptv.net/", "General TV"),
        TvPortal(104, "QUICK TV", "http://172.19.17.28/", "General TV"),
        TvPortal(106, "CLOUD TV", "http://172.19.178.180/", "General TV"),
        TvPortal(108, "Redforce Live", "http://redforce.live/", "General TV"),
        TvPortal(109, "Jatrapala Live TV", "http://jatrapala.com/live-tv.html", "General TV"),
        TvPortal(110, "BanglaTube TV", "http://172.50.50.8/", "General TV"),
        TvPortal(111, "Ideal TV", "http://172.16.60.2/", "General TV"),
        TvPortal(112, "Nethome TV", "http://172.16.200.205/", "General TV"),
        TvPortal(113, "Tv Portal BDIX", "http://198.195.239.50/", "General TV"),
        TvPortal(114, "FUN TIME TV", "http://172.20.21.22/live_tv.php?key=1", "General TV"),
        TvPortal(115, "Deltainfo IPTV", "http://iptv.deltainfonet.com/", "General TV"),
        TvPortal(116, "KSB NET TV", "http://tv.ksbnet.net/", "General TV"),
        TvPortal(117, "BAS NET TV", "http://10.99.99.99/", "General TV"),
        TvPortal(118, "ANTBD TV", "http://172.17.50.112/", "General TV"),
        TvPortal(105, "LIVE SPORTS 1", "http://10.47.57.10/", "Sports"),
        TvPortal(107, "LIVE SPORTS 2", "http://172.16.200.211/", "Sports")
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val categories = tvServers.groupBy { serverObj -> serverObj.category }
        val homeLists = categories.map { (catName, servers) ->
            val responses = servers.map { server ->
                newLiveSearchResponse(
                    name = server.name,
                    url = server.url,
                    type = TvType.Live
                ) {
                    this.posterUrl = "https://raw.githubusercontent.com/google/material-design-icons/master/png/hardware/tv/materialicons/48dp/2x/baseline_tv_black_48dp.png"
                }
            }
            HomePageList(catName, responses)
        }
        return newHomePageResponse(homeLists, hasNext = false)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        return tvServers.filter { serverObj -> serverObj.name.contains(query, ignoreCase = true) }.map { server ->
            newLiveSearchResponse(
                name = server.name,
                url = server.url,
                type = TvType.Live
            ) {
                this.posterUrl = "https://raw.githubusercontent.com/google/material-design-icons/master/png/hardware/tv/materialicons/48dp/2x/baseline_tv_black_48dp.png"
            }
        }
    }

    override suspend fun load(url: String): LoadResponse {
        val server = tvServers.find { serverObj -> serverObj.url == url }
        val title = server?.name ?: "BDIX Live TV"

        return newLiveStreamLoadResponse(
            name = title,
            url = url,
            dataUrl = url
        ) {
            this.plot = "Direct BDIX Local TV Stream via Xtreme'x Network."
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
            val directStream = doc.selectFirst("video source")?.attr("src")
                ?: doc.selectFirst("video")?.attr("src")
                ?: doc.html().substringAfter("source: '", "").substringBefore("'")

            if (directStream.isBlank()) {
                callback(
                    ExtractorLink(
                        source = this@XtremexTvProvider.name,
                        name = "${this@XtremexTvProvider.name} Direct Link",
                        url = data,
                        referer = data,
                        quality = Qualities.P1080.value,
                        type = if (data.contains(".m3u8")) ExtractorLinkType.M3U8 else ExtractorLinkType.VIDEO
                    )
                )
                return true
            }

            val finalUrl = when {
                directStream.startsWith("http://") || directStream.startsWith("https://") -> directStream
                directStream.startsWith("//") -> "https:$directStream"
                else -> java.net.URI(data).resolve(directStream).toString()
            }

            callback(
                ExtractorLink(
                    source = this@XtremexTvProvider.name,
                    name = "${this@XtremexTvProvider.name} Stream",
                    url = finalUrl,
                    referer = data,
                    quality = Qualities.P1080.value,
                    type = if (finalUrl.contains(".m3u8")) ExtractorLinkType.M3U8 else ExtractorLinkType.VIDEO
                )
            )
            true
        } catch (e: Exception) {
            callback(
                ExtractorLink(
                    source = this@XtremexTvProvider.name,
                    name = "${this@XtremexTvProvider.name} Direct Link",
                    url = data,
                    referer = data,
                    quality = Qualities.P1080.value,
                    type = if (data.contains(".m3u8")) ExtractorLinkType.M3U8 else ExtractorLinkType.VIDEO
                )
            )
            true
        }
    }
}
