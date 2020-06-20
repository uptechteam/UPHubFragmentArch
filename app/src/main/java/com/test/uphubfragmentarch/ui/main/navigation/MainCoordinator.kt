package com.test.uphubfragmentarch.ui.main.navigation

import com.test.uphubfragmentarch.navigation.Navigator
import com.test.uphubfragmentarch.ui.main.user.UserCoordinator
import javax.inject.Inject

interface MainCoordinator : UserCoordinator {
    fun onProfileShow(userId: Int)
}

class DefaultMainCoordinator @Inject constructor(

) : MainCoordinator {
    private lateinit var navigator: Navigator<MainRoute>

    fun initCoordinator(navigator: Navigator<MainRoute>) {
        this.navigator = navigator
    }

    override fun onProfileShow(userId: Int) {
        navigator.navigate(MainRoute.Profile(userId))
    }

    override fun userCtorOnProfileClicked(id: Int) {
        navigator.navigate(MainRoute.Profile(id))
    }
}