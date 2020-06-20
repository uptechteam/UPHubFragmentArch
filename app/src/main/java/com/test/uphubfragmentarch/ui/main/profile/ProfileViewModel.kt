package com.test.uphubfragmentarch.ui.main.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import com.test.uphubfragmentarch.data.db.model.User
import com.test.uphubfragmentarch.data.repository.UsersRepository
import com.test.uphubfragmentarch.ui.BaseViewModel
import com.test.uphubfragmentarch.util.SingleLiveEvent
import com.test.uphubfragmentarch.util.click
import com.test.uphubfragmentarch.util.set
import javax.inject.Inject

class ProfileViewModel @Inject constructor(
    usersRepository: UsersRepository
) : BaseViewModel() {
    private val getUser = MutableLiveData<Int>()

    val user: LiveData<User> = getUser.switchMap {
        usersRepository.getUser(it)
    }

    val navigateToSignIn = SingleLiveEvent<Unit>()

    fun onSignIn() {
        navigateToSignIn.click()
    }

    fun onUserId(userId: Int) {
        getUser.set(userId)
    }
}