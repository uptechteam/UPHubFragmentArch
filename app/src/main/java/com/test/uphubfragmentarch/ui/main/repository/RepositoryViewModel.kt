package com.test.uphubfragmentarch.ui.main.repository

import android.os.Handler
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.test.uphubfragmentarch.data.PagingListing
import com.test.uphubfragmentarch.data.db.model.Repository
import com.test.uphubfragmentarch.data.repository.ReposRepository
import com.test.uphubfragmentarch.ui.BaseViewModel
import com.test.uphubfragmentarch.util.Debounce
import com.test.uphubfragmentarch.util.click
import com.test.uphubfragmentarch.util.switchMap
import javax.inject.Inject

class RepositoryViewModel @Inject constructor(
    private val reposRepository: ReposRepository
) : BaseViewModel() {

    val search = MutableLiveData<String>()
    private val fetch = MutableLiveData<Unit>()
    private val debounce = Debounce(Handler())

    private val listing: LiveData<PagingListing<List<Repository>>> =
        MediatorLiveData<PagingListing<List<Repository>>>().apply {
            fun onCall(search: String) {
                value = reposRepository.getRepositories(search)
            }
            addSource(fetch) { onCall(search.value ?: "") }
            addSource(search) { query -> debounce.offer { onCall(query) } }
        }

    val models: LiveData<List<RepositoryUI>> =
        listing.switchMap { it.data }
            .map { it.map { repo -> RepositoryUI(repo) } }

    val globalProgress: LiveData<Boolean> = listing.switchMap { it.initialProgress }

    fun refresh() {
        fetch.click()
    }

//
//    val search = MutableLiveData<String>()
//    private val fetch = MutableLiveData<Boolean>()
//    private val debounce = Debounce(Handler())
//
//    private val state = MediatorLiveData<PagingState<Repository>>().apply {
//        fun onCall(search: String, forceUpdate: Boolean = false) {
//            value = value?.takeIf {
//                it.search == search && !forceUpdate
//            }?.let {
//                if (it.fetchNext != null) it.fetchNext?.invoke() else return
//            } ?: pagingUseCase.fetch(jobComposite, search)
//        }
//        addSource(fetch) { onCall(search.value ?: "", it) }
//        addSource(search) { debounce.offer { onCall(it) } }
//    }
//
//    val models: LiveData<Pair<Boolean, List<RepositoryUI>>> = state.switchMap { state ->
//        state.model.map { repos ->
//            state.isFirstFetch to repos.map {
//                RepositoryUI(it)
//            } + (if (state.fetchNext != null) listOf(RepositoryUI()) else listOf())
//        }
//    }

//    val globalProgress: LiveData<Boolean> = state.zip(state.switchMap { it.progress }).map { (state, progress) ->
//        state.isFirstFetch && progress
//    }

    fun onFetchMore(forceUpdate: Boolean = false) {
//        if (state.value?.inProgress() != true)
//            fetch.set(forceUpdate)
    }


    fun onEndOfListReached() {
        listing.value?.onMore?.invoke()

    }
}