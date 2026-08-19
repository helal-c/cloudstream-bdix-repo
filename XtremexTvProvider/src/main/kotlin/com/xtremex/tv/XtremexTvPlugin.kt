package com.xtremex.tv

import android.content.Context
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.plugins.*

@CloudstreamPlugin
class XtremexTvPlugin : Plugin() {
    override fun load(context: Context) {
        registerMainAPI(XtremexTvProvider())
    }
}
