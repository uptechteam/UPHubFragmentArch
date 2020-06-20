package com.test.uphubfragmentarch.util

import android.os.Handler

class Debounce(private val handler: Handler, private val delay: Long = DEFAULT_DELAY) {

    companion object {

        private const val DEFAULT_DELAY = 250L
    }

    private var lastAction: (() -> Unit)? = null

    private val runnable = Runnable {
        lastAction?.invoke()
        lastAction = null
    }

    fun offer(action: () -> Unit) {
        val run = (lastAction == null)
        lastAction = action
        if (run) handler.postDelayed(runnable, delay)
    }
}