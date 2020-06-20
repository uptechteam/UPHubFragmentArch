package com.test.uphubfragmentarch.domain

import androidx.annotation.StringRes
import com.test.uphubfragmentarch.R
import com.test.uphubfragmentarch.data.UserPreferences
import com.test.uphubfragmentarch.data.remote.ForbiddenException
import com.test.uphubfragmentarch.data.remote.GithubService
import com.test.uphubfragmentarch.data.remote.UnauthorizedException
import com.test.uphubfragmentarch.util.set
import okhttp3.Credentials
import javax.inject.Inject

interface LoginInteractor {
    suspend fun login(login: String, password: String): Result<String, LoginError>

    enum class LoginError(@StringRes val errorText: Int) {
        UNKNOWN(R.string.common_error_smth_went_wrong),
        AUTH_LIMIT(R.string.common_error_smth_went_wrong),
        UNAUTH(R.string.login_error_creds)
    }
}

class DefaultLoginInteractor @Inject constructor(
    private val userPreferences: UserPreferences,
    private val githubService: GithubService
) : LoginInteractor {
    override suspend fun login(
        login: String,
        password: String
    ): Result<String, LoginInteractor.LoginError> {
        if (userPreferences.getUserToken(login).isBlank()) {
            try {
                val token = githubService.getToken(Credentials.basic(login, password))
                    .await().token
                userPreferences.setUserToken(login, token)
            } catch (ex: Exception) {
                return when (ex) {
                    is ForbiddenException -> Result.Error(LoginInteractor.LoginError.AUTH_LIMIT)
                    is UnauthorizedException -> Result.Error(LoginInteractor.LoginError.UNAUTH)
                    else -> Result.Error(LoginInteractor.LoginError.UNKNOWN)
                }
                //TODO catch or throw to coroutine context
            }
        }

        userPreferences.currentUserLogin.set(login)
        return Result.Success(login)
    }
//
//    private suspend fun updateCurrentUser() {
//        val user = service.getUserOauth().await()
//        db.updateUser(user.asGeneralModel())
//        userPrefs.currentUserId.set(user.id)
//    }
}