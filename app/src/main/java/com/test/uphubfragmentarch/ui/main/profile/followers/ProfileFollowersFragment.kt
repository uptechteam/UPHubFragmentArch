package com.test.uphubfragmentarch.ui.main.profile.followers

import com.test.uphubfragmentarch.ui.BaseRecyclerViewAdapter
import com.test.uphubfragmentarch.ui.BaseViewHolder
import com.test.uphubfragmentarch.ui.main.profile.ProfilePageFragment
import com.test.uphubfragmentarch.ui.main.user.UserAdapter
import com.test.uphubfragmentarch.ui.main.user.UserUI

class ProfileFollowersFragment : ProfilePageFragment<UserUI>() {

    companion object {
        fun getInstance(userId: Int) = ProfileFollowersFragment()
            .apply {
            arguments =
                getBundle(
                    userId,
                    ProfilePageFragment.Companion.Type.Follower
                )
        }
    }

    override val adapter: BaseRecyclerViewAdapter<out BaseViewHolder<UserUI>, UserUI> =
        UserAdapter {
            viewModel.onItemClicked(
                it
            )
        }
}