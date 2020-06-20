package com.test.uphubfragmentarch.util

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData

abstract class LivePreference<T>(val prefs: SharedPreferences, val key: String, val default: T) :
    MutableLiveData<T>() {

    private val prefListener = SharedPreferences.OnSharedPreferenceChangeListener { _, changedKey ->
        if (key != changedKey) return@OnSharedPreferenceChangeListener
        setValue(getPrefValue())
    }

    override fun onActive() {
        updateValue()
        prefs.registerOnSharedPreferenceChangeListener(prefListener)
        super.onActive()
    }

    override fun onInactive() {
        prefs.unregisterOnSharedPreferenceChangeListener(prefListener)
        super.onInactive()
    }

    private fun updateValue() = set(getPrefValue())

    override fun setValue(value: T) {
        if (this.value != value) super.setValue(value)
        if (value != getPrefValue()) writePrefValue(value)
    }

    abstract fun getPrefValue(): T

    abstract fun writePrefValue(value: T)
}

class LiveStringPreference(
    prefs: SharedPreferences,
    key: String,
    default: String = ""
) : LivePreference<String>(prefs, key, default) {

    override fun getPrefValue(): String =
        prefs.getString(key, default) ?: default

    override fun writePrefValue(value: String) =
        prefs.edit().putString(key, value).apply()
}

class LiveIntPreference(
    prefs: SharedPreferences,
    key: String,
    default: Int = -1
) : LivePreference<Int>(prefs, key, default) {

    override fun getPrefValue(): Int =
        prefs.getInt(key, default)

    override fun writePrefValue(value: Int) =
        prefs.edit().putInt(key, value).apply()
}

class LiveBoolPreference(
    prefs: SharedPreferences,
    key: String,
    default: Boolean = true
) : LivePreference<Boolean>(prefs, key, default) {
    override fun getPrefValue() =
        prefs.getBoolean(key, default)

    override fun writePrefValue(value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }
}

