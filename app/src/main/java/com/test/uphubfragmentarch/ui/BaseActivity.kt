package com.test.uphubfragmentarch.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ViewDataBinding

abstract class BaseActivity<T : BaseViewModel> : AppCompatActivity() {

//    protected abstract val binding: ViewDataBinding

    protected lateinit var viewModel: T

    @Suppress("UNCHECKED_CAST")
    fun initActivity(vm: BaseViewModel) {
        viewModel = vm as T
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        binding.lifecycleOwner = this
//        binding.executePendingBindings()
    }
}