package com.test.uphubfragmentarch.ui.main.profile.subscriptions

import androidx.lifecycle.LiveData
import com.test.uphubfragmentarch.data.repository.UsersRepository
import com.test.uphubfragmentarch.ui.main.navigation.MainCoordinator
import com.test.uphubfragmentarch.ui.main.profile.ProfilePageViewModel
import com.test.uphubfragmentarch.ui.main.user.UserUI
import com.test.uphubfragmentarch.util.map
import com.test.uphubfragmentarch.util.switchMap
import javax.inject.Inject

class SubscriptionViewModel @Inject constructor(
    usersRepository: UsersRepository,
    private val mainCoordinator: MainCoordinator
) : ProfilePageViewModel<UserUI>() {

    private val listing = fetch.map {
        usersRepository.getUsers(it,
            UsersRepository.Type.Subscriptions
        )
    }
    override val models: LiveData<List<UserUI>> = listing
        .switchMap { it.data }
        .map { list -> list.map {
            UserUI.fromUser(
                it
            )
        } }

    override val progress: LiveData<Boolean> =
        listing.switchMap { it.initialProgress }

    override fun onMore() {
        listing.value?.onMore?.invoke()
    }

    override fun onItemClicked(item: UserUI) {
        mainCoordinator.onProfileShow(item.id)
    }

    override fun refresh() {
        listing.value?.refresh?.invoke()
    }
}