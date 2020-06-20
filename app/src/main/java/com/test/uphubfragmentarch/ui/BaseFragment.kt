package com.test.uphubfragmentarch.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment


abstract class BaseFragment<T : BaseViewModel, B : ViewDataBinding>() : Fragment() {

    protected lateinit var binding: B
    protected lateinit var viewModel: T

    @Suppress("UNCHECKED_CAST")
    fun initFragment(vm: BaseViewModel) {
        viewModel = vm as T
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        provideBinding(inflater, container).also {
            binding = it
            binding.lifecycleOwner = viewLifecycleOwner
        }.root

    override fun onDestroyView() {
        super.onDestroyView()
        binding.unbind()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        onBinding(binding, savedInstanceState)
    }

    open fun onBinding(bind: B, bundle: Bundle?) {
    }

    abstract fun provideBinding(inflater: LayoutInflater, container: ViewGroup?): B

}

