package com.test.uphubfragmentarch.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.test.uphubfragmentarch.R
import com.test.uphubfragmentarch.databinding.FragmentMainBinding
import com.test.uphubfragmentarch.ui.BaseFragment

class FragmentMain : BaseFragment<MainViewModel, FragmentMainBinding>() {

    override fun provideBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMainBinding = FragmentMainBinding.inflate(inflater, container, false)

    override fun onBinding(bind: FragmentMainBinding, bundle: Bundle?) {
        super.onBinding(bind, bundle)
        activity?.let {
            bind.bottomNavigation.setupWithNavController(
                Navigation.findNavController(
                    it,
                    R.id.mainHost
                )
            )
        }
    }
}