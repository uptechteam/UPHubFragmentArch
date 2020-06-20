package com.test.uphubfragmentarch.data

import android.content.Context
import com.test.uphubfragmentarch.data.repository.ReposRepository.Companion.USER_REPOS_THROTTLE
import com.test.uphubfragmentarch.util.LiveBoolPreference
import com.test.uphubfragmentarch.util.LiveIntPreference
import com.test.uphubfragmentarch.util.LiveStringPreference
import java.util.*
import java.util.concurrent.TimeUnit

class UserPreferences(context: Context) {

    companion object {

        private const val USER_PREFS = "user_prefs"

        private const val TOKEN = "token"
        private const val SHOW_LOGIN = "show_login"
        private const val CURRENT_USER_LOGIN = "current_user_login"
        private const val CURRENT_USER_ID = "current_user_id"

        private const val PROFILE_FOLLOW_FETCH = "profile_follow_fetch_"
        private const val PROFILE_SUBSCRB_FETCH = "profile_subscrb_fetch_"
        private const val PROFILE_REPOS_FETCH = "profile_repos_fetch_"
    }

    private val prefs =
        context.applicationContext.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE)

    var currentUserLogin = LiveStringPreference(prefs, CURRENT_USER_LOGIN)

    var currentUserId = LiveIntPreference(prefs, CURRENT_USER_ID)

    val hideLogin = LiveBoolPreference(prefs, SHOW_LOGIN, false)

    fun setUserToken(login: String, token: String) =
        prefs.edit().putString(getUserLoginKey(login), token).apply()

    fun getUserToken(login: String): String =
        prefs.getString(getUserLoginKey(login), "") ?: ""

    fun getCurrentToken() = getUserToken(currentUserLogin.getPrefValue())

    private fun getUserLoginKey(login: String) = "${login}_auth_token"

    fun onProfileFollowFetch(userId: Int) =
        prefs.edit().putLong(PROFILE_FOLLOW_FETCH + userId, Date().time).apply()

    fun onProfileSubscriberFetch(userId: Int) =
        prefs.edit().putLong(PROFILE_SUBSCRB_FETCH + userId, Date().time).apply()

    fun onProfileReposFetch(userId: Int) =
        prefs.edit().putLong(PROFILE_REPOS_FETCH + userId, Date().time).apply()

    fun checkProfileFollowFetch(userId: Int) =
        (Date().time - prefs.getLong(PROFILE_FOLLOW_FETCH + userId, 0)) > USER_FETCH_THROTTLE

    fun checkProfileSubscriberFetch(userId: Int) =
        (Date().time - prefs.getLong(PROFILE_SUBSCRB_FETCH + userId, 0)) > USER_FETCH_THROTTLE

    fun checkProfileRepositoryFetch(userId: Int) =
        (Date().time - prefs.getLong(PROFILE_REPOS_FETCH + userId, 0)) > USER_REPOS_THROTTLE
}

val USER_FETCH_THROTTLE = TimeUnit.SECONDS.toMillis(30)
