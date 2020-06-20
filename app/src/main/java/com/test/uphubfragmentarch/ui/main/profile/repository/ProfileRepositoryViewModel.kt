package com.test.uphubfragmentarch.ui.main.profile.repository

import androidx.lifecycle.LiveData
import com.test.uphubfragmentarch.data.repository.ReposRepository
import com.test.uphubfragmentarch.ui.main.profile.ProfilePageViewModel
import com.test.uphubfragmentarch.ui.main.repository.RepositoryUI
import com.test.uphubfragmentarch.util.map
import com.test.uphubfragmentarch.util.switchMap
import javax.inject.Inject

class ProfileRepositoryViewModel @Inject constructor(
    reposRepository: ReposRepository
) : ProfilePageViewModel<RepositoryUI>() {
    private val listing = fetch.map {
        reposRepository.getUserRepos(it)
    }

    override val models: LiveData<List<RepositoryUI>> =
        listing.switchMap { it.data.map { list -> list.map {
            RepositoryUI(
                it
            )
        } } }

    override val progress: LiveData<Boolean> =
        listing.switchMap { it.initialProgress }

    override fun onMore() {
        listing.value?.onMore?.invoke()
    }

    override fun onItemClicked(item: RepositoryUI) {

    }

    override fun refresh() {
        listing.value?.refresh?.invoke()
    }
}