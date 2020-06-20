package com.test.uphubfragmentarch

import android.app.Application
import com.facebook.stetho.Stetho
import com.test.uphubfragmentarch.di.AppComponent

class UpHubApp : Application() {

    private val component = AppComponent(this)

    init {
        registerActivityLifecycleCallbacks(component)
    }

    override fun onCreate() {
        super.onCreate()
        Stetho.initializeWithDefaults(this)
    }
}