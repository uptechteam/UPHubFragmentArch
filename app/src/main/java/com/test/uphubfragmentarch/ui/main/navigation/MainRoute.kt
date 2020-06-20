package com.test.uphubfragmentarch.ui.main.navigation

import android.os.Bundle
import androidx.annotation.IdRes
import com.test.uphubfragmentarch.R
import com.test.uphubfragmentarch.navigation.Navigator
import com.test.uphubfragmentarch.ui.main.profile.ProfileFragment

sealed class MainRoute(
    @IdRes val id: Int,
    val bundle: Bundle? = null
) : Navigator.Route {

    object Main : MainRoute(R.id.repository) {
        override val destination: Navigator.Destination
            get() = Navigator.Destination("main")
    }

    data class Profile(val userId: Int) : MainRoute(
        R.id.profile_view,
        ProfileFragment.getBundle(userId)
    ) {
        override val destination: Navigator.Destination
            get() = Navigator.Destination("Profile")
    }

}