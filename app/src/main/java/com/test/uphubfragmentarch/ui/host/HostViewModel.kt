package com.test.uphubfragmentarch.ui.host

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.test.uphubfragmentarch.data.UserPreferences
import com.test.uphubfragmentarch.ui.BaseViewModel
import javax.inject.Inject

class HostViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : BaseViewModel() {

    val route: LiveData<HostRoute> = userPreferences
        .hideLogin.map {
            if (it) HostRoute.Main else HostRoute.Login
        }
}