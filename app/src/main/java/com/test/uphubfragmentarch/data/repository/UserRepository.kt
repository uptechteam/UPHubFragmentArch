package com.test.uphubfragmentarch.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.test.uphubfragmentarch.data.PagingListing
import com.test.uphubfragmentarch.data.db.UserDao
import com.test.uphubfragmentarch.data.db.model.User
import com.test.uphubfragmentarch.data.db.model.UserFollowing
import com.test.uphubfragmentarch.data.remote.ApiException
import com.test.uphubfragmentarch.data.remote.GithubService
import com.test.uphubfragmentarch.util.set
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

interface UsersRepository {
    fun searchUsers(search: String): PagingListing<List<User>>
    fun getUsers(ownerId: Int, type: Type): PagingListing<List<User>>
    fun getUser(userId: Int): LiveData<User>

    enum class Type {
        Followers, Subscriptions
    }
}

class DefaultUserRepository @Inject constructor(
    private val githubService: GithubService,
    scope: CoroutineScope,
    private val db: UserDao
) : UsersRepository, CoroutineScope by scope {

    override fun getUser(userId: Int): LiveData<User> {
        return db.listen(userId).also {
            launch {
                db.updateUser(
                    githubService.getUser(
                        db.getLogin(userId)
                    ).await().asGeneralModel()
                )
            }
        }
    }

    override fun getUsers(ownerId: Int, type: UsersRepository.Type): PagingListing<List<User>> {
        return when (type) {
            UsersRepository.Type.Followers -> {
                createPageBasedPagingListingFromSource(db.listenFollowers(ownerId)) { page ->
                    try {
                        val subs =
                            githubService.getUserFollowers(db.getLogin(ownerId), page).await()
                                .map { it.asGeneralModel() }
                        db.insert(subs)
                        db.insertFollowing(subs.map { UserFollowing(it.id, ownerId) })
                        subs.isEmpty()
                    } catch (ex: ApiException) {
                        ex.printStackTrace()
                        true
                    }
                }
            }
            UsersRepository.Type.Subscriptions ->
                createPageBasedPagingListingFromSource(db.listenSubscription(ownerId)) { page ->
                    try {
                        val subs =
                            githubService.getUserSubscription(db.getLogin(ownerId), page).await()
                                .map { it.asGeneralModel() }
                        db.insert(subs)
                        db.insertFollowing(subs.map { UserFollowing(it.id, ownerId) })
                        subs.isEmpty()
                    } catch (ex: ApiException) {
                        ex.printStackTrace()
                        true
                    }
                }
        }
    }

    override fun searchUsers(search: String): PagingListing<List<User>> {
        return if (search.isNotEmpty()) searchUsersInternal(search) else getUsers()
    }

    private fun getUsers(): PagingListing<List<User>> {
        val source = MutableLiveData<List<User>>()
        return createIdBasePagingListingFromSource(
            source
        ) { id ->
            try {
                val users = githubService.getUsers(id).await()
                    .map { it.asGeneralModel() }
                source.postValue(users)
                db.insert(users)
                users.lastOrNull()?.id.toString()
            } catch (ex: ApiException) {
                null
            }
        }
    }

    private fun searchUsersInternal(search: String): PagingListing<List<User>> {
        return createPageBasedPagingListing { page ->
            val response = try {
                githubService.searchUsers(search, page).await()
            } catch (ex: ApiException) {
                if (page == 1) {
                    val users = db.searchUser(search)
                    return@createPageBasedPagingListing PagingResponse(users, users.size)
                }
                null
            }

            response?.let {
                val users = it.items.map { it.asGeneralModel() }
                db.insert(users)
                PagingResponse(users, it.totalCount)
            }
        }
    }

}
typealias PagingResponse<T> = Pair<List<T>, Int>

fun <T> CoroutineScope.createIdBasePagingListingFromSource(
    data: LiveData<List<T>>,
    onNext: suspend (String?) -> String?
): PagingListing<List<T>> {
    val initialProgress = MutableLiveData(false)
    var onMore: (() -> Unit)? = null
    var loading = false
    var lastId: String? = null

    fun fetch(initial: Boolean = false) {
        loading = true
        if (initial) {
            initialProgress.set(loading)
        }
        launch {
            lastId = onNext(lastId)
            onMore = lastId?.let {
                {
                    if (!loading) {
                        fetch()
                    }
                }
            }
            loading = false
            if (initial) {
                initialProgress.set(loading)
            }
        }
    }

    val refresh = {
        lastId = null
        fetch(true)
    }

    return PagingListing(
        data,
        initialProgress,
        MutableLiveData(),
        {
            onMore?.invoke()
        },
        refresh
    )
        .also { fetch(true) }
}

fun <T> CoroutineScope.createPageBasedPagingListingFromSource(
    data: LiveData<List<T>>,
    fetch: suspend (Int) -> Boolean //page, Finished
): PagingListing<List<T>> {
    val initialProgress = MutableLiveData(false)
    var page = 1
    var onMore: (() -> Unit)? = null
    var loading: Boolean

    fun fetch(initial: Boolean = false) {
        loading = true
        if (initial) {
            initialProgress.set(loading)
        }
        launch {
            val finished = fetch.invoke(page) ?: return@launch
            onMore = if (!finished) { ->
                page++
                if (!loading) {
                    fetch()
                }
            } else null
            loading = false
            if (initial) {
                initialProgress.set(loading)
            }
        }
    }

    val refresh = {
        page = 1
        fetch(true)
    }

    return PagingListing(
        data,
        initialProgress,
        MutableLiveData(),
        {
            onMore?.invoke()
        },
        refresh
    )
        .also { fetch(true) }
}


//TODO add error stream to listing
fun <T> CoroutineScope.createIdBasedPagingListing(
    nextId: (List<T>) -> String?,
    fetch: suspend (String?) -> List<T>?
): PagingListing<List<T>> {
    val data = MutableLiveData<List<T>>()
    val initialProgress = MutableLiveData(false)
    var onMore: (() -> Unit)? = null
    var loading = false
    var lastId: String? = null

    fun fetch(initial: Boolean = false) {
        loading = true
        if (initial) {
            initialProgress.set(loading)
        }
        launch {
            val items = fetch.invoke(lastId) ?: return@launch
            val newList = if (initial) {
                items
            } else {
                data.value?.let { it + items } ?: items
            }
            onMore = if (items.isNotEmpty()) { ->
                lastId = nextId(items)
                if (!loading) {
                    fetch()
                }
            } else null
            data.postValue(newList)
            loading = false
            if (initial) {
                initialProgress.set(loading)
            }
        }
    }

    val refresh = {
        lastId = null
        fetch(true)
    }

    return PagingListing(
        data,
        initialProgress,
        MutableLiveData(),
        {
            onMore?.invoke()
        },
        refresh
    )
        .also { fetch(true) }
}

fun <T> CoroutineScope.createPageBasedPagingListing(
    fetch: suspend (Int) -> PagingResponse<T>?
): PagingListing<List<T>> {
    val data = MutableLiveData<List<T>>()
    val initialProgress = MutableLiveData(false)
    var page = 1
    var onMore: (() -> Unit)? = null
    var loading = false

    fun fetch(initial: Boolean = false) {
        loading = true
        if (initial) {
            initialProgress.set(loading)
        }
        launch {
            val response = fetch.invoke(page) ?: return@launch
            val items = response.first
            val newList = if (initial) {
                items
            } else {
                data.value?.let { it + items } ?: items
            }
            onMore = if (newList.size < response.second) { ->
                page++
                if (!loading) {
                    fetch()
                }
            } else null
            data.postValue(newList)
            loading = false
            if (initial) {
                initialProgress.set(loading)
            }
        }
    }

    val refresh = {
        page = 1
        fetch(true)
    }

    return PagingListing(
        data,
        initialProgress,
        MutableLiveData(),
        {
            onMore?.invoke()
        },
        refresh
    )
        .also { fetch(true) }
}