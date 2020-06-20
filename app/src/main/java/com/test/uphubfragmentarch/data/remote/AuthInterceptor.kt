package com.test.uphubfragmentarch.data.remote

import com.test.uphubfragmentarch.data.UserPreferences
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
        private val userPrefs: UserPreferences
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response = chain.proceed(chain.request()
            .newBuilder().apply {
                userPrefs.getCurrentToken().takeIf {
                    it.isNotBlank()
                }?.let { token ->
                    addHeader("Authorization", "Bearer $token")
                }
            }.build()
    )
}