package com.test.uphubfragmentarch.ui.main.user

import android.os.Handler
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.test.uphubfragmentarch.data.PagingListing
import com.test.uphubfragmentarch.data.db.model.User
import com.test.uphubfragmentarch.data.repository.UsersRepository
import com.test.uphubfragmentarch.ui.BaseViewModel
import com.test.uphubfragmentarch.util.Debounce
import com.test.uphubfragmentarch.util.click
import com.test.uphubfragmentarch.util.switchMap
import javax.inject.Inject

class UserViewModel @Inject constructor(
    private val usersRepository: UsersRepository,
    private val userCoordinator: UserCoordinator
) : BaseViewModel() {

    val search = MutableLiveData<String>()
    private val fetch = MutableLiveData<Unit>()
    private val debounce = Debounce(Handler())

    private val listing: LiveData<PagingListing<List<User>>> =
        MediatorLiveData<PagingListing<List<User>>>().apply {
            fun onCall(search: String) {
                value = usersRepository.searchUsers(search)
            }
            addSource(fetch) { onCall(search.value ?: "") }
            addSource(search) { query -> debounce.offer { onCall(query) } }
        }

    val models: LiveData<List<UserUI>> =
        listing.switchMap { it.data }
            .map { it.map { user -> UserUI.fromUser(user) } }

    val globalProgress: LiveData<Boolean> = listing.switchMap { it.initialProgress }

    fun refresh() {
        fetch.click()
    }

    fun onFetchMore() {
        listing.value?.onMore?.invoke()
    }

    fun onUserClick(it: UserUI) {
        userCoordinator.userCtorOnProfileClicked(it.id)
    }
}