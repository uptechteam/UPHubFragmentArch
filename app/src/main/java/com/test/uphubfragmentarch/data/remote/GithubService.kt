package com.test.uphubfragmentarch.data.remote

import com.test.uphubfragmentarch.data.remote.model.*
import kotlinx.coroutines.Deferred
import retrofit2.http.*

interface GithubService {

    companion object {
        const val ENDPOINT = "https://api.github.com/"
    }

    /**
     * https://developer.github.com/v3/repos/#list-all-public-repositories
     */
    @GET("repositories")
    fun getRepositories(@Query("since") lastId: String?): Deferred<List<ApiRepository>>

    /**
     * https://developer.github.com/v3/search/#search-repositories
     */
    @GET("/search/repositories")
    fun searchRepositories(
        @Query("q") q: String,
        @Query("page") page: Int = 1,
        @Query("sort") sort: String = "stars",
        @Query("order") order: String = "desc"
    ): Deferred<RepositorySearchResponse>

    /**
     * https://developer.github.com/v3/search/#search-users
     */
    @GET("/search/users")
    fun searchUsers(
        @Query("q") q: String,
        @Query("page") page: Int = 1,
        @Query("sort") sort: String = "stars",
        @Query("order") order: String = "desc"
    ): Deferred<UserSearchResponse>

    /**
     * https://developer.github.com/v3/repos/#list-all-public-repositories
     */
    @GET("users")
    fun getUsers(@Query("since") lastId: String?): Deferred<List<ApiUser>>

    /**
     * https://developer.github.com/v3/oauth_authorizations/#create-a-new-authorization
     */
    @POST("authorizations")
    fun getToken(
        @Header("Authorization") credential: String,
        @Body info: DeviceInfo = DeviceInfo()
    ): Deferred<AuthToken>

    /**
     * Auth required!
     * https://developer.github.com/v3/users/#get-the-authenticated-user
     */
    @GET("user")
    fun getUserOauth(): Deferred<ApiUser>

    /**
     * https://developer.github.com/v3/users/#get-a-single-user
     */
    @GET("users/{login}")
    fun getUser(@Path("login") login: String): Deferred<ApiUser>

    /**
     * https://developer.github.com/v3/repos/#list-user-repositories
     */
    @GET("users/{user}/repos")
    fun getUserRepositories(
        @Path("user") user: String,
        @Query("page") page: Int,
        @Query("sort") sort: String = "created",
        @Query("type") type: String = "all"
    ): Deferred<List<ApiRepository>>

    /**
     * https://developer.github.com/v3/users/followers/
     */
    @GET("users/{user}/followers")
    fun getUserFollowers(
        @Path("user") user: String,
        @Query("page") page: Int
    ): Deferred<List<ApiUser>>

    /**
     * https://developer.github.com/v3/users/followers/#list-users-followed-by-another-user
     */
    @GET("users/{user}/following")
    fun getUserSubscription(
        @Path("user") user: String,
        @Query("page") page: Int
    ): Deferred<List<ApiUser>>
}