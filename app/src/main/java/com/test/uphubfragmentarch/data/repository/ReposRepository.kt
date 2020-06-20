package com.test.uphubfragmentarch.data.repository

import com.test.uphubfragmentarch.data.PagingListing
import com.test.uphubfragmentarch.data.UserPreferences
import com.test.uphubfragmentarch.data.db.RepositoryDao
import com.test.uphubfragmentarch.data.db.UserDao
import com.test.uphubfragmentarch.data.db.model.Repository
import com.test.uphubfragmentarch.data.remote.ApiException
import com.test.uphubfragmentarch.data.remote.GithubService
import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.TimeUnit
import javax.inject.Inject

interface ReposRepository {

    companion object {
        val USER_REPOS_THROTTLE = TimeUnit.SECONDS.toMillis(30)
    }

    fun getRepositories(
        search: String = ""
    ): PagingListing<List<Repository>>

    fun getUserRepos(userId: Int): PagingListing<List<Repository>>
}

class DefaultReposRepository @Inject constructor(
    private val service: GithubService,
    private val dao: RepositoryDao,
    private val userPreferences: UserPreferences,
    private val userDao: UserDao,
    scope: CoroutineScope
) : ReposRepository, CoroutineScope by scope {

    override fun getRepositories(search: String): PagingListing<List<Repository>> {
        if (search.isNotEmpty()) return createPageBasedPagingListing { page ->
            val response = try {
                service.searchRepositories(search, page).await()
            } catch (ex: java.lang.Exception) {
                null
            }
            response?.let { PagingResponse(it.items.map { it.asGeneralModel() }, it.totalCount) }
        } else return createIdBasedPagingListing({
            it.last().id.toString()
        }, {
            try {
                service.getRepositories(it).await()
                    .map { it.asGeneralModel() }
            } catch (ex: Exception) {
                null
            }
        })
    }

    override fun getUserRepos(userId: Int): PagingListing<List<Repository>> {
        return createPageBasedPagingListingFromSource(dao.listenUser(userId)) { page ->
            if (!userPreferences.checkProfileRepositoryFetch(userId)) return@createPageBasedPagingListingFromSource true

            try {
                val repos = service.getUserRepositories(userDao.getLogin(userId), page)
                    .await().map { it.asGeneralModel() }
                repos.forEach { dao.updateRepository(it) }
                repos.isEmpty().also {
                    if (it) userPreferences.onProfileReposFetch(userId)
                }
            } catch (ex: ApiException) {
                ex.printStackTrace()
                true
            }
        }
    }
}