package com.xtremex.ftp

import android.content.Context
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.plugins.*

@CloudstreamPlugin
class XtremexFtpPlugin : Plugin() {
    override fun load(context: Context) {
        registerMainAPI(XtremexFtpProvider())
    }
}
