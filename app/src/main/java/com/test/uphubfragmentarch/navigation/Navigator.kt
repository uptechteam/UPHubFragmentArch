package com.test.uphubfragmentarch.navigation

interface Navigator<T> where T : Navigator.Route {

    fun navigate(route: T)
    fun navigate(route: T, intermediateRoutes: Collection<T>)

    /**
     * Pops backstack if it's possible
     * @return true if stack has been popped, false otherwise
     */
    fun popBackStack(): Boolean

    interface Route {
        val destination: Destination
    }

    open class Destination(
        val name: String
    )
}