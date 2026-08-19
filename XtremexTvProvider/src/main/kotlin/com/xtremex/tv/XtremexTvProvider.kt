package com.xtremex.tv

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.Qualities

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
        val categories = tvServers.groupBy { it.category }
        val homeLists = categories.map { (catName, servers) ->
            val responses = servers.map { server ->
                LiveSearchResponse(
                    name = server.name,
                    url = server.url,
                    apiName = this.name,
                    type = TvType.Live,
                    posterUrl = "https://raw.githubusercontent.com/google/material-design-icons/master/png/hardware/tv/materialicons/48dp/2x/baseline_tv_black_48dp.png"
                )
            }
            HomePageList(catName, responses)
        }
        return HomePageResponse(homeLists)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        return tvServers.filter { it.name.contains(query, ignoreCase = true) }.map { server ->
            LiveSearchResponse(
                name = server.name,
                url = server.url,
                apiName = this.name,
                type = TvType.Live
            )
        }
    }

    override suspend fun load(url: String): LoadResponse {
        val server = tvServers.find { it.url == url }
        // এখানে TvType.Live সরিয়ে শুধু url রাখা হয়েছে, যার কারণে কম্পাইল এররটি হচ্ছিল
        return newLiveStreamLoadResponse(server?.name ?: "BDIX Live TV", url) {
            this.plot = "Direct BDIX Local TV Stream via Xtreme'x Network."
        }
    }

    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        return try {
            val doc = app.get(data, timeout = 5).document
            val directStream = doc.selectFirst("video source")?.attr("src") ?: doc.selectFirst("video")?.attr("src") ?: doc.html().substringAfter("source: '").substringBefore("'")
            val finalUrl = if (directStream.startsWith("http")) directStream else fixUrl(directStream)
            
            callback(ExtractorLink(name, "$name Stream", if (finalUrl.contains(".m3u8")) finalUrl else data, data, Qualities.P1080.value, finalUrl.contains(".m3u8")))
            true
        } catch (e: Exception) {
            callback(ExtractorLink(name, "$name Direct Link", data, "", Qualities.P1080.value, data.contains(".m3u8")))
            true
        }
    }
}
