package com.test.uphubfragmentarch.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import com.test.uphubfragmentarch.databinding.FragmentLoginBinding
import com.test.uphubfragmentarch.ui.BaseFragment
import com.test.uphubfragmentarch.util.observe

class FragmentLogin : BaseFragment<LoginViewModel, FragmentLoginBinding>() {

    override fun provideBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentLoginBinding = FragmentLoginBinding.inflate(inflater, container, false)

    override fun onBinding(bind: FragmentLoginBinding, bundle: Bundle?) {
        super.onBinding(bind, bundle)
        binding.viewModel = viewModel
        binding.popup.animate()
            .alpha(1F)
            .translationY(0F)
            .setInterpolator(DecelerateInterpolator()).duration = 650L
        binding.executePendingBindings()
        observe(viewModel.error) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }
    }
}