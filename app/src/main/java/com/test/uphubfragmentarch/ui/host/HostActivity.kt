package com.test.uphubfragmentarch.ui.host

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.test.uphubfragmentarch.R
import com.test.uphubfragmentarch.navigation.Navigator
import com.test.uphubfragmentarch.ui.BaseActivity
import com.test.uphubfragmentarch.util.observe
import kotlinx.android.synthetic.main.activity_host.*

class HostActivity : BaseActivity<HostViewModel>() {

    private lateinit var navigator: Navigator<HostRoute>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_host)
        navigator = HostCoordinator(fragmentContainer.findNavController())

        observe(viewModel.route) {
//            navigator.navigate(it)
        }
    }

    override fun onBackPressed() {
        if (!navigator.popBackStack()) {
            super.onBackPressed()
        }
    }
}

class HostCoordinator(private val controller: NavController) : Navigator<HostRoute> {
    override fun navigate(route: HostRoute) {
        when (route) {
            HostRoute.Login -> controller.navigate(R.id.fragmentLogin, null, with(NavOptions.Builder()) {
                setLaunchSingleTop(true)
                build()
            })
            HostRoute.Main -> controller.navigate(R.id.fragmentMain, null, with(NavOptions.Builder()) {
                setLaunchSingleTop(true)
                build()
            })
        }
    }

    override fun navigate(route: HostRoute, intermediateRoutes: Collection<HostRoute>) {
        navigate(route)
    }

    override fun popBackStack(): Boolean {
        return controller.popBackStack()
    }
}

sealed class HostRoute : Navigator.Route {

    object Login : HostRoute() {
        override val destination: Navigator.Destination
            get() = Navigator.Destination("login")
    }

    object Main : HostRoute() {
        override val destination: Navigator.Destination
            get() = Navigator.Destination("main")
    }
}
