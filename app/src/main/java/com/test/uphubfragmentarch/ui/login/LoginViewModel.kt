package com.test.uphubfragmentarch.ui.login

import androidx.databinding.Bindable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.test.uphubfragmentarch.data.Resources
import com.test.uphubfragmentarch.data.UserPreferences
import com.test.uphubfragmentarch.domain.LoginInteractor
import com.test.uphubfragmentarch.domain.Result
import com.test.uphubfragmentarch.ui.BaseViewModel
import com.test.uphubfragmentarch.util.set
import com.test.uphubfragmentarch.util.toSingleEvent
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LoginViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val loginInteractor: LoginInteractor,
    private val resources: Resources
) : BaseViewModel() {

    private val _error = MutableLiveData<String>()
    private val _progress = MutableLiveData<Boolean>(false)

    @Bindable
    val login = MutableLiveData<String>()

    @Bindable
    val password = MutableLiveData<String>()

    val progress: LiveData<Boolean> = _progress

    val error: LiveData<String>
        get() = _error.toSingleEvent()

    fun onSkipPressed() {
        userPreferences.hideLogin.setValue(true)
    }

    fun onSignUp() {
        launch {
            _progress.set(true)
            when (val loginResult = loginInteractor.login(
                login.value ?: "",
                password.value ?: ""
            )) {
                is Result.Error -> {
                    _error.set(resources.getString(loginResult.error.errorText))
                }
                is Result.Success -> {
                    onSkipPressed()
                }
            }
            withContext(NonCancellable) {
                _progress.set(false)
            }
        }
    }
}