package com.test.uphubfragmentarch.di

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.*
import androidx.navigation.Navigation
import com.test.uphubfragmentarch.R
import com.test.uphubfragmentarch.ui.host.HostActivity
import com.test.uphubfragmentarch.ui.login.FragmentLogin
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

class HostInjector(
    hostFactory: ViewModelProvider.Factory,
    private val hostActivity: HostActivity
) : FragmentLifecycleInjector(hostFactory) {

    private var mainInjector: FragmentLifecycleInjector? = null
    private var loginInjector: FragmentLifecycleInjector? = null

    override fun onFragmentAttached(fm: FragmentManager, f: Fragment, context: Context) {
        when (f) {
            is FragmentLogin -> {
                val loginComponent = LoginComponent.invoke()
                loginInjector = LoginInjector(
                    f,
                    loginComponent.vmFactory(),
                    loginComponent,
                    hostActivity
                )
                f.childFragmentManager.registerFragmentLifecycleCallbacks(loginInjector!!, false)
                super.onFragmentAttached(fm, f, context)
            }
            is FragmentMain -> {
                val mainComponent = MainComponent.invoke()
                mainInjector = MainInjector(
                    f,
                    mainComponent.vmFactory(),
                    mainComponent,
                    hostActivity
                )
                f.childFragmentManager.registerFragmentLifecycleCallbacks(mainInjector!!, true)
                super.onFragmentAttached(fm, f, context)
            }
        }
    }

    override fun onFragmentDetached(fm: FragmentManager, f: Fragment) {
        super.onFragmentDetached(fm, f)
        if (hostActivity.isChangingConfigurations) return
        when (f) {
            is FragmentLogin -> {
                LoginComponent.kill()
                loginInjector = null
            }
            is FragmentMain -> {
                MainComponent.kill()
                mainInjector = null
            }
        }
    }


    //TODO ideally these fragments are containers and it's vms are stored in parent component
    //But for example simplicity these fragments contain scope related references, thus vms are stored
    //in related scopes
    override fun getViewModelFactory(injectingFragment: Fragment): ViewModelProvider.Factory {
        return when (injectingFragment) {
            is FragmentLogin -> loginInjector!!.viewModelFactory
            is FragmentMain -> mainInjector!!.viewModelFactory
            else -> super.getViewModelFactory(injectingFragment)
        }
    }
}

class MainInjector(
    fragment: Fragment,
    mainFactory: ViewModelProvider.Factory,
    private val mainComponent: MainComponent,
    private val activity: Activity
) : FragmentLifecycleInjector(mainFactory), LifecycleObserver, Observer<LifecycleOwner> {

    init {
        fragment.viewLifecycleOwnerLiveData.observe(fragment, this)
    }

    override fun onChanged(t: LifecycleOwner) {
        t.lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun init() {
        val controller = Navigation.findNavController(activity, R.id.mainHost)
        val navigator = MainNavigator(controller)
        val coordinator = mainComponent.mainCoordinator as DefaultMainCoordinator
        coordinator.initCoordinator(navigator)
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
    fragment: Fragment,
    factory: ViewModelProvider.Factory,
    private val component: LoginComponent,
    private val activity: Activity
) : FragmentLifecycleInjector(factory) {

}

