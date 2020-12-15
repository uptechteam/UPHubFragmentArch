package com.test.uphubfragmentarch.di

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.contentValuesOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.*
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import com.test.uphubfragmentarch.R
import com.test.uphubfragmentarch.ui.host.HostActivity
import com.test.uphubfragmentarch.ui.main.FragmentMain
import com.test.uphubfragmentarch.ui.main.navigation.DefaultMainCoordinator
import com.test.uphubfragmentarch.ui.main.navigation.MainNavigator
import com.test.uphubfragmentarch.ui.main.profile.followers.FollowersViewModel
import com.test.uphubfragmentarch.ui.main.profile.followers.ProfileFollowersFragment
import com.test.uphubfragmentarch.ui.main.profile.repository.ProfileRepositoryFragment
import com.test.uphubfragmentarch.ui.main.profile.repository.ProfileRepositoryViewModel
import com.test.uphubfragmentarch.ui.main.profile.subscriptions.ProfileSubscriptionsFragment
import com.test.uphubfragmentarch.ui.main.profile.subscriptions.SubscriptionViewModel
import com.test.uphubfragmentarch.ui.main.repository.RepositoryFragment
import com.test.uphubfragmentarch.ui.main.user.UserFragment
import kotlin.collections.mutableMapOf
import kotlin.collections.set

class HostInjector(
    hostFactory: ViewModelProvider.Factory,
    private val hostActivity: HostActivity
) : FragmentLifecycleInjector(hostFactory), NavController.OnDestinationChangedListener {

    private var mainInjector: FragmentLifecycleInjector? = null
    private var loginInjector: FragmentLifecycleInjector? = null

    private val map = mutableMapOf<Int, NavBackStackEntry>()

    inner class DestinationLifecycleObserver(
        val destinationId: Int,
        val onCreate: () -> Unit,
        val onKill: () -> Unit
    ) : LifecycleObserver {

        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        fun onStart() {
            Log.e("Life", "start $destinationId")
            onCreate.invoke()
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            Log.e("Life", "destroy $destinationId")
            onKill.invoke()
            map.remove(destinationId)
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
        fun onResume() {
            Log.e("Life", "resume $destinationId")
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        fun onPause() {
            Log.e("Life", "pause $destinationId")
        }
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {

        destination.parent?.let { navGraph ->
            if (!map.containsKey(navGraph.id)) {
                val backstack = controller.getBackStackEntry(navGraph.id)
                val (create, kill) = when (navGraph.id) {
                    R.id.mainSubGraph -> Pair({
                        val comp = MainComponent.invoke()
                        val contr = Navigation.findNavController(hostActivity, R.id.mainHost)
                        val navigator = MainNavigator(contr)
                        val coordinator = comp.mainCoordinator as DefaultMainCoordinator
                        coordinator.initCoordinator(navigator)
                        mainInjector = MainInjector(
                            R.id.mainSubGraph,
                            comp.vmFactory(),
                        )
                        Unit
                    }, {
                        MainComponent.kill()
                    })
                    R.id.loginSubGraph -> Pair({
                        LoginComponent.invoke().apply {
                            loginInjector = LoginInjector(
                                vmFactory(),
                                this,
                                hostActivity
                            )
                        }
                        Unit
                    }, {
                        LoginComponent.kill()
                    })
                    else -> Pair({}, {})
                }

                backstack.lifecycle.addObserver(
                    DestinationLifecycleObserver(
                        navGraph.id,
                        create, kill
                    )
                )
                map[navGraph.id] = backstack
            }
        }
    }
//
//    override fun onFragmentViewCreated(
//        fm: FragmentManager,
//        f: Fragment,
//        v: View,
//        savedInstanceState: Bundle?
//    ) {
//        super.onFragmentViewCreated(fm, f, v, savedInstanceState)
//
//        val controller =
//            (f.childFragmentManager.findFragmentById(R.id.mainHost) as NavHostFragment).navController
//        val navigator = MainNavigator(controller)
//        val coordinator = mainComponent.mainCoordinator as DefaultMainCoordinator
//        coordinator.initCoordinator(navigator)
//
//
//        when (f) {
//            is FragmentMain -> {
//
//            }
//        }
//    }
}

class MainInjector(
    mainFactory: ViewModelProvider.Factory,
    private val ownerId : Int,
    private val controller: NavController
) : FragmentLifecycleInjector(mainFactory), NavController.OnDestinationChangedListener {

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {

    }

    override fun getViewModelClazz(injectingFragment: Fragment): Class<out ViewModel> {
        return when (injectingFragment) {
            is ProfileSubscriptionsFragment -> SubscriptionViewModel::class.java
            is ProfileRepositoryFragment -> ProfileRepositoryViewModel::class.java
            is ProfileFollowersFragment -> FollowersViewModel::class.java
            else -> super.getViewModelClazz(injectingFragment)
        }
    }

    override fun getViewModelStoreOwner(injectingFragment: Fragment): ViewModelStoreOwner {
        return when (injectingFragment) {
            is UserFragment -> injectingFragment.parentFragment as ViewModelStoreOwner
            is RepositoryFragment -> injectingFragment.parentFragment as ViewModelStoreOwner
            else -> super.getViewModelStoreOwner(injectingFragment)
        }
    }
}

class LoginInjector(
    factory: ViewModelProvider.Factory,
    private val component: LoginComponent,
    private val activity: Activity
) : FragmentLifecycleInjector(factory) {

}

