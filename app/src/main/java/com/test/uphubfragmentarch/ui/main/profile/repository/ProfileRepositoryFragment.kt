package com.test.uphubfragmentarch.ui.main.profile.repository

import com.test.uphubfragmentarch.ui.BaseRecyclerViewAdapter
import com.test.uphubfragmentarch.ui.BaseViewHolder
import com.test.uphubfragmentarch.ui.main.profile.ProfilePageFragment
import com.test.uphubfragmentarch.ui.main.repository.RepositoryAdapter
import com.test.uphubfragmentarch.ui.main.repository.RepositoryUI

class ProfileRepositoryFragment : ProfilePageFragment<RepositoryUI>() {

    companion object {
        fun getInstance(userId: Int) = ProfileRepositoryFragment()
            .apply {
            arguments =
                getBundle(
                    userId,
                    ProfilePageFragment.Companion.Type.Repository
                )
        }
    }

    override val adapter: BaseRecyclerViewAdapter<out BaseViewHolder<RepositoryUI>, RepositoryUI> =
        RepositoryAdapter()
}