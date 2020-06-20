package com.test.uphubfragmentarch.ui.main.profile.subscriptions

import com.test.uphubfragmentarch.ui.BaseRecyclerViewAdapter
import com.test.uphubfragmentarch.ui.BaseViewHolder
import com.test.uphubfragmentarch.ui.main.profile.ProfilePageFragment
import com.test.uphubfragmentarch.ui.main.user.UserAdapter
import com.test.uphubfragmentarch.ui.main.user.UserUI

class ProfileSubscriptionsFragment : ProfilePageFragment<UserUI>() {
    companion object {
        fun getInstance(userId: Int) = ProfileSubscriptionsFragment()
            .apply {
            arguments =
                getBundle(
                    userId,
                    ProfilePageFragment.Companion.Type.Subscription
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