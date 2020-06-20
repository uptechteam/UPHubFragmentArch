package com.test.uphubfragmentarch.data.remote.model

import com.test.uphubfragmentarch.BuildConfig
import java.text.SimpleDateFormat
import java.util.*

data class DeviceInfo(
    var clientId: String = CLIENT_ID,
    var clientSecret: String = CLIENT_SECRET,
    var scopes: List<String> = SCOPES,
    var note: String = BuildConfig.APPLICATION_ID + " " + SimpleDateFormat(
                if (BuildConfig.DEBUG) PER_SECOND else PER_DEVICE
        ).format(Date())
) {
    companion object {

        private const val CLIENT_ID = "774e83fb4d323843359d"
        private const val CLIENT_SECRET = "90520a85e431962266d69d29ee1d05d887ff1a28"

        private val SCOPES = listOf("user", "repo", "gist", "notifications", "read:org")

        private const val PER_DEVICE = " android"
        private const val PER_DAY = "MMM dd,yyyy"
        private const val PER_HOUR = "MMM dd,yyyy hh"
        private const val PER_SECOND = "MMM dd,yyyy hh:mm:ss"
    }
}