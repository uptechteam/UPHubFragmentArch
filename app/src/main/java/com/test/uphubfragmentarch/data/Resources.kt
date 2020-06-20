package com.test.uphubfragmentarch.data

import android.content.Context
import androidx.annotation.StringRes
import javax.inject.Inject

interface Resources {
    fun getString(@StringRes stringRes: Int): String
}

class AndroidResources @Inject constructor(private val context: Context) : Resources {

    override fun getString(stringRes: Int) = context.getString(stringRes)
}