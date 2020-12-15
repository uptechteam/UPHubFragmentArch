package com.test.uphubfragmentarch.di

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.test.uphubfragmentarch.ui.BaseFragment
import com.test.uphubfragmentarch.ui.BaseViewModel
import java.lang.reflect.ParameterizedType

open class FragmentLifecycleInjector(
    private val viewModelFactory: ViewModelProvider.Factory
) : FragmentManager.FragmentLifecycleCallbacks() {

    @Suppress("UNCHECKED_CAST")
    override fun onFragmentAttached(fm: FragmentManager, f: Fragment, context: Context) {
        super.onFragmentAttached(fm, f, context)
        (f as? BaseFragment<*, *>)?.apply {
            val clazz = getViewModelClazz(this)
            val vm = ViewModelProvider(
                getViewModelStoreOwner(f),
                viewModelFactory
            )[clazz]
            doInject(this, vm as BaseViewModel)
        }
    }

    open fun doInject(injectingFragment: BaseFragment<*, *>, vm: BaseViewModel) {
        try {
            injectingFragment.initFragment(vm)
        } catch (ex: ClassCastException) {
            ex.printStackTrace()
        }
    }

    open fun getViewModelStoreOwner(injectingFragment: Fragment): ViewModelStoreOwner {
        return injectingFragment
    }

//    open fun getViewModelFactory(injectingFragment: Fragment): ViewModelProvider.Factory {
//        return viewModelFactory
//    }

    open fun getViewModelClazz(injectingFragment: Fragment): Class<out ViewModel> {
        val fragmentClass = injectingFragment::class.java
        val superType = fragmentClass.genericSuperclass
        val args = (superType as ParameterizedType).actualTypeArguments
        return args[0] as Class<out ViewModel>
    }
}