package com.examples.rentors.utils

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

var ISO_8601_FORMAT: SimpleDateFormat =
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            this.timeZone = TimeZone.getTimeZone("UTC")
        }
