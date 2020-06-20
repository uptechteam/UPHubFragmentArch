package com.test.uphubfragmentarch.ui.main.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.test.uphubfragmentarch.R
import com.test.uphubfragmentarch.databinding.FragmentProfileBinding
import com.test.uphubfragmentarch.ui.BaseFragment
import com.test.uphubfragmentarch.ui.main.profile.followers.ProfileFollowersFragment
import com.test.uphubfragmentarch.ui.main.profile.repository.ProfileRepositoryFragment
import com.test.uphubfragmentarch.ui.main.profile.subscriptions.ProfileSubscriptionsFragment
import com.test.uphubfragmentarch.util.observe

class ProfileFragment : BaseFragment<ProfileViewModel, FragmentProfileBinding>() {

    companion object {
        private const val USER_ID = "user_id"


        fun getBundle(id: Int) = bundleOf(USER_ID to id)
    }

    override fun provideBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentProfileBinding = FragmentProfileBinding.inflate(inflater, container, false)


    private val titles = listOf(
        R.string.profile_repos,
        R.string.profile_subscr,
        R.string.profile_follow
    )

    private lateinit var adapter: ProfilePagerAdapter

    override fun onBinding(bind: FragmentProfileBinding, bundle: Bundle?) {
        super.onBinding(bind, bundle)
        val userId = arguments?.getInt(USER_ID) ?: throw IllegalArgumentException()

        adapter = ProfilePagerAdapter(
            listOf(
                ProfileRepositoryFragment.getInstance(userId),
                ProfileSubscriptionsFragment.getInstance(userId),
                ProfileFollowersFragment.getInstance(userId)
            ), childFragmentManager
        )
        bind.viewModel = viewModel
        bind.pager.adapter = adapter
        bind.tabs.setupWithViewPager(bind.pager)
        bind.lifecycleOwner = this

//        observe(viewModel.navigateToSignIn) {
//            startActivity(LoginActivity.getIntent(requireContext()))
//        }

        observe(viewModel.user) { user ->
            listOf(user.publicRepos, user.following, user.followers).forEachIndexed { index, i ->
                bind.tabs.getTabAt(index)?.text = resources.getString(titles[index], i)
            }
        }
        viewModel.onUserId(userId)
    }

}