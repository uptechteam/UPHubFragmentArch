package com.test.uphubfragmentarch.util

fun <T> guard(dangerTask: () -> T): T? = try {
    dangerTask()
} catch (e: Exception) {
    e.printStackTrace()
    null
}

suspend fun <T> guardAsync(dangerTask: suspend () -> T): T? = try {
    dangerTask()
} catch (e: Exception) {
    e.printStackTrace()
    null
}
