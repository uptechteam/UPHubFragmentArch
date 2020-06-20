package com.test.uphubfragmentarch.ui.main.navigation

import androidx.navigation.NavController
import com.test.uphubfragmentarch.navigation.Navigator

class MainNavigator(
    private val navController: NavController
) : Navigator<MainRoute> {

    override fun navigate(route: MainRoute) {
        navController.navigate(route.id, route.bundle, null)
    }

    override fun navigate(route: MainRoute, intermediateRoutes: Collection<MainRoute>) {
        navigate(route)
    }

    override fun popBackStack(): Boolean {
        return navController.popBackStack()
    }
}